package com.konakart.app;

import com.konakart.appif.*;

/**
 *  The KonaKart Custom Engine - SearchForProducts - Generated by CreateKKCustomEng
 */
@SuppressWarnings("all")
public class SearchForProducts
{
    KKEng kkEng = null;

    /**
     * Constructor
     */
     public SearchForProducts(KKEng _kkEng)
     {
         kkEng = _kkEng;
     }

     public ProductsIf searchForProducts(String sessionId, DataDescriptorIf dataDesc, ProductSearchIf prodSearch, int languageId) throws KKException
     {
         return kkEng.searchForProducts(sessionId, dataDesc, prodSearch, languageId);
     }
}
