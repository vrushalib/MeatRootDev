//
// (c) 2015 DS Data Systems UK Ltd, All rights reserved.
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
import com.konakart.appif.ExternalLoginInputIf;
import com.konakart.appif.ExternalLoginResultIf;

/**
 * Interface for external login modules
 * 
 */
public interface ExternalLoginInterface
{
    /**
     * Called to perform a login
     * 
     * @param loginVal
     *            Object containing login information
     * @return Returns a LoginValidationResult object with information regarding the success of the
     *         login attempt
     * @throws Exception
     */
    public ExternalLoginResultIf externalLogin(ExternalLoginInputIf loginVal) throws Exception;

    /**
     * Determines whether the module is available
     * 
     * @return True or False
     * @throws KKException
     */
    public boolean isAvailable() throws KKException;

}
