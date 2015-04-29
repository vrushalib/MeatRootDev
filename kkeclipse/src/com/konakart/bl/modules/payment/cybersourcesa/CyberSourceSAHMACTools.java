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

package com.konakart.bl.modules.payment.cybersourcesa;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * HMAC Tools for the CyberSource gateway
 */
public class CyberSourceSAHMACTools
{
    private static String HMAC_ALGORITHM = "HmacSHA256";

    private static SecretKey getMacKey(String secret)
    {
        try
        {
            return new SecretKeySpec(secret.getBytes("ASCII"), HMAC_ALGORITHM);
        } catch (UnsupportedEncodingException e)
        {
            return null;
        }
    }

    /**
     * Get a HMAC signature using the specified secret 
     * @param secret
     * @param signingData
     * @return a Base64 encoded signature
     */
    public static String getBase64EncodedSignature(String secret, String signingData)
    {
        SecretKey key = getMacKey(secret);
        try
        {
            Mac mac = Mac.getInstance(key.getAlgorithm());
            mac.init(getMacKey(secret));
            byte[] digest = mac.doFinal(signingData.getBytes("UTF8"));
            return new String(Base64.encodeBase64(digest), "ASCII");
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (IllegalStateException e)
        {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        } catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Verify the signature
     * @param secret
     * @param sig
     * @param signedData
     * @return true if the signature is verified
     */
    public static boolean verifyBase64EncodedSignature(String secret, String sig, String signedData)
    {
        if (secret == null || sig == null || signedData == null)
            return false;

        SecretKey key = getMacKey(secret);
        try
        {
            Mac mac = Mac.getInstance(key.getAlgorithm());
            mac.init(getMacKey(secret));
            byte[] digest = mac.doFinal(signedData.getBytes("UTF8"));
            return sig.equals(new String(Base64.encodeBase64(digest), "ASCII"));
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (IllegalStateException e)
        {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        } catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}