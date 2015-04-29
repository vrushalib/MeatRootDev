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

package com.konakartadmin.modules.payment.authorizenet;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.TorqueException;

import com.anet.api.ARBAPI;
import com.anet.api.ARBCreditCard;
import com.anet.api.ARBCustomer;
import com.anet.api.ARBMessage;
import com.anet.api.ARBNameAndAddress;
import com.anet.api.ARBOrder;
import com.anet.api.ARBPayment;
import com.anet.api.ARBPaymentSchedule;
import com.anet.api.ARBSubscription;
import com.anet.api.util.BasicXmlDocument;
import com.konakart.app.NameValue;
import com.konakart.app.PaymentOptions;
import com.konakart.appif.CreditCardIf;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.util.KKConstants;
import com.konakart.util.PrettyXmlPrinter;
import com.konakartadmin.app.AdminCountry;
import com.konakartadmin.app.AdminCurrency;
import com.konakartadmin.app.AdminDataDescriptor;
import com.konakartadmin.app.AdminIpnHistories;
import com.konakartadmin.app.AdminIpnHistory;
import com.konakartadmin.app.AdminIpnSearch;
import com.konakartadmin.app.AdminOrder;
import com.konakartadmin.app.AdminOrderRefund;
import com.konakartadmin.app.AdminOrderRefundSearch;
import com.konakartadmin.app.AdminOrderRefundSearchResult;
import com.konakartadmin.app.AdminOrderStatusHistory;
import com.konakartadmin.app.AdminOrderTotal;
import com.konakartadmin.app.AdminOrderUpdate;
import com.konakartadmin.app.AdminPaymentSchedule;
import com.konakartadmin.app.AdminSubscription;
import com.konakartadmin.app.KKAdminException;
import com.konakartadmin.appif.KKAdminIf;
import com.konakartadmin.modules.payment.AdminBasePayment;
import com.konakartadmin.modules.payment.AdminPaymentIf;
import com.workingdogs.village.DataSetException;

/**
 * Class used to communicate with the payment gateway from the admin app
 */
public class AdminPayment extends AdminBasePayment implements AdminPaymentIf
{
    /** the log */
    protected static Log log = LogFactory.getLog(AdminPayment.class);

    private static String code = "authorizenet";

    // Configuration Keys

    /**
     * The Authorize.Net zone, if greater than zero, should reference a GeoZone. If the
     * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_ZONE = "MODULE_PAYMENT_AUTHORIZENET_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_SORT_ORDER = "MODULE_PAYMENT_AUTHORIZENET_SORT_ORDER";

    /**
     * The Authorize.Net Url used to POST the payment request.
     * "https://secure.authorize.net/gateway/transact.dll"
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_REQUEST_URL = "MODULE_PAYMENT_AUTHORIZENET_REQUEST_URL";

    /**
     * The Authorize.Net Url used to POST the Automated Recurring Billing request.
     * "https://apitest.authorize.net/xml/v1/request.api"
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_ARB_REQUEST_URL = "MODULE_PAYMENT_AUTHORIZENET_ARB_REQUEST_URL";

    /**
     * The Authorize.Net API Login ID for this installation
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_LOGIN = "MODULE_PAYMENT_AUTHORIZENET_LOGIN";

    /**
     * The Authorize.Net transaction key for this installation
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_TXNKEY = "MODULE_PAYMENT_AUTHORIZENET_TXNKEY";

    /**
     * Used to make test transactions
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_TESTMODE = "MODULE_PAYMENT_AUTHORIZENET_TESTMODE";

    /**
     * To show CVV field
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_SHOW_CVV = "MODULE_PAYMENT_AUTHORIZENET_SHOW_CVV";

    // Authorize.net constants for response
    private static final int respCodePosition = 1;

    private static final int txnIdPosition = 7;

    private static final int txnAmountPosition = 10;

    private static final int txnTypePosition = 12;

    private static final String approved = "1";

    private static final String declined = "2";

    private static final String error = "3";

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET1 = -1;

    private static final String RET1_DESC = "There was an unexpected Gateway Response.";

    private static final int RET2 = -2;

    private static final String RET2_DESC = "Transaction Error";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "Payment successful. TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "Payment not successful.";

    private static final String ORDER_HISTORY_REFUND_COMMENT_OK = "Refund successful. TransactionId = ";

    private static final String ORDER_HISTORY_REFUND_COMMENT_KO = "Refund not successful.";

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
     * This method executes the transaction with the payment gateway. The action attribute of the
     * options object instructs the method as to what transaction should be executed. E.g. It could
     * be a payment or a payment confirmation for a transaction that has already been authorized
     * etc.
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
         * Decide what to do based on the action. We could be using this class to manage
         * subscriptions
         */
        if (options.getAction() == KKConstants.ACTION_CREATE_SUBSCRIPTION)
        {
            return createSubscription(options);
        } else if (options.getAction() == KKConstants.ACTION_UPDATE_SUBSCRIPTION)
        {
            return updateSubscription(options);
        } else if (options.getAction() == KKConstants.ACTION_CANCEL_SUBSCRIPTION)
        {
            return cancelSubscription(options);
        } else if (options.getAction() == KKConstants.ACTION_GET_SUBSCRIPTION_STATUS)
        {
            return getSubscriptionStatus(options);
        } else if (options.getAction() == KKConstants.ACTION_REFUND)
        {
            return doRefund(options);
        }

        // Read the configuration variables
        ConfigVariables configs = getConfigVariables();

        String gatewayResult = null;
        String transactionId = null;

        AdminIpnHistory ipnHistory = new AdminIpnHistory();
        ipnHistory.setModuleCode(code);

        AdminOrder order = getAdminOrderMgr().getOrderForOrderId(options.getOrderId());
        if (order == null)
        {
            throw new KKAdminException("An order does not exist for id = " + options.getOrderId());
        }

        // Get the scale for currency calculations
        AdminCurrency currency = getAdminCurrMgr().getCurrency(order.getCurrencyCode());
        if (currency == null)
        {
            throw new KKAdminException("A currency does not exist for code = "
                    + order.getCurrencyCode());
        }
        int scale = new Integer(currency.getDecimalPlaces()).intValue();

        List<NameValue> parmList = new ArrayList<NameValue>();

