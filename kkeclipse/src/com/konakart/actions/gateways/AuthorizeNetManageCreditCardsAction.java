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

import java.net.HttpURLConnection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.PaymentDetailsIf;

/**
 * This class is an Action class called before displaying the popup AuthorizeNet CIM page to manage
 * credit card details.
 */
public class AuthorizeNetManageCreditCardsAction extends AuthorizeNetBaseAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(AuthorizeNetManageCreditCardsAction.class);

    private static final long serialVersionUID = 1L;

    private String token;

    private String xsrf_token;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        try
        {
            int custId = -1;
            KKAppEng kkAppEng = this.getKKAppEng(request, response);
            custId = this.loggedIn(request, response, kkAppEng, "Checkout", xsrf_token);
            if (custId < 0)
            {
                return SUCCESS;
            }

            CustomerIf cust = kkAppEng.getCustomerMgr().getCurrentCustomer();
            if (cust == null)
            {
                return SUCCESS;
            }

            // Get authentication details from the Auth Net module
            getAuthNetAuthentication(kkAppEng);

            // Has the customer got an AuthorizeNet profile. If not we need to get one
            if (cust.getExtReference1() == null || cust.getExtReference1().length() == 0)
            {
                String authNetCustId = createCustomerProfile(kkAppEng, custId);
                if (authNetCustId != null)
                {
                    cust.setExtReference1(authNetCustId);
                    kkAppEng.getCustomerMgr().editCustomer(cust);
                }
            }

            if (cust.getExtReference1() != null && cust.getExtReference1().length() > 0)
            {
                token = getHostedProfilePageRequest(kkAppEng, cust.getExtReference1(), custId);
            }

            return SUCCESS;
        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * Creates an AuthorizeNet profile for KK customers if they haven't already got one
     * 
     * @param kkAppEng
     * @param custId
     * @return Returns the AuthorizeNet profile id
     * @throws Exception
     */
    private String createCustomerProfile(KKAppEng kkAppEng, int custId) throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<profile>");
        sb.append("<merchantCustomerId>" + custId + "</merchantCustomerId>");
        sb.append("</profile>");
        sb = getMessage("createCustomerProfileRequest", sb);

        String ret = sendMsgToGateway(kkAppEng, sb, "createCustomerProfileRequest",
                "customerProfileId", custId, null);

        return ret;
    }

    /**
     * Get a token in order to be able to display the popup to manage credit card details
     * 
     * @param kkAppEng
     * @param profileId
     * @param custId
     * @return Returns the value of the token
     * @throws Exception
     */
    private String getHostedProfilePageRequest(KKAppEng kkAppEng, String profileId, int custId)
            throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<customerProfileId>" + profileId + "</customerProfileId>");
        sb.append("<hostedProfileSettings>");
        sb.append("<setting>");
        sb.append("<settingName>hostedProfilePageBorderVisible</settingName>");
        sb.append("<settingValue>false</settingValue>");
        sb.append("</setting>");
        sb.append("</hostedProfileSettings>");
        sb = getMessage("getHostedProfilePageRequest", sb);

        String ret = sendMsgToGateway(kkAppEng, sb, "getHostedProfilePageRequest", "token", custId,
                profileId);

        return ret;
    }

    /**
     * Add things specific to AuthorizeNet to the connection
     */
    protected void customizeConnection(HttpURLConnection connection, PaymentDetailsIf pd,
            List<NameValueIf> paramList)
    {
        connection.setRequestProperty("content-type", "text/xml");
    }

    /**
     * @return the xsrf_token
     */
    public String getXsrf_token()
    {
        return xsrf_token;
    }

    /**
     * @param xsrf_token
     *            the xsrf_token to set
     */
    public void setXsrf_token(String xsrf_token)
    {
        this.xsrf_token = xsrf_token;
    }

    /**
     * @return the token
     */
    public String getToken()
    {
        return token;
    }

    /**
     * @param token
     *            the token to set
     */
    public void setToken(String token)
    {
        this.token = token;
    }

}
