//
// (c) 2014 DS Data Systems UK Ltd, All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.struts2.ServletActionContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.konakart.al.KKAppEng;
import com.konakart.al.KKAppException;
import com.konakart.app.IpnHistory;
import com.konakart.app.KKException;
import com.konakart.app.OrderUpdate;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderUpdateIf;
import com.konakart.bl.ConfigConstants;

/**
 * This class is an Action class called when a customer chooses to pay using a stored credit card
 */
public class AuthorizeNetCIMPayAction extends AuthorizeNetBaseAction
{
    private static final long serialVersionUID = 1L;

    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "authorizenet";

    // Authorize.net constants for response
    private static final int respCodePosition = 1;

    private static final int txnIdPosition = 7;

    private static final int txnAmountPosition = 10;

    private static final int txnTypePosition = 12;

    private static final int txnCardNumPosition = 51;

    private static final String approved = "1";

    private static final String declined = "2";

    private static final String error = "3";

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET1 = -1;

    private static final String RET1_DESC = "There was an unexpected Gateway Response.";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "Payment successful. TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "Payment not successful.";

    private String id;

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
            kkAppEng = this.getKKAppEng(request, response);

            // Check that CIM is enabled
            if (!kkAppEng.getConfigAsBoolean("MODULE_PAYMENT_AUTHORIZENET_ENABLE_CIM", false))
            {
                throw new KKException("AuthorizeNet CIM is not enabled");
            }

            // Get the payment profile id
            String paymentProfileId = request.getParameter("id");
            if (paymentProfileId == null || paymentProfileId.length() == 0)
            {
                throw new KKException("A Payment Profile Id was not passed to the action.");
            }

            // Ensure that we have an order and payment details
            OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();
            validateOrder(order, code);

            // Set the order id for the ipnHistory object
            ipnHistory.setOrderId(order.getId());

            // Set the name of the Admin class that can be used to perform captures or credits
            ipnHistory
                    .setAdminPaymentClass("com.konakartadmin.modules.payment.authorizenet.AdminPayment");

            // Check the customer
            CustomerIf cust = kkAppEng.getCustomerMgr().getCurrentCustomer();
            if (cust == null || cust.getExtReference1() == null
                    || cust.getExtReference1().length() == 0)
            {
                throw new KKAppException("A customer with external reference was not found");
            }

            // See if we need to send an email, by looking at the configuration
            boolean sendEmail = kkAppEng.getConfigAsBoolean(ConfigConstants.SEND_EMAILS, false);

            // Create the message

            // Get authentication details from the Auth Net module
            getAuthNetAuthentication(kkAppEng);

            // Get the total
            String total = null;
            if (order.getPaymentDetails().getParameters() != null)
            {
                for (int i = 0; i < order.getPaymentDetails().getParameters().length; i++)
                {
                    NameValueIf nv = order.getPaymentDetails().getParameters()[i];
                    if (nv.getName() != null && nv.getName().equals("x_amount"))
                    {
                        total = nv.getValue();
                    }
                }
            }

            if (total == null)
            {
                throw new KKAppException("Total not found in payment details");
            }

            StringBuffer sb = new StringBuffer();
            sb.append("<transaction>");
            sb.append("<profileTransAuthCapture>");
            sb.append("<amount>" + total + "</amount>");
            sb.append("<customerProfileId>" + cust.getExtReference1() + "</customerProfileId>");
            sb.append("<customerPaymentProfileId>" + paymentProfileId
                    + "</customerPaymentProfileId>");
            sb.append("</profileTransAuthCapture>");
            sb.append("</transaction>");
            sb = getMessage("createCustomerProfileTransactionRequest", sb);

            // Get a response from the gateway
            String gatewayResp = getGatewayResponse(sb, "createCustomerProfileTransactionRequest");

            // Now process the XML response
            if (gatewayResp == null)
            {
                throw new KKAppException("No response from gateway");
            }

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
            Document doc = builder.parse(bais);

            String resultCode = null;
            String code = null;
            String text = null;
            String directResponse = null;

