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

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

import org.apache.struts2.components.Component;
import org.apache.struts2.components.Property;
import org.apache.struts2.views.jsp.ComponentTagSupport;

import com.konakart.al.KKAppEng;
import com.konakart.app.NameValue;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * 
 * Base tag containing tag utilities
 * 
 */
public class BaseTag extends ComponentTagSupport
{
    private static final long serialVersionUID = 1L;

    protected static final String END_DIV = "</div>";

    protected static final String END_A = "</a>";

    protected static final String END_SPAN = "</span>";

    protected static final String D_QUOTE = "\"";

    protected static final String CLASS = " class=";

    protected static final String ID = " id=";

    protected static final String REL = " rel=";

    protected static final String HREF = " href=";

    protected KKAppEng eng;

    /**
     * Returns HTML for a start div
     * 
     * @param c
     * @return Returns HTML for a start div
     */
    protected String getStartDiv(String c)
    {
        if (c == null)
        {
            return "<div>";
        }
        return "<div class=" + D_QUOTE + c + D_QUOTE + ">";
    }

    /**
     * Returns HTML for a start div
     * 
     * @param c
     * @param idIn
     * @return Returns HTML for a start div
     */
    protected String getStartDiv(String c, String idIn)
    {
        c = (c == null) ? "" : CLASS + D_QUOTE + c + D_QUOTE;
        String newId = (idIn == null) ? "" : ID + D_QUOTE + idIn + D_QUOTE;
        return "<div" + c + newId + " >";
    }

    /**
     * Returns HTML for a start span
     * 
     * @param c
     * @return Returns HTML for a start span
     */
    protected String getStartSpan(String c)
    {
        c = (c == null) ? "" : CLASS + D_QUOTE + c + D_QUOTE;
        return "<span" + c + " >";
    }

    /**
     * Returns HTML for a start span
     * 
     * @param c
     * @param idIn
     * @return Returns HTML for a start span
     */
    protected String getStartSpan(String c, String idIn)
    {
        c = (c == null) ? "" : CLASS + D_QUOTE + c + D_QUOTE;
        String newId = (idIn == null) ? "" : ID + D_QUOTE + idIn + D_QUOTE;
        return "<span" + c + newId + " >";
    }

    /**
     * Returns HTML for a start span
     * 
     * @param c
     * @param idIn
     * @param rel
     * @return Returns HTML for a start span
     */
    protected String getStartSpan(String c, String idIn, String rel)
    {
        c = (c == null) ? "" : CLASS + D_QUOTE + c + D_QUOTE;
        String newId = (idIn == null) ? "" : ID + D_QUOTE + idIn + D_QUOTE;
        rel = (rel == null) ? "" : REL + D_QUOTE + rel + D_QUOTE;
        return "<span" + c + newId + rel + " >";
    }

    /**
     * Returns HTML for a start link
     * 
     * @param c
     * @param href
     * @return Returns HTML for a start link
     */
    protected String getStartA(String c, String href)
    {
        c = (c == null) ? "" : CLASS + D_QUOTE + c + D_QUOTE;
        href = (href == null) ? "" : HREF + D_QUOTE + href + D_QUOTE;
        return "<a" + c + href + " >";
    }

    /**
     * Returns HTML for a start link
     * 
     * @param c
     * @param href
     * @param idIn
     * @return Returns HTML for a start link
     */
    protected String getStartA(String c, String href, String idIn)
    {
        c = (c == null) ? "" : CLASS + D_QUOTE + c + D_QUOTE;
        String newId = (idIn == null) ? "" : ID + D_QUOTE + idIn + D_QUOTE;
        href = (href == null) ? "" : HREF + D_QUOTE + href + D_QUOTE;
        return "<a" + c + href + newId + " >";
    }

    /**
     * Gets the text from the message catalog
     * 
     * @param key
     * @return Gets the text from the message catalog
     */
    protected String getMsg(String key)
    {
        return eng.getMsg(key);
    }

