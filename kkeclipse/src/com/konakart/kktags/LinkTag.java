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

import com.konakart.al.KKAppEng;
import com.konakart.app.NameValue;

/**
 * 
 * Tag used to create a link. The source anchor for the link is modified when running as a portlet.
 * 
 */
public class LinkTag extends BaseTag
{
    private static final long serialVersionUID = 1L;

    private String href;

    private String kkClass;

    private String style;

    private String onmouseover;

    private String onclick;

    private String id;
    
    private String title;
    
    private String target;

    public int doStartTag() throws JspException
    {
        try
        {
            KKAppEng eng = (KKAppEng) pageContext.getSession().getAttribute("konakartKey");
            setEng(eng);
            JspWriter writer = pageContext.getOut();
            if (eng.isPortlet() && href != null)
            {
                String[] actionSplit = href.split("\\?");
                if (actionSplit.length == 1)
                {
                    href = createPortalURL(href, null);
                    writer.write(createLink());
                } else
                {
                    String[] parmsSplit = actionSplit[1].split("&");
                    NameValue[] nvArray = new NameValue[parmsSplit.length];
                    for (int i = 0; i < parmsSplit.length; i++)
                    {
                        String parm = parmsSplit[i];
                        int index = parm.indexOf("=");
                        if (index > -1)
                        {
                            nvArray[i] = new NameValue(parm.substring(0, index),
                                    parm.substring(index + 1));
                        }
                    }
                    href = createPortalURL(actionSplit[0], nvArray);
                    writer.write(createLink());
                }
            } else
            {
                writer.write(createLink());
            }
        } catch (IOException e)
        {
            String msg = "Problem in LinkTag";
            throw new JspException(msg, e);
        }
        return EVAL_BODY_INCLUDE;
    }

    private String createLink()
    {
        StringBuffer link = new StringBuffer("<a ");
        if (href != null)
        {
            if (eng.isPortlet() && (href.contains("DigitalDownloadPortlet") || href.contains("DownloadInvoicePortlet")))
            {
                // Stops Liferay complaining about invalid content type
                href = href.replace("p_p_state=normal", "p_p_state=exclusive");
                // Convert to a resource URL. Render URL always sets content type to text/html 
                href = href.replace("p_p_lifecycle=0", "p_p_lifecycle=2");
            }
            link.append(" href=\"" + href + "\"");
        }
        if (style != null)
        {
            link.append(" style=\"" + style + "\"");
        }
        if (kkClass != null)
        {
            link.append(" class=\"" + kkClass + "\"");
        }
        if (onmouseover != null)
        {
            link.append(" onmouseover=\"" + onmouseover + "\"");
        }
        if (onclick != null)
        {
            link.append(" onclick=\"" + onclick + "\"");
        }
        if (id != null)
        {
            link.append(" id=\"" + id + "\"");
        }
        if (title != null)
        {
            link.append(" title=\"" + title + "\"");
        }
        if (target != null)
        {
            link.append(" target=\"" + target + "\"");
        }
        link.append(">");
        return link.toString();
    }

    public int doEndTag() throws JspException
    {
        try
        {
            JspWriter writer = pageContext.getOut();
            writer.write("</a>");
        } catch (IOException e)
        {
            String msg = "Problem in LinkTag";
            throw new JspException(msg, e);
        }
        return EVAL_PAGE;
    }

    /**
     * @return the href
     */
    public String getHref()
    {
        return href;
    }

    /**
     * @param href
     *            the href to set
     */
    public void setHref(String href)
    {
        this.href = href;
    }

    /**
     * @return the style
     */
    public String getStyle()
    {
        return style;
    }

    /**
     * @param style
     *            the style to set
     */
    public void setStyle(String style)
    {
        this.style = style;
    }

    /**
     * @return the kkClass
     */
    public String getKkClass()
    {
        return kkClass;
    }

    /**
     * @param kkClass
     *            the kkClass to set
     */
    public void setKkClass(String kkClass)
    {
        this.kkClass = kkClass;
    }

    /**
     * @return the onmouseover
     */
    public String getOnmouseover()
    {
        return onmouseover;
    }

    /**
     * @param onmouseover the onmouseover to set
     */
    public void setOnmouseover(String onmouseover)
    {
        this.onmouseover = onmouseover;
    }

    /**
     * @return the onclick
     */
    public String getOnclick()
    {
        return onclick;
    }

    /**
     * @param onclick the onclick to set
     */
    public void setOnclick(String onclick)
    {
        this.onclick = onclick;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the target
     */
    public String getTarget()
    {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(String target)
    {
        this.target = target;
    }

}
