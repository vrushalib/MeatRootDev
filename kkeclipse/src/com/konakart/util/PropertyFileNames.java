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
package com.konakart.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.KKException;

/**
 * For overriding properties file names
 */
public class PropertyFileNames
{
    /** the log */
    protected static Log log = LogFactory.getLog(PropertyFileNames.class);

    /** Property File code */
    public static final int KONAKART_SERVER_PROPERTIES_FILE = 1;

    /** Property File code */
    public static final int KONAKARTADMIN_SERVER_PROPERTIES_FILE = 2;

    /** Property File code */
    public static final int KONAKARTADMIN_GWT_PROPERTIES_FILE = 3;

    /** Property File code */
    public static final int KONAKART_WS_CLIENT_PROPERTIES_FILE = 4;

    /** Property File code */
    public static final int KONAKART_WS_SERVER_PROPERTIES_FILE = 5;

    /** Property File code */
    public static final int KONAKARTADMIN_WS_CLIENT_PROPERTIES_FILE = 6;

    /** Property File code */
    public static final int KONAKARTADMIN_WS_SERVER_PROPERTIES_FILE = 7;

    /** Property File code */
    public static final int KONAKART_JSON_CLIENT_PROPERTIES_FILE = 8;

    /** Property File code */
    public static final int KONAKART_JSON_SERVER_PROPERTIES_FILE = 9;

    /** Property File code */
    // Just a placeholder for the future - no JSON on the Admin side at the moment
    public static final int KONAKARTADMIN_JSON_CLIENT_PROPERTIES_FILE = 10;

    /** Property File code */
    // Just a placeholder for the future - no JSON on the Admin side at the moment
    public static final int KONAKARTADMIN_JSON_SERVER_PROPERTIES_FILE = 11;

    /** Property File code */
    public static final int KONAKART_GWT_PROPERTIES_FILE = 12;

    /** Property File code */
    public static final int KONAKART_APPENG_CLIENT_PROPERTIES_FILE = 13;

    /** Property File code */
    public static final int KONAKART_APPENG_SERVER_PROPERTIES_FILE = 14;

    /** Property File code */
    public static final int KONAKART_RMI_CLIENT_PROPERTIES_FILE = 15;

    /** Property File code */
    public static final int KONAKART_RMI_SERVER_PROPERTIES_FILE = 16;

    /** Property File code */
    public static final int KONAKARTADMIN_RMI_CLIENT_PROPERTIES_FILE = 17;

    /** Property File code */
    public static final int KONAKARTADMIN_RMI_SERVER_PROPERTIES_FILE = 18;

    /** Property File code */
    public static final int KONAKART_VELOCITY_PROPERTIES_FILE = 19;

    /** Property File code */
    public static final int KONAKART_JOBS_PROPERTIES_FILE = 20;

