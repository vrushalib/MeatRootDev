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
package com.konakart.bl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.appif.KKEngIf;

/**
 * A demo implementation of the reorder manager.
 * 
 */
public class ReorderMgr extends BaseMgr implements ReorderMgrInterface
{
    /** the log */
    protected static Log log = LogFactory.getLog(ReorderMgr.class);

    /**
     * Constructor
     * 
     * @param eng
     * @throws Exception
     */
    public ReorderMgr(KKEngIf eng) throws Exception
    {
        super.init(eng, log);

        if (log.isDebugEnabled())
        {
            if (eng != null && eng.getEngConf() != null && eng.getEngConf().getStoreId() != null)
            {
                log
                        .debug("ReorderMgr instantiated for store id = "
                                + eng.getEngConf().getStoreId());
            }
        }
    }

    /**
     * Called when the stock level of a product has fallen below the limit. The sku (if available)
     * is passed as a parameter along with the product id.
     * 
     * @param productId
     * @param sku
     * @param currentQuantity
     * @throws Exception
     */
    public void reorder(int productId, String sku, int currentQuantity) throws Exception
    {
        log.info("Reorder required for product id = " + productId + " and sku = " + sku
                + " . The remaining quantity is " + currentQuantity);
    }

}