    /**
     * Gets the text from the message catalog
     * 
     * @param key
     * @param arg0
     * @return Gets the text from the message catalog
     */
    protected String getMsg(String key, String arg0)
    {
        return eng.getMsg(key, arg0);
    }

    /**
     * Gets the text from the message catalog
     * 
     * @param key
     * @param arg0
     * @param arg1
     * @return Gets the text from the message catalog
     */
    protected String getMsg(String key, String arg0, String arg1)
    {
        return eng.getMsg(key, arg0, arg1);
    }

    /**
     * Gets the text from the message catalog
     * 
     * @param key
     * @param arg0
     * @param arg1
     * @param arg2
     * @return Gets the text from the message catalog
     */
    protected String getMsg(String key, String arg0, String arg1, String arg2)
    {
        return eng.getMsg(key, arg0, arg1, arg2);
    }

    /**
     * Gets the text from the message catalog using place holders
     * 
     * @param key
     * @param args
     * @return Gets the text from the message catalog
     */
    protected String getMsg(String key, String[] args)
    {
        return eng.getMsg(key, args);
    }

    /**
     * Creates HTML to display an image
     * 
     * @param c
     * @param src
     * @param title
     * @return Returns the HTML to display an image
     */
    protected StringBuffer getImg(String c, String src, String title)
    {
        return getImg(c, src, title, /* addBase */true);
    }

    /**
     * Creates HTML to display an image
     * 
     * @param c
     * @param src
     * @param title
     * @param addBase
     * @return Returns the HTML to display an image
     */
    protected StringBuffer getImg(String c, String src, String title, boolean addBase)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<img ");
        if (c != null)
        {
            sb.append(CLASS + D_QUOTE + c + D_QUOTE + " ");
        }
        sb.append("src=" + D_QUOTE + ((addBase) ? eng.getImageBase() + "/" : "") + src + D_QUOTE
                + " ");
        sb.append("border=" + D_QUOTE + "0" + D_QUOTE + " ");
        sb.append("alt=" + D_QUOTE + title + D_QUOTE + " ");
        sb.append("title=" + D_QUOTE + title + D_QUOTE + " ");
        sb.append(">");
        return sb;
    }

    /**
     * Returns HTML to create a button
     * 
     * @param title
     * @return Returns HTML to create a button
     */
    protected String getButton(String title)
    {

        return "<span class=\"button\"><span>" + title + "</span></span>";
    }

    /**
     * End tag code
     */
    public int doEndTag() throws JspException
    {
        return EVAL_PAGE;
    }

    /**
     * Struts method
     */
    public Component getBean(ValueStack stack, HttpServletRequest arg1, HttpServletResponse arg2)
    {
        return new Property(stack);
    }

    /**
     * @return the eng
     */
    public KKAppEng getEng()
    {
        return eng;
    }

    /**
     * @param eng
     *            the eng to set
     */
    public void setEng(KKAppEng eng)
    {
        this.eng = eng;
    }

    /**
     * Creates a portal URL used when KK runs as a portlet
     * 
     * @param strutsAction
     * @param paramArray
     * @return Return the URL
     */
    protected String createPortalURL(String strutsAction, NameValue[] paramArray)
    {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        RenderResponse renderResponse = (RenderResponse) request
                .getAttribute("javax.portlet.response");

        PortletURL portletURL = renderResponse.createRenderURL();
        if (paramArray != null && paramArray.length > 0)
        {
            for (int i = 0; i < paramArray.length; i++)
            {
                NameValue nv = paramArray[i];
                portletURL.setParameter(nv.getName(), nv.getValue());
            }
        }
        portletURL.setParameter("struts.portlet.action", "/" + strutsAction);
        portletURL.setParameter("struts.portlet.mode", "view");
        String url = portletURL.toString();
        return url;
    }
    
    /**
     * Utility method
     * @param sb
     * @param data
     * @param debug
     */
    protected void append(StringBuffer sb, String data, boolean debug){
        if (debug)
        {
            sb.append("\n"+data);
        } else
        {
            sb.append(data);
        }
    }

}
