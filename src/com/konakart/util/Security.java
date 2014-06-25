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
package com.konakart.util;

import java.security.NoSuchAlgorithmException;

/**
 * Class for managing password encryption. You may customize this class by adding your own
 * encryption algorithm.
 */
public class Security
{
    /**
     * Encrypts the password using the chosen algorithm and returns the encrypted password.
     * 
     * @param password
     *            in clear text
     * @return Returns an encrypted password
     * @throws NoSuchAlgorithmException
     *             thrown if the chosen algorithm isn't available.
     */
    public static String encrypt(String password) throws NoSuchAlgorithmException
    {
        return SecurityBase.encrypt(password);
    }

    /**
     * Compare the hashed password from the DB with the one in clear text provided by the user. The
     * clear text password must be encrypted using the chosen algorithm and then compared with the
     * hashed password stored in the database.
     * 
     * @param dbPassword
     *            raw encrypted password value from the database
     * @param password
     *            the password in clear text as entered by the user
     * @return Returns true if the password matches the one in the database. Otherwise returns
     *         false.
     * @throws NoSuchAlgorithmException
     *             thrown if the chosen algorithm isn't available.
     */
    public static boolean checkPassword(String dbPassword, String password)
            throws NoSuchAlgorithmException
    {
        return SecurityBase.checkPassword(dbPassword, password);
    }

}
