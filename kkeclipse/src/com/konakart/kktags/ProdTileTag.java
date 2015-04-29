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
import java.math.BigDecimal;
import java.util.Random;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.KKAppException;
import com.konakart.app.NameValue;
import com.konakart.appif.ProductIf;
import com.konakart.appif.PromotionIf;
import com.konakart.appif.PromotionResultIf;

/**
 * 
 * Tag used to write out a product tile
 * 
 */
public class ProdTileTag extends BaseTag
{
    private static final long serialVersionUID = 1L;

    private ProductIf prod;

    private String style;

    // Required since we may show more than one tile of the same product on a page which would make
    // the ids match.
    private String rand = String.valueOf(new Random().nextInt(1000));

    /**
     * Used when being called from another tag
     * 
     * @param _eng
     * @param _prod
     * @param context
     */
    public void init(KKAppEng _eng, ProductIf _prod, PageContext context)
    {
        this.eng = _eng;
        this.prod = _prod;
        if (this.pageContext == null)
        {
            this.pageContext = context;
        }
    }
    
    /**
     * Used when being called from another tag
     * 
     * @param _eng
     * @param _prod
     * @param context
     * @param _style 
     */
    public void init(KKAppEng _eng, ProductIf _prod, PageContext context, String _style)
    {
        this.eng = _eng;
        this.prod = _prod;
        this.style = _style;
        if (this.pageContext == null)
        {
            this.pageContext = context;
        }
    }