    /**
     * Returns the properties file name for the property file indicated by the propertyFileCode
     * parameter.
     * 
     * @param propertyFileCode
     *            Property File code as an integer. Could be one of:
     *            <p>
     *            <ul>
     *            <li>
     *            PropertyFileNames.KONAKART_SERVER_PROPERTIES_FILE = Properties file used by the
     *            KonaKart Engine on the server side.
     *            <li>
     *            PropertyFileNames.KONAKARTADMIN_SERVER_PROPERTIES_FILE = Properties file used by
     *            the KonaKart Admin Engine on the server side.
     *            <li>
     *            PropertyFileNames.KONAKARTADMIN_GWT_PROPERTIES_FILE = Properties file used by the
     *            KonaKart Admin App client side.
     *            <li>
     *            PropertyFileNames.KONAKART_WS_CLIENT_PROPERTIES_FILE = Properties file used by the
     *            KonaKart Web Service on the client side.
     *            <li>
     *            PropertyFileNames.KONAKART_WS_SERVER_PROPERTIES_FILE = Properties file used by the
     *            KonaKart Web Service on the server side.
     *            <li>
     *            PropertyFileNames.KONAKARTADMIN_WS_CLIENT_PROPERTIES_FILE = Properties file used
     *            by the KonaKart Admin Web Service on the client side.
     *            <li>
     *            PropertyFileNames.KONAKARTADMIN_WS_SERVER_PROPERTIES_FILE = Properties file used
     *            by the KonaKart Admin Web Service on the server side.
     *            <li>
     *            PropertyFileNames.KONAKART_JSON_CLIENT_PROPERTIES_FILE = Properties file used by
     *            the KonaKart JSON Service on the client side.
     *            <li>
     *            PropertyFileNames.KONAKART_JSON_SERVER_PROPERTIES_FILE = Properties file used by
     *            the KonaKart JSON Service on the server side.
     *            <li>
     *            PropertyFileNames.KONAKARTADMIN_JSON_CLIENT_PROPERTIES_FILE = Properties file used
     *            by the KonaKart Admin JSON Service on the client side. Note that this is just a
     *            placeholder because there is currently no JSON server on the Admin side.
     *            <li>
     *            PropertyFileNames.KONAKARTADMIN_JSON_SERVER_PROPERTIES_FILE = Properties file used
     *            by the KonaKart Admin JSON Service on the server side. Note that this is just a
     *            placeholder because there is currently no JSON server on the Admin side.
     *            <li>
     *            PropertyFileNames.KONAKART_GWT_PROPERTIES_FILE = Properties file used by the
     *            KonaKart GWT Application (One Page Checkout) on the client side.
     *            <li>
     *            PropertyFileNames.KONAKART_APPENG_CLIENT_PROPERTIES_FILE = Properties file used by
     *            the KonaKart Client Engine on the client side.
     *            <li>
     *            PropertyFileNames.KONAKART_APPENG_SERVER_PROPERTIES_FILE = Properties file used by
     *            the KonaKart Client Engine on the server side.
     *            <li>
     *            PropertyFileNames.KONAKART_RMI_CLIENT_PROPERTIES_FILE = Properties file used by
     *            the KonaKart Client Side of the RMI Engine.
     *            <li>
     *            PropertyFileNames.KONAKART_RMI_SERVER_PROPERTIES_FILE = Properties file used by
     *            the KonaKart Server Side of the RMI Engine.
     *            <li>
     *            PropertyFileNames.KONAKARTADMIN_RMI_CLIENT_PROPERTIES_FILE = Properties file used
     *            by the KonaKart Admin Client Side of the RMI Engine.
     *            <li>
     *            PropertyFileNames.KONAKARTADMIN_RMI_SERVER_PROPERTIES_FILE = Properties file used
     *            by the KonaKart Admin Server Side of the RMI Engine.
     *            <li>
     *            PropertyFileNames.KONAKART_VELOCITY_PROPERTIES_FILE = Velocity Properties file used
     *            by the KonaKart Server.
     *            <li>
     *            PropertyFileNames.KONAKART_JOBS_PROPERTIES_FILE = Jobs Properties file used
     *            by Quartz jobs in the KonaKartAdmin Server.
     *            </ul>
     * @param def
     *            Default value
     * @return Returns the file name for the specified code
     * @throws KKException
     *             raised if the propertyFileCode is unrecognised
     * 
     */
    public String getFileName(int propertyFileCode, String def) throws KKException
    {
        if (propertyFileCode == PropertyFileNames.KONAKART_WS_CLIENT_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKART_SERVER_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKARTADMIN_SERVER_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKARTADMIN_GWT_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKART_WS_SERVER_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKARTADMIN_WS_CLIENT_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKARTADMIN_WS_SERVER_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKART_JSON_CLIENT_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKART_JSON_SERVER_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKARTADMIN_JSON_CLIENT_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKARTADMIN_JSON_SERVER_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKART_APPENG_CLIENT_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKART_GWT_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKART_APPENG_SERVER_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKART_RMI_CLIENT_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKART_RMI_SERVER_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKARTADMIN_RMI_CLIENT_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKARTADMIN_RMI_SERVER_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKART_VELOCITY_PROPERTIES_FILE)
        {
            return def;
        }

        if (propertyFileCode == PropertyFileNames.KONAKART_JOBS_PROPERTIES_FILE)
        {
            return def;
        }

        String msg = "Unknown property file code: " + propertyFileCode;
        log.warn(msg);
        throw new KKException(msg);
    }
}