        parmList.add(new NameValue("x_delim_data", "True"));
        parmList.add(new NameValue("x_relay_response", "False"));
        parmList.add(new NameValue("x_login", configs.getAuthorizeNetLoginId()));
        parmList.add(new NameValue("x_tran_key", configs.getAuthorizeNetTxnKey()));
        parmList.add(new NameValue("x_delim_char", ","));
        parmList.add(new NameValue("x_encap_char", ""));
        parmList.add(new NameValue("x_method", "CC"));

        // AuthorizeNet requires details of the final price - inclusive of tax.
        BigDecimal total = null;
        for (int i = 0; i < order.getOrderTotals().length; i++)
        {
            AdminOrderTotal ot = order.getOrderTotals()[i];
            if (ot.getClassName().equals(OrderTotalMgr.ot_total))
            {
                total = ot.getValue().setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
        }

        if (total == null)
        {
            throw new KKAdminException("An Order Total was not found");
        }

        parmList.add(new NameValue("x_amount", total.toString()));
        parmList.add(new NameValue("x_currency_code", currency.getCode()));
        parmList.add(new NameValue("x_invoice_num", order.getId())); // TODO
        parmList.add(new NameValue("x_test_request", (configs.isTestMode() ? "TRUE" : "FALSE")));

        // Set the billing address

        // Set the billing name. If the name field consists of more than two strings, we take the
        // last one as being the surname.
        String bName = order.getBillingName();
        if (bName != null)
        {
            String[] names = bName.split(" ");
            int len = names.length;
            if (len >= 2)
            {
                StringBuffer firstName = new StringBuffer();
                for (int i = 0; i < len - 1; i++)
                {
                    if (firstName.length() == 0)
                    {
                        firstName.append(names[i]);
                    } else
                    {
                        firstName.append(" ");
                        firstName.append(names[i]);
                    }
                }
                parmList.add(new NameValue("x_first_name", firstName.toString()));
                parmList.add(new NameValue("x_last_name", names[len - 1]));
            }
        }
        parmList.add(new NameValue("x_company", order.getBillingCompany()));
        parmList.add(new NameValue("x_city", order.getBillingCity()));
        parmList.add(new NameValue("x_state", order.getBillingState()));
        parmList.add(new NameValue("x_zip", order.getBillingPostcode()));
        parmList.add(new NameValue("x_phone", order.getCustomerTelephone()));
        parmList.add(new NameValue("x_cust_id", order.getCustomerId()));
        parmList.add(new NameValue("x_email", order.getCustomerEmail()));

        StringBuffer addrSB = new StringBuffer();
        addrSB.append(order.getBillingStreetAddress());
        if (order.getBillingSuburb() != null && order.getBillingSuburb().length() > 0)
        {
            addrSB.append(", ");
            addrSB.append(order.getBillingSuburb());
        }
        if (addrSB.length() > 60)
        {
            parmList.add(new NameValue("x_address", addrSB.substring(0, 56) + "..."));
        } else
        {
            parmList.add(new NameValue("x_address", addrSB.toString()));
        }

        // Country requires the two letter country code
        AdminCountry country = getAdminTaxMgr().getCountryByName(order.getBillingCountry());
        if (country != null)
        {
            parmList.add(new NameValue("x_country", country.getIsoCode2()));
        }

        /*
         * The following code may be customized depending on the process which could be any of the
         * following:
         */
        int mode = 1;

        if (mode == 1 && options.getCreditCard() != null)
        {
            /*
             * 1 . Credit card details have been passed into the method and so we use those for the
             * payment.
             */
            parmList.add(new NameValue("x_card_num", options.getCreditCard().getCcNumber()));
            parmList.add(new NameValue("x_card_code", options.getCreditCard().getCcCVV()));
            parmList.add(new NameValue("x_exp_date", options.getCreditCard().getCcExpires()));
            parmList.add(new NameValue("x_type", "AUTH_CAPTURE"));
        } else if (mode == 2)
        {
            /*
             * 2 . We get the Credit card details from the order since they were encrypted and saved
             * on the order.
             */
            parmList.add(new NameValue("x_card_num", order.getCcNumber()));
            parmList.add(new NameValue("x_card_code", order.getCcCVV()));
            parmList.add(new NameValue("x_exp_date", order.getCcExpires()));
            parmList.add(new NameValue("x_type", "AUTH_CAPTURE"));
        } else if (mode == 3)
        {
            /*
             * 3.If when the products were ordered through the store front application, an AUTH_ONLY
             * transaction was done instead of an AUTH_CAPTURE transaction and so now we need to do
             * a PRIOR_AUTH_CAPTURE transaction using the transaction id that was saved on the order
             * when the status was set.
             */
            /*
             * Get the transaction id from the status trail of the order. This bit of code will have
             * to be customized because it depends which state was used to capture the transaction
             * id and whether the transaction id is the only string in the comments field or whether
             * it has to be parsed out. The transaction id may also have been stored in an Order
             * custom field in which case it can be retrieved directly from there.
             */
            String authTransId = null;
            if (order.getStatusTrail() != null)
            {
                for (int i = 0; i < order.getStatusTrail().length; i++)
                {
                    AdminOrderStatusHistory sh = order.getStatusTrail()[i];
                    if (sh.getOrderStatus().equals(
                            "ENTER_THE_STATUS_WHERE_THE_AUTH_TRANS_ID_WAS SAVED"))
                    {
                        authTransId = sh.getComments();
                    }
                }
            }
            if (authTransId == null)
            {
                throw new KKAdminException(
                        "The authorized transaction cannot be confirmed since a transaction id cannot be found on the order");
            }

            parmList.add(new NameValue("x_type", "PRIOR_AUTH_CAPTURE"));
            parmList.add(new NameValue("x_trans_id", authTransId));
        }

        AdminPaymentDetails pDetails = new AdminPaymentDetails();
        pDetails.setParmList(parmList);
        pDetails.setRequestUrl(configs.getAuthorizeNetRequestUrl());

        // Do the post
        String gatewayResp = postData(pDetails);

        gatewayResp = URLDecoder.decode(gatewayResp, "UTF-8");
        if (log.isDebugEnabled())
        {
            log.debug("Unformatted GatewayResp = \n" + gatewayResp);
        }

        // Process the parameters sent in the callback
        StringBuffer sb = new StringBuffer();
        String[] parms = gatewayResp.split(",");
        if (parms != null)
        {
            for (int i = 0; i < parms.length; i++)
            {
                String parm = parms[i];
                sb.append(getRespDesc(i + 1));
                sb.append("=");
                sb.append(parm);
                sb.append("\n");

                if (i + 1 == respCodePosition)
                {
                    gatewayResult = parm;
                    ipnHistory.setGatewayResult(getGatewayResultDescription(parm));
                } else if (i + 1 == txnIdPosition)
                {
                    ipnHistory.setGatewayTransactionId(parm);
                    transactionId = parm;
                } else if (i + 1 == txnTypePosition)
                {
                    ipnHistory.setTxType(parm);
                } else if (i + 1 == txnAmountPosition)
                {
                    try
                    {
                        ipnHistory.setTxAmount(new BigDecimal(parm));
                    } catch (Exception e)
                    {
                    }
                }
            }
        }

        // Put the response in the ipnHistory record
        ipnHistory.setGatewayFullResponse(sb.toString());

        if (log.isDebugEnabled())
        {
            log.debug("Formatted Authorize.net response data:");
            log.debug(sb.toString());
        }

        AdminOrderUpdate updateOrder = new AdminOrderUpdate();
        updateOrder.setUpdatedById(order.getCustomerId());

        // Determine whether the request was successful or not.
        if (gatewayResult != null && gatewayResult.equals(approved))
        {
            String comment = ORDER_HISTORY_COMMENT_OK + transactionId;
            getAdminOrderMgr().updateOrder(order.getId(),
                    com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, comment, /* notifyCustomer */
                    false, updateOrder);

            // Save the ipnHistory
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
            ipnHistory.setOrderId(order.getId());
            ipnHistory.setCustomerId(order.getCustomerId());
            getAdminOrderMgr().insertIpnHistory(ipnHistory);

        } else if (gatewayResult != null
                && (gatewayResult.equals(declined) || gatewayResult.equals(error)))
        {
            String comment = ORDER_HISTORY_COMMENT_KO;
            getAdminOrderMgr().updateOrder(order.getId(),
                    com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, comment, /* notifyCustomer */
                    false, updateOrder);

            // Save the ipnHistory
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
            ipnHistory.setOrderId(order.getId());
            ipnHistory.setCustomerId(order.getCustomerId());
            getAdminOrderMgr().insertIpnHistory(ipnHistory);

        } else
        {
            /*
             * We only get to here if there was an unknown response from the gateway
             */
            String comment = RET1_DESC;
            getAdminOrderMgr().updateOrder(order.getId(),
                    com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, comment, /* notifyCustomer */
                    false, updateOrder);

            // Save the ipnHistory
            ipnHistory.setKonakartResultDescription(RET1_DESC + " - " + gatewayResult);
            ipnHistory.setKonakartResultId(RET1);
            ipnHistory.setOrderId(order.getId());
            ipnHistory.setCustomerId(order.getCustomerId());
            getAdminOrderMgr().insertIpnHistory(ipnHistory);
        }

        return new NameValue[]
        { new NameValue(KKConstants.RETURN_CODE, "0") };
    }