    public int doStartTag() throws JspException
    {
        try
        {
            setEng((com.konakart.al.KKAppEng) pageContext.getSession().getAttribute("konakartKey"));
            StringBuffer sb = new StringBuffer();
            renderTag(sb);
            JspWriter writer = pageContext.getOut();
            writer.write(sb.toString());
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
     * Method renders the tag in the StringBuffer passed in as a parameter
     * 
     * @param sb
     * @throws JspException
     * @throws KKAppException
     */
    public void renderTag(StringBuffer sb) throws JspException, KKAppException
    {
        int qtyWarn = eng.getStockWarnLevel();

        if (style != null && style.equalsIgnoreCase("small"))
        {
            // Outer div for product tile
            sb.append(getStartDiv("item style-small"));
            // Image
            getImageLink(sb, KKAppEng.IMAGE_SMALL, /*addLink*/true);
            // Title
            getTitleLink(sb);
            // Reviews
            getReviews(sb);
            // Prices
            getPrices(sb);

            // End of product tile div
            sb.append(END_DIV);
        } else
        {
            // Outer div for product tile
            sb.append(getStartDiv("item"));

            // Float over
            sb.append(getStartDiv("item-over", rand + "ov-" + Integer.toString(prod.getId())));

            if (prod.getQuantity() > qtyWarn)
            {
                sb.append(getStartDiv("items-left green"));
                sb.append(getMsg("product.tile.in.stock"));
                sb.append(END_DIV);
            } else if (prod.getQuantity() <= qtyWarn && prod.getQuantity() > 0)
            {
                sb.append(getStartDiv("items-left amber"));
                sb.append(getMsg("product.tile.limited.stock", Integer.toString(prod.getQuantity())));
                sb.append(END_DIV);
            } else
            {
                sb.append(getStartDiv("items-left red"));
                sb.append(getMsg("product.tile.out.of.stock"));
                sb.append(END_DIV);
            }
            sb.append(getStartDiv("item-buttons-container"));
            sb.append(getStartDiv("item-buttons centered"));
            if (eng.getQuotaMgr().canAddToBasket(prod, null) > 0)
            {
                sb.append(getStartA("add-to-cart-button button small-rounded-corners", "#", rand
                        + "atc-" + Integer.toString(prod.getId())));
                sb.append(getMsg("common.add.to.cart"));
                sb.append(END_A);
            }
            if (eng.isWishListEnabled())
            {
                String imgBase = eng.getImageBase();
                sb.append(getStartDiv("add-to-wishlist-container centered"));
                sb.append(getStartSpan(null));
                sb.append(getImg("plus-button", imgBase + "/plus-button.png",
                        getMsg("common.add.to.wishlist"), /* add base */false));
                sb.append(getStartA("add-to-wishlist", "#",
                        rand + "atc-" + Integer.toString(prod.getId())));
                sb.append(getMsg("common.add.to.wishlist"));
                sb.append(END_A);
                sb.append(END_SPAN);
                sb.append(END_DIV); // add-to-wishlist-container centered
            }
            sb.append(END_DIV); // item-buttons centered
            sb.append(END_DIV); // item-buttons-container
            sb.append(getStartDiv("item-overlay"));
            sb.append(END_DIV); // item-overlay
            sb.append(END_DIV); // item-over

            // Image
            getImageLink(sb, KKAppEng.IMAGE_MEDIUM, /*addLink*/false);

            // Title
            getTitleLink(sb);

            // Reviews
            getReviews(sb);

            // Pricing
            BigDecimal saving = getPrices(sb);

            // Shipping
            if (prod.getType() == com.konakart.bl.ProductMgr.FREE_SHIPPING
                    || prod.getType() == com.konakart.bl.ProductMgr.FREE_SHIPPING_BUNDLE_PRODUCT_TYPE)
            {
                sb.append(getStartDiv("label free-shipping"));
                if (saving == null)
                {
                    sb.append(getMsg("product.tile.free.shipping"));
                } else
                {
                    sb.append(getMsg("product.tile.free.shipping.wrap"));
                }
                sb.append(END_DIV);
            }

            // Saving
            if (saving != null)
            {
                sb.append(getStartDiv("label save"));
                sb.append(eng.getMsg("common.save"));
                sb.append("&nbsp;");
                sb.append(eng.formatPrice(saving));
                sb.append(END_DIV);
            }

            // Show that there is a promotion active
            if (prod.getPromotionResults() != null && prod.getPromotionResults().length > 0)
            {
                PromotionResultIf promotionResult = prod.getPromotionResults()[0];
                PromotionIf promotion = eng.getProductMgr().getPromotionMap()
                        .get(promotionResult.getPromotionId());
                if (promotion != null)
                {
                    sb.append(getStartDiv("label save"));
                    sb.append(promotion.getName());
                    sb.append(END_DIV);
                }
            }

            // End of product tile div
            sb.append(END_DIV);
        }
    }

    private void getImageLink(StringBuffer sb, int size, boolean addLink)
    {
        String url;
        if (eng.isPortlet())
        {
            url = createPortalURL("SelectProd.action", new NameValue[]
            { new NameValue("prodId", prod.getId()) });
        } else
        {
            url = "SelectProd.action?prodId=" + prod.getId();
        }
        if (addLink)
        {
            sb.append(getStartA(null, url)); 
        }
        sb.append(getImg("item-img", eng.getProdImage(prod, size), prod.getName(), /* addBase */
                false));
        if (addLink)
        {
            sb.append(END_A);
        }
    }

    private void getTitleLink(StringBuffer sb)
    {
        String url;
        if (eng.isPortlet())
        {
            url = createPortalURL("SelectProd.action", new NameValue[]
            { new NameValue("prodId", prod.getId()) });
        } else
        {
            url = "SelectProd.action?prodId=" + prod.getId();
        }
        sb.append(getStartA("item-title", url));
        sb.append(prod.getName());
        sb.append(END_A);
    }

    private void getReviews(StringBuffer sb)
    {
        sb.append(getStartDiv("rating"));
        int rating = (prod.getRating() == null) ? 0 : prod.getRating()
                .setScale(0, java.math.BigDecimal.ROUND_HALF_UP).intValue();
        if (prod.getNumberReviews() > 0)
        {
            String url;
            if (eng.isPortlet())
            {
                url = createPortalURL("SelectProd.action", new NameValue[]
                { new NameValue("prodId", prod.getId()), new NameValue("showRevs", "t") });
            } else
            {
                url = "SelectProd.action?prodId=" + prod.getId() + "&showRevs=t";
            }
            sb.append(getStartA("item-title", url));
        }
        for (int i = 0; i < rating; i++)
        {
            sb.append(getStartSpan("star full"));
            sb.append(END_SPAN);
        }
        for (int i = rating; i < 5; i++)
        {
            sb.append(getStartSpan("star empty"));
            sb.append(END_SPAN);
        }
        sb.append(" (" + prod.getNumberReviews() + " " + getMsg("common.reviews") + ")");
        if (prod.getNumberReviews() > 0)
        {
            sb.append(END_A);
        }
        sb.append(END_DIV);
    }

    /**
     * Returns the saving
     * 
     * @param sb
     * @return Returns the saving
     * @throws KKAppException
     */
    private BigDecimal getPrices(StringBuffer sb) throws KKAppException
    {
        BigDecimal saving = null;
        sb.append(getStartDiv("pricing"));
        if (eng.displayPriceWithTax())
        {
            if (prod.getSpecialPriceIncTax() != null)
            {
                saving = prod.getPriceIncTax().subtract(prod.getSpecialPriceIncTax());
                sb.append(getStartDiv("price old"));
                sb.append(eng.formatPrice(prod.getPriceIncTax()));
                sb.append(END_DIV);

                sb.append(getStartDiv("price"));
                sb.append(eng.formatPrice(prod.getSpecialPriceIncTax()));
                sb.append(END_DIV);
            } else
            {
                sb.append(getStartDiv("price"));
                sb.append(eng.formatPrice(prod.getPriceIncTax()));
                sb.append(END_DIV);
            }
        } else
        {
            if (prod.getSpecialPriceExTax() != null)
            {
                saving = prod.getPriceExTax().subtract(prod.getSpecialPriceExTax());
                sb.append(getStartDiv("price old"));
                sb.append(eng.formatPrice(prod.getPriceExTax()));
                sb.append(END_DIV);

                sb.append(getStartDiv("price"));
                sb.append(eng.formatPrice(prod.getSpecialPriceExTax()));
                sb.append(END_DIV);
            } else
            {
                sb.append(getStartDiv("price"));
                sb.append(eng.formatPrice(prod.getPriceExTax()));
                sb.append(END_DIV);
            }
        }
        sb.append(END_DIV);
        return saving;
    }

    /**
     * @return the prod
     */
    public ProductIf getProd()
    {
        return prod;
    }

    /**
     * @param prod
     *            the prod to set
     */
    public void setProd(ProductIf prod)
    {
        this.prod = prod;
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

}
