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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.portlet.context.PortletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.KKAppEng.SessionCheckData;
import com.konakart.al.KKAppException;
import com.konakart.al.StoreInfo;
import com.konakart.app.CustomerEvent;
import com.konakart.app.KKCookie;
import com.konakart.app.KKException;
import com.konakart.appif.CustomerEventIf;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.CustomerTagIf;
import com.konakart.appif.KKCookieIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.servlet.AppEngServlet;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Base Action for KonaKart application. All of the other action classes extend this class.
 */
public class BaseAction extends ActionSupport implements ServletRequestAware, SessionAware

{
    private static final long serialVersionUID = 1L;

    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(BaseAction.class);

    protected static final String KKLOGIN = "Login";

    protected static final String WELCOME = "Welcome";

    protected static final String GUEST_CUSTOMER_ID = "GUEST_CUSTOMER_ID";

    protected static final String CUSTOMER_LOCALE = "CUSTOMER_LOCALE";

    protected static final String CUSTOMER_NAME = "CUSTOMER_NAME";

    protected static final String CUSTOMER_UUID = "CUSTOMER_UUID";

    protected static final String AGREED_COOKIES = "AGREED_COOKIES";

    protected static final int COOKIE_MAX_AGE_IN_SECS = 365 * 24 * 60 * 60;

    /*
     * Event actions
     */
    protected static final int ACTION_NEW_CUSTOMER_VISIT = 1;

    protected static final int ACTION_CUSTOMER_LOGIN = 2;

    protected static final int ACTION_ENTER_CHECKOUT = 3;

    protected static final int ACTION_CONFIRM_ORDER = 4;

    protected static final int ACTION_PAYMENT_METHOD_SELECTED = 5;

    protected static final int ACTION_REMOVE_FROM_CART = 6;

    protected static final int ACTION_PRODUCT_VIEWED = 7;

    /*
     * Customer tags
     */
    protected static final String TAG_PRODUCTS_VIEWED = "PRODUCTS_VIEWED";

    protected static final String TAG_CATEGORIES_VIEWED = "CATEGORIES_VIEWED";

    protected static final String TAG_MANUFACTURERS_VIEWED = "MANUFACTURERS_VIEWED";

    protected static final String TAG_SEARCH_STRING = "SEARCH_STRING";

    protected static final String TAG_COUNTRY_CODE = "COUNTRY_CODE";

    protected static final String TAG_BIRTH_DATE = "BIRTH_DATE";

    protected static final String TAG_LOGIN_DATE = "LOGIN_DATE";

    protected static final String TAG_IS_MALE = "IS_MALE";

    protected static final String TAG_PROD_PAGE_SIZE = "PROD_PAGE_SIZE";

    protected static final String TAG_ORDER_PAGE_SIZE = "ORDER_PAGE_SIZE";

    protected static final String TAG_REVIEW_PAGE_SIZE = "REVIEW_PAGE_SIZE";

    /*
     * Constants for SEO
     */
    protected static final String SEO_DELIM = "_";

    protected static final String SEO_TYPE = ".action";

    protected static final int SEO_TYPE_LENGTH = SEO_TYPE.length();

    protected static final int SEO_SEL_CAT_CODE = 1;

    protected static final String SEO_SEL_CAT = "SelectCat";

    protected static final int SEO_SEL_PROD_CODE = 2;

    protected static final String SEO_SEL_PROD = "SelectProd";

    protected static final int SEO_SEARCH_BY_MANU_BY_LINK_CODE = 3;

    protected static final String SEO_SEARCH_BY_MANU_BY_LINK = "ShowSearchByManufacturerResultsByLink";

    protected static final int SEO_PRODS_FOR_MANU_CODE = 5;

    protected static final String SEO_PRODS_FOR_MANU = "ShowProductsForManufacturer";

    protected static final int SEO_OFF = 0;

    protected static final int SEO_PARAMETERS = 1;

    protected static final int SEO_DIRECTORY = 2;

    /*
     * Following tags declared public because used in managers
     */
    /** Customer tag */
    public static final String TAG_PRODUCTS_IN_CART = "PRODUCTS_IN_CART";

    /** Customer tag */
    public static final String TAG_PRODUCTS_IN_WISHLIST = "PRODUCTS_IN_WISHLIST";

    /** Customer tag */
    public static final String TAG_CART_TOTAL = "CART_TOTAL";

    /** Customer tag */
    public static final String TAG_WISHLIST_TOTAL = "WISHLIST_TOTAL";

    /** Struts session */
    public Map<String, Object> strutsSession;

    /** Copy of the KKAppEng */
    private static KKAppEng appEng;

    /** KKAppEng */
    private KKAppEng jspEng;

