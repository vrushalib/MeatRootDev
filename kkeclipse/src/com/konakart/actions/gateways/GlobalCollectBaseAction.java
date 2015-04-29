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

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.bl.modules.payment.globalcollect.GlobalCollectUtils;
import com.konakart.util.Utils;

/**
 * This class is a Base Action class for sending and parsing messages to/from GlobalCollect.
 */
public class GlobalCollectBaseAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(GlobalCollectBaseAction.class);
    
    private static final long serialVersionUID = 1L;

    // Return codes and descriptions
    protected static final int RET0 = 0;

    protected static final String RET0_DESC = "Transaction OK";

    protected static final int RET1 = -1;

    protected static final String RET1_DESC = "There was an unexpected Gateway Response. Response = ";

    protected static final String RET3_DESC = "There was an unexpected Gateway Response.";

    protected static final int RET4 = -4;

    protected static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    /**
     * Add things specific to GlobalCollect to the connection
     */
    protected void customizeConnection(HttpURLConnection connection, PaymentDetailsIf pd,
            List<NameValueIf> paramList)
    {
        connection.setRequestProperty("content-type", "text/xml; charset=utf-8");
    }

    /**
     * @param gatewayResp
     * @throws Exception
     */
    protected Map<String, String> parseGlobaLCollectResponseToMap(String gatewayResp, KKEngIf eng)
            throws Exception
    {
        GlobalCollectUtils globalCollectUtils = new GlobalCollectUtils();
        return globalCollectUtils.parseGlobalCollectResponseToMap(gatewayResp);
    }

    /**
     * Clears the IpnHistory object
     * 
     * @param in
     * @return Returns the cleared IpnHistory object
     */
    protected IpnHistoryIf clearIpnHistory(IpnHistoryIf in)
    {
        in.setGatewayResult(null);
        in.setGatewayTransactionId(null);
        in.setGatewayFullResponse(null);
        in.setKonakartResultDescription(null);
        in.setKonakartResultId(0);
        return in;
    }

    /**
     * Use this to truncate the result description so that it fits in the database column OK
     * 
     * @param desc
     *            the result description (which may be too long)
     * @return a truncated result description
     */
    protected String getResultDescription(String desc)
    {
        if (desc == null)
        {
            return null;
        }

        return desc.substring(0, Math.min(255, desc.length()));
    }

    /**
     * Common code for setting up the redirect response.
     * 
     * @param response
     */
    protected void setupResponseForTemporaryRedirect(HttpServletResponse response)
    {
        response.setStatus(302);
        response.setHeader("Expires", "Wed, 11 Jan 1984 05:00:00 GMT");
        response.setHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Connection", "close");
    }
    
    /**
     * Return a ModuleCode to go in the IpnHistory record
     * 
     * @param pd
     * @return a Module code guaranteed to fit in the module code field of the IpnHistory record
     */
    protected String getModuleCodeForIpnRecord(PaymentDetailsIf pd)
    {
        return Utils.trim("GC - " + pd.getSubCode(), 32, true);
    }
    
    /**
     * Return a TransactionType to go in the IpnHistory record
     * 
     * @param pd
     * @return a TransactionType guaranteed to fit in the module code field of the IpnHistory record
     */
    protected String getTxTypeForIpnRecord(PaymentDetailsIf pd)
    {
        return Utils.trim(pd.getSubCode() + " = " + pd.getTitle(), 128, true);
    }
}
