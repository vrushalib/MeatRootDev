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

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Set;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.Torque;

import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;
import com.konakart.bl.modules.ordertotal.thomson.Thomson.StaticData;
import com.konakart.blif.MultiStoreMgrIf;
import com.konakart.db.KKBasePeer;
import com.konakart.db.KKCriteria;
import com.konakart.om.BaseIpnHistoryPeer;
import com.konakart.util.ExceptionUtils;

/**
 * A handler to log messages in a friendly format
 */
public class HeaderLoggingHandler implements SOAPHandler<SOAPMessageContext>
{
    private KKEngIf eng = null;

    private MultiStoreMgrIf multiStoreMgr = null;

    private StaticData sd = null;

    private OrderIf order = null;

    private String txType = null;

    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(HeaderLoggingHandler.class);

    public boolean handleMessage(SOAPMessageContext smc)
    {
        Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        logSoapMsg(smc);
        return outboundProperty;
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public Set getHeaders()
    {
        // Not implemented
        return null;
    }

    public boolean handleFault(SOAPMessageContext context)
    {
        logSoapMsg(context);
        return true;
    }

    /**
     * Outputs the soap msg to the logger
     * 
     * @param context
     */
    public void logSoapMsg(SOAPMessageContext context)
    {
        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        String msgType = null;
        if (outboundProperty.booleanValue())
        {
            msgType = "Request";
        } else
        {
            msgType = "Response";
        }

        SOAPMessage message = context.getMessage();
        try
        {
            TransformerFactory tff = TransformerFactory.newInstance();
            Transformer tf = tff.newTransformer();

            // Set formatting

            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            Source sc = message.getSOAPPart().getContent();

            ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(streamOut);
            tf.transform(sc, result);

            if (log.isDebugEnabled())
            {
                log.debug(msgType
                        + ":\n"
                        + streamOut.toString()
                        + "\nOrderId          : "
                        + getOrder().getId()
                        + "\nOrderLifeCycleId : "
                        + getOrder().getLifecycleId()
                        + "\nOrder Status     : "
                        + getOrder().getStatus()
                        + " = "
                        + getOrder().getStatusText()
                        + "\n------------------------------------------------------------------------");
            }

            if (sd.isSaveMsgDb())
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Save " + msgType + " for order " + getOrder().getId() + "TxId "
                            + getOrder().getLifecycleId());
                }

                // if (getOrder().getId() > 0)
                // {
                // Save the message on an Order Status History record

                try
                {
                    KKCriteria insertC = getNewCriteria(false);

                    // Insert the ipn_history record

                    insertC.addForInsert(BaseIpnHistoryPeer.GATEWAY_FULL_RESPONSE,
                            streamOut.toString());
                    insertC.addForInsert(BaseIpnHistoryPeer.GATEWAY_TRANSACTION_ID, getOrder()
                            .getLifecycleId());
                    insertC.addForInsert(BaseIpnHistoryPeer.KONAKART_RESULT_DESCRIPTION, "Tax "
                            + msgType);
                    insertC.add(BaseIpnHistoryPeer.MODULE_CODE, Thomson.code);
                    insertC.add(BaseIpnHistoryPeer.ORDER_ID, getOrder().getId());
                    insertC.add(BaseIpnHistoryPeer.CUSTOMERS_ID, getOrder().getCustomerId());
                    insertC.add(BaseIpnHistoryPeer.DATE_ADDED, new Date());
                    insertC.addForInsert(BaseIpnHistoryPeer.TRANSACTION_AMOUNT, getOrder()
                            .getTotalIncTax());
                    insertC.addForInsert(BaseIpnHistoryPeer.TRANSACTION_TYPE, getTxType());

                    KKBasePeer.doInsert(insertC);
                } catch (Exception e)
                {
                    log.warn("Problem saving Tax History record : "
                            + ExceptionUtils.exceptionToString(e));
                    throw e;
                }
                // }
            } else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Do not save " + msgType + " for order " + getOrder().getId()
                            + "TxId " + getOrder().getLifecycleId());
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    protected KKCriteria getNewCriteria(boolean allStores)
    {
        MultiStoreMgrIf mgr = getMultiStoreMgr();
        if (mgr != null)
        {
            return mgr.getNewCriteria(allStores);
        }

        log.info("Create new Criteria for the Default DB called : " + Torque.getDefaultDB());

        KKCriteria crit = new KKCriteria();
        return crit;
    }

    public void close(MessageContext context)
    {
        // Not implemented
    }

    /**
     * @return the eng
     */
    public KKEngIf getEng()
    {
        return eng;
    }

    /**
     * @param eng
     *            the eng to set
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
     * @param order
     *            the order to set
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
     * @param txType
     *            the txType to set
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
     * @param multiStoreMgr
     *            the multiStoreMgr to set
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
     * @param sd
     *            the sd to set
     */
    public void setSd(StaticData sd)
    {
        this.sd = sd;
    }
}
