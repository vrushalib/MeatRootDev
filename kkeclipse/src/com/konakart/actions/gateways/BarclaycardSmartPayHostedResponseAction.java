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

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.app.IpnHistory;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.bl.modules.payment.barclaycardsmartpayhosted.BarclaycardSmartPayHosted;

/**
 * This class is an Action class for what to do when a payment result is received from Barclaycard
 * SmartPay Hosted.
 * <p>
 * The result could be Authorised, Refused, Cancelled, Pending or Error
 */
public class BarclaycardSmartPayHostedResponseAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(BarclaycardSmartPayHostedResponseAction.class);

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        String authResult = null;
        String pspReference = null;
        String merchantReference = null;
        String paymentMethod = null;
        String skinCode = null;
        String merchantSig = null;
        String shopperLocale = null;
        String merchantReturnData = null;
        KKAppEng kkAppEng = null;

        if (log.isDebugEnabled())
        {
            log.debug(BarclaycardSmartPayHosted.BC_SPAY_HOSTED_GATEWAY_CODE + " Response Action");
        }

        // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setOrderId(-1);
        ipnHistory.setModuleCode(BarclaycardSmartPayHosted.BC_SPAY_HOSTED_GATEWAY_CODE);

        try
        {
            int custId;

            // Get an instance of the KonaKart engine
            // kkAppEng = this.getKKAppEng(request); // v3.2 code
            kkAppEng = this.getKKAppEng(request, response); // v4.1 code

            custId = this.loggedIn(request, response, kkAppEng, "Checkout");

            // Check to see whether the user is logged in
            if (custId < 0)
            {
                return KKLOGIN;
            }

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
                        if (paramName.equalsIgnoreCase("authResult"))
                        {
                            authResult = paramValue;
                        } else if (paramName.equalsIgnoreCase("pspReference"))
                        {
                            pspReference = paramValue;
                        } else if (paramName.equalsIgnoreCase("merchantReference"))
                        {
                            merchantReference = paramValue;
                        } else if (paramName.equalsIgnoreCase("skinCode"))
                        {
                            skinCode = paramValue;
                        } else if (paramName.equalsIgnoreCase("merchantSig"))
                        {
                            merchantSig = paramValue;
                        } else if (paramName.equalsIgnoreCase("paymentMethod"))
                        {
                            paymentMethod = paramValue;
                        } else if (paramName.equalsIgnoreCase("shopperLocale"))
                        {
                            shopperLocale = paramValue;
                        } else if (paramName.equalsIgnoreCase("merchantReturnData"))
                        {
                            merchantReturnData = paramValue;
                        } else
                        {
                            log.warn("Unknown Parameter in response:  '" + paramName + "' = '"
                                    + paramValue + "'");
                        }
                    }
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug(BarclaycardSmartPayHosted.BC_SPAY_HOSTED_GATEWAY_CODE
                        + " Raw Response data:\n" + sb.toString());
                log.debug("\n    authResult                = " + authResult
                        + "\n    pspReference              = " + pspReference
                        + "\n    merchantReference         = " + merchantReference
                        + "\n    skinCode                  = " + skinCode
                        + "\n    merchantSig               = " + merchantSig
                        + "\n    paymentMethod             = " + paymentMethod
                        + "\n    shopperLocale             = " + shopperLocale
                        + "\n    merchantReturnData        = " + merchantReturnData);
            }

            // If we didn't receive an authResult, we log a warning and return
            if (authResult == null)
            {
                throw new Exception("No authResult returned for the "
                        + BarclaycardSmartPayHosted.BC_SPAY_HOSTED_GATEWAY_CODE + " module");
            }

            // If we didn't receive an AUTHORISED authResult, we let the user Try Again

            if (!authResult.equals("AUTHORISED"))
            {
                return "CheckoutError";
            }

            // If successful, we forward to "Approved"

            return "Approved";

        } catch (Exception e)
        {
            e.printStackTrace();

            return super.handleException(request, e);
        }
    }
}
