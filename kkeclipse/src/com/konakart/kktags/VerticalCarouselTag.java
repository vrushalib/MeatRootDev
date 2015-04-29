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
import java.util.Random;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import com.konakart.al.KKAppException;
import com.konakart.appif.ProductIf;

/**
 * 
 * Tag used to write out a carousel with products in it
 * 
 */
public class VerticalCarouselTag extends BaseTag
{
    private static final long serialVersionUID = 1L;

    private ProductIf[] prods;

    private String title;


    public int doStartTag() throws JspException
    {
        try
        {
            boolean debug = false;
            setEng((com.konakart.al.KKAppEng) pageContext.getSession().getAttribute("konakartKey"));
            // Required since we may show more than one carousel on the same page
            String rand = String.valueOf(new Random().nextInt(1000));

            if (prods != null && prods.length > 0)
            {

                JspWriter writer = pageContext.getOut();

                StringBuffer sb = new StringBuffer();

                append(sb, "<script type=\"text/javascript\">", debug);
                append(sb, "$(function() {", debug);

                append(sb, "var vjc" + rand + " = $('#vjc" + rand + "');", debug);
                append(sb, "vjc" + rand, debug);
                append(sb, ".on('jcarousel:reload jcarousel:create', function () {", debug);
                append(sb, "vjc" + rand + ".jcarousel('scroll', 0, true, function(scrolled) {",
                        debug);
                append(sb, "setVerticalControls(vjc" + rand + ",$('#vjc" + rand + "-prev'),$('#vjc"
                        + rand + "-next'));", debug);
                append(sb, "})", debug);
                append(sb, "})", debug);
                append(sb, ".jcarousel({vertical: true, wrap: null});", debug);

                append(sb, "$('#vjc" + rand + "-prev').jcarouselControl({", debug);
                append(sb, "method: function() {", debug);
                append(sb, "vjc" + rand + ".jcarousel('scroll', '-='+vjc" + rand
                        + ".jcarousel('visible').length);", debug);
                append(sb, "setVerticalControls(vjc" + rand + ",$('#vjc" + rand + "-prev'),$('#vjc"
                        + rand + "-next'));", debug);
                append(sb, "}", debug);
                append(sb, "}); ", debug);

                append(sb, "$('#vjc" + rand + "-next').jcarouselControl({", debug);
                append(sb, "method: function() {", debug);
                append(sb, "vjc" + rand + ".jcarousel('scroll', '+='+vjc" + rand
                        + ".jcarousel('visible').length);", debug);
                append(sb, "setVerticalControls(vjc" + rand + ",$('#vjc" + rand + "-prev'),$('#vjc"
                        + rand + "-next'));", debug);
                append(sb, "}", debug);
                append(sb, "}); ", debug);

                append(sb, "});", debug);

                append(sb, "</script>", debug);

                /*
                 * HTML
                 */
                append(sb, "<div class=\"item-area-sidebar rounded-corners\">", debug);
                append(sb, "<div class=\"item-area-header jcarousel-wrapper\">", debug);
                append(sb, "<h2 class=\"item-area-title\">" + title + "</h2>", debug);
                append(sb,
                        "<a href=\"#\" id=\"vjc"
                                + rand
                                + "-prev\" class=\"jcarousel-vertical-control-prev jcarousel-border-prev\"></a>",
                        debug);
                append(sb,
                        "<a href=\"#\" id=\"vjc"
                                + rand
                                + "-next\" class=\"jcarousel-vertical-control-next jcarousel-border-next\"></a>",
                        debug);
                append(sb, "</div>", debug);
                append(sb, "<div id=\"vjc" + rand + "\" class=\"jcarousel-vertical\">", debug);
                append(sb, "<ul>", debug);
                for (int j = 0; j < prods.length; j++)
                {
                    ProductIf prod = prods[j];
                    append(sb, "<li>", debug);
                    ProdTileTag ptt = new ProdTileTag();
                    ptt.init(eng, prod, this.pageContext, "small");
                    ptt.renderTag(sb);
                    append(sb, "</li>", debug);
                }
                append(sb, "</ul>", debug);
                append(sb, "</div>", debug);
                append(sb, "</div>", debug);

                writer.write(sb.toString());
            }
        } catch (IOException e)
        {
            String msg = "Cannot write prod tile tag content";
            throw new JspException(msg, e);
        } catch (KKAppException e)
        {
            throw new JspException(e.getMessage(), e);
        }
        return EVAL_PAGE;
    }

    /**
     * @return the prods
     */
    public ProductIf[] getProds()
    {
        return prods;
    }

    /**
     * @param prods
     *            the prods to set
     */
    public void setProds(ProductIf[] prods)
    {
        this.prods = prods;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

}