    /**
     * Get the configuration variables
     * 
     * @return Returns an object containing the values of the configuration variables
     * 
     * @throws Exception
     * @throws DataSetException
     * @throws TorqueException
     */
    public ConfigVariables getConfigVariables() throws TorqueException, DataSetException, Exception
    {
        ConfigVariables configs = new ConfigVariables();

        String confVal = getAdminConfigMgr().getConfigurationValue(
                MODULE_PAYMENT_AUTHORIZENET_REQUEST_URL);
        if (confVal == null)
        {
            throw new KKAdminException(
                    "The Configuration MODULE_PAYMENT_AUTHORIZENET_REQUEST_URL must be set to the URL for"
                            + " sending the request to Authorize.Net. (e.g. https://secure.authorize.net/gateway/transact.dll)");
        }
        configs.setAuthorizeNetRequestUrl(confVal);

        confVal = getAdminConfigMgr().getConfigurationValue(
                MODULE_PAYMENT_AUTHORIZENET_ARB_REQUEST_URL);
        configs.setAuthorizeNetARBRequestUrl(confVal);

        confVal = getAdminConfigMgr().getConfigurationValue(MODULE_PAYMENT_AUTHORIZENET_LOGIN);
        if (confVal == null)
        {
            throw new KKAdminException(
                    "The Configuration MODULE_PAYMENT_AUTHORIZENET_LOGIN must be set to the"
                            + " Authorize.Net API ID for this installation");
        }
        configs.setAuthorizeNetLoginId(confVal);

        confVal = getAdminConfigMgr().getConfigurationValue(MODULE_PAYMENT_AUTHORIZENET_TXNKEY);
        if (confVal == null)
        {
            throw new KKAdminException(
                    "The Configuration MODULE_PAYMENT_AUTHORIZENET_TXNKEY must be set to the"
                            + " Current Authorize.Net Transaction Key for this installation");
        }

        configs.setAuthorizeNetTxnKey(confVal);
        configs.setTestMode(getAdminConfigMgr().getConfigurationValueAsBool(
                MODULE_PAYMENT_AUTHORIZENET_TESTMODE, true));
        configs.setZone(getAdminConfigMgr().getConfigurationIntValueOrDefault(
                MODULE_PAYMENT_AUTHORIZENET_ZONE, 0));
        configs.setSortOrder(getAdminConfigMgr().getConfigurationIntValueOrDefault(
                MODULE_PAYMENT_AUTHORIZENET_SORT_ORDER, 0));
        configs.setShowCVV(getAdminConfigMgr().getConfigurationValueAsBool(
                MODULE_PAYMENT_AUTHORIZENET_SHOW_CVV, true));

        return configs;
    }

