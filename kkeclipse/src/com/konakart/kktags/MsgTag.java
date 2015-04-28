//
// (c) 2012 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakart.kktags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * 
 * Tag used to retrieve a message from the message catalog
 * 
 */
public class MsgTag extends BaseTag
{
    private static final long serialVersionUID = 1L;

    private String key;

    private String arg0;

    private String arg1;

    private String arg2;

    public int doStartTag() throws JspException
    {
        try
        {
            setEng((com.konakart.al.KKAppEng) pageContext.getSession().getAttribute("konakartKey"));
            JspWriter writer = pageContext.getOut();
            String msg = getMsg(key, arg0, arg1, arg2);
            writer.write(msg);
        } catch (IOException e)
        {
            String msg = "Problem in MsgTag";
            throw new JspException(msg, e);
        }
        return EVAL_PAGE;
    }

    /**
     * @return the key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * @return the arg0
     */
    public String getArg0()
    {
        return arg0;
    }

    /**
     * @param arg0
     *            the arg0 to set
     */
    public void setArg0(String arg0)
    {
        this.arg0 = arg0;
    }

    /**
     * @return the arg1
     */
    public String getArg1()
    {
        return arg1;
    }

    /**
     * @param arg1
     *            the arg1 to set
     */
    public void setArg1(String arg1)
    {
        this.arg1 = arg1;
    }

    /**
     * @return the arg2
     */
    public String getArg2()
    {
        return arg2;
    }

    /**
     * @param arg2
     *            the arg2 to set
     */
    public void setArg2(String arg2)
    {
        this.arg2 = arg2;
    }

}
