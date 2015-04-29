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
public class CarouselTag extends BaseTag
{
    private static final long serialVersionUID = 1L;

    private ProductIf[] prods;

    private String title;

    private String width;

    private String widthSmall;

    private String breakpointSmall;

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

                append(sb, "var jc" + rand + " = $('#jc" + rand + "');", debug);
                append(sb, "jc" + rand, debug);
                append(sb, ".on('jcarousel:reload jcarousel:create', function () {", debug);
                append(sb, "var itemWidth =  " + width + ";", debug);
                append(sb, "var width = jc" + rand + ".width();", debug);
                append(sb, "if (width < " + breakpointSmall + "){", debug);
                append(sb, "itemWidth =  " + widthSmall + ";", debug);
                append(sb, "}", debug);
                append(sb, "var numItems = Math.floor(width / itemWidth);", debug);
                append(sb, "if (numItems == 0) {", debug);
                append(sb, "numItems = 1;", debug);
                append(sb, "jc" + rand + ".jcarousel('items').css('width', itemWidth + 'px');",
                        debug);
                append(sb, "} else {", debug);
                append(sb, "var extra = width - (numItems*itemWidth);", debug);
                append(sb, "var extraPerItem  = extra / numItems;", debug);
                append(sb, "rightMargin = Math.ceil(extraPerItem/2);", debug);
                append(sb, "var leftMargin = Math.floor(extraPerItem/2);", debug);
                append(sb, "jc" + rand + ".jcarousel('items').css('width', itemWidth + 'px');",
                        debug);
                append(sb, "jc" + rand
                        + ".jcarousel('items').css('margin-left', leftMargin + 'px');", debug);
                append(sb, "jc" + rand
                        + ".jcarousel('items').css('margin-right', rightMargin + 'px');", debug);
                append(sb, "}", debug);
                append(sb, "jc" + rand + ".jcarousel('scroll', 0, true, function(scrolled) {",
                        debug);
                append(sb, "setControls(jc" + rand + ",$('#jc" + rand + "-prev'),$('#jc" + rand
                        + "-next'));", debug);
                append(sb, "})", debug);

                append(sb, "})", debug);

                append(sb, ".jcarousel({wrap: null});", debug);

                append(sb, "$('#jc" + rand + "-prev').jcarouselControl({", debug);
                append(sb, "method: function() {", debug);
                append(sb, "jc" + rand + ".jcarousel('scroll', '-='+jc" + rand
                        + ".jcarousel('visible').length, true, function(scrolled) {", debug);
                append(sb, "setControls(jc" + rand + ",$('#jc" + rand + "-prev'),$('#jc" + rand
                        + "-next'));", debug);
                append(sb, "})} ", debug);
                append(sb, "}); ", debug);

                append(sb, "$('#jc" + rand + "-next').jcarouselControl({", debug);
                append(sb, "method: function() {", debug);
                append(sb, "jc" + rand + ".jcarousel('scroll', '+='+jc" + rand
                        + ".jcarousel('visible').length, true, function(scrolled) {", debug);
                append(sb, "setControls(jc" + rand + ",$('#jc" + rand + "-prev'),$('#jc" + rand
                        + "-next'));", debug);
                append(sb, "})} ", debug);
                append(sb, "}); ", debug);

                // Swipe
                append(sb, "jc" + rand + ".swipe({", debug);
                append(sb,
                        "swipeRight: function(event, direction, distance, duration, fingerCount) {",
                        debug);
                append(sb, "jc" + rand + ".jcarousel('scroll', '-='+jc" + rand
                        + ".jcarousel('visible').length, true, function(scrolled) {", debug);
                append(sb, "setControls(jc" + rand + ",$('#jc" + rand + "-prev'),$('#jc" + rand
                        + "-next'));", debug);
                append(sb, "}) ", debug);
                append(sb, "},", debug);
                append(sb,
                        "swipeLeft: function(event, direction, distance, duration, fingerCount) {",
                        debug);
                append(sb, "jc" + rand + ".jcarousel('scroll', '+='+jc" + rand
                        + ".jcarousel('visible').length, true, function(scrolled) {", debug);
                append(sb, "setControls(jc" + rand + ",$('#jc" + rand + "-prev'),$('#jc" + rand
                        + "-next'));", debug);
                append(sb, "}) ", debug);
                append(sb, "}", debug);
                append(sb, "});", debug);
                append(sb, "});", debug);

                append(sb, "</script>", debug);

                /*
                 * Start of HTML
                 */

                append(sb, "<div class=\"item-area wide rounded-corners\">", debug);
                append(sb, "<div class=\"item-area-header\">", debug);
                append(sb, "<h2 class=\"item-area-title\">", debug);
                append(sb, title, debug);
                append(sb, "</h2>", debug);

                append(sb, "<div class=\"item-area-navigation jcarousel-wrapper\">", debug);
                append(sb, "<a href=\"#\" id=\"jc" + rand
                        + "-prev\" class=\"jcarousel-control-prev jcarousel-border-prev\"></a>",
                        debug);
                append(sb, "<a href=\"#\" id=\"jc" + rand
                        + "-next\" class=\"jcarousel-control-next jcarousel-border-next\"></a>",
                        debug);
                append(sb, "</div>", debug);
                append(sb, "</div>", debug);

                append(sb, "<div id=\"jc" + rand + "\" class=\"jcarousel\">", debug);
                append(sb, "<ul>", debug);
                for (int j = 0; j < prods.length; j++)
                {
                    ProductIf prod = prods[j];
                    append(sb, "<li>", debug);
                    ProdTileTag ptt = new ProdTileTag();
                    ptt.init(eng, prod, this.pageContext);
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

    /**
     * @return the width
     */
    public String getWidth()
    {
        return width;
    }

    /**
     * @param width
     *            the width to set
     */
    public void setWidth(String width)
    {
        this.width = width;
    }

    /**
     * @return the widthSmall
     */
    public String getWidthSmall()
    {
        return widthSmall;
    }

    /**
     * @param widthSmall
     *            the widthSmall to set
     */
    public void setWidthSmall(String widthSmall)
    {
        this.widthSmall = widthSmall;
    }

    /**
     * @return the breakpointSmall
     */
    public String getBreakpointSmall()
    {
        return breakpointSmall;
    }

    /**
     * @param breakpointSmall
     *            the breakpointSmall to set
     */
    public void setBreakpointSmall(String breakpointSmall)
    {
        this.breakpointSmall = breakpointSmall;
    }

}