    /**
     * Authorize.net returns a response as delimiter separated variables. In order to make them
     * readable, we tag each one with a description before saving in the ipnHistory table.
     * 
     * @param position
     * @return Response Description
     */
    private String getRespDesc(int position)
    {
        String ret = "unknown";
        switch (position)
        {
        case 1:
            ret = "Response Code";
            break;
        case 2:
            ret = "Response Subcode";
            break;
        case 3:
            ret = "Response Reason Code";
            break;
        case 4:
            ret = "Response Reason Text";
            break;
        case 5:
            ret = "Approval Code";
            break;
        case 6:
            ret = "AVS Result Code";
            break;
        case 7:
            ret = "Transaction ID";
            break;
        case 8:
            ret = "Invoice Number";
            break;
        case 9:
            ret = "Description";
            break;
        case 10:
            ret = "Amount";
            break;
        case 11:
            ret = "Method";
            break;
        case 12:
            ret = "Transaction Type";
            break;
        case 13:
            ret = "Customer ID";
            break;
        case 14:
            ret = "Cardholder First Name";
            break;
        case 15:
            ret = "Cardholder Last Name";
            break;
        case 16:
            ret = "Company";
            break;
        case 17:
            ret = "Billing Address";
            break;
        case 18:
            ret = "City";
            break;
        case 19:
            ret = "State";
            break;
        case 20:
            ret = "Zip";
            break;
        case 21:
            ret = "Country";
            break;
        case 22:
            ret = "Phone";
            break;
        case 23:
            ret = "Fax";
            break;
        case 24:
            ret = "Email";
            break;
        case 25:
            ret = "Ship to First Name";
            break;
        case 26:
            ret = "Ship to Last Name";
            break;
        case 27:
            ret = "Ship to Company";
            break;
        case 28:
            ret = "Ship to Address";
            break;
        case 29:
            ret = "Ship to City";
            break;
        case 30:
            ret = "Ship to State";
            break;
        case 31:
            ret = "Ship to Zip";
            break;
        case 32:
            ret = "Ship to Country";
            break;
        case 33:
            ret = "Tax Amount";
            break;
        case 34:
            ret = "Duty Amount";
            break;
        case 35:
            ret = "Freight Amount";
            break;
        case 36:
            ret = "Tax Exempt Flag";
            break;
        case 37:
            ret = "PO Number";
            break;
        case 38:
            ret = "MD5 Hash";
            break;
        case 39:
            ret = "(CVV2/CVC2/CID)Response Code";
            break;
        case 40:
            ret = "(CAVV) Response Code";
            break;
        default:
            break;
        }

        return ret;
    }

    /**
     * Performs a credit operation to refund money to a customer
     * 
     * @param options
     *            PaymentOptions containing information necessary to carry out the transaction
     * @return Returns an array of NameValue objects
     * @throws Exception
     */
    protected NameValue[] doRefund(PaymentOptions options) throws Exception
    {
        if (options == null)
        {
            throw new KKAdminException(
                    "A null PaymentOptions object was passed to the doCredit() method");
        }

        // Get the refund object
        AdminOrderRefundSearch search = new AdminOrderRefundSearch();
        search.setId(options.getRefundId());
        AdminOrderRefundSearchResult refundResult = getAdminOrderMgr()
                .getOrderRefunds(search, 0, 1);
        if (refundResult == null || refundResult.getOrderRefundArray() == null
                || refundResult.getOrderRefundArray().length == 0)
        {
            throw new KKAdminException("A Refund object with id = " + options.getRefundId()
                    + " could not be found.");
        }

        AdminOrderRefund refund = refundResult.getOrderRefundArray()[0];

        /*
         * Last 4 digits of the card number are stored in the custom1 field of the IPN History so we
         * must get them to pass to the payment gateway for the credit operation
         */
        AdminDataDescriptor dd = new AdminDataDescriptor();
        dd.setOrderBy(AdminDataDescriptor.ORDER_BY_ORDER_ID_DESCENDING);
        AdminIpnSearch ipnSearch = new AdminIpnSearch();
        ipnSearch.setOrderId(options.getOrderId());
        AdminIpnHistories ipnResult = getAdminOrderMgr().searchForIpnHistory(dd, ipnSearch);
        if (ipnResult == null || ipnResult.getIpnHistoryArray() == null
                || ipnResult.getIpnHistoryArray().length == 0)
        {
            throw new KKAdminException(
                    "Could not find any payment gateway payment information for order id = "
                            + options.getOrderId());
        }

        String cardNum = null;
        for (int i = 0; i < ipnResult.getIpnHistoryArray().length; i++)
        {
            AdminIpnHistory ipn = ipnResult.getIpnHistoryArray()[i];
            if (ipn.getCustom1() != null && ipn.getCustom1().length() == 4)
            {
                cardNum = ipn.getCustom1();
            }
        }

        if (cardNum == null)
        {
            throw new KKAdminException(
                    "Could not find the last 4 digits of the card number from the custom1 field of the payment history from the gateway");
        }

        AdminOrder order = getAdminOrderMgr().getOrderForOrderId(options.getOrderId());
        if (order == null)
        {
            throw new KKAdminException("An order does not exist for id = " + options.getOrderId());
        }

        // Read the configuration variables
        ConfigVariables configs = getConfigVariables();

        String gatewayResult = null;
        String transactionId = null;

        AdminIpnHistory ipnHistory = new AdminIpnHistory();
        ipnHistory.setModuleCode(code);
        ipnHistory.setTxAmount(refund.getRefundAmount());

        // Get the scale for currency calculations
        AdminCurrency currency = getAdminCurrMgr().getCurrency(order.getCurrencyCode());
        if (currency == null)
        {
            throw new KKAdminException("A currency does not exist for code = "
                    + order.getCurrencyCode());
        }

        List<NameValue> parmList = new ArrayList<NameValue>();

        parmList.add(new NameValue("x_delim_data", "True"));
        parmList.add(new NameValue("x_relay_response", "False"));
        parmList.add(new NameValue("x_login", configs.getAuthorizeNetLoginId()));
        parmList.add(new NameValue("x_tran_key", configs.getAuthorizeNetTxnKey()));
        parmList.add(new NameValue("x_delim_char", ","));
        parmList.add(new NameValue("x_encap_char", ""));
        parmList.add(new NameValue("x_method", "CC"));
        parmList.add(new NameValue("x_type", "CREDIT"));
        parmList.add(new NameValue("x_trans_id", refund.getGatewayCreditId()));
        parmList.add(new NameValue("x_amount", refund.getRefundAmount().toString()));
        parmList.add(new NameValue("x_card_num", cardNum));
        parmList.add(new NameValue("x_currency_code", currency.getCode()));
        parmList.add(new NameValue("x_invoice_num", order.getId())); // TODO
        parmList.add(new NameValue("x_test_request", (configs.isTestMode() ? "TRUE" : "FALSE")));

        AdminPaymentDetails pDetails = new AdminPaymentDetails();
        pDetails.setParmList(parmList);
        pDetails.setRequestUrl(configs.getAuthorizeNetRequestUrl());

        // Do the post
        String gatewayResp = postData(pDetails);

        gatewayResp = URLDecoder.decode(gatewayResp, "UTF-8");
        if (log.isDebugEnabled())
        {
            log.debug("Unformatted GatewayResp = \n" + gatewayResp);
        }

        // Process the parameters sent in the callback
        StringBuffer sb = new StringBuffer();
        String[] parms = gatewayResp.split(",");
        if (parms != null)
        {
            for (int i = 0; i < parms.length; i++)
            {
                String parm = parms[i];
                sb.append(getRespDesc(i + 1));
                sb.append("=");
                sb.append(parm);
                sb.append("\n");

                if (i + 1 == respCodePosition)
                {
                    gatewayResult = parm;
                    ipnHistory.setGatewayResult(getGatewayResultDescription(parm));
                } else if (i + 1 == txnIdPosition)
                {
                    ipnHistory.setGatewayTransactionId(parm);
                    transactionId = parm;
                } else if (i + 1 == txnTypePosition)
                {
                    ipnHistory.setTxType(parm);
                } else if (i + 1 == txnAmountPosition)
                {
                    try
                    {
                        ipnHistory.setTxAmount(new BigDecimal(parm));
                    } catch (Exception e)
                    {
                    }
                }
            }
        }

        // Put the response in the ipnHistory record
        ipnHistory.setGatewayFullResponse(sb.toString());

        if (log.isDebugEnabled())
        {
            log.debug("Formatted Authorize.net response data:");
            log.debug(sb.toString());
        }

        AdminOrderUpdate updateOrder = new AdminOrderUpdate();
        updateOrder.setUpdatedById(order.getCustomerId());

        NameValue[] ret = null;

        // Determine whether the request was successful or not.
        if (gatewayResult != null && gatewayResult.equals(approved))
        {
            String comment = ORDER_HISTORY_REFUND_COMMENT_OK + transactionId;
            getAdminOrderMgr().updateOrder(order.getId(),
                    com.konakart.bl.OrderMgr.REFUND_APPROVED_STATUS, comment, /* notifyCustomer */
                    refund.isCustomerNotified(), updateOrder);

            // Save the ipnHistory
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
            ipnHistory.setOrderId(order.getId());
            ipnHistory.setCustomerId(order.getCustomerId());
            int id = getAdminOrderMgr().insertIpnHistory(ipnHistory);
            refund.setIpnHistoryId(id);
            refund.setRefundStatus(1);
            getAdminOrderMgr().editOrderRefund(refund);

            ret = new NameValue[]
            { new NameValue(KKConstants.RETURN_CODE, "0") };

        } else if (gatewayResult != null
                && (gatewayResult.equals(declined) || gatewayResult.equals(error)))
        {
            String comment = ORDER_HISTORY_REFUND_COMMENT_KO;
            getAdminOrderMgr().updateOrder(order.getId(),
                    com.konakart.bl.OrderMgr.REFUND_DECLINED_STATUS, comment, /* notifyCustomer */
                    refund.isCustomerNotified(), updateOrder);

            // Save the ipnHistory
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
            ipnHistory.setOrderId(order.getId());
            ipnHistory.setCustomerId(order.getCustomerId());
            int id = getAdminOrderMgr().insertIpnHistory(ipnHistory);
            refund.setIpnHistoryId(id);
            refund.setRefundStatus(-1);
            getAdminOrderMgr().editOrderRefund(refund);

            ret = new NameValue[]
            { new NameValue(KKConstants.RETURN_CODE, "-1") };

        } else
        {
            /*
             * We only get to here if there was an unknown response from the gateway
             */
            String comment = RET1_DESC;
            getAdminOrderMgr().updateOrder(order.getId(),
                    com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, comment, /* notifyCustomer */
                    false, updateOrder);

            // Save the ipnHistory
            ipnHistory.setKonakartResultDescription(RET1_DESC + " - " + gatewayResult);
            ipnHistory.setKonakartResultId(RET1);
            ipnHistory.setOrderId(order.getId());
            ipnHistory.setCustomerId(order.getCustomerId());
            int id = getAdminOrderMgr().insertIpnHistory(ipnHistory);
            refund.setIpnHistoryId(id);
            refund.setRefundStatus(-1);
            getAdminOrderMgr().editOrderRefund(refund);

            ret = new NameValue[]
            { new NameValue(KKConstants.RETURN_CODE, "-1") };

        }
        return ret;
    }

