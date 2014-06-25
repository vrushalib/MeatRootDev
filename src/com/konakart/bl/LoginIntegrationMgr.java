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
package com.konakart.bl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.KKException;
import com.konakart.appif.KKEngIf;

/**
 * Used to provide an integration point when a customer attempts a login so that custom security
 * logic can be implemented.
 * 
 */
public class LoginIntegrationMgr extends BaseMgr implements LoginIntegrationMgrInterface
{
    /** the log */
    protected static Log log = LogFactory.getLog(LoginIntegrationMgr.class);

    /**
     * Constructor
     * 
     * @param eng
     * @throws Exception
     */
    public LoginIntegrationMgr(KKEngIf eng) throws Exception
    {
        super.init(eng, log);
        
        if (log.isDebugEnabled())
        {
            if (eng != null && eng.getEngConf() != null && eng.getEngConf().getStoreId() != null)
            {
                log.debug("LoginIntegrationMgr instantiated for store id = "
                        + eng.getEngConf().getStoreId());
            }
        }
    }

    /**
     * Called whenever a login attempt is made. This method should return:
     * <ul>
     * <li>A negative number in order for the login attempt to fail. The KonaKart login() method
     * will return a null sessionId</li>
     * <li>Zero to signal that this method is not implemented. The KonaKart login() method will
     * perform the credential check.</li>
     * <li>A positive number for the login attempt to pass. The KonaKart login() will not check
     * credentials, and will log in the customer, returning a valid session id.</li>
     * </ul>
     * 
     * @param emailAddr
     *            The user name required to log in
     * @param password
     *            The log in password
     * @return Returns an integer
     * @throws KKException
     */
    public int checkCredentials(String emailAddr, String password) throws KKException
    {
        return 0;
    }

}
