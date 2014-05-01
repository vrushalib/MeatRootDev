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
package com.konakart.bl.modules.shipping;

import com.konakart.app.KKException;
import com.konakart.app.Order;
import com.konakart.app.ShippingQuote;


/**
 * 
 * 
 */
public interface ShippingInterface
{
    /**
     * @param order 
     * @param info 
     * @return Returns a descriptor for this module
     * @throws Exception 
     */
    public ShippingQuote getQuote(Order order, ShippingInfo info) throws Exception;
    
    /**
     * @return True or False
     * @throws KKException 
     */
    public boolean isAvailable()throws KKException;
    
    /**
     * Refreshes the static variables from the copy in the database
     * 
     * @throws KKException
     */
    public void setStaticVariables() throws KKException;

    
}