    /**
     * This method calls AuthorizeNet to create a subscription. The KK subscription object is
     * updated to include the AuthorizeNet subscription id. All transaction information is stored in
     * the IPN History.
     * 
     * @param options
     *            PaymentOptions containing information necessary to carry out the transaction
     * @return Returns an array of NameValue objects
     * @throws Exception
     */
    protected NameValue[] createSubscription(PaymentOptions options) throws Exception
    {
        if (options == null)
        {
            throw new KKAdminException(
                    "A null PaymentOptions object was passed to the createSubscription() method");
        }

        if (options.getCreditCard() == null)
        {
            throw new KKAdminException(
                    "A null Credit Card object was passed to the createSubscription() method");
        }

        AdminOrder order = getAdminOrderMgr().getOrderForOrderId(options.getOrderId());
        if (order == null)
        {
            throw new KKAdminException("An order does not exist for id = " + options.getOrderId());
        }

        AdminSubscription kkSubscription = getAdminBillingMgr().getSubscription(
                options.getSubscriptionId());
        if (kkSubscription == null)
        {
            throw new KKAdminException("A subscription does not exist for id = "
                    + options.getSubscriptionId());
        }
        if (kkSubscription.getPaymentSchedule() == null)
        {
            throw new KKAdminException(
                    "A payment schedule does not exist for the subscription with id = "
                            + options.getSubscriptionId());
        }

        // Read the configuration variables
        ConfigVariables configs = getConfigVariables();

        ARBAPI api = new ARBAPI(configs.getAuthorizeNetLoginId(), configs.getAuthorizeNetTxnKey());

        // Get the kk payment schedule
        AdminPaymentSchedule kkPaymentSchedule = kkSubscription.getPaymentSchedule();
        // Get the KK Credit card
        CreditCardIf kkCreditCard = options.getCreditCard();

        // Get the authorize net time unit which is days or months
        String anTimeUnit = null;
        if (kkPaymentSchedule.getTimeUnit() == AdminPaymentSchedule.DAILY)
        {
            anTimeUnit = "days";
        } else if (kkPaymentSchedule.getTimeUnit() == AdminPaymentSchedule.MONTHLY)
        {
            anTimeUnit = "months";
        } else
        {
            throw new KKAdminException("Authorize.Net does not support a time unit of : "
                    + kkPaymentSchedule.getTimeUnit() + " . It only supports days or months.");
        }

        // Get the authorize net total occurrences ( 9999 maps to the kk value of -1 which means
        // never ending)
        int anTotalOccurrences = 9999;
        if (kkPaymentSchedule.getNumPayments() > -1)
        {
            anTotalOccurrences = kkPaymentSchedule.getNumPayments();
        }

        ARBPaymentSchedule newSchedule = new ARBPaymentSchedule();
        newSchedule.setIntervalLength(kkPaymentSchedule.getTimeLength());
        newSchedule.setSubscriptionUnit(anTimeUnit);
        newSchedule.setStartDate(kkSubscription.getStartDate());
        newSchedule.setTotalOccurrences(anTotalOccurrences);
        newSchedule.setTrialOccurrences(0);

        // Create a new credit card
        ARBCreditCard creditCard = new ARBCreditCard();
        creditCard.setCardNumber(kkCreditCard.getCcNumber());
        creditCard.setExpirationDate(kkCreditCard.getCcExpires());
        if (configs.isShowCVV())
        {
            creditCard.setCardCode(kkCreditCard.getCcCVV());
        }
        ARBPayment payment = new ARBPayment(creditCard);

        // Create a billing info
        ARBNameAndAddress billingInfo = new ARBNameAndAddress();

        // Set the billing name. If the name field consists of more than two strings, we take the
        // last one as being the surname.
        String bName = order.getBillingName();
        if (bName != null)
        {
            String[] names = bName.split(" ");
            int len = names.length;
            if (len >= 2)
            {
                StringBuffer firstName = new StringBuffer();
                for (int i = 0; i < len - 1; i++)
                {
                    if (firstName.length() == 0)
                    {
                        firstName.append(names[i]);
                    } else
                    {
                        firstName.append(" ");
                        firstName.append(names[i]);
                    }
                }
                billingInfo.setFirstName(firstName.toString());
                billingInfo.setLastName(names[len - 1]);
            }
        }

        // Create a customer and specify billing info
        ARBCustomer customer = new ARBCustomer();
        customer.setId(String.valueOf(order.getCustomerId()));
        customer.setBillTo(billingInfo);

        // Create an order object and add the order id
        ARBOrder arbOrder = new ARBOrder();
        arbOrder.setInvoiceNumber(Integer.toString(order.getId()));

        // Create a subscription and specify payment, schedule and customer
        ARBSubscription newSubscription = new ARBSubscription();
        newSubscription.setPayment(payment);
        newSubscription.setSchedule(newSchedule);
        newSubscription.setCustomer(customer);
        newSubscription.setAmount(kkSubscription.getAmount().doubleValue());
        newSubscription.setTrialAmount((kkSubscription.getTrialAmount() == null) ? new Double(0)
                : kkSubscription.getTrialAmount().doubleValue());
        newSubscription.setOrder(arbOrder);

        // Give this subscription a name = Order Number
        newSubscription.setName(order.getOrderNumber());

        // Create a new subscription request from the subscription object
        // Returns XML document. Also holds internal pointer as current_request.
        api.createSubscriptionRequest(newSubscription);

        // Common code to manage the Gateway Post
        manageARBGatewayPost(configs, api, kkSubscription,/* create */true);

        // Remove credit card details
        newSubscription.setPayment(null);
        payment = null;
        creditCard = null;
        kkCreditCard = null;

        return new NameValue[]
        { new NameValue(KKConstants.RETURN_CODE, "0") };
    }

