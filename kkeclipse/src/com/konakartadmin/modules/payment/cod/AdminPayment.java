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

package com.konakartadmin.modules.payment.cod;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.TorqueException;

import com.konakart.app.NameValue;
import com.konakart.app.PaymentOptions;
import com.konakartadmin.appif.KKAdminIf;
import com.konakartadmin.modules.payment.AdminBasePayment;
import com.konakartadmin.modules.payment.AdminPaymentIf;
import com.workingdogs.village.DataSetException;

/**
 * Class used to communicate with the payment gateway from the admin app. This implementation is
 * merely a dummy used for testing the interface.
 */
public class AdminPayment extends AdminBasePayment implements AdminPaymentIf
{
    /** the log */
    protected static Log log = LogFactory.getLog(AdminPayment.class);

    /**
     * Constructor
     * 
     * @param eng
     * @throws Exception
     * @throws DataSetException
     * @throws TorqueException
     */
    public AdminPayment(KKAdminIf eng) throws TorqueException, DataSetException, Exception
    {
        super.init(eng);
    }

    /**
     * This method executes the transaction with the payment gateway.
     * 
     * @param options
     * @return Returns an array of NameValue objects that may contain any return information
     *         considered useful by the caller.
     * @throws Exception
     */
    public NameValue[] execute(PaymentOptions options) throws Exception
    {
        checkRequired(options, "PaymentOptions", "options");

        /*
         * Dump the options for debug purposes
         */
        if (log.isDebugEnabled())
        {
            log.debug(options.toString());
        }

        /*
         * We simply create NameValue pairs of the input parameters and return those
         */
        List<NameValue> nameVs = new ArrayList<NameValue>();

        NameValue nv = new NameValue("action", options.getAction());
        nameVs.add(nv);

        nv = new NameValue("orderId", options.getOrderId());
        nameVs.add(nv);

        for (int i = 0; i < options.getParameters().length; i++)
        {
            NameValue nvp = options.getParameters()[i];
            nameVs.add(nvp);
        }

        if (options.getCreditCard() != null)
        {
            nv = new NameValue("CC number", options.getCreditCard().getCcNumber());
            nameVs.add(nv);
            nv = new NameValue("CC owner", options.getCreditCard().getCcOwner());
            nameVs.add(nv);
        }

        return nameVs.toArray(new NameValue[0]);
    }
}
