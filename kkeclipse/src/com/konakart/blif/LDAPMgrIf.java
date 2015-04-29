//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is the proprietary property of
// DS Data Systems UK Ltd. and is protected by English copyright law,
// the laws of foreign jurisdictions, and international treaties,
// as applicable. No part of this document may be reproduced,
// transmitted, transcribed, transferred, modified, published, or
// translated into any language, in any form or by any means, for
// any purpose other than expressly permitted by DS Data Systems UK Ltd.
// in writing.
//
package com.konakart.blif;

import com.konakart.app.KKException;

/**
 * The interface that must be implemented by an LDAP Manager
 */
public interface LDAPMgrIf
{
    /**
     * Called if the LDAP module is installed and active. This method should return:
     * <ul>
     * <li>A negative number in order for the login attempt to fail. The KonaKart login() method
     * will return a null sessionId</li>
     * <li>Zero to signal that this method is not implemented. The KonaKart login() method will
     * perform the credential check.</li>
     * <li>A positive number for the login attempt to pass. The KonaKart login() will not check
     * credentials, and will log in the customer, returning a valid session id.</li>
     * </ul>
     * This method may need to be modified slightly depending on the structure of your LDAP. The
     * example works when importing the exampleData.ldif file in the LDAP module jar:
     * 
     * dn: cn=Robert Smith,ou=people,dc=example,dc=com<br/>
     * objectclass: inetOrgPerson<br/>
     * cn: Robert Smith<br/>
     * cn: Robert J Smith<br/>
     * cn: bob smith<br/>
     * sn: smith<br/>
     * uid: rjsmith<br/>
     * userpassword: rJsmitH<br/>
     * carlicense: HISCAR 123<br/>
     * homephone: 555-111-2222<br/>
     * mail: r.smith@example.com<br/>
     * mail: rsmith@example.com<br/>
     * mail: bob.smith@example.com<br/>
     * description: swell guy<br/>
     * 
     * The code attempts to connect to LDAP using the username, password and URL in the
     * configuration variables set when the module was installed through the admin app.<br/>
     * 
     * After having connected, the person object is searched for using the email address of the
     * user. If found we use the "cn" attribute and the password of the user to attempt to bind to
     * LDAP. If the bind is successful, we return a positive number which means that authentication
     * was successful.
     * 
     * @param emailAddr
     *            The user name required to log in
     * @param password
     *            The log in password
     * @return Returns an integer
     * @throws KKException
     */
    public int checkCredentials(String emailAddr, String password) throws KKException;

}