    /**
     * Method that needs to be implemented to update a subscription
     * 
     * @param options
     * @return Returns an array of NameValue objects that may contain any return information
     *         considered useful by the caller.
     * @throws Exception
     */
    protected NameValue[] updateSubscription(PaymentOptions options) throws Exception
    {
        if (options == null)
        {
            throw new KKAdminException(
                    "A null PaymentOptions object was passed to the createSubscription() method");
        }

        if (options.getCreditCard() == null)
        {
            throw new KKAdminException(
                    "A null Credit Card object was passed to the createSubscription() method");
        }

        AdminOrder order = getAdminOrderMgr().getOrderForOrderId(options.getOrderId());
        if (order == null)
        {
            throw new KKAdminException("An order does not exist for id = " + options.getOrderId());
        }

        AdminSubscription kkSubscription = getAdminBillingMgr().getSubscription(
                options.getSubscriptionId());
        if (kkSubscription == null)
        {
            throw new KKAdminException("A subscription does not exist for id = "
                    + options.getSubscriptionId());
        }
        if (kkSubscription.getPaymentSchedule() == null)
        {
            throw new KKAdminException(
                    "A payment schedule does not exist for the subscription with id = "
                            + options.getSubscriptionId());
        }

        // Read the configuration variables
        ConfigVariables configs = getConfigVariables();

        ARBAPI api = new ARBAPI(configs.getAuthorizeNetLoginId(), configs.getAuthorizeNetTxnKey());

        // Get the kk payment schedule
        // AdminPaymentSchedule kkPaymentSchedule = kkSubscription.getPaymentSchedule();
        // Get the KK Credit card
        CreditCardIf kkCreditCard = options.getCreditCard();

        // Get the authorize net total occurrences ( 9999 maps to the kk value of -1 which means
        // never ending)
        // int anTotalOccurrences = 9999;
        // if (kkPaymentSchedule.getNumPayments() > -1)
        // {
        // anTotalOccurrences = kkPaymentSchedule.getNumPayments();
        // }

        /*
         * Not typical to change payment schedule
         */
        // Length and unit cannot be changed
        // ARBPaymentSchedule newSchedule = new ARBPaymentSchedule();
        // newSchedule.setStartDate(kkSubscription.getStartDate());
        // newSchedule.setTotalOccurrences(anTotalOccurrences);
        // newSchedule.setTrialOccurrences(0);

        // Create a new credit card
        ARBCreditCard creditCard = new ARBCreditCard();
        creditCard.setCardNumber(kkCreditCard.getCcNumber());
        creditCard.setExpirationDate(kkCreditCard.getCcExpires());
        if (configs.isShowCVV())
        {
            creditCard.setCardCode(kkCreditCard.getCcCVV());
        }
        ARBPayment payment = new ARBPayment(creditCard);

        /*
         * Not typical to change billing info
         */
        // Create a billing info
        // ARBNameAndAddress billingInfo = new ARBNameAndAddress();
        //
        // // Set the billing name. If the name field consists of more than two strings, we take the
        // // last one as being the surname.
        // String bName = order.getBillingName();
        // if (bName != null)
        // {
        // String[] names = bName.split(" ");
        // int len = names.length;
        // if (len >= 2)
        // {
        // StringBuffer firstName = new StringBuffer();
        // for (int i = 0; i < len - 1; i++)
        // {
        // if (firstName.length() == 0)
        // {
        // firstName.append(names[i]);
        // } else
        // {
        // firstName.append(" ");
        // firstName.append(names[i]);
        // }
        // }
        // billingInfo.setFirstName(firstName.toString());
        // billingInfo.setLastName(names[len - 1]);
        // }
        // }
        //
        // // Create a customer and specify billing info
        // ARBCustomer customer = new ARBCustomer();
        // customer.setId(String.valueOf(order.getCustomerId()));
        // customer.setBillTo(billingInfo);

        // Create a subscription and specify payment, schedule and customer
        ARBSubscription updateSubscription = new ARBSubscription();
        updateSubscription.setPayment(payment);
        // updateSubscription.setSchedule(newSchedule);
        // updateSubscription.setCustomer(customer);
        updateSubscription.setAmount(kkSubscription.getAmount().doubleValue());
        // updateSubscription.setTrialAmount((kkSubscription.getTrialAmount() == null) ? new
        // Double(0)
        // : kkSubscription.getTrialAmount().doubleValue());

        // Give this subscription a name = Order Number
        // updateSubscription.setName(order.getOrderNumber());

        // Set code
        updateSubscription.setSubscriptionId(kkSubscription.getSubscriptionCode());

        // Create a new subscription request from the subscription object. Also holds internal
        // pointer as current_request.
        api.updateSubscriptionRequest(updateSubscription);

        // Common code to manage the Gateway Post
        manageARBGatewayPost(configs, api, kkSubscription,/* create */false);

        // Remove credit card details
        updateSubscription.setPayment(null);
        payment = null;
        creditCard = null;
        kkCreditCard = null;

        return new NameValue[]
        { new NameValue(KKConstants.RETURN_CODE, "0") };
    }