    /**
     * Sets the variable kkAppEng to the KKAppEng instance saved in the session. If cannot be found,
     * then it is instantiated and attached.
     * 
     * @param request
     * @param response
     * @return Returns a KonaKart client engine instance
     * @throws KKException
     * @throws KKAppException
     */
    protected KKAppEng getKKAppEng(HttpServletRequest request, HttpServletResponse response)
            throws KKAppException, KKException
    {
        HttpSession session = request.getSession();
        KKAppEng kkAppEng = (KKAppEng) session.getAttribute(KKAppEng.KONAKART_KEY);
        if (kkAppEng == null)
        {
            if (log.isInfoEnabled())
            {
                log.info("KKAppEng not found on the session");
            }

            String storeIdForNewEngine = getStoreIdFromRequest(request);

            StoreInfo si = new StoreInfo();
            si.setStoreId(storeIdForNewEngine);
            kkAppEng = new KKAppEng(si, session);

            if (log.isInfoEnabled())
            {
                log.info("Set KKAppEng on the session for storeId " + si.getStoreId());
            }

            /*
             * Add KKAppEng to the struts and the HTTP sessions. In order for the struts jsp tags to
             * see it, it has to be added to the struts session as well.
             */
            session.setAttribute(KKAppEng.KONAKART_KEY, kkAppEng);
            strutsSession.put(KKAppEng.KONAKART_KEY, kkAppEng);

            String customerUuid = manageCookies(request, response, kkAppEng);
            if (customerUuid != null)
            {
                // Get the locale from the cookie
                String savedLocale = getKKCookie(customerUuid, CUSTOMER_LOCALE, kkAppEng);
                if (savedLocale != null)
                {
                    // Set the engine with the new locale
                    kkAppEng.setLocale(savedLocale);
                }
            }

            // Insert event
            insertCustomerEvent(kkAppEng, ACTION_NEW_CUSTOMER_VISIT);
        }

        kkAppEng.setPageTitle(kkAppEng.getMsg("seo.default.title"));
        kkAppEng.setMetaDescription(kkAppEng.getMsg("seo.default.meta.description"));
        kkAppEng.setMetaKeywords(kkAppEng.getMsg("seo.default.meta.keywords"));

        /* Save a copy in the base action */
        appEng = kkAppEng;

        /* Used by the JSP */
        jspEng = kkAppEng;

        return kkAppEng;
    }

    /**
     * In a multi-store scenario, you should implement your own logic here to extract the storeId
     * from the request and return it.
     * 
     * @param request
     * @return Returns the storeId
     */
    private String getStoreIdFromRequest(HttpServletRequest request)
    {
        /*
         * If the server name could contain store1.localhost or store2.localhost which both point to
         * localhost, we could get the store id from the server name.
         */
        /*
         * if (request != null && request.getServerName() != null) { String[] nameArray =
         * request.getServerName().split("\\."); if (nameArray.length > 1) { String storeId =
         * nameArray[0]; return storeId; } }
         */

        return AppEngServlet.getDefaultStoreId();
    }

    /**
     * A common method that contains the code to deal with exceptions
     * 
     * @param request
     * @param e
     * @return Returns a string
     */
    protected String handleException(HttpServletRequest request, Exception e)
    {

        if (e != null && e.getClass().getName().equals("com.konakart.app.KKException"))
        {
            KKException ex = (KKException) e;

            switch (ex.getCode())
            {
            case KKException.KK_STORE_DELETED:
                if (appEng != null)
                {
                    addActionError(appEng.getMsg("unavailable.deleted"));
                }
                return new String("Unavailable");
            case KKException.KK_STORE_DISABLED:
                if (appEng != null)
                {
                    addActionError(appEng.getMsg("unavailable.disabled"));
                }
                return new String("Unavailable");
            case KKException.KK_STORE_UNDER_MAINTENANCE:
                if (appEng != null)
                {
                    addActionError(appEng.getMsg("unavailable.maintenance"));
                }
                return new String("Unavailable");
            }
        }

        Long time = System.currentTimeMillis();
        log.error("A customer has received the following exception ( ref: " + time + " )", e);
        if (appEng != null)
        {
            addActionError(Long.toString(time));
        }
        return new String("Exception");
    }

    /**
     * Checks to see whether we are logged in.
     * 
     * @param kkAppEng
     *            The KonaKart client engine instance
     * @param forwardAfterLogin
     *            tells us which page to forward to after login.
     * @param request
     * @param response
     * @return Returns the CustomerId if logged in. Otherwise a negative number.
     * @throws KKException
     * @throws KKAppException
     */
    protected int loggedIn(HttpServletRequest request, HttpServletResponse response,
            KKAppEng kkAppEng, String forwardAfterLogin) throws KKException, KKAppException
    {

        return loggedIn(request, response, kkAppEng, forwardAfterLogin, /* checkXSRF */true, /* xsrfToken */
                null);
    }

    /**
     * Checks to see whether we are logged in.
     * 
     * @param kkAppEng
     *            The KonaKart client engine instance
     * @param forwardAfterLogin
     *            tells us which page to forward to after login.
     * @param request
     * @param response
     * @param xsrfToken
     * @return Returns the CustomerId if logged in. Otherwise a negative number.
     * @throws KKException
     * @throws KKAppException
     */
    protected int loggedIn(HttpServletRequest request, HttpServletResponse response,
            KKAppEng kkAppEng, String forwardAfterLogin, String xsrfToken) throws KKException,
            KKAppException
    {

        return loggedIn(request, response, kkAppEng, forwardAfterLogin, /* checkXSRF */true,
                xsrfToken);
    }

