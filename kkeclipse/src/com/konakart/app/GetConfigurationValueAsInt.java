package com.konakart.app;

/**
 *  The KonaKart Custom Engine - GetConfigurationValueAsInt - Generated by CreateKKCustomEng
 */
@SuppressWarnings("all")
public class GetConfigurationValueAsInt
{
    KKEng kkEng = null;

    /**
     * Constructor
     */
     public GetConfigurationValueAsInt(KKEng _kkEng)
     {
         kkEng = _kkEng;
     }

     public int getConfigurationValueAsInt(String key) throws KKException
     {
         return kkEng.getConfigurationValueAsInt(key);
     }
}
