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
//

package com.konakart.bl.modules.ordertotal.thomson;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;
import com.konakart.bl.modules.ordertotal.thomson.Thomson.StaticData;
import com.konakart.blif.MultiStoreMgrIf;

/**
 * Handler Resolver
 */
public class HeaderHandlerResolver implements HandlerResolver
{
    private KKEngIf eng = null;

    private MultiStoreMgrIf multiStoreMgr = null;

    private StaticData sd = null;
    
    private OrderIf order = null;
    
    private String txType = null;
    
    private String uName = "not-set-yet";

    private String pWord = "not-set-yet";

    private boolean secure = false;

    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(HeaderHandlerResolver.class);

    /**
     * @return the secure
     */
    public boolean isSecure()
    {
        return secure;
    }

    /**
     * @param secure
     *            the secure to set
     */
    public void setSecure(boolean secure)
    {
        this.secure = secure;
    }

    /**
     * @param username
     */
    public void setUName(String username)
    {
        uName = username;
    }

    /**
     * @param password
     */
    public void setPWord(String password)
    {
        pWord = password;
    }

    /**
     * @return the uName
     */
    public String getUName()
    {
        return uName;
    }

    /**
     * @return the pWord
     */
    public String getPWord()
    {
        return pWord;
    }

    @SuppressWarnings("rawtypes")
    public List<Handler> getHandlerChain(PortInfo portInfo)
    {
        List<Handler> handlerChain = new ArrayList<Handler>();

        if (isSecure())
        {
            HeaderSecrityHandler hsh = new HeaderSecrityHandler();
            hsh.setUName(getUName());
            hsh.setPWord(getPWord());

            handlerChain.add(hsh);
        }

        HeaderLoggingHandler hlh = new HeaderLoggingHandler();
        hlh.setEng(eng);
        hlh.setOrder(order);
        hlh.setTxType(txType);
        hlh.setMultiStoreMgr(multiStoreMgr);
        hlh.setSd(sd);

        handlerChain.add(hlh);

        return handlerChain;
    }

    /**
     * @return the eng
     */
    public KKEngIf getEng()
    {
        return eng;
    }

    /**
     * @param eng the eng to set
     */
    public void setEng(KKEngIf eng)
    {
        this.eng = eng;
    }

    /**
     * @return the order
     */
    public OrderIf getOrder()
    {
        return order;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(OrderIf order)
    {
        this.order = order;
    }

    /**
     * @return the txType
     */
    public String getTxType()
    {
        return txType;
    }

    /**
     * @param txType the txType to set
     */
    public void setTxType(String txType)
    {
        this.txType = txType;
    }

    /**
     * @return the multiStoreMgr
     */
    public MultiStoreMgrIf getMultiStoreMgr()
    {
        return multiStoreMgr;
    }

    /**
     * @param multiStoreMgr the multiStoreMgr to set
     */
    public void setMultiStoreMgr(MultiStoreMgrIf multiStoreMgr)
    {
        this.multiStoreMgr = multiStoreMgr;
    }

    /**
     * @return the sd
     */
    public StaticData getSd()
    {
        return sd;
    }

    /**
     * @param sd the sd to set
     */
    public void setSd(StaticData sd)
    {
        this.sd = sd;
    }
}