    /**
     * Method that needs to be implemented to cancel a subscription
     * 
     * @param options
     * @return Returns an array of NameValue objects that may contain any return information
     *         considered useful by the caller.
     * @throws Exception
     */
    protected NameValue[] cancelSubscription(PaymentOptions options) throws Exception
    {
        AdminSubscription kkSubscription = getAdminBillingMgr().getSubscription(
                options.getSubscriptionId());
        if (kkSubscription == null)
        {
            throw new KKAdminException("A subscription does not exist for id = "
                    + options.getSubscriptionId());
        }

        // Read the configuration variables
        ConfigVariables configs = getConfigVariables();

        ARBAPI api = new ARBAPI(configs.getAuthorizeNetLoginId(), configs.getAuthorizeNetTxnKey());

        // Create a subscription and specify subscription id
        ARBSubscription cancelSubscription = new ARBSubscription();
        cancelSubscription.setSubscriptionId(kkSubscription.getSubscriptionCode());

        // Create a new subscription request from the subscription object. Also holds internal
        // pointer as current_request.
        api.cancelSubscriptionRequest(cancelSubscription);

        // Common code to manage the Gateway Post
        manageARBGatewayPost(configs, api, kkSubscription,/* create */false);

        return new NameValue[]
        { new NameValue(KKConstants.RETURN_CODE, "0") };
    }

    /**
     * Return the status of a subscription. When active it returns the string "active"
     * 
     * @param options
     * @return Returns an array of NameValue objects that may contain any return information
     *         considered useful by the caller.
     * @throws Exception
     */
    protected NameValue[] getSubscriptionStatus(PaymentOptions options) throws Exception
    {

        AdminSubscription kkSubscription = getAdminBillingMgr().getSubscription(
                options.getSubscriptionId());
        if (kkSubscription == null)
        {
            throw new KKAdminException("A subscription does not exist for id = "
                    + options.getSubscriptionId());
        }

        // Read the configuration variables
        ConfigVariables configs = getConfigVariables();

        ARBAPI api = new ARBAPI(configs.getAuthorizeNetLoginId(), configs.getAuthorizeNetTxnKey());

        // Create a subscription and specify subscription id
        //
        ARBSubscription getSubscriptionStatus = new ARBSubscription();
        getSubscriptionStatus.setSubscriptionId(kkSubscription.getSubscriptionCode());

        // Create a new subscription request from the subscription object.
        // Also holds internal pointer as current_request.
        //
        api.getSubscriptionStatusRequest(getSubscriptionStatus);

        // Common code to manage the Gateway Post
        String status = manageARBGatewayPost(configs, api, kkSubscription,/* create */false);

        // Return the status
        return new NameValue[]
        { new NameValue("status", status) };
    }

