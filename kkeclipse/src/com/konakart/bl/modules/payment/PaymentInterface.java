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
package com.konakart.bl.modules.payment;

import com.konakart.app.KKException;
import com.konakart.app.Order;
import com.konakart.app.PaymentDetails;
import com.konakart.appif.NameValueIf;

/**
 * Payment modules must implement this interface
 */
public interface PaymentInterface
{
    /**
     * @param order
     * @param info
     * @return Returns information in a PaymentDetails object
     * @throws Exception
     */
    public PaymentDetails getPaymentDetails(Order order, PaymentInfo info) throws Exception;

    /**
     * Method used to return any custom information required from the payment module.
     * 
     * @param sessionId
     * @param parameters
     * @return Returns information in a PaymentDetails object
     * @throws Exception
     */
    public PaymentDetails getPaymentDetailsCustom(String sessionId, NameValueIf[] parameters)
            throws Exception;

    /**
     * Is the payment module available?
     * 
     * @return True or False
     * @throws KKException
     */
    public boolean isAvailable() throws KKException;

    /**
     * Refreshes the static variables from the copy in the database
     * 
     * @throws KKException
     */
    public void setStaticVariables() throws KKException;

}