    /**
     * Checks to see whether we are logged in.
     * 
     * @param kkAppEng
     *            The KonaKart client engine instance
     * @param forwardAfterLogin
     *            tells us which page to forward to after login.
     * @param request
     * @param response
     * @param checkXSRF
     * @param xsrfToken
     * @return Returns the CustomerId if logged in. Otherwise a negative number.
     * @throws KKException
     * @throws KKAppException
     */
    protected int loggedIn(HttpServletRequest request, HttpServletResponse response,
            KKAppEng kkAppEng, String forwardAfterLogin, boolean checkXSRF, String xsrfToken)
            throws KKException, KKAppException
    {
        // If the session is null, set the forward and return a negative number.
        if ((kkAppEng.getSessionId() == null))
        {
            if (forwardAfterLogin != null)
            {
                kkAppEng.setForwardAfterLogin(forwardAfterLogin);
            }
            return -1;
        }

        int custId;
        if (kkAppEng.getSessionCheckData().needToCheck())
        {
            // If an exception is thrown, set the forward and return it
            if (log.isDebugEnabled())
            {
                log.debug("Calling checkSession on KKEng");
            }
            try
            {
                custId = kkAppEng.getEng().checkSession(kkAppEng.getSessionId());
                SessionCheckData scd = kkAppEng.getSessionCheckData();
                scd.setCustId(custId);
                scd.setCheckTime(System.currentTimeMillis());
            } catch (KKException e)
            {
                log.debug(e.getMessage());
                if (forwardAfterLogin != null)
                {
                    kkAppEng.setForwardAfterLogin(forwardAfterLogin);
                }

                kkAppEng.getCustomerMgr().logout();

                // Ensure that the guest customer is the one in the cookie
                manageCookieLogout(request, response, kkAppEng);

                return -1;
            }
        } else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Using cached sessionId");
            }
            custId = kkAppEng.getSessionCheckData().getCustId();
        }

        // Check the XSRF token for a post. Don't check anything we are redirected to after a login
        // since the token wasn't available at the time of the post
        if (kkAppEng.getXsrfToken() != null && checkXSRF && request.getServletPath() != null
                && !request.getServletPath().contains("LoginSubmit"))
        {
            String method = request.getMethod();
            if (method != null && method.equalsIgnoreCase("POST"))
            {
                String token = (xsrfToken != null) ? xsrfToken : request.getParameter("xsrf_token");
                if (token == null || !token.equals(kkAppEng.getXsrfToken()))
                {
                    log.warn("Possible XSRF attack for customer with id = " + custId);
                    return -1;
                }
            }
        }

        // At this point we return a valid customer Id
        return custId;
    }

    /**
     * If the application sends us a KKException with the cause equal to a more detailed exception
     * such as KKPasswordDoesntMatchException, sometimes the application has to be able to detect
     * this so that it can handle the exception by sending an error message back to the screen.
     * 
     * @param request
     * @param e
     *            The exception that has been captured
     * @param cause
     *            The name of the cause Exception to look for i.e. KKPasswordDoesntMatchException
     * @param message
     *            The message to send out to the application
     * @param forward
     *            The forward if we detect the cause
     * @return ActionForward
     */
    protected String getForward(HttpServletRequest request, Exception e, String cause,
            String message, String forward)
    {

        if (e.getCause() != null && e.getCause().getClass().getName().equals(cause))
        {
            addActionError(message);
            return forward;
        }

        // For the web services case the cause turns out to be an org.apache.axis.AxisFault.
        // However within the message there is a message that says e.g. nested exception is:
        // com.konakart.app.KKPasswordDoesntMatchException so we search for the name of the
        // exception class that is the cause
        if (e.getMessage() != null && e.getMessage().indexOf(cause) > -1)
        {
            addActionError(message);
            return forward;
        }

        return handleException(request, e);
    }

    /**
     * Determines whether we are using SSL or not. If we are logged in, then we should be using SSL.
     * SSL can also be forced by setting the forceSSL boolean. If we should be using SSL but aren't
     * (or vice versa) we do a redirect by returning an action forward with the correct URL to
     * redirect to. Otherwise we return null
     * 
     * @param request
     * @param custId
     *            The customer id
     * @param forceSSL
     *            Set to true if we should force SSL.
     * @return ActionForward
     * @throws KKException
     */
    protected String checkSSL(KKAppEng eng, HttpServletRequest request, int custId, boolean forceSSL)
            throws KKException
    {
        try
        {
            if (eng == null)
            {
                throw new KKException("checkSSL called with KKAppEng set to null");
            }

            if (eng.isPortlet())
            {
                // Take no action if in a Portlet
                return null;
            }

            String sslPort = eng.getSslPort();
            String standardPort = eng.getStandardPort();
            boolean activateCheck = eng.isEnableSSL();
            String sslBaseUrl = eng.getSslBaseUrl();

            if (activateCheck && request != null)
            {
                boolean isSSL = false;
                StringBuffer redirectUrl;

                if (request.getRequestURL() == null)
                {
                    throw new KKException(
                            "Cannot determine whether SSL is being used because getRequestURL() returns null");
                }

                if (request.getRequestURL().substring(0, 5).equalsIgnoreCase("https"))
                {
                    isSSL = true;
                }

                if (log.isDebugEnabled())
                {
                    log.debug("getServerName = " + request.getServerName());
                    log.debug("getServerPort = " + request.getServerPort());
                    log.debug("getServletPath = " + request.getServletPath());
                    log.debug("getRequestURI = " + request.getRequestURI());
                    log.debug("getRequestURL = " + request.getRequestURL());
                    log.debug("isSSL = " + isSSL);
                    log.debug("custId = " + custId);
                }

                if (!isSSL && (custId > -1 || forceSSL))
                {
                    // We aren't using SSL but should be
                    redirectUrl = new StringBuffer();
                    if (sslBaseUrl != null)
                    {
                        redirectUrl.append(sslBaseUrl);
                        redirectUrl.append(request.getRequestURI());
                    } else
                    {
                        redirectUrl.append("https://");
                        redirectUrl.append(request.getServerName());
                        // Insert the port if it is non standard
                        if (sslPort != null && !sslPort.equals("443"))
                        {
                            redirectUrl.append(":");
                            redirectUrl.append(sslPort);
                        }
                        redirectUrl.append(request.getRequestURI());
                    }

                    /*
                     * The following is called for security reasons. In some cases (such as when
                     * using Tomcat) the session id is appended to the URL in the browser (i.e.
                     * jsessionid=E2D1B0B2B8C5478B7F6F3C3C5D9BB0FB). If a hacker managed to get this
                     * session id while the customer wasn't logged in, he could use it to access
                     * sensitive information once the customer has logged in since the session id
                     * doesn't change. The following method creates a new session and substitutes
                     * it.
                     */
                    changeSession(request);
                } else if (isSSL && (custId < 0) && !forceSSL)
                {
                    // We are using SSL but shouldn't be
                    redirectUrl = new StringBuffer();
                    redirectUrl.append("http://");
                    redirectUrl.append(request.getServerName());
                    // Insert the port if it is non standard
                    if (standardPort != null && !standardPort.equals("80"))
                    {
                        redirectUrl.append(":");
                        redirectUrl.append(standardPort);
                    }
                    redirectUrl.append(request.getRequestURI());
                } else
                {
                    // Don't need to do anything
                    return null;
                }

                // Get the parameters
                StringBuffer parms = new StringBuffer();
                Enumeration<String> en = request.getParameterNames();
                while (en.hasMoreElements())
                {
                    String paramName = en.nextElement();
                    String paramValue = request.getParameter(paramName);
                    if (parms.length() > 0)
                    {
                        parms.append("&");
                    } else
                    {
                        parms.append("?");
                    }
                    parms.append(paramName);
                    parms.append("=");
                    parms.append(paramValue);
                }

                // Append the parameters to the redirect url
                redirectUrl.append(parms);
                if (log.isDebugEnabled())
                {
                    log.debug("redirectUrl = " + redirectUrl);
                }
                return redirectUrl.toString();
            }

            return null;
        } catch (Exception e)
        {
            log.error(e);
            return null;
        }
    }

    /**
     * Method used to create a browser cookie when a customer first accesses the application. If the
     * cookie already exists then we retrieve the guest customer id from the cookie which will be
     * used to retrieve and cart items that the customer added to the cart on his last visit.
     * 
     * @param request
     * @param response
     * @param kkAppEng
     * @return Returns the Customer UUID
     * @throws KKException
     * @throws KKAppException
     */
    private String manageCookies(HttpServletRequest request, HttpServletResponse response,
            KKAppEng kkAppEng) throws KKException, KKAppException
    {
        if (!kkAppEng.isKkCookieEnabled())
        {
            return null;
        }

        /*
         * The current customer should at this point be a guest customer with a negative customer id
         */
        CustomerIf currentCustomer = kkAppEng.getCustomerMgr().getCurrentCustomer();
        if (currentCustomer == null)
        {
            log.warn("Current customer is set to null in the manageCookies method. This should never happen");
            return null;
        }

        /*
         * Get the customerUuid from the browser cookie. A new cookie is created if it doesn't exist
         */
        String customerUuid = getCustomerUuidFromBrowserCookie(request, response, kkAppEng);

        /*
         * Get the guestCustomerId from the KK database.
         */
        String guestCustomerIdStr = getKKCookie(customerUuid, GUEST_CUSTOMER_ID, kkAppEng);

        if (guestCustomerIdStr == null)
        {
            /*
             * If it doesn't exist, then we create it
             */
            setKKCookie(customerUuid, GUEST_CUSTOMER_ID, Integer.toString(currentCustomer.getId()),
                    kkAppEng);

        } else
        {
            /*
             * Set the current customer id with the one retrieved from the cookie and fetch any cart
             * items that he may have.
             */
            currentCustomer.setId(Integer.parseInt(guestCustomerIdStr));
            kkAppEng.getBasketMgr().getBasketItemsPerCustomer();
            if (kkAppEng.getWishListMgr().allowWishListWhenNotLoggedIn())
            {
                kkAppEng.getWishListMgr().fetchCustomersWishLists();
            }

            // Get the product page size
            String prodPageSizeStr = getKKCookie(customerUuid, TAG_PROD_PAGE_SIZE, kkAppEng);
            if (prodPageSizeStr != null && prodPageSizeStr.length() > 0)
            {
                try
                {
                    int prodPageSize = Integer.parseInt(prodPageSizeStr);
                    kkAppEng.getProductMgr().setMaxDisplaySearchResults(prodPageSize);
                } catch (NumberFormatException e)
                {
                    log.warn("The product page size value stored in the cookie for customer with guest id "
                            + guestCustomerIdStr + " is not a numeric value: " + prodPageSizeStr);
                }
            }

            // Get the order page size
            String orderPageSizeStr = getKKCookie(customerUuid, TAG_ORDER_PAGE_SIZE, kkAppEng);
            if (orderPageSizeStr != null && orderPageSizeStr.length() > 0)
            {
                try
                {
                    int orderPageSize = Integer.parseInt(orderPageSizeStr);
                    kkAppEng.getOrderMgr().setPageSize(orderPageSize);
                } catch (NumberFormatException e)
                {
                    log.warn("The order page size value stored in the cookie for customer with guest id "
                            + guestCustomerIdStr + " is not a numeric value: " + orderPageSizeStr);
                }
            }

            // Get the review page size
            String reviewPageSizeStr = getKKCookie(customerUuid, TAG_REVIEW_PAGE_SIZE, kkAppEng);
            if (reviewPageSizeStr != null && reviewPageSizeStr.length() > 0)
            {
                try
                {
                    int reviewPageSize = Integer.parseInt(reviewPageSizeStr);
                    kkAppEng.getReviewMgr().setPageSize(reviewPageSize);
                } catch (NumberFormatException e)
                {
                    log.warn("The review page size value stored in the cookie for customer with guest id "
                            + guestCustomerIdStr + " is not a numeric value: " + reviewPageSizeStr);
                }
            }

            // Figure out whether the customer has agreed to cookies
            String agreedCookies = getKKCookie(customerUuid, AGREED_COOKIES, kkAppEng);
            if (agreedCookies != null)
            {
                kkAppEng.setAgreedCookies(true);
            }
        }

        if (log.isDebugEnabled())
        {
            log.debug("GUEST_CUSTOMER_ID cookie value = "
                    + getKKCookie(customerUuid, GUEST_CUSTOMER_ID, kkAppEng));
            log.debug("CUSTOMER_NAME cookie value = "
                    + getKKCookie(customerUuid, CUSTOMER_NAME, kkAppEng));
            log.debug("AGREED_COOKIES cookie value = "
                    + getKKCookie(customerUuid, AGREED_COOKIES, kkAppEng));
            log.debug("CUSTOMER_LOCALE cookie value = "
                    + getKKCookie(customerUuid, CUSTOMER_LOCALE, kkAppEng));
            log.debug("PROD_PAGE_SIZE cookie value = "
                    + getKKCookie(customerUuid, TAG_PROD_PAGE_SIZE, kkAppEng));
            log.debug("ORDER_PAGE_SIZE cookie value = "
                    + getKKCookie(customerUuid, TAG_ORDER_PAGE_SIZE, kkAppEng));
            log.debug("REVIEW_PAGE_SIZE cookie value = "
                    + getKKCookie(customerUuid, TAG_REVIEW_PAGE_SIZE, kkAppEng));
        }

        /*
         * Call class where you can place custom code
         */
        CustomCookieAction cca = new CustomCookieAction();
        cca.manageCookiesOnEntry(request, response, kkAppEng);

        return customerUuid;

    }

    /**
     * Utility method that can be used to set a KKCookie. It attempts to get the UUID from the
     * browser cookie and creates a new browser cookie if it doesn't find one.
     * 
     * @param attrId
     * @param attrValue
     * @param request
     * @param response
     * @param kkAppEng
     * @throws KKException
     */
    protected void setKKCookie(String attrId, String attrValue, HttpServletRequest request,
            HttpServletResponse response, KKAppEng kkAppEng) throws KKException
    {
        /*
         * Get the CustomerUuid from the browser cookie and create the cookie if it doesn't exist.
         */
        String uuid = getCustomerUuidFromBrowserCookie(request, response, kkAppEng);

        /*
         * Now we can save the KKCookie
         */
        setKKCookie(uuid, attrId, attrValue, kkAppEng);

    }

    /**
     * Utility method to set a KKCookie when we have the customerUuid
     * 
     * @param customerUuid
     * @param attrId
     * @param attrValue
     * @param kkAppEng
     * @throws KKException
     */
    protected void setKKCookie(String customerUuid, String attrId, String attrValue,
            KKAppEng kkAppEng) throws KKException
    {
        if (customerUuid != null)
        {
            KKCookieIf kkCookie = new KKCookie();
            kkCookie.setCustomerUuid(customerUuid);
            kkCookie.setAttributeId(attrId);
            kkCookie.setAttributeValue(attrValue);
            kkAppEng.getEng().setCookie(kkCookie);
        }
    }

    /**
     * Utility method to read a KKCookie. It attempts to get the UUID from the browser cookie and
     * creates a new browser cookie if it doesn't find one.
     * 
     * @param attrId
     * @param request
     * @param response
     * @param kkAppEng
     * @return the value of the cookie
     * @throws KKException
     */
    protected String getKKCookie(String attrId, HttpServletRequest request,
            HttpServletResponse response, KKAppEng kkAppEng) throws KKException
    {
        /*
         * Get the CustomerUuid from the browser cookie and create the cookie if it doesn't exist.
         */
        String uuid = getCustomerUuidFromBrowserCookie(request, response, kkAppEng);

        /*
         * Now get the KKCookie
         */
        return getKKCookie(uuid, attrId, kkAppEng);
    }

    /**
     * Utility method to read a KKCookie when we have the CustomerUuid
     * 
     * @param customerUuid
     * @param attrId
     * @param kkAppEng
     * @return the value of the cookie
     * @throws KKException
     */
    protected String getKKCookie(String customerUuid, String attrId, KKAppEng kkAppEng)
            throws KKException
    {
        if (customerUuid == null)
        {
            return null;
        }
        KKCookieIf kkCookie = kkAppEng.getEng().getCookie(customerUuid, attrId);
        if (kkCookie != null)
        {
            return kkCookie.getAttributeValue();
        }
        return null;
    }

    /**
     * Utility method to get the CustomerUuid from the browser cookie and create the cookie if it
     * doesn't exist.
     * 
     * @param request
     * @param response
     * @param kkAppEng
     * @return Returns the CustomerUuid
     */
    private String getCustomerUuidFromBrowserCookie(HttpServletRequest request,
            HttpServletResponse response, KKAppEng kkAppEng)
    {
        Cookie[] cookies = null;
        if (kkAppEng.isPortlet())
        {
            cookies = PortletActionContext.getRenderRequest().getCookies();
        } else
        {
            cookies = request.getCookies();
        }

        /*
         * Try to find the cookie we are looking for
         */
        String uuid = null;
        if (cookies != null)
        {
            for (int i = 0; i < cookies.length; i++)
            {
                Cookie cookie = cookies[i];
                String cookieName = cookie.getName();
                if (cookieName.equals(CUSTOMER_UUID))
                {
                    /*
                     * If we find the cookie we get the value and update the max age.
                     */
                    uuid = cookie.getValue();
                    cookie.setMaxAge(COOKIE_MAX_AGE_IN_SECS);
                    cookie.setPath("/");
                    if (kkAppEng.isPortlet())
                    {
                        PortletActionContext.getRenderResponse().addProperty(cookie);
                    } else
                    {
                        response.addCookie(cookie);
                    }
                }
            }
        }

        /*
         * If the browser cookie doesn't exist then we have to create it and store a newly created
         * UUID string
         */
        if (uuid == null)
        {
            UUID uuidObject = UUID.randomUUID();
            uuid = uuidObject.toString();
            /*
             * Create a browser cookie with the UUID
             */
            Cookie uuidCookie = new Cookie(CUSTOMER_UUID, uuid);
            uuidCookie.setMaxAge(COOKIE_MAX_AGE_IN_SECS);
            uuidCookie.setPath("/");

            // Not available in gwt_dev.jar or early versions of servlet-api.jar
            try
            {
                // set HTTP Only if we can
                uuidCookie.setHttpOnly(true);
            } catch (java.lang.NoSuchMethodError nsme)
            {
                // Older servlet jar
            }

            if (kkAppEng.isPortlet())
            {
                PortletActionContext.getRenderResponse().addProperty(uuidCookie);
            } else
            {
                response.addCookie(uuidCookie);
            }
        }

        return uuid;
    }

    /**
     * When we log out, ensure that the new guest customer that is created has the id saved in the
     * browser cookie.
     * 
     * @param request
     * @param response
     * @param kkAppEng
     * @throws KKException
     * @throws KKAppException
     */
    protected void manageCookieLogout(HttpServletRequest request, HttpServletResponse response,
            KKAppEng kkAppEng) throws KKException, KKAppException
    {
        if (!kkAppEng.isKkCookieEnabled())
        {
            return;
        }

        CustomerIf currentCustomer = kkAppEng.getCustomerMgr().getCurrentCustomer();
        if (currentCustomer != null)
        {
            String guestCustomerIdStr = getKKCookie(GUEST_CUSTOMER_ID, request, response, kkAppEng);
            // Only get the basket items if we can retrieve a temporary customer from the cookie
            if (guestCustomerIdStr != null)
            {
                try
                {
                    currentCustomer.setId(Integer.parseInt(guestCustomerIdStr));
                    kkAppEng.getBasketMgr().getBasketItemsPerCustomer();
                } catch (NumberFormatException e)
                {

                }
            }
        }

        /*
         * Call class where you can place custom code
         */
        CustomCookieAction cca = new CustomCookieAction();
        cca.manageCookiesAfterLogout(request, response, kkAppEng);
    }

    /**
     * Returns a customer event object with the action and customer id attributes populated. If
     * events aren't enabled, then null is returned.
     * 
     * @param kkAppEng
     *            App eng instance
     * @param action
     *            Event action
     * @return Returns a customer event object or null if events aren't enabled
     */
    protected CustomerEventIf getCustomerEvent(KKAppEng kkAppEng, int action)
    {
        String enabled = kkAppEng.getConfig(ConfigConstants.ENABLE_CUSTOMER_EVENTS);
        if (enabled != null && enabled.equalsIgnoreCase("true"))
        {
            CustomerEventIf event = new CustomerEvent();
            event.setAction(action);
            CustomerIf currentCust = kkAppEng.getCustomerMgr().getCurrentCustomer();
            if (currentCust != null)
            {
                event.setCustomerId(currentCust.getId());
            }
            return event;
        }
        return null;
    }

    /**
     * Inserts a customer event where all of the available parameters are passed
     * 
     * @param kkAppEng
     *            App eng instance
     * @param action
     *            Event action
     * @param str1
     * @param str2
     * @param int1
     * @param int2
     * @param dec1
     * @param dec2
     * @throws KKException
     */
    protected void insertCustomerEvent(KKAppEng kkAppEng, int action, String str1, String str2,
            int int1, int int2, BigDecimal dec1, BigDecimal dec2) throws KKException
    {
        CustomerEventIf event = getCustomerEvent(kkAppEng, action);
        if (event != null)
        {
            event.setData1Str(str1);
            event.setData2Str(str2);
            event.setData1Int(int1);
            event.setData2Int(int2);
            event.setData1Dec(dec1);
            event.setData2Dec(dec2);
            kkAppEng.getEng().insertCustomerEvent(event);
        }
    }

    /**
     * Shortcut method for inserting a customer event passing no custom event data
     * 
     * @param kkAppEng
     * @param action
     * @throws KKException
     */
    protected void insertCustomerEvent(KKAppEng kkAppEng, int action) throws KKException
    {
        insertCustomerEvent(kkAppEng, action, null, null, 0, 0, null, null);
    }

    /**
     * Shortcut method for inserting a customer event passing an integer as event data
     * 
     * @param kkAppEng
     * @param action
     * @param int1
     * @throws KKException
     */
    protected void insertCustomerEvent(KKAppEng kkAppEng, int action, int int1) throws KKException
    {
        insertCustomerEvent(kkAppEng, action, null, null, int1, 0, null, null);
    }

    /**
     * Shortcut method for inserting a customer event passing a string as event data
     * 
     * @param kkAppEng
     * @param action
     * @param str1
     * @throws KKException
     */
    protected void insertCustomerEvent(KKAppEng kkAppEng, int action, String str1)
            throws KKException
    {
        insertCustomerEvent(kkAppEng, action, str1, null, 0, 0, null, null);
    }

    /**
     * Shortcut method for inserting a customer event passing a decimal as event data
     * 
     * @param kkAppEng
     * @param action
     * @param dec1
     * @throws KKException
     */
    protected void insertCustomerEvent(KKAppEng kkAppEng, int action, BigDecimal dec1)
            throws KKException
    {
        insertCustomerEvent(kkAppEng, action, null, null, 0, 0, dec1, null);
    }

    /**
     * Method called when a customer logs in or logs out. When logging in we need to decide whether
     * to update the customer's PRODUCTS_VIEWED tag value from the value of the guest customer's
     * tag. When logging out we need to make the same decision in the opposite direction. We only do
     * the updates if the tag value of the "oldTag" is more recent than the tag value of the
     * "newTag".
     * 
     * @param oldTag
     *            When logging in, it is the tag of the guest customer. When logging out, it is the
     *            tag of the logged in customer.
     * @param newTag
     *            When logging in, it is the tag of the logged in customer. When logging out, it is
     *            the tag of the guest customer.
     * @throws KKException
     * @throws KKAppException
     */
    protected void updateRecentlyViewedProducts(KKAppEng kkAppEng, CustomerTagIf oldTag,
            CustomerTagIf newTag) throws KKAppException, KKException
    {
        if (oldTag != null && oldTag.getDateAdded() != null && oldTag.getValue() != null
                && oldTag.getValue().length() > 0)
        {
            if (newTag == null || newTag.getDateAdded() == null
                    || newTag.getDateAdded().before(oldTag.getDateAdded()))
            {
                /*
                 * If new tag doesn't exist or old tag is newer than new tag, then give newTag the
                 * value of old tag
                 */
                kkAppEng.getCustomerTagMgr().insertCustomerTag(TAG_PRODUCTS_VIEWED,
                        oldTag.getValue());
            }
        }
    }

    /**
     * Method changes the session before performing the login in order to avoid a Session Fixation
     * attack.
     * 
     * @param kkAppEng
     * @param request
     * @param emailAddr
     * @param password
     * @return Return the session id
     * @throws KKException
     * @throws KKAppException
     */
    protected String login(KKAppEng kkAppEng, HttpServletRequest request, String emailAddr,
            String password) throws KKException, KKAppException
    {
        if (!kkAppEng.isPortlet())
        {
            // Change the session
            changeSession(request);

            // Set this session to null to avoid struts interceptors from throwing an exception
            // because
            // the session is invalid
            ActionContext context = ActionContext.getContext();
            context.setSession(null);
        }

        // Login and return the new session
        String sessionId = kkAppEng.getCustomerMgr().login(emailAddr, password);
        return sessionId;
    }

    /**
     * Creates a new session when we switch to SSL to avoid hackers using the current session id
     * (which may have been visible on the URL) to log into the application
     * 
     * @param request
     *            HttpServletRequest
     */

    private void changeSession(HttpServletRequest request)
    {
        /* Used to temporarily store objects from current session */
        HashMap<String, Object> currentSessionMap = new HashMap<String, Object>();

        /* Loop through all objects saved in the current session and place them in the hash map */
        HttpSession currentSession = request.getSession();

        Enumeration<String> atrNameEnum = currentSession.getAttributeNames();
        while (atrNameEnum.hasMoreElements())
        {
            String attrName = atrNameEnum.nextElement();
            currentSessionMap.put(attrName, currentSession.getAttribute(attrName));
        }

        /* Invalidate the current session */
        currentSession.invalidate();
        currentSession = null;

        /* Create a new session */
        HttpSession newSession = request.getSession(true);

        /* Load the new session with objects saved in the hash map */
        Set<String> atrNameSet = currentSessionMap.keySet();
        for (String attrName : atrNameSet)
        {
            newSession.setAttribute(attrName, currentSessionMap.get(attrName));
        }

        return;
    }

    /**
     * Common code for setting up the redirect response. A few headers are set to stop the browser
     * from caching the permanent redirect.
     * 
     * @param response
     */
    protected void setupResponseForSEORedirect(HttpServletResponse response)
    {
        response.setStatus(301);
        response.setHeader("Expires", "Wed, 11 Jan 1984 05:00:00 GMT");
        response.setHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Connection", "close");
    }

    /**
     * Common code for setting up the redirect response when switching from HTTP to HTTPS and vice
     * versa
     * 
     * @param response
     */
    protected void setupResponseForSSLRedirect(HttpServletResponse response, String redirForward)
    {
        response.setStatus(302);
        response.setHeader("Location", redirForward);
    }

    /**
     * A URL encoder which converts spaces to "-" and doesn't encode forward or back slashes since
     * many app servers don't accept these because of security issues.
     * 
     * @param in
     * @return Returns an encoded string
     * @throws UnsupportedEncodingException
     */
    protected String kkURLEncode(String in) throws UnsupportedEncodingException
    {
        if (in == null)
        {
            return "";
        }
        String inp = in.replace(" ", "-");
        String encoded = URLEncoder.encode(inp, "UTF-8");

        // Remove encoded forward slash
        encoded = encoded.replace("%2F", "-");

        // Remove encoded backwards slash
        encoded = encoded.replace("%5C", "-");

        return encoded;
    }

    /**
     * Required for implementing ServletRequestAware interface
     */
    public void setServletRequest(HttpServletRequest arg0)
    {

    }

    /**
     * Required for implementing SessionAware interface
     */
    public void setSession(Map<String, Object> session)
    {
        this.strutsSession = session;
    }

    /**
     * Utility method called from JSPs to return an empty string when the value of the input string
     * is null.
     * 
     * @param in
     * @return Returns an empty string if the input string is null
     */
    public String noNull(String in)
    {
        return (in == null) ? "" : in;
    }

    /**
     * Returns the base redirect URL used in AJAX calls
     * 
     * @param request
     *            The HttpServletRequest
     * 
     * @return Returns the base redirect URL used in AJAX calls
     */
    public String getRedirectURL(HttpServletRequest request)
    {
        String redirectURL = request.getRequestURI().toString();
        String servletPath = request.getServletPath();
        redirectURL = redirectURL.substring(0, redirectURL.length() - servletPath.length());
        return redirectURL;
    }

    /**
     * @return the jspEng
     */
    public KKAppEng getJspEng()
    {
        return jspEng;
    }

    /**
     * @param jspEng
     *            the jspEng to set
     */
    public void setJspEng(KKAppEng jspEng)
    {
        this.jspEng = jspEng;
    }

    /**
     * Common method used to strip unwanted characters from form input strings
     * 
     * @param inputStr
     * @return Returns the cleaned string
     */
    protected String escapeFormInput(String inputStr)
    {
        return StringEscapeUtils.escapeHtml4(inputStr);
    }

    // /**
    // * The following method provides an example of how you can get user information from Liferay.
    // * You can use this information to determine whether the customer has already registered with
    // * KonaKart or to register the customer using the KonaKart APIs.
    // *
    // * @param kkAppEng
    // * @throws KKAppException
    // */
    // private void liferaySSO(KKAppEng kkAppEng) throws KKAppException
    // {
    // try
    // {
    // RenderRequest renderReq = PortletActionContext.getRenderRequest();
    // ThemeDisplay themeDisplay = (ThemeDisplay) renderReq
    // .getAttribute(WebKeys.THEME_DISPLAY);
    // User user = themeDisplay.getUser();
    // List<Address> addresses = user.getAddresses();
    //
    // if (user != null && addresses != null && addresses.size() > 0
    // && addresses.get(0) != null)
    // {
    // // Determine whether the customer is already registered
    // boolean doesExist = kkAppEng.getEng().doesCustomerExistForEmail(
    // user.getEmailAddress());
    // if (doesExist)
    // {
    // // Add code here
    // } else
    // {
    // // Add code here
    // }
    // }
    // } catch (Exception e)
    // {
    // throw new KKAppException(e);
    // }
    // }

}
