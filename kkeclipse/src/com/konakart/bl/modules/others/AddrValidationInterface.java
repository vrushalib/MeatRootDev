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
package com.konakart.bl.modules.others;

import com.konakart.app.KKException;
import com.konakart.appif.AddrValidationResultIf;
import com.konakart.appif.AddressIf;

/**
 * Interface for address validation modules
 * 
 */
public interface AddrValidationInterface
{
    /**
     * Called to validate an address
     * 
     * @param addr
     * @return Returns an AddrValidationResult object
     * @throws Exception
     */
    public AddrValidationResultIf validateAddress(AddressIf addr) throws Exception;

    /**
     * Determines whether the module is available
     * 
     * @return True or False
     * @throws KKException
     */
    public boolean isAvailable() throws KKException;

}
