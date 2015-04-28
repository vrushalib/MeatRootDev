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
// Original version contributed by Chris Derham (Atomus Ltd)
//

package com.konakart.bl.modules.payment.moneybookers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Convenience class for MD5 encoding
 */
public class Md5Wrapper 
{
    private MessageDigest digest;

    /**
     * Constructor
     */
    public Md5Wrapper() 
    {
        try 
        {
            digest = java.security.MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) 
        {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    /**
     * @param source
     * @return an encoded String
     */
    public String encode(String source) 
    {
        digest.update(source.getBytes());
        byte[] raw = digest.digest();
        String encodedHex = bytesToHex(raw);
        return encodedHex;
    }
    
    /**
     * Converts the byte array into a hexadecimal string
     * 
     * @param b byte array to convert a hexadecimal String
     * @return Return a Hex String
     */
    private static String bytesToHex(byte[] b)
    {
        char hexDigit[] =
        { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        StringBuffer buf = new StringBuffer();
        
        for (int j = 0; j < b.length; j++)
        {
            buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
            buf.append(hexDigit[b[j] & 0x0f]);
        }
        return buf.toString();
    }
}
