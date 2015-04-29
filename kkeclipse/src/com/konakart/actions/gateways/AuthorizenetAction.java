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

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.app.CreditCard;
import com.konakart.app.IpnHistory;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.OrderUpdate;
import com.konakart.app.Subscription;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderProductIf;
import com.konakart.appif.OrderUpdateIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.appif.PaymentScheduleIf;
import com.konakart.appif.ProductIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.bl.LanguageMgr;

/**
 * This class is an Action class for sending credit card details to Authorize.net and receiving
 * confirmation
 */
public class AuthorizenetAction extends AuthorizeNetBaseAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(AuthorizenetAction.class);

    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "authorizenet";

    // Authorize.net constants for request

    private static final String x_card_num = "x_card_num";

    private static final String x_exp_date = "x_exp_date";

    private static final String x_card_code = "x_card_code";

    // Authorize.net constants for response
    private static final int respCodePosition = 1;

    private static final int txnIdPosition = 7;

    private static final int txnAmountPosition = 10;

    private static final int txnTypePosition = 12;

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

            // Set the name of the Admin class that can be used to perform captures or credits
            ipnHistory
                    .setAdminPaymentClass("com.konakartadmin.modules.payment.authorizenet.AdminPayment");

            // Create a parameter list for the credit card details.
            PaymentDetailsIf pd = order.getPaymentDetails();

            // See if we need to send an email, by looking at the configuration
            String sendEmailsConfig = kkAppEng.getConfig(ConfigConstants.SEND_EMAILS);
            boolean sendEmail = false;
            if (sendEmailsConfig != null && sendEmailsConfig.equalsIgnoreCase("true"))
            {
                sendEmail = true;
            }

            /*
             * Check for recurring billing. This can be commented out if not relevant. It may need
             * to be changed to only create one subscription for the whole order rather than one for
             * each product. You may also need to pay for some products and create a subscription
             * for others.
             */
            // boolean isSubscription = manageRecurringBilling(kkAppEng, order);
            // if (isSubscription)
            // {
            // kkAppEng.getEng().changeOrderStatus(kkAppEng.getSessionId(), order.getId(),
            // com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, /* customerNotified */
            // sendEmail, "Some Comment");
            // return "Approved");
            // }

            List<NameValueIf> parmList = new ArrayList<NameValueIf>();
            parmList.add(new NameValue(x_card_num, pd.getCcNumber()));
            if (pd.getCcNumber() != null && pd.getCcNumber().length() > 3)
            {
                // Save last 4 digits of the CC number in custom1
                ipnHistory.setCustom1(pd.getCcNumber().substring(pd.getCcNumber().length() - 4,
                        pd.getCcNumber().length()));
            }

            if (pd.isShowCVV())
            {
                parmList.add(new NameValue(x_card_code, pd.getCcCVV()));
            }
            parmList.add(new NameValue(x_exp_date, pd.getCcExpiryMonth() + pd.getCcExpiryYear()));

            // Do the post
            String gatewayResp = postData(pd, parmList);

            gatewayResp = URLDecoder.decode(gatewayResp, "UTF-8");
            if (log.isDebugEnabled())
            {
                log.debug("Unformatted GatewayResp = \n" + gatewayResp);
            }

            // Process the parameters sent in the callback
            StringBuffer sb = new StringBuffer();
            String[] parms = gatewayResp.split(",");
            if (parms != null)
            {
                for (int i = 0; i < parms.length; i++)
                {
                    String parm = parms[i];
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
                        ipnHistory.setGatewayCreditId(parm); // Used for future credit transactions
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
                    }
                }
            }

            // Put the response in the ipnHistory record
            ipnHistory.setGatewayFullResponse(sb.toString());

            if (log.isDebugEnabled())
            {
                log.debug("Formatted Authorize.net response data:");
                log.debug(sb.toString());
            }

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
     * This code determines whether the order contains any products that require recurring billing.
     * If it does, then subscriptions are created for these products. It may need to be changed to
     * only create one subscription for the whole order rather than one for each product. You may
     * also need to pay for some products and create a subscription for others.
     * 
     * @param kkAppEng
     * @param order
     * @return Returns true if a subscription was created
     */
    @SuppressWarnings("unused")
    private boolean manageRecurringBilling(KKAppEng kkAppEng, OrderIf order) throws KKException
    {
        boolean isSubscription = false;
        if (order.getOrderProducts() != null)
        {
            for (int i = 0; i < order.getOrderProducts().length; i++)
            {
                OrderProductIf op = order.getOrderProducts()[i];
                ProductIf prod = kkAppEng.getEng().getProduct(null, op.getProductId(),
                        LanguageMgr.DEFAULT_LANG);
                PaymentScheduleIf schedule = null;
                if (prod != null)
                {
                    schedule = prod.getPaymentSchedule();
                }
                if (schedule != null && prod != null)
                {
                    /*
                     * The product requires recurring billing so we insert a Subscription. Some of
                     * the optional attributes are commented and may be set depending on your
                     * business requirements.
                     */
                    isSubscription = true;
                    Subscription sub = new Subscription();
                    sub.setOrderId(order.getId());
                    sub.setPaymentScheduleId(schedule.getId());
                    sub.setProductId(prod.getId());
                    sub.setCustomerId(order.getCustomerId());
                    sub.setOrderNumber(order.getOrderNumber());
                    sub.setProductSku(op.getSku());
                    sub.setAmount(op.getFinalPriceIncTax());
                    sub.setActive(true);
                    sub.setStartDate(new GregorianCalendar());
                    // sub.setLastBillingDate();
                    // sub.setNextBillingDate();
                    // sub.setTrialAmount();

                    PaymentDetailsIf pd = order.getPaymentDetails();
                    CreditCard cc = new CreditCard();
                    cc.setCcExpires(pd.getCcExpiryYear() + "-" + pd.getCcExpiryMonth());
                    cc.setCcNumber(pd.getCcNumber());
                    if (pd.isShowCVV())
                    {
                        cc.setCcCVV(pd.getCcCVV());
                    }
                    sub.setCreditCard(cc);

                    /*
                     * Call the engine to insert the subscription. The subscription may also be
                     * created in the payment gateway at this point by implementing the relevant
                     * code in the OrderIntegrationMgr() in the afterInsertSubscription() method.
                     */
                    kkAppEng.getEng().insertSubscription(kkAppEng.getSessionId(), sub);

                    isSubscription = true;
                }
            }
        }
        return isSubscription;
    }


}