    /**
     * Common code for all recurring billing requests. Returns the status which is valid for the
     * getSubscriptionStatus() API call.
     * 
     * @param configs
     * @param api
     * @throws Exception
     */
    private String manageARBGatewayPost(ConfigVariables configs, ARBAPI api,
            AdminSubscription kkSubscription, boolean create) throws Exception
    {
        BasicXmlDocument reqXML = api.getCurrentRequest();
        String reqStr = reqXML.dump();

        if (log.isDebugEnabled())
        {
            try
            {
                log.debug("Formatted GatewayRequest =\n" + PrettyXmlPrinter.printXml(reqStr));
            } catch (Exception e)
            {
                log.debug("Exception pretty-printing gateway request: " + e.getMessage());
            }
        }

        AdminPaymentDetails pDetails = new AdminPaymentDetails();
        pDetails.setSendData(reqStr);
        pDetails.setRequestUrl(configs.getAuthorizeNetARBRequestUrl());
        pDetails.setContentType("text/xml");

        // Do the post
        String gatewayResp = postData(pDetails);

        if (log.isDebugEnabled())
        {
            log.debug("Unformatted GatewayResp = \n" + gatewayResp);
        }

        AdminIpnHistory ipnHistory = new AdminIpnHistory();
        ipnHistory.setModuleCode(code);
        ipnHistory.setGatewayFullResponse(gatewayResp);
        ipnHistory.setOrderId(kkSubscription.getOrderId());
        ipnHistory.setCustomerId(kkSubscription.getCustomerId());
        ipnHistory.setSubscriptionId(kkSubscription.getId());

        try
        {
            api.parseResponse(gatewayResp);
        } catch (Exception e)
        {
            ipnHistory.setKonakartResultDescription(RET1_DESC + " - " + e.getMessage());
            ipnHistory.setKonakartResultId(RET1);
            getAdminOrderMgr().insertIpnHistory(ipnHistory);
            throw e;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Result Code : " + api.getResultCode());
            log.debug("Subscription Id : " + api.getResultSubscriptionId());
        }

        if (api.getResultCode() != null && api.getResultCode().equalsIgnoreCase("OK"))
        {
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
            if (create)
            {
                // Update the subscription to add the AuthorizeNet subscription code
                kkSubscription.setSubscriptionCode(api.getResultSubscriptionId());
                getAdminBillingMgr().updateSubscription(kkSubscription);
            }
        } else
        {
            ipnHistory.setKonakartResultDescription(RET2_DESC);
            ipnHistory.setKonakartResultId(RET2);
        }

        // Set the Gateway Result
        StringBuffer sb = new StringBuffer(api.getResultCode());
        if (api.getMessages() != null)
        {
            for (Iterator<ARBMessage> iterator = api.getMessages().iterator(); iterator.hasNext();)
            {
                ARBMessage msg = iterator.next();
                sb.append(" - ");
                sb.append(msg.getCode());
                // If we have only one msg then add description
                if (api.getMessages().size() == 1)
                {
                    sb.append(" - ");
                    sb.append(msg.getText());
                }
            }
        }

        String gatewayRes = sb.toString();
        if (gatewayRes.length() > 128)
        {
            gatewayRes = gatewayRes.substring(0, 128);
        }
        ipnHistory.setGatewayResult(gatewayRes);

        ipnHistory.setGatewayTransactionId(api.getResultSubscriptionId());
        getAdminOrderMgr().insertIpnHistory(ipnHistory);

        return api.getResultStatus();
    }

    /**
     * Adds a description to the Gateway Result
     * 
     * @param code
     * @return Returns a result with an added description
     */
    protected String getGatewayResultDescription(String code)
    {
        if (code == null)
        {
            return code;
        }
        String ret = code;
        if (code.equals("1"))
        {
            ret += " - This transaction has been approved.";
        } else if (code.equals("2"))
        {
            ret += " - This transaction has been declined.";
        } else if (code.equals("3"))
        {
            ret += " - There has been an error processing this transaction.";
        } else if (code.equals("4"))
        {
            ret += " - This transaction is being held for review.";
        }
        return ret;
    }

    /**
     * Used to store the configuration data of this module
     */
    protected class ConfigVariables
    {
        private int sortOrder = -1;

        // The Authorize.Net Url used to POST the payment request.
        // "https://secure.authorize.net/gateway/transact.dll"
        private String authorizeNetRequestUrl;

        // The Authorize.Net Automated Recurring Billing Url used to POST the payment request.
        // "https://apitest.authorize.net/xml/v1/request.api"
        private String authorizeNetARBRequestUrl;

        // Login ID
        private String authorizeNetLoginId;

        // Transaction Key
        private String authorizeNetTxnKey;

        // Test/Live Mode indicator
        private boolean testMode = true;

        // Show the CVV entry field on the UI
        private boolean showCVV = true;

        // zone where AuthorizeNet will be made available
        private int zone;

        /**
         * @return the sortOrder
         */
        public int getSortOrder()
        {
            return sortOrder;
        }

        /**
         * @param sortOrder
         *            the sortOrder to set
         */
        public void setSortOrder(int sortOrder)
        {
            this.sortOrder = sortOrder;
        }

        /**
         * @return the authorizeNetRequestUrl
         */
        public String getAuthorizeNetRequestUrl()
        {
            return authorizeNetRequestUrl;
        }

        /**
         * @param authorizeNetRequestUrl
         *            the authorizeNetRequestUrl to set
         */
        public void setAuthorizeNetRequestUrl(String authorizeNetRequestUrl)
        {
            this.authorizeNetRequestUrl = authorizeNetRequestUrl;
        }

        /**
         * @return the authorizeNetLoginId
         */
        public String getAuthorizeNetLoginId()
        {
            return authorizeNetLoginId;
        }

        /**
         * @param authorizeNetLoginId
         *            the authorizeNetLoginId to set
         */
        public void setAuthorizeNetLoginId(String authorizeNetLoginId)
        {
            this.authorizeNetLoginId = authorizeNetLoginId;
        }

        /**
         * @return the authorizeNetTxnKey
         */
        public String getAuthorizeNetTxnKey()
        {
            return authorizeNetTxnKey;
        }

        /**
         * @param authorizeNetTxnKey
         *            the authorizeNetTxnKey to set
         */
        public void setAuthorizeNetTxnKey(String authorizeNetTxnKey)
        {
            this.authorizeNetTxnKey = authorizeNetTxnKey;
        }

        /**
         * @return the testMode
         */
        public boolean isTestMode()
        {
            return testMode;
        }

        /**
         * @param testMode
         *            the testMode to set
         */
        public void setTestMode(boolean testMode)
        {
            this.testMode = testMode;
        }

        /**
         * @return the showCVV
         */
        public boolean isShowCVV()
        {
            return showCVV;
        }

        /**
         * @param showCVV
         *            the showCVV to set
         */
        public void setShowCVV(boolean showCVV)
        {
            this.showCVV = showCVV;
        }

        /**
         * @return the zone
         */
        public int getZone()
        {
            return zone;
        }

        /**
         * @param zone
         *            the zone to set
         */
        public void setZone(int zone)
        {
            this.zone = zone;
        }

        /**
         * @return the authorizeNetARBRequestUrl
         */
        public String getAuthorizeNetARBRequestUrl()
        {
            return authorizeNetARBRequestUrl;
        }

        /**
         * @param authorizeNetARBRequestUrl
         *            the authorizeNetARBRequestUrl to set
         */
        public void setAuthorizeNetARBRequestUrl(String authorizeNetARBRequestUrl)
        {
            this.authorizeNetARBRequestUrl = authorizeNetARBRequestUrl;
        }
    }
}