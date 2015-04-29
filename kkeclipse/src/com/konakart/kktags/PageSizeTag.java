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
 * Tag used to write out a drop list to select number of items to view on page
 * 
 */
public class PageSizeTag extends BaseTag
{
    private static final long serialVersionUID = 1L;

    private String action;

    private String name;

    private String sizes;

    private int maxNum;

    private long timestamp = -1;
    
    private String type;

    public int doStartTag() throws JspException
    {
        try
        {
            setEng((com.konakart.al.KKAppEng) pageContext.getSession().getAttribute("konakartKey"));

            JspWriter writer = pageContext.getOut();

            StringBuffer sb = new StringBuffer();

            sb.append("<span class=\"show-per-page navigation-element navigation-dropdown\">");
            sb.append("<form action=\"");
            if (eng.isPortlet())
            {
                sb.append(createPortalURL(action, null));
            } else
            {
                sb.append(action);
            }
            sb.append("\" method=\"post\">");

            sb.append("<input type=\"hidden\" value=\"");
            sb.append(eng.getXsrfToken());
            sb.append("\" name=\"xsrf_token\"/>");

            if (type == null || !type.equalsIgnoreCase("small"))
            {
                sb.append(eng.getMsg("common.show"));
            }
            sb.append("<select name=\"");
            sb.append(name);
            sb.append("\" onchange=\"submit()\">");

            String[] sizeArray = sizes.split(",");
            for (int i = 0; i < sizeArray.length; i++)
            {
                String sizeStr = sizeArray[i];
                int size = Integer.parseInt(sizeStr);
                sb.append("<option value=\"");
                sb.append(sizeStr);
                sb.append("\" ");
                if (size == maxNum)
                {
                    sb.append("selected=\"selected\"");
                }
                sb.append(">");
                sb.append(sizeStr);
                sb.append("</option>");
            }

            sb.append("</select>");
            if (timestamp != -1)
            {
                sb.append("<input type=\"hidden\" name=\"t\" value=\"");
                sb.append(timestamp);
                sb.append("\"/>");
            }
            sb.append("&nbsp;");
            sb.append(eng.getMsg("common.per.page"));
            sb.append("</form>");
            sb.append("</span>");

            writer.write(sb.toString());
        } catch (IOException e)
        {
            String msg = "Cannot write prod tile tag content";
            throw new JspException(msg, e);
        }
        return EVAL_PAGE;
    }

    /**
     * @return the action
     */
    public String getAction()
    {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(String action)
    {
        this.action = action;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the sizes
     */
    public String getSizes()
    {
        return sizes;
    }

    /**
     * @param sizes
     *            the sizes to set
     */
    public void setSizes(String sizes)
    {
        this.sizes = sizes;
    }

    /**
     * @return the maxNum
     */
    public int getMaxNum()
    {
        return maxNum;
    }

    /**
     * @param maxNum
     *            the maxNum to set
     */
    public void setMaxNum(int maxNum)
    {
        this.maxNum = maxNum;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp()
    {
        return timestamp;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }
}
