package com.konakart.app;

import com.konakart.appif.*;

/**
 *  The KonaKart Custom Engine - EditWishList - Generated by CreateKKCustomEng
 */
@SuppressWarnings("all")
public class EditWishList
{
    KKEng kkEng = null;

    /**
     * Constructor
     */
     public EditWishList(KKEng _kkEng)
     {
         kkEng = _kkEng;
     }

     public void editWishList(String sessionId, WishListIf wishList) throws KKException
     {
         kkEng.editWishList(sessionId, wishList);
     }
}
