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

/**
 * Allows a MoneyBookers MD5 signature to be validated
 */
public class MoneyBookersSignature 
{
    private String secretWordMd5Uppercase;
    private String merchantId;
    private String transactionId;
    private String mbAmount;
    private String mbCurrency;
    private String status;

    /**
     * @param secretWord
     */
    public void setSecretWord(String secretWord) 
    {
        Md5Wrapper wrapper = new Md5Wrapper();
        secretWordMd5Uppercase = wrapper.encode(secretWord).toUpperCase();
    }

    /**
     * @param merchantId
     */
    public void setMerchantId(String merchantId) 
    {
        this.merchantId = merchantId;
    }

    /**
     * @param transactionId
     */
    public void setTransactionId(String transactionId) 
    {
        this.transactionId = transactionId;
    }

    /**
     * @param mbAmount
     */
    public void setMbAmount(String mbAmount) 
    {
        this.mbAmount = mbAmount;
    }

    /**
     * @param mbCurrency
     */
    public void setMbCurrency(String mbCurrency) 
    {
        this.mbCurrency = mbCurrency;
    }

    /**
     * @param status
     */
    public void setStatus(String status) 
    {
        this.status = status;
    }

    /**
     * @param signature
     * @return true if the signature matches, otherwise false
     */
    public boolean matches(String signature) 
    {
        StringBuilder sb = new StringBuilder();
        addPart(sb, merchantId);
        addPart(sb, transactionId);
        addPart(sb, secretWordMd5Uppercase);
        addPart(sb, mbAmount);
        addPart(sb, mbCurrency);
        addPart(sb, status);

        Md5Wrapper wrapper = new Md5Wrapper();
        String calculatedSignature = wrapper.encode(sb.toString());
        
        return calculatedSignature.toUpperCase().equals(signature);
    }

    private void addPart(StringBuilder sb, String string) 
    {
        if (string != null) 
        {
            sb.append(string);
        }
    }
}