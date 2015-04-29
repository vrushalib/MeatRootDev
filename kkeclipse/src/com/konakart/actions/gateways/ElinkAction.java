//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is free software; you can redistribute 
// it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This software is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

package com.konakart.actions.gateways;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.app.IpnHistory;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.OrderUpdate;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderUpdateIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.bl.ConfigConstants;

/**
 * This class is an Action class for sending credit card details to Elink and receiving confirmation
 */
public class ElinkAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(ElinkAction.class);

    // Module name must be the same as the class name although it can be all in lowercase in order
    // to remain compatible with osCommerce.
    private static String code = "elink";

    // Elink constants for request

    private static final String CardAccountNum = "CardAccountNum";

    private static final String ExpirationDate = "ExpirationDate";

    private static final String CardHolderZip = "CardHolderZip";

    private static final String CVV2 = "CVV2";

    private static final String CardHolderName = "CardHolderName";

    // Elink constants for response
    private static final int respMsgFormatPosition = 1;

    private static final int respCreditCardPosition = 6;

    private static final int respTransactionStatusPosition = 11;

    private static final int respReferenceNumPosition = 14;

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "Elink payment successful. Elink TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "Elink payment not successful. Elink TransactionId = ";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        String gatewayResult = null;
        String transactionId = null;

        // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setModuleCode(code);
        KKAppEng kkAppEng = null;

        try
        {
            int custId;

            kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, "Checkout");

            // Check to see whether the user is logged in
            if (custId < 0)
            {
                return KKLOGIN;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Get the order
            OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();
            validateOrder(order, code);

            // Set the order id for the ipnHistory object
            ipnHistory.setOrderId(order.getId());

            // Create a parameter list for the credit card details.
            PaymentDetailsIf pd = order.getPaymentDetails();

            List<NameValueIf> parmList = new ArrayList<NameValueIf>();

            parmList.add(new NameValue(CardAccountNum, pd.getCcNumber()));
            if (pd.isShowCVV())
            {
                parmList.add(new NameValue(CVV2, pd.getCcCVV()));
            }
            parmList.add(new NameValue(ExpirationDate, pd.getCcExpiryMonth() + pd.getCcExpiryYear()));
            parmList.add(new NameValue(CardHolderZip, pd.getCcPostcode()));
            parmList.add(new NameValue(CardHolderName, pd.getCcOwner()));

            // Do the post
            String gatewayResp = postData(pd, parmList);

            gatewayResp = URLDecoder.decode(gatewayResp, "UTF-8");
            if (log.isDebugEnabled())
            {
                log.debug("Unformatted GatewayResp = \n" + gatewayResp);
            }

            if (gatewayResp == null)
            {
                throw new KKException(
                        "The eLink gateway returned a null response for the payment request.");
            }

            // Process the parameters sent in the reply
            StringBuffer sb = new StringBuffer();
            String[] parms = gatewayResp.split("\\|");
            if (parms != null && parms.length > 0)
            {
                for (int i = 0; i < parms.length; i++)
                {
                    String parm = parms[i];
                    sb.append(getRespDesc(i + 1));
                    sb.append("=");
                    if (i + 1 == respCreditCardPosition)
                    {
                        /*
                         * The following code is so that the credit card number is not saved in the
                         * IPN History table for security reasons.
                         */
                        sb.append("****************");
                    } else
                    {
                        sb.append(parm);
                    }

                    sb.append("\n");

                    if (i + 1 == respMsgFormatPosition)
                    {
                        if (parm == null || !parm.equalsIgnoreCase("R1"))
                        {
                            throw new KKException(
                                    "The eLink gateway response has an internal message format of "
                                            + parm + " instead of R1.");
                        }
                    } else if (i + 1 == respTransactionStatusPosition)
                    {
                        gatewayResult = parm;
                        ipnHistory.setGatewayResult(parm);
                    } else if (i + 1 == respReferenceNumPosition)
                    {
                        ipnHistory.setGatewayTransactionId(parm);
                        transactionId = parm;
                    }
                }
            }

            // Put the response in the ipnHistory record
            ipnHistory.setGatewayFullResponse(sb.toString());

            if (log.isDebugEnabled())
            {
                log.debug("Formatted Elink response data:");
                log.debug(sb.toString());
            }

            // See if we need to send an email, by looking at the configuration
            String sendEmailsConfig = kkAppEng.getConfig(ConfigConstants.SEND_EMAILS);
            boolean sendEmail = false;
            if (sendEmailsConfig != null && sendEmailsConfig.equalsIgnoreCase("true"))
            {
                sendEmail = true;
            }

            OrderUpdateIf updateOrder = new OrderUpdate();
            updateOrder.setUpdatedById(kkAppEng.getActiveCustId());

            // Determine whether the request was successful or not.If successful, we update the
            // inventory as well as changing the state of the order
            if (gatewayResult != null && (gatewayResult.equals("00") || gatewayResult.equals("11")))
            {
                String comment = ORDER_HISTORY_COMMENT_OK + transactionId;
                kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                        com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, /* customerNotified */
                        sendEmail, comment, updateOrder);

                // Update the inventory
                kkAppEng.getOrderMgr().updateInventory(order.getId());

                // Save the ipnHistory
                ipnHistory.setKonakartResultDescription(RET0_DESC);
                ipnHistory.setKonakartResultId(RET0);
                ipnHistory.setOrderId(order.getId());
                ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                // If we received no exceptions, delete the basket
                kkAppEng.getBasketMgr().emptyBasket();

                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */true);
                }

                return "Approved";

            }

            // The transaction was not approved
            String comment = ORDER_HISTORY_COMMENT_KO + transactionId;
            kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                    com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, /* customerNotified */
                    sendEmail, comment, updateOrder);

            // Save the ipnHistory
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
            ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
            kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

            if (sendEmail)
            {
                sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */false);
            }

            String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
            { comment + ". " + getErrorDesc(gatewayResult) });
            addActionError(msg);

            // Redirect the user back to the credit card screen
            return "TryAgain";

        } catch (Exception e)
        {
            try
            {
                ipnHistory.setKonakartResultDescription(RET4_DESC + e.getMessage());
                ipnHistory.setKonakartResultId(RET4);
                if (kkAppEng != null)
                {
                    kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);
                }
            } catch (KKException e1)
            {
                return super.handleException(request, e1);
            }
            return super.handleException(request, e);
        }
    }

    /**
     * Returns a description for some common problems
     * 
     * @param _code
     * @return Returns a description for some common problems
     */
    private String getErrorDesc(String _code)
    {
        if (_code != null)
        {

            if (_code.equalsIgnoreCase("54"))
            {
                return ("The card has expired.");
            } else if (_code.equalsIgnoreCase("84") || _code.equalsIgnoreCase("94"))
            {
                return ("Duplicate transaction.");
            }
        }
        return "";
    }

    /**
     * Elink returns a response as delimiter separated variables. In order to make them readable, we
     * tag each one with a description before saving in the ipnHistory table.
     * 
     * @param position
     * @return Response Description
     */
    private String getRespDesc(int position)
    {
        String ret = "unknown";
        switch (position)
        {
        case 1:
            ret = "MessageFormat";
            break;
        case 2:
            ret = "ePayAccountNum";
            break;
        case 3:
            ret = "TransactionCode";
            break;
        case 4:
            ret = "SequenceNum";
            break;
        case 5:
            ret = "MailOrderIdentifier";
            break;
        case 6:
            ret = "AccountNum";
            break;
        case 7:
            ret = "ExpirationDate";
            break;
        case 8:
            ret = "AuthorizedAmount";
            break;
        case 9:
            ret = "AuthorizationDate";
            break;
        case 10:
            ret = "AuthorizationTime";
            break;
        case 11:
            ret = "TransactionStatus";
            break;
        case 12:
            ret = "CustomerNum";
            break;
        case 13:
            ret = "OrderNum";
            break;
        case 14:
            ret = "ReferenceNum";
            break;
        case 15:
            ret = "AuthorizationResponseCode";
            break;
        case 16:
            ret = "AuthorizationSource";
            break;
        case 17:
            ret = "AuthorizationCharacteristicIndicator";
            break;
        case 18:
            ret = "TransactionID";
            break;
        case 19:
            ret = "ValidationCode";
            break;
        case 20:
            ret = "SIC/CatCode";
            break;
        case 21:
            ret = "CountryCode";
            break;
        case 22:
            ret = "AVSResponseCode";
            break;
        case 23:
            ret = "MerchantStoreNum";
            break;
        case 24:
            ret = "CVV2ResponseCode";
            break;
        case 25:
            ret = "CAVVCODE";
            break;
        case 26:
            ret = "CrossReferenceNum";
            break;
        case 27:
            ret = "ExtendedTransactionStatus";
            break;
        case 28:
            ret = "CAVVResponseCode";
            break;
        case 29:
            ret = "XID";
            break;
        case 30:
            ret = "ECIValue";
            break;
        default:
            break;
        }

        return ret;
    }
}
