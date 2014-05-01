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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
 * This class is an Action class for sending credit card details to YourPay and receiving
 * confirmation
 */
public class YourpayAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(YourpayAction.class);

    // Module name must be the same as the class name although it can be all in lower case in order
    // to remain compatible with osCommerce.
    private static String code = "yourpay";

    private static final String APPROVED = "APPROVED";

    private static final String DECLINED = "DECLINED";

    private static final String FRAUD = "FRAUD";

    private static final String PROCESSING_ERROR = "PROCESSING_ERROR";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "Payment successful. YourPay Ref Number = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "Payment not successful. YourPay Reply = ";

    // IPN History Result IDs
    private static final int RESULT_APPROVED = 1;

    private static final int RESULT_DECLINED = -1;

    private static final int RESULT_UNKNOWN_PROCESSING_ERROR = -2;

    private static final int RESULT_UNKNOWN_ERROR = -3;

    private static final int RESULT_UNKNOWN_EXCEPTION = -4;

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        if (log.isDebugEnabled())
        {
            log.debug("YourPayAction: code = " + code);
        }

        // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setModuleCode(code);
        KKAppEng kkAppEng = null;

        try
        {
            int custId;

            kkAppEng = this.getKKAppEng(request, response);

            boolean yourPayDebugMode = log.isDebugEnabled();

            // Check to see whether the user is logged in
            custId = this.loggedIn(request, response, kkAppEng, "Checkout");
            if (custId < 0)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("YourPayAction: NotLoggedIn");
                }
                return KKLOGIN;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("YourPayAction: Redirect SSL");
                }
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Get the order
            OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();
            validateOrder(order, code);

            if (log.isDebugEnabled())
            {
                log.debug("YourPayAction: Order " + order.getId() + " validated");
            }

            // Set the order id for the ipnHistory object
            ipnHistory.setOrderId(order.getId());

            // Get the parameter list for the payment that have been set up earlier
            PaymentDetailsIf pd = order.getPaymentDetails();

            // Now make a second parameter list containing parameters we don't save
            List<NameValueIf> parmList = new ArrayList<NameValueIf>();
            parmList.add(new NameValue("cardnumber", pd.getCcNumber()));
            parmList.add(new NameValue("expmonth", pd.getCcExpiryMonth()));

            // OK.. this needs to change in 93 years or so....
            parmList.add(new NameValue("expyear", "20" + pd.getCcExpiryYear()));

            if (log.isDebugEnabled())
            {
                log.debug("YourPayAction: Add CC number " + pd.getCcNumber());
                log.debug("YourPayAction: Add Exp Month " + pd.getCcExpiryMonth());
                log.debug("YourPayAction: Add Exp Year " + pd.getCcExpiryYear());
            }

            if (pd.isShowCVV())
            {
                parmList.add(new NameValue("cvm", pd.getCcCVV()));
            }

            parmList.add(new NameValue("bname", pd.getCcOwner()));
            parmList.add(new NameValue("baddr1", pd.getCcStreetAddress()));
            parmList.add(new NameValue("bzip", pd.getCcPostcode()));

            if (log.isDebugEnabled())
            {
                log.debug("YourPayAction: Post the payment details to the gateway");
            }

            // Do the post
            String gatewayResp = postData(pd, parmList);

            if (log.isDebugEnabled())
            {
                log.debug("Gateway Response: \n" + gatewayResp + "\n");
            }

            if (yourPayDebugMode)
            {
                // Write the response to an HTML file which is handy for diagnosing problems

                try
                {
                    String outputFilename = getLogFileDirectory(kkAppEng) + "yourpay_resp_"
                            + order.getId() + ".html";
                    File myOutFile = new File(outputFilename);
                    if (log.isDebugEnabled())
                    {
                        log.debug("Write gateway response to " + myOutFile.getAbsolutePath());
                    }
                    BufferedWriter bw = new BufferedWriter(new FileWriter(myOutFile));
                    bw.write(gatewayResp);
                    bw.close();
                } catch (Exception e)
                {
                    // dump the exception and continue
                    e.printStackTrace();
                }
            }

            // Process the parameters returned by the gateway

            // Since YourPay Connect returns an HTML response we have to parse that to figure out
            // what to do next.

            String status = getFieldValue(gatewayResp, "status");
            String failReason = getFieldValue(gatewayResp, "failReason");
            String refNumber = getFieldValue(gatewayResp, "refnumber");
            // String approval_code = getFieldValue(gatewayResp, "approval_code");

            if (status == null)
            {
                // is it a form processing error?
                int idxFormProcErr = gatewayResp.indexOf("<P>Form Processing Error</P>");

                if (idxFormProcErr == -1)
                {
                    idxFormProcErr = gatewayResp.indexOf("Please fill in the required information");
                }

                if (idxFormProcErr != -1)
                {
                    status = PROCESSING_ERROR;

                    int idxItemTagStart, idxItemTagEnd;

                    idxItemTagStart = gatewayResp.indexOf("<LI>", idxFormProcErr);
                    while (idxItemTagStart != -1)
                    {
                        idxItemTagEnd = gatewayResp.indexOf("</LI>", idxItemTagStart);

                        if (idxItemTagEnd != -1)
                        {
                            if (failReason == null)
                            {
                                failReason = new String();
                            }
                            failReason += removeFontTag(gatewayResp.substring(idxItemTagStart + 4,
                                    idxItemTagEnd).trim())
                                    + " ";
                        }

                        idxItemTagStart = gatewayResp.indexOf("<LI>", idxItemTagEnd);
                    }

                    if (log.isDebugEnabled())
                    {
                        log.debug("YourPay Form Processing Error Response: \n" + failReason);
                    }
                }
            }

            // Get a friendlier version of the gateway response showing the separate field values
            if (log.isDebugEnabled())
            {
                log.debug("Parse YourPay Response picking out name-value pairs");
            }
            StringBuffer sb = new StringBuffer();
            int idx = gatewayResp.indexOf(" name=\"");
            while (idx != -1)
            {
                int idxNameEnd = gatewayResp.indexOf("\" ", idx);

                // If the "> isn't found we return null
                if (idxNameEnd != -1)
                {
                    String name = gatewayResp.substring(idx + 7, idxNameEnd);
                    String value = getFieldValue(gatewayResp, name);

                    if (value != null)
                    {
                        sb.append(name + " = " + value + "\n");
                    }
                }

                idx = gatewayResp.indexOf(" name=\"", idx + 5);
            }

            if (status != null)
            {
                ipnHistory.setGatewayResult(status);
            } else
            {
                ipnHistory.setGatewayResult("Error");
            }

            ipnHistory.setGatewayTransactionId(refNumber);

            // Put the response in the ipnHistory record
            ipnHistory.setGatewayFullResponse(sb.toString());

            if (log.isDebugEnabled())
            {
                log.debug("Formatted YourPay response data: ");
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
            if (status != null && status.equals(APPROVED))
            {
                String comment = ORDER_HISTORY_COMMENT_OK + refNumber;
                // + " Approval Code: " + approval_code;
                kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                        com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, /* customerNotified */
                        sendEmail, comment, updateOrder);

                // Update the inventory
                kkAppEng.getOrderMgr().updateInventory(order.getId());

                // Save the ipnHistory
                ipnHistory.setKonakartResultDescription(comment);
                ipnHistory.setKonakartResultId(RESULT_APPROVED);
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

            } else if (status != null
                    && (status.equals(DECLINED) || status.equals(FRAUD) || status
                            .equals(PROCESSING_ERROR)))
            {
                String comment = ORDER_HISTORY_COMMENT_KO + failReason;
                kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                        com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, /* customerNotified */
                        sendEmail, comment, updateOrder);

                // Save the ipnHistory
                ipnHistory.setKonakartResultDescription(failReason);
                if (status.equals(DECLINED))
                {
                    ipnHistory.setKonakartResultId(RESULT_DECLINED);
                } else
                {
                    ipnHistory.setKonakartResultId(RESULT_UNKNOWN_PROCESSING_ERROR);
                }
                ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */false);
                }

                String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
                { failReason });
                addActionError(msg);

                // Redirect the user back to the credit card screen
                return "TryAgain";
            } else
            {
                /*
                 * We only get to here if there was an unknown response from the gateway
                 */
                String comment = "YourPay Error: ";

                if (status != null)
                {
                    comment += "Status = " + status;
                }

                if (failReason != null)
                {
                    comment += " - " + failReason;
                }

                kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                        com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, /* customerNotified */
                        sendEmail, comment, updateOrder);

                // Save the ipnHistory
                ipnHistory.setKonakartResultDescription(comment);
                ipnHistory.setKonakartResultId(RESULT_UNKNOWN_ERROR);
                ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */false);
                }

                String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
                { comment });
                addActionError(msg);

                // Redirect the user back to the credit card screen
                return "TryAgain";
            }
        } catch (Exception e)
        {
            try
            {
                ipnHistory.setKonakartResultDescription(e.getMessage());
                ipnHistory.setKonakartResultId(RESULT_UNKNOWN_EXCEPTION);
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
     * Return the value of the specified field in the HTML response String
     * 
     * @param response
     *            String from gateway - for YourPay this is an HTML String
     * @param fieldName
     *            the field name whose value we seek to find in in the response
     * @return Return the field's value; If the field isn't found return null
     */
    private String getFieldValue(String response, String fieldName)
    {
        int idx = response.indexOf("name=\"" + fieldName + "\" value");

        // If the field isn't found we return null
        if (idx == -1)
        {
            return null;
        }

        // now move on to: value="the value"
        idx = response.indexOf("value=\"", idx);

        // If the value isn't found we return null
        if (idx == -1)
        {
            return null;
        }

        // now move on to: "> at the end of the value
        int idxEnd = response.indexOf("\">", idx);

        // If the "> isn't found we return null
        if (idxEnd == -1)
        {
            return null;
        }

        return response.substring(idx + 7, idxEnd);
    }

    /**
     * Remove the <font ***> and </font> tags that surround the string if they're present
     * 
     * @param str
     *            the string to process
     * @return a new String without the font tags
     */
    private String removeFontTag(String str)
    {
        String newStr = str;
        if (log.isDebugEnabled())
        {
            log.debug("RemoveFontTag from '" + str + "'");
        }

        if (!str.endsWith("</font>"))
        {
            if (log.isDebugEnabled())
            {
                log.debug("Returning '" + newStr + "'");
            }
            return newStr;
        }

        newStr = str.substring(0, str.length() - 7);

        int idx = newStr.indexOf(">");

        if (idx == -1)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Returning '" + newStr + "'");
            }
            return newStr;
        }

        newStr = newStr.substring(idx + 1);
        if (log.isDebugEnabled())
        {
            log.debug("Returning '" + newStr + "'");
        }

        return newStr;
    }
}
