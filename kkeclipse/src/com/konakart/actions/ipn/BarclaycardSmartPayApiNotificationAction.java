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

package com.konakart.actions.ipn;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.konakart.actions.gateways.BaseGatewayAction;
import com.konakart.al.KKAppEng;
import com.konakart.app.IpnHistory;
import com.konakart.app.KKException;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.SSOTokenIf;
import com.konakart.bl.modules.payment.barclaycardsmartpayapi.BarclaycardSmartPayApi;

/**
 * This class is an Action class for what to do when a payment notification callback is received
 * from Barclaycard SmartPay Api.
 */
public class BarclaycardSmartPayApiNotificationAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(BarclaycardSmartPayApiNotificationAction.class);

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET2 = -2;

    private static final String RET2_DESC = "HTTP Authentication Failed";

    private static final int RET3 = -3;

    private static final String RET3_DESC = "Unable to obtain order number";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There has been an unexpected exception. Please look at the log.";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        String httpAuthStr = null;
        String httpUsername;
        String httpPassword;
        String pspReference = null;
        String merchantReference = null;
        String merchantAccountCode = null;
        String eventDate = null;
        String successString = null;
        boolean success = false;
        String paymentMethod = null;
        String value = null;
        String currency = null;
        String reason = null;

        String eventCode = null;
        String status = null;

        String sessionId = null;
        KKAppEng kkAppEng = null;

        if (log.isDebugEnabled())
        {
            log.debug(BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE + " Notification Action");
        }

        // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setOrderId(-1);
        ipnHistory.setModuleCode(BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE);

        try
        {
            // Process the parameters sent in the callback
            StringBuffer sb = new StringBuffer();
            if (request != null)
            {
                Enumeration<String> en = request.getParameterNames();
                while (en.hasMoreElements())
                {
                    String paramName = en.nextElement();
                    String paramValue = request.getParameter(paramName);
                    if (sb.length() > 0)
                    {
                        sb.append("\n");
                    }
                    sb.append(paramName);
                    sb.append(" = ");
                    sb.append(paramValue);

                    // Capture important variables so that we can determine whether the transaction
                    // was successful
                    if (paramName != null)
                    {
                        if (paramName.equalsIgnoreCase("eventCode"))
                        {
                            eventCode = paramValue;
                        } else if (paramName.equalsIgnoreCase("pspReference"))
                        {
                            pspReference = paramValue;
                        } else if (paramName.equalsIgnoreCase("merchantReference"))
                        {
                            merchantReference = paramValue;
                        } else if (paramName.equalsIgnoreCase("merchantAccountCode"))
                        {
                            merchantAccountCode = paramValue;
                        } else if (paramName.equalsIgnoreCase("eventDate"))
                        {
                            eventDate = paramValue;
                        } else if (paramName.equalsIgnoreCase("success"))
                        {
                            successString = paramValue;
                            success = Boolean.valueOf(successString);
                        } else if (paramName.equalsIgnoreCase("paymentMethod"))
                        {
                            paymentMethod = paramValue;
                        } else if (paramName.equalsIgnoreCase("value"))
                        {
                            value = paramValue;
                        } else if (paramName.equalsIgnoreCase("currency"))
                        {
                            currency = paramValue;
                        } else if (paramName.equalsIgnoreCase("reason"))
                        {
                            reason = paramValue;
                        }
                    }
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug(BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE
                        + " Raw Notification Data:\n" + sb.toString());
                log.debug("\n    merchantAccountCode       = " + merchantAccountCode + "\n"
                        + "    eventCode                 = " + eventCode + "\n"
                        + "    eventDate                 = " + eventDate + "\n"
                        + "    merchantReference         = " + merchantReference + "\n"
                        + "    pspReference              = " + pspReference + "\n"
                        + "    paymentMethod             = " + paymentMethod + "\n"
                        + "    amount                    = " + value + "\n"
                        + "    currency                  = " + currency + "\n"
                        + "    success                   = " + successString + "\n"
                        + "    reason                    = " + reason);
            }

            // If we didn't receive an eventCode, we log a warning and return
            if (eventCode == null)
            {
                log.warn("No eventCode returned by "
                        + BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE);
                return null;
            }

            status = eventCode;
            if (eventCode.equals("AUTHORISATION"))
            {
                if (success)
                {
                    status += " successful";
                } else
                {
                    status += " unsuccessful";
                }
            }

            // Fill more details of the IPN history class
            ipnHistory.setGatewayResult(status);
            ipnHistory.setGatewayFullResponse(sb.toString());
            ipnHistory.setGatewayTransactionId(pspReference);

            /*
             * Get the uuid from the request so that we can look up the SSO Token
             */
            if (merchantReference == null)
            {
                throw new Exception(
                        "The callback from BarclaycardSmartPayApi did not contain the 'merchantReference' parameter.");
            }

            // Get an instance of the KonaKart engine and look up the token
            kkAppEng = this.getKKAppEng(request, response);
            SSOTokenIf token = kkAppEng.getEng().getSSOToken(merchantReference, /* deleteToken */
            true);
            if (token == null)
            {
                throw new Exception("The SSOToken from the BarclaycardSmartPayApi callback is null");
            }

            /*
             * Use the session of the logged in user to initialise kkAppEng
             */
            try
            {
                kkAppEng.getEng().checkSession(token.getSessionId());
            } catch (KKException e)
            {
                throw new Exception(
                        "The SessionId from the SSOToken in the BarclaycardSmartPayApi Callback is not valid: "
                                + token.getSessionId());
            }

            // Log in the user
            kkAppEng.getCustomerMgr().loginBySession(token.getSessionId());
            sessionId = token.getSessionId();

            /*
             * Get the parameters from the token
             */
            String custom1 = token.getCustom1();
            String[] custom1Array = custom1.split("~");
            if (custom1Array == null || custom1Array.length != 3)
            {
                throw new Exception("Custom1 field of token doesn't contain expected data: "
                        + token.getCustom1());
            }
            httpAuthStr = custom1Array[0];
            int orderId = Integer.parseInt(custom1Array[1]);
            String countryCode = custom1Array[2];
            httpUsername = token.getCustom2();
            httpPassword = token.getCustom3();

            if (countryCode == null)
            {
                log.warn("CountryCode not returned in the "
                        + BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE + " response");
            }

            ipnHistory.setOrderId(orderId);

            // Do HTTP Authentication if required
            if (httpAuthStr != null && Boolean.valueOf(httpAuthStr))
            {
                // Get Authorization header
                String auth = null;

                if (request != null)
                {
                    auth = request.getHeader("Authorization");
                }

                // Do we allow that user?
                if (!allowUser(auth, httpUsername, httpPassword))
                {
                    // Not allowed, so return "unauthorized"
                    response.setContentType("text/plain");
                    response.setHeader("WWW-Authenticate", "BASIC realm=\"Protected Page\"");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    log.warn("Notification from " + BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE
                            + " could not be Authenticated");

                    ipnHistory.setKonakartResultDescription(RET2_DESC);
                    ipnHistory.setKonakartResultId(RET2);
                    kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);
                    return null;
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug("Accept Notification for "
                        + BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE);
            }

            // We always accept the Notification if we get this far
            response.setContentType("text/plain");
            response.getWriter().print("[accepted]\n");

            if (orderId < 0)
            {
                ipnHistory.setKonakartResultDescription(RET3_DESC);
                ipnHistory.setKonakartResultId(RET3);
                kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);
                return null;
            }

            // If it's not an AUTHORISATION event, we just throw it away
            if (!eventCode.equals("AUTHORISATION"))
            {
                if (log.isInfoEnabled())
                {
                    log.info("'" + eventCode + "' notification sent from "
                            + BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE + " discarded");
                }
                return null;
            }

            // If we're about to set the order status to the current value we'll assume this is a
            // duplicate Notification from Barclaycard and not do any updates

            int currentOrderStatus = kkAppEng.getEng().getOrderStatus(sessionId, orderId);

            if (log.isDebugEnabled())
            {
                log.debug("currentOrderStatus for orderId " + orderId + " = " + currentOrderStatus);
            }

            if ((success && currentOrderStatus == com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS)
                    || (!success && currentOrderStatus == com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Possible Duplicate '" + eventCode + "' notification sent from "
                            + BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE + " discarded");
                }
                return null;
            }

            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
            kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);

            return null;

        } catch (Exception e)
        {
            try
            {
                if (sessionId != null)
                {
                    ipnHistory.setKonakartResultDescription(RET4_DESC);
                    ipnHistory.setKonakartResultId(RET4);
                    if (kkAppEng != null)
                    {
                        kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);
                    }
                }
            } catch (KKException e1)
            {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return null;
        } finally
        {
            if (sessionId != null && kkAppEng != null)
            {
                try
                {
                    kkAppEng.getEng().logout(sessionId);
                } catch (KKException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Checks the user information sent in the Authorization header to see if the user is allowed
     * 
     * @param auth
     * @param username
     * @param password
     * @return true if the user is authenticated, otherwise false
     */
    protected boolean allowUser(String auth, String username, String password)
    {
        if (auth == null)
        {
            return false; // credentials are missing
        }

        if (!auth.toUpperCase().startsWith("BASIC "))
        {
            return false; // we only do BASIC
        }

        // Get encoded user and password, comes after "BASIC "
        String userpassEncoded = auth.substring(6);

        // Decode it
        String userpassDecoded = new String(Base64.decodeBase64(userpassEncoded.getBytes()));

        if (log.isDebugEnabled())
        {
            log.debug("auth credentials decoded = "
                    + userpassDecoded.substring(0, Math.min(userpassDecoded.length(), 4)) + "*****");
            log.debug("stored credentials       = "
                    + username.substring(0, Math.min(username.length(), 4)) + "*****");
        }

        // Check our stored username and password to see if this user and password are "allowed"
        if ((username + ":" + password).equals(userpassDecoded))
        {
            return true;
        }

        return false;
    }
}
