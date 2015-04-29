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

import java.net.URLEncoder;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.KKAppException;
import com.konakart.app.KKException;
import com.konakart.app.ReviewSearch;
import com.konakart.appif.CategoryIf;
import com.konakart.appif.ProductIf;
import com.konakart.bl.ConfigConstants;

/**
 * Called to show the details of a product.
 */
public class ShowProductDetailsAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String seoProdId;

    /**
     * Called to show the details of a product when you don't have the product id. An example of
     * this is when you attempt to add a product to the wish list but you need to login first It
     * gets the id from the current product and then calls ShowProductDetailsAction passing the id
     * as a parameter.
     * 
     * @return Returns the forward
     */
    public String preSelect()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        try
        {
            /*
             * Get the product id from the selected product
             */
            KKAppEng kkAppEng = this.getKKAppEng(request, response);
            ProductIf selectedProd = kkAppEng.getProductMgr().getSelectedProduct();
            if (selectedProd == null)
            {
                return WELCOME;
            }
            return showProdDetails(String.valueOf(selectedProd.getId()));
        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    public String execute()
    {
        return showProdDetails(null);
    }

    /**
     * Prepares data to show the details of a product
     * 
     * @param preSelId
     * @return Returns the forward
     */
    public String showProdDetails(String preSelId)
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, null);

            // Force the user to login if configured to do so
            if (custId < 0 && kkAppEng.isForceLogin())
            {
                return KKLOGIN;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId,/* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Get prod id from request parameter or from method parameter
            String prodId = (preSelId == null) ? request.getParameter("prodId") : preSelId;

            // See whether we need to open the reviews tab
            String showRevs = request.getParameter("showRevs");
            if (showRevs != null)
            {
                kkAppEng.getReviewMgr().setShowTab(true);
            }

            // If parameter == null action may have been called after an SEO redirect so use
            // seoProdId
            prodId = (prodId == null) ? this.seoProdId : prodId;
            if (prodId == null)
            {
                return WELCOME;
            }

            // Test to see whether the productId is an integer
            int prodIdInt;
            try
            {
                prodIdInt = new Integer(prodId).intValue();
            } catch (Exception e)
            {
                return WELCOME;
            }

            if (log.isDebugEnabled())
            {
                log.debug("Product Id of selected product from application = " + prodId);
            }

            ProductIf selectedProd = kkAppEng.getProductMgr().getSelectedProduct();
            CategoryIf selectedCat = kkAppEng.getCategoryMgr().getCurrentCat();

            int seoFormat = kkAppEng.getConfigAsInt(ConfigConstants.SEO_URL_FORMAT);
            if (seoFormat == SEO_DIRECTORY)
            {
                /*
                 * Since we do a permanent redirect, some browsers cache the redirect URL and so the
                 * default action is called first and then we come here.
                 */
                if (this.seoProdId != null)
                {
                    if (selectedProd == null || selectedCat == null
                            || selectedProd.getId() != prodIdInt)
                    {
                        // Fetch the product related data from the database
                        kkAppEng.getProductMgr().fetchSelectedProduct(prodIdInt);
                        // Return if the product doesn't exist
                        if (kkAppEng.getProductMgr().getSelectedProduct() == null)
                        {
                            return SUCCESS;
                        }

                        kkAppEng.getProductMgr().updateProductViewedCount(prodIdInt);
                        if (!kkAppEng.getPropertyAsBoolean("prod.details.hide.also.purchased",
                                false))
                        {
                            kkAppEng.getProductMgr().fetchAlsoPurchasedArray();
                        }
                        if (!kkAppEng.getPropertyAsBoolean("prod.details.hide.related", false))
                        {
                            kkAppEng.getProductMgr().fetchRelatedProducts();
                        }
                        ReviewSearch search = new ReviewSearch();
                        search.setProductId(prodIdInt);
                        kkAppEng.getReviewMgr().fetchReviews(null, search);
                        selectedProd = kkAppEng.getProductMgr().getSelectedProduct();
                        selectedCat = kkAppEng.getCategoryMgr().getCurrentCat();
                    }
                    return preparePage(request, kkAppEng, selectedProd, selectedCat);
                }
            } else if (seoFormat == SEO_PARAMETERS)
            {
                /*
                 * Check to see whether we are here after the SEO redirect which fetches the
                 * selected product from the database and sets SEO data on the URL.
                 */
                if (selectedProd != null && selectedCat != null
                        && selectedProd.getId() == prodIdInt
                        && request.getParameter(kkAppEng.getMsg("seo.product.name")) != null)
                {
                    return preparePage(request, kkAppEng, selectedProd, selectedCat);
                }
            }

            /*
             * We fetch the data for the selected product and set SEO data on the URL and then do a
             * redirect
             */

            // Fetch the product related data from the database
            kkAppEng.getProductMgr().fetchSelectedProduct(prodIdInt);
            // Return if the product doesn't exist
            if (kkAppEng.getProductMgr().getSelectedProduct() == null)
            {
                return SUCCESS;
            }

            kkAppEng.getProductMgr().updateProductViewedCount(prodIdInt);
            if (!kkAppEng.getPropertyAsBoolean("prod.details.hide.also.purchased", false))
            {
                kkAppEng.getProductMgr().fetchAlsoPurchasedArray();
            }
            if (!kkAppEng.getPropertyAsBoolean("prod.details.hide.related", false))
            {
                kkAppEng.getProductMgr().fetchRelatedProducts();
            }

            ReviewSearch search = new ReviewSearch();
            search.setProductId(prodIdInt);
            kkAppEng.getReviewMgr().fetchReviews(null, search);

            // Do a redirect
            ProductIf prod = kkAppEng.getProductMgr().getSelectedProduct();
            CategoryIf cat = kkAppEng.getCategoryMgr().getCurrentCat();

            StringBuffer url = null;
            if (!kkAppEng.isPortlet())
            {
                url = request.getRequestURL();
            }

            /*
             * If the url we get from the request is set to null (as is sometimes the case when we
             * have been implemented as a portlet), then we mustn't do a redirect.
             */
            if (url == null)
            {
                return preparePage(request, kkAppEng, prod, cat);
            }

            if (seoFormat == SEO_DIRECTORY)
            {
                int fromIndex = 0;
                int index = 0;
                while ((index = url.indexOf("/", fromIndex)) > -1)
                {
                    fromIndex = index + 1;
                }

                url = new StringBuffer(url.substring(0, fromIndex));

                if (cat.getName() != null && cat.getName().length() > 0)
                {
                    url.append(kkURLEncode(cat.getName()));
                    url.append("/");
                    CategoryIf parentCat = cat.getParent();
                    while (parentCat != null)
                    {
                        url.insert(fromIndex, kkURLEncode(parentCat.getName()) + "/");
                        parentCat = parentCat.getParent();
                    }
                }
                if (prod.getManufacturerName() != null && prod.getManufacturerName().length() > 0)
                {
                    url.append(kkURLEncode(prod.getManufacturerName()));
                    url.append("/");
                }
                if (prod.getName() != null && prod.getName().length() > 0)
                {
                    url.append(kkURLEncode(prod.getName()));
                    url.append("/");
                }
                if (prod.getModel() != null && prod.getModel().length() > 0)
                {
                    url.append(kkURLEncode(prod.getModel()));
                    url.append("/");
                }
                url.append(SEO_SEL_PROD_CODE + SEO_DELIM);
                url.append(prod.getId());
                url.append(SEO_TYPE);
            } else if (seoFormat == SEO_PARAMETERS)
            {
                // Add the product id
                url.append("?prodId=");
                url.append(prod.getId());

                // Add seo friendly info
                if (prod.getManufacturerName() != null && prod.getManufacturerName().length() > 0
                        && kkAppEng.getMsg("seo.product.manufacturer").length() > 0)
                {
                    url.append("&");
                    url.append(URLEncoder.encode(kkAppEng.getMsg("seo.product.manufacturer"),
                            "UTF-8"));
                    url.append("=");
                    url.append(URLEncoder.encode(prod.getManufacturerName(), "UTF-8"));
                }
                if (cat.getName() != null && cat.getName().length() > 0
                        && kkAppEng.getMsg("seo.product.category").length() > 0)
                {
                    url.append("&");
                    url.append(URLEncoder.encode(kkAppEng.getMsg("seo.product.category"), "UTF-8"));
                    url.append("=");
                    url.append(URLEncoder.encode(cat.getName(), "UTF-8"));
                }
                if (prod.getName() != null && prod.getName().length() > 0
                        && kkAppEng.getMsg("seo.product.name").length() > 0)
                {
                    url.append("&");
                    url.append(URLEncoder.encode(kkAppEng.getMsg("seo.product.name"), "UTF-8"));
                    url.append("=");
                    url.append(URLEncoder.encode(prod.getName(), "UTF-8"));
                }
                if (prod.getModel() != null && prod.getModel().length() > 0
                        && kkAppEng.getMsg("seo.product.model").length() > 0)
                {
                    url.append("&");
                    url.append(URLEncoder.encode(kkAppEng.getMsg("seo.product.model"), "UTF-8"));
                    url.append("=");
                    url.append(URLEncoder.encode(prod.getModel(), "UTF-8"));
                }

            } else
            {
                // Don't do a redirect
                return preparePage(request, kkAppEng, prod, cat);
            }

            // Do a 301 redirect (permanent) so that search engines index URL
            response.setHeader("Location", url.toString());
            setupResponseForSEORedirect(response);

            return null;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * Common code to prepare the data required for the page before the JSP draws it
     * 
     * @param mapping
     * @param request
     * @param kkAppEng
     * @param selectedProd
     * @param selectedCat
     * @return Returns an ActionForward
     * @throws KKException
     * @throws KKAppException
     */
    private String preparePage(HttpServletRequest request, KKAppEng kkAppEng,
            ProductIf selectedProd, CategoryIf selectedCat) throws KKException, KKAppException
    {
        String selectedManuName = selectedProd.getManufacturerName();
        if (selectedManuName == null)
        {
            if (log.isInfoEnabled())
            {
                log.info("Manufacturer Name is null for Product Id " + selectedProd.getId());
            }
            selectedManuName = "";
        }

        String selectedCatName = selectedCat.getName();
        if (selectedCatName == null)
        {
            if (log.isInfoEnabled())
            {
                log.info("Category Name is null for Category Id " + selectedCat.getId());
            }
            selectedCatName = "";
        }

        String selectedProdName = selectedProd.getName();
        if (selectedProdName == null)
        {
            if (log.isInfoEnabled())
            {
                log.info("Product Name is null for Product Id " + selectedProd.getId());
            }
            selectedProdName = "";
        }

        String selectedProdModel = selectedProd.getModel();
        if (selectedProdModel == null)
        {
            if (log.isInfoEnabled())
            {
                log.info("Product Model is null for Product Id " + selectedProd.getId());
            }
            selectedProdModel = "";
        }

        // Set page title
        String title = kkAppEng.getMsg("seo.product.title.template");
        title = title.replace("$category", selectedCatName);
        title = title.replace("$manufacturer", selectedManuName);
        title = title.replace("$name", selectedProdName);
        title = title.replace("$model", selectedProdModel);
        kkAppEng.setPageTitle(title);

        // Set the meta description
        String description = kkAppEng.getMsg("seo.product.meta.description.template");
        description = description.replace("$category", selectedCatName);
        description = description.replace("$manufacturer", selectedManuName);
        description = description.replace("$name", selectedProdName);
        description = description.replace("$model", selectedProdModel);
        kkAppEng.setMetaDescription(description);

        // Set the meta keywords
        String keywords = kkAppEng.getMsg("seo.meta.keywords.template");
        keywords = keywords.replace("$category", selectedCatName);
        keywords = keywords.replace("$manufacturer", selectedManuName);
        keywords = keywords.replace("$name", selectedProdName);
        keywords = keywords.replace("$model", selectedProdModel);
        kkAppEng.setMetaKeywords(keywords);

        // Set bread crumbs
        ArrayList<CategoryIf> catList = new ArrayList<CategoryIf>();
        CategoryIf lcat = selectedCat;
        // Put all category hierarchy in a list
        while (true)
        {
            catList.add(lcat);
            if (lcat.getParent() == null)
            {
                break;
            }
            lcat = lcat.getParent();
        }
        // Get cat hierarchy from list to build the bread crumbs
        String url = "SelectCat.action?catId=";
        for (int i = catList.size() - 1; i > -1; i--)
        {
            CategoryIf lcat1 = catList.get(i);
            if (i == catList.size() - 1)
            {
                kkAppEng.getNav().set(lcat1.getName(), url + lcat1.getId());
            } else
            {
                kkAppEng.getNav().add(lcat1.getName(), url + lcat1.getId());
            }
        }

        // Add the product bread crumb
        kkAppEng.getNav().add(selectedProdName, "SelectProd.action?prodId=" + selectedProd.getId());

        // Set the PRODUCTS_VIEWED customer tag for this customer
        kkAppEng.getCustomerTagMgr().addToCustomerTag(TAG_PRODUCTS_VIEWED, selectedProd.getId());

        // Insert event
        insertCustomerEvent(kkAppEng, ACTION_PRODUCT_VIEWED, selectedProd.getId());

        return SUCCESS;
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

}
