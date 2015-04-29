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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import com.konakart.appif.OptionIf;

/**
 * 
 * Tag used to format product options depending on the type
 * 
 */
public class ProdOptionTag extends BaseTag
{
    private static final long serialVersionUID = 1L;

    private OptionIf[] options;
    
    private boolean addFirstBreak = true;

    public int doStartTag() throws JspException
    {
        try
        {
            setEng((com.konakart.al.KKAppEng) pageContext.getSession().getAttribute("konakartKey"));

            JspWriter writer = pageContext.getOut();

            StringBuffer sb = new StringBuffer();
            if (options != null && options.length > 0)
            {
                for (int i = 0; i < options.length; i++)
                {
                    OptionIf option = options[i];
                    if (i==0)
                    {
                        if (addFirstBreak)
                        {
                            sb.append("<br>");
                        }
                    }else {
                        sb.append("<br>");
                    }
                    if (option.getType() == com.konakart.app.Option.TYPE_SIMPLE)
                    {
                        sb.append("<span class=\"shopping-cart-item-option\"> - "
                                + option.getName() + ": " + option.getValue() + "</span>");
                    } else if (option.getType() == com.konakart.app.Option.TYPE_VARIABLE_QUANTITY)
                    {
                        sb.append("<span class=\"shopping-cart-item-option\"> - "
                                + option.getName() + ": " + option.getQuantity() + " "
                                + option.getValue() + "</span>");
                    } else if (option.getType() == com.konakart.app.Option.TYPE_CUSTOMER_PRICE)
                    {
                        String price = "";
                        if (option.getCustomerPrice() != null)
                        {
                            price = eng.formatPrice(option.getCustomerPrice());
                        }
                        sb.append("<span class=\"shopping-cart-item-option\"> - "
                                + option.getName() + ": " + price + "</span>");
                    } else if (option.getType() == com.konakart.app.Option.TYPE_CUSTOMER_TEXT)
                    {
                        String text = (option.getCustomerText()==null)?"":option.getCustomerText();
                        sb.append("<span class=\"shopping-cart-item-option\"> - "
                                + option.getName() + ": " + text + "</span>");
                    }
                }
            }
            writer.write(sb.toString());
        } catch (Exception e)
        {
            String msg = "Cannot write prod tile tag content";
            throw new JspException(msg, e);
        }
        return EVAL_PAGE;
    }

    /**
     * @return the options
     */
    public OptionIf[] getOptions()
    {
        return options;
    }

    /**
     * @param options
     *            the options to set
     */
    public void setOptions(OptionIf[] options)
    {
        this.options = options;
    }

    /**
     * @return the addFirstBreak
     */
    public boolean isAddFirstBreak()
    {
        return addFirstBreak;
    }

    /**
     * @param addFirstBreak the addFirstBreak to set
     */
    public void setAddFirstBreak(boolean addFirstBreak)
    {
        this.addFirstBreak = addFirstBreak;
    }

}
