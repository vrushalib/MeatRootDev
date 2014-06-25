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
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.XMLUtils;
import com.konakart.app.ProductSearch;
import com.konakart.appif.CategoryIf;
import com.konakart.appif.DataDescriptorIf;
import com.konakart.appif.ManufacturerIf;
import com.konakart.appif.ProductIf;
import com.konakart.appif.ProductsIf;
import com.konakart.appif.ReviewsIf;

/**
 * Used for the OpenLaszlo Catalog Inspector demo.
 */
public class XMLOverHTTPAction extends BaseAction
{

    private static final String methodName = "methodName";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        KKAppEng kkAppEng = null;

        try
        {

            kkAppEng = this.getKKAppEng(request, response);

            if (request == null)
            {
                throw new Exception("The request is null");
            }

            // Figure out which method to call based on the methodName parameter
            String method = request.getParameter(methodName);
            if (method == null)
            {
                throw new Exception("The methodName parameter is null");
            } else if (method.equals("getCategoryTree"))
            {
                getCategoryTree(kkAppEng, request);
            } else if (method.equals("getProductsPerCategory"))
            {
                getProductsPerCategory(kkAppEng, request);
            } else if (method.equals("getProductsPerManufacturer"))
            {
                getProductsPerManufacturer(kkAppEng, request);
            } else if (method.equals("getReviewsPerProduct"))
            {
                getReviewsPerProduct(kkAppEng, request);
            } else if (method.equals("getProduct"))
            {
                getProduct(kkAppEng, request);
            } else if (method.equals("getAllManufacturers"))
            {
                getAllManufacturers(kkAppEng);
            } else if (method.equals("searchForProducts"))
            {
                searchForProducts(kkAppEng, request);
            } else
            {
                throw new Exception("The methodName " + method + " is not implemented");
            }

            if (log.isDebugEnabled())
            {
                log.debug("XML response from engine :");
                log.debug(kkAppEng.getXMLOverHTTPResp());
            }

            return SUCCESS;

        } catch (Exception e)
        {
            String msg = null;
            if (e.getMessage() == null)
            {
                e.printStackTrace();
                msg = "Unknown cause for Exception. Look at server logs";
            } else
            {
                msg = e.getMessage();
            }
            if (kkAppEng != null)
            {
                kkAppEng.setXMLOverHTTPResp(XMLUtils.EXCEPTION_START + msg + XMLUtils.EXCEPTION_END);
                if (log.isDebugEnabled())
                {
                    log.debug("XML exception from engine :");
                    log.debug(kkAppEng.getXMLOverHTTPResp());
                }
            }
            return SUCCESS;
        }

    }

    /**
     * getCategoryTree
     * 
     * @param kkAppEng
     * @param request
     * @throws Exception
     */
    private void getCategoryTree(KKAppEng kkAppEng, HttpServletRequest request) throws Exception
    {
        int languageId;
        boolean getNumProducts;
        String parm1 = request.getParameter("languageId");
        if (parm1 == null)
        {
            throw new Exception("The languageId parameter is not set");
        }
        languageId = new Integer(parm1).intValue();

        String parm2 = request.getParameter("getNumProducts");
        if (parm2 == null)
        {
            throw new Exception("The getNumProducts parameter is not set");
        }
        if (parm2.equalsIgnoreCase("true"))
        {
            getNumProducts = true;
        } else
        {
            getNumProducts = false;
        }

        CategoryIf[] catArray = kkAppEng.getEng().getCategoryTree(languageId, getNumProducts);
        kkAppEng.setXMLOverHTTPResp(XMLUtils.getXML(kkAppEng, catArray));
    }

    /**
     * getProductsPerCategory
     * 
     * @param kkAppEng
     * @param request
     * @throws Exception
     */
    private void getProductsPerCategory(KKAppEng kkAppEng, HttpServletRequest request)
            throws Exception
    {

        String sessionId = null;
        String parm1 = request.getParameter("sessionId");
        if (parm1 != null)
        {
            sessionId = parm1;
        }

        String parm2 = request.getParameter("dataDesc");
        if (parm2 == null)
        {
            throw new Exception("The dataDesc parameter is not set");
        }
        DataDescriptorIf dataDesc = XMLUtils.getDataDesc(parm2);

        String parm3 = request.getParameter("categoryId");
        if (parm3 == null)
        {
            throw new Exception("The categoryId parameter is not set");
        }
        int categoryId = new Integer(parm3).intValue();

        String parm4 = request.getParameter("languageId");
        if (parm4 == null)
        {
            throw new Exception("The languageId parameter is not set");
        }
        int languageId = new Integer(parm4).intValue();

        ProductsIf products = kkAppEng.getEng().getProductsPerCategory(sessionId, dataDesc,
                categoryId, /* searchInSubCats */false, languageId);

        kkAppEng.setXMLOverHTTPResp(XMLUtils.getXML(kkAppEng, products));

    }

    /**
     * searchForProducts
     * 
     * @param kkAppEng
     * @param request
     * @throws Exception
     */
    private void searchForProducts(KKAppEng kkAppEng, HttpServletRequest request) throws Exception
    {

        String sessionId = null;
        String parm1 = request.getParameter("sessionId");
        if (parm1 != null)
        {
            sessionId = parm1;
        }

        String parm2 = request.getParameter("dataDesc");
        if (parm2 == null)
        {
            throw new Exception("The dataDesc parameter is not set");
        }
        DataDescriptorIf dataDesc = XMLUtils.getDataDesc(parm2);

        String parm3 = request.getParameter("searchText");
        if (parm3 == null)
        {
            throw new Exception("The searchString parameter is not set");
        }
        String searchText = parm3;

        String parm4 = request.getParameter("languageId");
        if (parm4 == null)
        {
            throw new Exception("The languageId parameter is not set");
        }
        int languageId = new Integer(parm4).intValue();

        ProductSearch ps = new ProductSearch();
        ps.setSearchText(searchText);

        ProductsIf products = kkAppEng.getEng().searchForProducts(sessionId, dataDesc, ps,
                languageId);

        kkAppEng.setXMLOverHTTPResp(XMLUtils.getXML(kkAppEng, products));

    }

    /**
     * getProductsPerCategory
     * 
     * @param kkAppEng
     * @param request
     * @throws Exception
     */
    private void getProductsPerManufacturer(KKAppEng kkAppEng, HttpServletRequest request)
            throws Exception
    {

        String sessionId = null;
        String parm1 = request.getParameter("sessionId");
        if (parm1 != null)
        {
            sessionId = parm1;
        }

        String parm2 = request.getParameter("dataDesc");
        if (parm2 == null)
        {
            throw new Exception("The dataDesc parameter is not set");
        }
        DataDescriptorIf dataDesc = XMLUtils.getDataDesc(parm2);

        String parm3 = request.getParameter("manufacturerId");
        if (parm3 == null)
        {
            throw new Exception("The manufacturerId parameter is not set");
        }
        int manufacturerId = new Integer(parm3).intValue();

        String parm4 = request.getParameter("languageId");
        if (parm4 == null)
        {
            throw new Exception("The languageId parameter is not set");
        }
        int languageId = new Integer(parm4).intValue();

        ProductsIf products = kkAppEng.getEng().getProductsPerManufacturer(sessionId, dataDesc,
                manufacturerId, languageId);

        kkAppEng.setXMLOverHTTPResp(XMLUtils.getXML(kkAppEng, products));

    }

    /**
     * getReviewsPerProduct
     * 
     * @param kkAppEng
     * @param request
     * @throws Exception
     */
    private void getReviewsPerProduct(KKAppEng kkAppEng, HttpServletRequest request)
            throws Exception
    {

        String parm1 = request.getParameter("dataDesc");
        if (parm1 == null)
        {
            throw new Exception("The dataDesc parameter is not set");
        }
        DataDescriptorIf dataDesc = XMLUtils.getDataDesc(parm1);

        String parm2 = request.getParameter("productId");
        if (parm2 == null)
        {
            throw new Exception("The productId parameter is not set");
        }
        int productId = new Integer(parm2).intValue();

        ReviewsIf reviews = kkAppEng.getEng().getReviewsPerProduct(dataDesc, productId);

        kkAppEng.setXMLOverHTTPResp(XMLUtils.getXML(kkAppEng, reviews));

    }

    /**
     * getProduct
     * 
     * @param kkAppEng
     * @param request
     * @throws Exception
     */
    private void getProduct(KKAppEng kkAppEng, HttpServletRequest request) throws Exception
    {

        String sessionId = null;
        String parm1 = request.getParameter("sessionId");
        if (parm1 != null)
        {
            sessionId = parm1;
        }

        String parm2 = request.getParameter("productId");
        if (parm2 == null)
        {
            throw new Exception("The productId parameter is not set");
        }
        int productId = new Integer(parm2).intValue();

        String parm3 = request.getParameter("languageId");
        if (parm3 == null)
        {
            throw new Exception("The languageId parameter is not set");
        }
        int languageId = new Integer(parm3).intValue();

        ProductIf product = kkAppEng.getEng().getProduct(sessionId, productId, languageId);
        kkAppEng.setXMLOverHTTPResp(XMLUtils.getXML(kkAppEng, product));

    }

    /**
     * getAllManufacturers
     * 
     * @param kkAppEng
     * @throws Exception
     */
    private void getAllManufacturers(KKAppEng kkAppEng) throws Exception
    {
        ManufacturerIf[] manufacturers = kkAppEng.getEng().getAllManufacturers();
        kkAppEng.setXMLOverHTTPResp(XMLUtils.getXML(kkAppEng, manufacturers));
    }
}
