package com.konakart.app;

import com.konakart.appif.*;

/**
 *  The KonaKart Custom Engine - GetShippingQuotes - Generated by CreateKKCustomEng
 */
@SuppressWarnings("all")
public class GetShippingQuotes
{
    KKEng kkEng = null;

    /**
     * Constructor
     */
     public GetShippingQuotes(KKEng _kkEng)
     {
         kkEng = _kkEng;
     }

     public ShippingQuoteIf[] getShippingQuotes(OrderIf order, int languageId) throws KKException
     {
         return kkEng.getShippingQuotes(order, languageId);
     }
}
