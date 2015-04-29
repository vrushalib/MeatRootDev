//
// (c) 2014 DS Data Systems UK Ltd, All rights reserved.
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

import com.konakart.al.ReviewMgr;
import com.konakart.app.DataDescConstants;

/**
 * 
 * Tag used to write out a drop list for sorting reviews
 * 
 */
public class ReviewOrderByTag extends BaseTag
{
    private static final long serialVersionUID = 1L;

    public int doStartTag() throws JspException
    {
        try
        {
            boolean debug = false;
            setEng((com.konakart.al.KKAppEng) pageContext.getSession().getAttribute("konakartKey"));

            if (eng == null)
            {
                return EVAL_PAGE;
            }

            ReviewMgr revMgr = eng.getReviewMgr();
            String sortBy = revMgr.getDataDesc().getOrderBy();

            JspWriter writer = pageContext.getOut();

            StringBuffer sb = new StringBuffer();

            append(sb, "<span class=\"sort-by navigation-element navigation-dropdown\">", debug);
            append(sb, "<form action=\"SortRev.action\" method=\"post\">", debug);
            append(sb, eng.getMsg("common.sort"), debug);
            append(sb, ":", debug);
            append(sb, "<input type=\"hidden\" value=\"", debug);
            append(sb, eng.getXsrfToken(), debug);
            append(sb, "\"  name=\"xsrf_token\"/>", debug);
            append(sb, "<select name=\"orderBy\" onchange=\"submit()\">", debug);
            append(sb, "<option  value=\"", debug);
            append(sb, DataDescConstants.ORDER_BY_DATE_ADDED_DESCENDING, debug);
            append(sb, "\"", debug);
            if (sortBy.equals(DataDescConstants.ORDER_BY_DATE_ADDED_DESCENDING))
            {
                append(sb, " selected=\"selected\"", debug);
            }
            append(sb, ">", debug);
            append(sb, eng.getMsg("show.reviews.body.sort.by.most.recent"), debug);
            append(sb, "</option>", debug);
            append(sb, "<option  value=\"", debug);
            append(sb, DataDescConstants.ORDER_BY_DATE_ADDED_ASCENDING, debug);
            append(sb, "\"", debug);
            if (sortBy.equals(DataDescConstants.ORDER_BY_DATE_ADDED_ASCENDING))
            {
                append(sb, " selected=\"selected\"", debug);
            }
            append(sb, ">", debug);
            append(sb, eng.getMsg("show.reviews.body.sort.by.oldest"), debug);
            append(sb, "</option>", debug);
            append(sb, "<option  value=\"", debug);
            append(sb, DataDescConstants.ORDER_BY_RATING_DESCENDING, debug);
            append(sb, "\"", debug);
            if (sortBy.equals(DataDescConstants.ORDER_BY_RATING_DESCENDING))
            {
                append(sb, " selected=\"selected\"", debug);
            }
            append(sb, ">", debug);
            append(sb, eng.getMsg("show.reviews.body.sort.by.rating.desc"), debug);
            append(sb, "</option>", debug);
            append(sb, "<option  value=\"", debug);
            append(sb, DataDescConstants.ORDER_BY_RATING_ASCENDING, debug);
            append(sb, "\"", debug);
            if (sortBy.equals(DataDescConstants.ORDER_BY_RATING_ASCENDING))
            {
                append(sb, " selected=\"selected\"", debug);
            }
            append(sb, ">", debug);
            append(sb, eng.getMsg("show.reviews.body.sort.by.rating.asc"), debug);
            append(sb, "</option>", debug);
            append(sb, "</select>", debug);
            append(sb, "<input type=\"hidden\" name=\"t\" value=\"", debug);

            append(sb, Long.toString(revMgr.getRevTimestamp()), debug);
            append(sb, "\"/>", debug);
            append(sb, "</form>", debug);
            append(sb, "</span>", debug);

            writer.write(sb.toString());
        } catch (IOException e)
        {
            String msg = "Cannot write review order by tag content";
            throw new JspException(msg, e);
        }
        return EVAL_PAGE;
    }

}
