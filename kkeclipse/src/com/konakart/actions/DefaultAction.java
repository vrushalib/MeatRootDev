//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
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
//

package com.konakart.actions;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

/**
 * Action gets called when URL maps to no other actions. This will also get called after the
 * redirect for creating the directory structure URL. In this case we attempt to decode the final
 * part of the URL which contains the information as to which struts action we need to call. For
 * example : http://localhost:8780/konakart/Hardware/Graphics-Cards/1,4,-1,2.action . All the
 * necessary information is contained in the part "1,4,-1,2"
 */
public class DefaultAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String seoManuId;

    private String seoProdId;

    private String seoCatId;

    private String seoProdsFound;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        try
        {
            try
            {
                StringBuffer url = request.getRequestURL();

                // System.out.println("DefaultAction Url = "+url);

                int fromIndex = 0;
                int index = 0;
                while ((index = url.indexOf("/", fromIndex)) > -1)
                {
                    fromIndex = index + 1;
                }
                String cmd = url.substring(fromIndex, url.length() - SEO_TYPE_LENGTH);
                if (cmd != null && cmd.length() > 0)
                {
                    String[] cmdArray = cmd.split(SEO_DELIM);
                    if (cmdArray.length > 0)
                    {
                        int cmdInt = Integer.parseInt(cmdArray[0]);
                        switch (cmdInt)
                        {
                        case SEO_SEL_CAT_CODE:
                            if (cmdArray.length > 1)
                            {
                                for (int i = 1; i < cmdArray.length; i++)
                                {
                                    String val = cmdArray[i];
                                    switch (i)
                                    {
                                    case 1:
                                        this.seoCatId = val;
                                        break;
                                    case 2:
                                        if (val != null && val.length() > 0)
                                        {
                                            this.seoManuId = val;
                                        }
                                        break;
                                    case 3:
                                        if (val != null && val.length() > 0)
                                        {
                                            this.seoProdsFound = val;
                                        }
                                        break;

                                    default:
                                        break;
                                    }

                                }
                                return SEO_SEL_CAT;
                            }
                            break;
                        case SEO_SEL_PROD_CODE:
                            if (cmdArray.length > 1)
                            {
                                for (int i = 1; i < cmdArray.length; i++)
                                {
                                    String val = cmdArray[i];
                                    switch (i)
                                    {
                                    case 1:
                                        this.seoProdId = val;
                                        break;
                                    default:
                                        break;
                                    }
                                }
                                return SEO_SEL_PROD;
                            }
                            break;
                        case SEO_PRODS_FOR_MANU_CODE:
                            if (cmdArray.length > 1)
                            {
                                for (int i = 1; i < cmdArray.length; i++)
                                {
                                    String val = cmdArray[i];
                                    switch (i)
                                    {
                                    case 1:
                                        this.seoManuId = val;
                                        break;
                                    default:
                                        break;
                                    }
                                }
                                return SEO_PRODS_FOR_MANU;
                            }
                            break;
                        case SEO_SEARCH_BY_MANU_BY_LINK_CODE:
                            if (cmdArray.length > 1)
                            {
                                for (int i = 1; i < cmdArray.length; i++)
                                {
                                    String val = cmdArray[i];
                                    switch (i)
                                    {
                                    case 1:
                                        this.seoManuId = val;
                                        break;
                                    default:
                                        break;
                                    }
                                }
                                return SEO_SEARCH_BY_MANU_BY_LINK;
                            }
                            break;
                        }
                    } else
                    {
                        return WELCOME;
                    }
                    return WELCOME;
                }
            } catch (Exception e)
            {
                if (log.isDebugEnabled())
                {
                    e.printStackTrace();
                }
                return WELCOME;
            }
            return WELCOME;
        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the seoProdId
     */
    public String getSeoProdId()
    {
        return seoProdId;
    }

    /**
     * @param seoProdId
     *            the seoProdId to set
     */
    public void setSeoProdId(String seoProdId)
    {
        this.seoProdId = seoProdId;
    }

    /**
     * @return the seoManuId
     */
    public String getSeoManuId()
    {
        return seoManuId;
    }

    /**
     * @param seoManuId
     *            the seoManuId to set
     */
    public void setSeoManuId(String seoManuId)
    {
        this.seoManuId = seoManuId;
    }

    /**
     * @return the seoCatId
     */
    public String getSeoCatId()
    {
        return seoCatId;
    }

    /**
     * @param seoCatId
     *            the seoCatId to set
     */
    public void setSeoCatId(String seoCatId)
    {
        this.seoCatId = seoCatId;
    }

    /**
     * @return the seoProdsFound
     */
    public String getSeoProdsFound()
    {
        return seoProdsFound;
    }

    /**
     * @param seoProdsFound
     *            the seoProdsFound to set
     */
    public void setSeoProdsFound(String seoProdsFound)
    {
        this.seoProdsFound = seoProdsFound;
    }

}
