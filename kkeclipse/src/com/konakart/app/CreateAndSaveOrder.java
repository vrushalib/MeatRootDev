package com.konakart.app;

import com.konakart.appif.*;

/**
 *  The KonaKart Custom Engine - CreateAndSaveOrder - Generated by CreateKKCustomEng
 */
@SuppressWarnings("all")
public class CreateAndSaveOrder
{
    KKEng kkEng = null;

    /**
     * Constructor
     */
     public CreateAndSaveOrder(KKEng _kkEng)
     {
         kkEng = _kkEng;
     }

     public OrderIf createAndSaveOrder(String emailAddr, String password, CustomerRegistrationIf custReg, BasketIf[] basketItemArray, String shippingModule, String paymentModule, int languageId) throws KKException
     {
         return kkEng.createAndSaveOrder(emailAddr, password, custReg, basketItemArray, shippingModule, paymentModule, languageId);
     }
}