            // get all elements
            int count = 0;
            NodeList list = doc.getElementsByTagName("*");
            for (int i = 0; i < list.getLength(); i++)
            {
                if (count == 4)
                {
                    break;
                }
                Node node = list.item(i);
                String name = node.getNodeName();
                if (name != null)
                {
                    if (name.equals("resultCode"))
                    {
                        Text datanode = (Text) node.getFirstChild();
                        resultCode = datanode.getData();
                        count++;
                    } else if (name.equals("code"))
                    {
                        Text datanode = (Text) node.getFirstChild();
                        code = datanode.getData();
                        count++;
                    } else if (name.equals("text"))
                    {
                        Text datanode = (Text) node.getFirstChild();
                        text = datanode.getData();
                        count++;
                    } else if (name.equals("directResponse"))
                    {
                        Text datanode = (Text) node.getFirstChild();
                        directResponse = datanode.getData();
                        count++;
                    }
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug("AuthorizeNet createCustomerProfileTransactionRequest response data:"
                        + "\n    resultCode                  = " + resultCode
                        + "\n    code                        = " + code
                        + "\n    text                        = " + text
                        + "\n    directResponse              = " + directResponse);
            }

            // Parse the direct response
            if (directResponse == null || directResponse.length() == 0)
            {
                String ret = "AuthorizeNet createCustomerProfileTransactionRequest response data:"
                        + "\n    resultCode                  = " + resultCode
                        + "\n    code                        = " + code
                        + "\n    text                        = " + text;

                ipnHistory.setGatewayFullResponse(ret + "\n\nResponse:\n" + gatewayResp);
                throw new KKAppException("No direct response attribute from gateway");
            }

            sb = new StringBuffer();
            String[] parms = directResponse.split(",");
            if (parms != null)
            {
                for (int i = 0; i < parms.length; i++)
                {
                    String parm = parms[i];
                    if (parm == null || parm.length() == 0)
                    {
                        continue;
                    }
                    sb.append(getRespDesc(i + 1));
                    sb.append("=");
                    sb.append(parm);
                    sb.append("\n");

                    if (i + 1 == respCodePosition)
                    {
                        gatewayResult = parm;
                        ipnHistory.setGatewayResult(getGatewayResultDescription(parm));
                    } else if (i + 1 == txnIdPosition)
                    {
                        ipnHistory.setGatewayTransactionId(parm);
                        ipnHistory.setGatewayCreditId(parm); // Used for future credit
                                                             // transactions
                        transactionId = parm;
                    } else if (i + 1 == txnTypePosition)
                    {
                        ipnHistory.setTxType(parm);
                    } else if (i + 1 == txnAmountPosition)
                    {
                        try
                        {
                            ipnHistory.setTxAmount(new BigDecimal(parm));
                        } catch (Exception e)
                        {
                        }
                    } else if (i + 1 == txnCardNumPosition)
                    {
                        // Save last 4 digits of the CC number in custom1
                        if (parm.length() >= 4)
                        {
                            ipnHistory.setCustom1(parm.substring(parm.length() - 4, parm.length()));
                        }
                    }
                }
            }

            // Put the response in the ipnHistory record
            ipnHistory.setGatewayFullResponse(sb.toString() + "\n" + gatewayResp);

            OrderUpdateIf updateOrder = new OrderUpdate();
            updateOrder.setUpdatedById(kkAppEng.getActiveCustId());

            // Determine whether the request was successful or not.If successful, we update the
            // inventory as well as changing the state of the order
            if (gatewayResult != null && gatewayResult.equals(approved))
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
            } else if (gatewayResult != null
                    && (gatewayResult.equals(declined) || gatewayResult.equals(error)))
            {
                String comment = ORDER_HISTORY_COMMENT_KO;
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
                { "" });
                addActionError(msg);

                // Redirect the user back to the credit card screen
                return "TryAgain";
            } else
            {
                /*
                 * We only get to here if there was an unknown response from the gateway
                 */
                String comment = RET1_DESC;
                kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                        com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, /* customerNotified */
                        sendEmail, comment, updateOrder);

                // Save the ipnHistory
                ipnHistory.setKonakartResultDescription(RET1_DESC + " - " + gatewayResult);
                ipnHistory.setKonakartResultId(RET1);
                ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */false);
                }

                String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
                { "" });
                addActionError(msg);

                // Redirect the user back to the credit card screen
                return "TryAgain";
            }

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
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }
}