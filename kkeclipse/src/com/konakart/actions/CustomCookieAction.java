package com.konakart.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.konakart.al.KKAppEng;
import com.konakart.app.KKException;

/**
 * Used to implement custom cookie code
 * 
 */
public class CustomCookieAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    /**
     * Called from within BaseAction.java when a customer first accesses the application.
     * 
     * @param request
     * @param response
     * @param kkAppEng
     * @throws KKException
     */
    public void manageCookiesOnEntry(HttpServletRequest request, HttpServletResponse response,
            KKAppEng kkAppEng) throws KKException
    {

    }

    /**
     * Called from LoginSubmitAction.java after a login
     * 
     * @param request
     * @param response
     * @param kkAppEng
     * @throws KKException
     */
    public void manageCookiesAfterLogin(HttpServletRequest request, HttpServletResponse response,
            KKAppEng kkAppEng) throws KKException
    {

    }

    /**
     * Called from LogoutAction.java after a logout.
     * 
     * @param request
     * @param response
     * @param kkAppEng
     * @throws KKException
     */
    public void manageCookiesAfterLogout(HttpServletRequest request, HttpServletResponse response,
            KKAppEng kkAppEng) throws KKException
    {

    }

}
