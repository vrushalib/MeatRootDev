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

package com.konakart.bl.modules.payment.globalcollect;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;

/**
 * Global Collect Utilities - Customers can provide custom classes that extend this interface to
 * provide custom functionality for the module for the interfaces that they wish to specialise.
 */
public class GlobalCollectCustomBase implements GlobalCollectCustomIf
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(GlobalCollectCustomBase.class);

    /**
     * An engine that can be used by the custom methods
     */
    private KKEngIf eng;
    
    /**
     * Constructor
     * @param _eng a KKENgIf engine
     */
    public GlobalCollectCustomBase(KKEngIf _eng)
    {
        this.eng = _eng;
    }

    public int getNumberOfInstallments(OrderIf order)
    {
        // By default we won't add any installments so we'll just return -1
        return -1;
    }

    /**
     * @return the engine
     */
    public KKEngIf getEng()
    {
        return eng;
    }
}
