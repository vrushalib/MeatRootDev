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
public class CarouselTag extends BaseTag
{
    private static final long serialVersionUID = 1L;

    private ProductIf[] prods;

    private String title;

    private String width;

    // Required since we may show more than one carousel on the same page
    private String rand = String.valueOf(new Random().nextInt(1000));

    public int doStartTag() throws JspException
    {
        try
        {
        	rand = String.valueOf(new Random().nextInt(1000));
            setEng((com.konakart.al.KKAppEng) pageContext.getSession().getAttribute("konakartKey"));
            if (prods != null && prods.length > 0)
            {
                int numProdsInt = 5;
                if (width != null)
                {
                    if (width.equalsIgnoreCase("wide"))
                    {
                        numProdsInt = 5;
                    } else if (width.equalsIgnoreCase("narrow"))
                    {
                        numProdsInt = 4;
                    }
                }

                JspWriter writer = pageContext.getOut();

                StringBuffer sb = new StringBuffer();

                sb.append("<script type=\"text/javascript\">");
                sb.append("$(function() {");
                sb.append("jQuery('#slider" + rand + "').jcarousel({");
                sb.append("vertical: false,");
                sb.append("scroll: " + numProdsInt + ",");
                sb.append("initCallback: slider" + rand + "initCallback,");
                sb.append("buttonNextCallback: slider" + rand + "nextCallback,");
                sb.append("buttonPrevCallback: slider" + rand + "prevCallback,");
                sb.append("visible:" + numProdsInt + ",");
                sb.append("buttonNextHTML: null,");
                sb.append("buttonPrevHTML: null");
                sb.append("});");
                sb.append("});");

                sb.append("function slider" + rand + "initCallback(carousel) {");
                sb.append("jQuery('#kk-forward-" + rand + "').bind('click', function() {");
                sb.append("carousel.next();");
                sb.append("return false;");
                sb.append("});");

                sb.append("jQuery('#kk-back-" + rand + "').bind('click', function() {");
                sb.append("carousel.prev();");
                sb.append("return false;");
                sb.append("});");
                sb.append("};");

                sb.append("function slider" + rand + "nextCallback(carousel,control,flag) {");
                sb.append("if (flag) {");
                sb.append("jQuery('#kk-forward-" + rand
                        + "').addClass(\"next-items\").removeClass(\"next-items-inactive\");");
                sb.append("} else {");
                sb.append("jQuery('#kk-forward-" + rand
                        + "').addClass(\"next-items-inactive\").removeClass(\"next-items\");");
                sb.append("}");
                sb.append("};");

                sb.append("function slider" + rand + "prevCallback(carousel,control,flag) {");
                sb.append("if (flag) {");
                sb.append("jQuery('#kk-back-"
                        + rand
                        + "').addClass(\"previous-items\").removeClass(\"previous-items-inactive\");");
                sb.append("} else {");
                sb.append("jQuery('#kk-back-"
                        + rand
                        + "').addClass(\"previous-items-inactive\").removeClass(\"previous-items\");");
                sb.append("}");
                sb.append("};");
                sb.append("</script>");

                sb.append("<div class=\"item-area " + width + " rounded-corners\">");
                sb.append("<div class=\"item-area-header\">");
                sb.append("<h2 class=\"item-area-title\">");
                sb.append(title);
                sb.append("</h2>");

                sb.append("<div class=\"item-area-navigation\">");
                sb.append("<span id=\"kk-back-" + rand + "\" class=\"item-arrow\"></span>");
                sb.append("<span class=\"separator\"></span>");
                sb.append("<span id=\"kk-forward-" + rand + "\" class=\"item-arrow\"></span>");
                sb.append("</div>");
                sb.append("</div>");

                sb.append("<div id=\"slider" + rand + "\" class=\"items jcarousel-skin-kk\">");
                sb.append("<ul>");
                for (int j = 0; j < prods.length; j++)
                {
                    ProductIf prod = prods[j];
                    sb.append("<li>");
                    ProdTileTag ptt = new ProdTileTag();
                    ptt.init(eng, prod, this.pageContext);
                    ptt.renderTag(sb);
                    sb.append("</li>");
                }
                sb.append("</ul>");
                sb.append("</div>");

                sb.append("</div>");

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

}
