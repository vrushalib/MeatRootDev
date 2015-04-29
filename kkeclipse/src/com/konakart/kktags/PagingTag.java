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
import java.util.ArrayList;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import com.konakart.al.ProductMgr;
import com.konakart.app.NameValue;

/**
 * 
 * Tag used to write out a page navigation widget
 * 
 */
public class PagingTag extends BaseTag
{
    private static final long serialVersionUID = 1L;

    private int showBack;

    private int showNext;

    private int currentPage;

    private ArrayList<Integer> pageList;

    private String action;

    private long timestamp;

    public int doStartTag() throws JspException
    {
        try
        {
            setEng((com.konakart.al.KKAppEng) pageContext.getSession().getAttribute("konakartKey"));

            String ts = "&t=" + timestamp;

            JspWriter writer = pageContext.getOut();

            StringBuffer sb = new StringBuffer();

            sb.append("<span class=\"item-overview-pagination navigation-element\">");
            if (showBack == 1)
            {
                String url;
                if (eng.isPortlet())
                {
                    url = createPortalURL(
                            action + ".action",
                            new NameValue[]
                            { new NameValue("navDir", ProductMgr.navBack),
                                    new NameValue("t", Long.toString(timestamp)) });
                } else
                {
                    url = action + ".action?navDir=" + ProductMgr.navBack + ts;
                }
                sb.append("<a class=\"pagination-element previous-items\" href=\"" + url
                        + "\" class=\"pageResults\"></a>");
            } else
            {
                sb.append("<a class=\"pagination-element previous-items inactive\"></a>");
            }
            for (java.util.Iterator<Integer> iterator = pageList.iterator(); iterator.hasNext();)
            {
                Integer pageNum = iterator.next();
                if (currentPage == pageNum.intValue())
                {
                    sb.append("<a class=\"pagination-element current\">" + pageNum.intValue()
                            + "</a>");
                } else
                {
                    String url;
                    if (eng.isPortlet())
                    {
                        url = createPortalURL(
                                action + ".action",
                                new NameValue[]
                                { new NameValue("navDir", pageNum),
                                        new NameValue("t", Long.toString(timestamp)) });
                    } else
                    {
                        url = action + ".action?navDir=" + pageNum + ts;
                    }

                    sb.append("<a href=\"" + url + "\"  class=\"pagination-element\">" + pageNum
                            + "</a>");
                }
            }
            if (showNext == 1)
            {
                String url;
                if (eng.isPortlet())
                {
                    url = createPortalURL(
                            action + ".action",
                            new NameValue[]
                            { new NameValue("navDir", ProductMgr.navNext),
                                    new NameValue("t", Long.toString(timestamp)) });
                } else
                {
                    url = action + ".action?navDir=" + ProductMgr.navNext + ts;
                }
                sb.append("<a class=\"pagination-element next-items\" href=\"" + url
                        + "\" class=\"pageResults\"></a>");
            } else
            {
                sb.append("<a class=\"pagination-element next-items inactive\"></a>");
            }
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
     * @return the showBack
     */
    public int getShowBack()
    {
        return showBack;
    }

    /**
     * @param showBack
     *            the showBack to set
     */
    public void setShowBack(int showBack)
    {
        this.showBack = showBack;
    }

    /**
     * @return the showNext
     */
    public int getShowNext()
    {
        return showNext;
    }

    /**
     * @param showNext
     *            the showNext to set
     */
    public void setShowNext(int showNext)
    {
        this.showNext = showNext;
    }

    /**
     * @return the currentPage
     */
    public int getCurrentPage()
    {
        return currentPage;
    }

    /**
     * @param currentPage
     *            the currentPage to set
     */
    public void setCurrentPage(int currentPage)
    {
        this.currentPage = currentPage;
    }

    /**
     * @return the pageList
     */
    public ArrayList<Integer> getPageList()
    {
        return pageList;
    }

    /**
     * @param pageList
     *            the pageList to set
     */
    public void setPageList(ArrayList<Integer> pageList)
    {
        this.pageList = pageList;
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
}
