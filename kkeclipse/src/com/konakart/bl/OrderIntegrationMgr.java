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
package com.konakart.bl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.Booking;
import com.konakart.app.Coupon;
import com.konakart.app.CreditCard;
import com.konakart.app.ExportOrderOptions;
import com.konakart.app.KKConfiguration;
import com.konakart.app.KKEng;
import com.konakart.app.MqOptions;
import com.konakart.app.OrderUpdate;
import com.konakart.app.PaymentOptions;
import com.konakart.app.PdfOptions;
import com.konakart.app.PdfResult;
import com.konakart.app.Subscription;
import com.konakart.appif.BookingIf;
import com.konakart.appif.CouponIf;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.EngineConfigIf;
import com.konakart.appif.ExportOrderOptionsIf;
import com.konakart.appif.ExportOrderResponseIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.LanguageIf;
import com.konakart.appif.MqOptionsIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderProductIf;
import com.konakart.appif.OrderStatusHistoryIf;
import com.konakart.appif.OrderTotalIf;
import com.konakart.appif.OrderUpdateIf;
import com.konakart.appif.PaymentScheduleIf;
import com.konakart.appif.ProductIf;
import com.konakart.appif.SubscriptionIf;
import com.konakart.bl.modules.ordertotal.OrderTotalInterface;
import com.konakart.blif.AdminEngineMgrIf;
import com.konakart.blif.BillingMgrIf;
import com.konakart.blif.OrderMgrIf;
import com.konakart.blif.OrderTotalMgrIf;
import com.konakart.blif.ProductMgrIf;
import com.konakart.blif.PromotionMgrIf;
import com.konakart.db.KKBasePeer;
import com.konakart.db.KKCriteria;
import com.konakart.mqif.MqMgrIf;
import com.konakart.om.BaseOrdersPeer;
import com.konakart.om.BaseProductsPeer;
import com.konakart.util.KKConstants;
import com.workingdogs.village.Record;

/**
 * Used to provide integration points when orders are saved and when the status of an order is
 * changed.
 */
public class OrderIntegrationMgr extends BaseMgr implements OrderIntegrationMgrInterface
{
    /** the log */
    protected static Log log = LogFactory.getLog(OrderIntegrationMgr.class);

    /** Mutex for synchronising population of the tax module Hash */
    private static String mutex = "taxModuleMapMutex";

    /** HashMap to lookup Module names by their code */
    private static HashMap<String, TaxModuleData> taxModuleMap;

    /**
     * Constructor
     * 
     * @param eng
     * @throws Exception
     */
    public OrderIntegrationMgr(KKEngIf eng) throws Exception
    {
        super.init(eng, log);

        if (log.isDebugEnabled())
        {
            if (eng != null && eng.getEngConf() != null && eng.getEngConf().getStoreId() != null)
            {
                log.debug("OrderIntegrationMgr instantiated for store id = "
                        + eng.getEngConf().getStoreId());
            }
        }
    }

    /**
     * Called just before an order is saved. This method gives the opportunity to modify any detail
     * of the order before it is saved. If null is returned, then no action is taken. If a non null
     * Order is returned, then this is the order that will be saved.
     * 
     * @param order
     * @return Returns an order object which will be saved
     */
    public OrderIf beforeSaveOrder(OrderIf order)
    {
        return null;
    }

    /**
     * Called whenever an order is saved. In order to improve performance, you may comment out API
     * calls from this method if the relative functionality isn't being used. i.e. comment out
     * manageRewardPoints() if reward points aren't being used.
     */
    public void saveOrder(OrderIf order)
    {
        if (log.isInfoEnabled())
        {
            log.info("The order with id = " + order.getId() + " has just been saved ");
        }

        /*
         * Manage a customer's reward points
         */
        manageRewardPoints(order);

        /*
         * When an order has a total of zero it can be saved with a payment received status. The
         * following methods should only be called when payment has been received.
         */
        if (order.getStatus() == com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS)
        {
            manageDigitalDownloads(order);
            manageGiftCertificates(order);
            manageGiftRegistries(order);

            // Post the order to the order message queue for processing by some other system
            // postOrderToQueue(order);

            // Uncomment to export the order - but remember to add the supported shipping modules
            // to konakart.properties (copy them from konakartadmin.properties).
            // createOrderExportForShipping(order);

            // Uncomment to export a full copy of the order to an XML file
            // createXmlOrderExport(order);

            // Uncomment to export the order for shipping
            // createOrderExportForShipping(order);

            // Uncomment if using a Tax Module (such as TaxCloud, Avalara etc)
            // manageTax(order);

            // Uncomment if using Bookable Products
            // manageBookings(order);
        }

        // By default we don't create any invoices.
        // createInvoice(order);

        // Only when not managed by payment gateway
        // manageSubscriptions(order);
    }

    /**
     * Called whenever the status of an order is changed. In order to improve performance, you may
     * comment out API calls from this method if the relative functionality isn't being used. i.e.
     * comment out manageRewardPoints() if reward points aren't being used. If all calls are
     * commented out, then you should also comment out the API call to get the order.
     */
    public void changeOrderStatus(int orderId, int currentStatus, int newStatus)
    {
        if (log.isInfoEnabled())
        {
            log.info("The order with id = " + orderId + " has just changed state from stateId = "
                    + currentStatus + " to stateId = " + newStatus);
        }

        try
        {
            // Get the order
            OrderIf order = getOrderMgr().getOrderForOrderId(orderId, LanguageMgr.DEFAULT_LANG);

            /*
             * Manage a customer's reward points
             */
            manageRewardPoints(order);

            if (newStatus == OrderMgr.PAYMENT_RECEIVED_STATUS)
            {
                manageDigitalDownloads(order);
                manageGiftCertificates(order);
                manageGiftRegistries(order);

                // Post the order to the order message queue for processing by some other system
                // postOrderToQueue(order);

                // Uncomment if using a Tax Module (such as TaxCloud, Avalara etc)
                // manageTax(order);

                // Uncomment if using Bookable Products
                // manageBookings(order);
            }

            /*
             * Uncomment the call to createInvoice() if you want your invoice to be created when the
             * order reaches a particular state and not when it is first saved.
             */
            // createInvoice(order);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Called just before a subscription is inserted. This method gives the opportunity to modify
     * any detail of the subscription before it is inserted. If null is returned, then no action is
     * taken. If a non null subscription is returned, then this is the subscription that will be
     * saved.
     * 
     * @param subscription
     * @return Returns a Subscription
     * @throws Exception
     */
    public SubscriptionIf beforeInsertSubscription(SubscriptionIf subscription) throws Exception
    {
        return null;
    }

    /**
     * Called after a Subscription object has been saved in the database. The id of the object has a
     * valid value.
     * 
     * @param subscription
     * @throws Exception
     */
    @SuppressWarnings("all")
    public void afterInsertSubscription(SubscriptionIf subscription) throws Exception
    {
        /*
         * This sample code isn't normally run. Remove the "if false" condition to enable it.
         */
        if (false)
        {
            /*
             * At this point we may want to send a subscription to a payment gateway so that the
             * payment gateway can perform the recurring billing. We receive the subscription code
             * back from the payment gateway and then save the subscription object in the database.
             * Shown below is an example using AuthorizeNet.
             */

            /*
             * Create an AdminEngineMgr, if possible (we need Enterprise Extensions for this)
             */
            AdminEngineMgrIf adEngineMgr = getAdminEngMgr();
            if (adEngineMgr == null)
            {
                // This shouldn't happen
                log.warn("AdminEngineMgr is null");
                return;
            }

            /*
             * Create a PaymentOptions object to pass to the method that calls the payment gateway.
             * The payment module gets the subscription code from the payment gateway and updates
             * the subscription in the KonaKart database to add the code.
             */
            PaymentOptions options = new PaymentOptions();
            options.setSubscriptionId(subscription.getId());
            options.setOrderId(subscription.getOrderId());
            options.setCreditCard((CreditCard) (subscription.getCreditCard()));
            options.setAction(KKConstants.ACTION_CREATE_SUBSCRIPTION);
            adEngineMgr.callPaymentModule(getEng().getEngConf(),
                    "com.konakartadmin.modules.payment.authorizenet.AdminPayment", options);
        }

    }

    /**
     * Called just before a subscription is updated. This method gives the opportunity to modify any
     * detail of the subscription before it is updated. If null is returned, then no action is
     * taken. If a non null subscription is returned, then this is the subscription that will be
     * saved.
     * 
     * @param subscription
     * @return Returns a Subscription
     * @throws Exception
     */
    public SubscriptionIf beforeUpdateSubscription(SubscriptionIf subscription) throws Exception
    {
        return null;
    }

    /**
     * Called after a Subscription object has been updated in the database.
     * 
     * @param subscription
     * @throws Exception
     */
    @SuppressWarnings("all")
    public void afterUpdateSubscription(SubscriptionIf subscription) throws Exception
    {
        /*
         * This sample code isn't normally run. Remove the "if false" condition to enable it.
         */
        if (false)
        {
            /*
             * At this point we may want to send a subscription to a payment gateway so that the
             * payment gateway can perform the recurring billing. We receive the subscription code
             * back from the payment gateway and then save the subscription object in the database.
             * Shown below is an example using AuthorizeNet.
             */

            /*
             * Create an AdminEngineMgr, if possible (we need Enterprise Extensions for this)
             */
            AdminEngineMgrIf adEngineMgr = getAdminEngMgr();
            if (adEngineMgr == null)
            {
                // This shouldn't happen
                log.warn("AdminEngineMgr is null");
                return;
            }

            /*
             * Create a PaymentOptions object to pass to the method that calls the payment gateway.
             * The payment module gets the subscription code from the subscription object in the
             * KonaKart database which it looks up using the id in the PaymentOptions.
             */
            PaymentOptions options = new PaymentOptions();
            options.setSubscriptionId(subscription.getId());
            options.setOrderId(subscription.getOrderId());
            options.setCreditCard((CreditCard) (subscription.getCreditCard()));
            options.setAction(KKConstants.ACTION_UPDATE_SUBSCRIPTION);
            adEngineMgr.callPaymentModule(getEng().getEngConf(),
                    "com.konakartadmin.modules.payment.authorizenet.AdminPayment", options);
        }
    }

    /**
     * Normally when a bundled product is bought, the number of times that it has been ordered
     * attribute in the database is only updated for the products making up the bundle. This method
     * updates it for the bundle product itself.
     * 
     * @param order
     */
    @SuppressWarnings("unused")
    private void updateBundledProductOrderedCount(OrderIf order)
    {
        try
        {
            // Check the order products to see whether any of them are bundles
            if (order != null && order.getOrderProducts() != null)
            {
                for (int i = 0; i < order.getOrderProducts().length; i++)
                {
                    OrderProductIf op = order.getOrderProducts()[i];
                    if (op.getType() == ProductMgr.BUNDLE_PRODUCT_TYPE
                            || op.getType() == ProductMgr.FREE_SHIPPING_BUNDLE_PRODUCT_TYPE)
                    {
                        // Get the current value of products ordered
                        KKCriteria selectC = getNewCriteria();
                        selectC.add(BaseProductsPeer.PRODUCTS_ID, op.getProductId());
                        selectC.addSelectColumn(BaseProductsPeer.PRODUCTS_ORDERED);
                        List<Record> rows = KKBasePeer.select(selectC);
                        if (rows.isEmpty())
                        {
                            return;
                        }

                        Record rec = rows.get(0);
                        int prodsOrdered = rec.getValue(1).asInt();

                        // Update the value
                        prodsOrdered += op.getQuantity();
                        KKCriteria updateC = getNewCriteria();
                        updateC.add(BaseProductsPeer.PRODUCTS_ORDERED, prodsOrdered);
                        KKBasePeer.doUpdate(selectC, updateC);
                    }
                }
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Creates the PDF invoice using the AdminPdfMgr if the code is present. This is normally
     * available only for the enterprise version of KonaKart.
     * <p>
     * The order record is updated in the database with the invoice_filename if the invoice is
     * created successfully.
     * 
     * @param order
     * @return a PdfResult object which can be null if the invoice cold not be created
     */
    public PdfResult createInvoice(OrderIf order)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Entered createInvoice()");
        }
        try
        {
            int langId = KKConstants.DEFAULT_LANGUAGE_ID;

            if (order.getLocale() == null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Locale on OrderId " + order.getId() + " is null");
                }
            } else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Locale on OrderId " + order.getId() + " = " + order.getLocale());
                }

                LanguageIf lang = getEng().getLanguagePerCode(order.getLocale().substring(0, 2));
                if (lang != null)
                {
                    langId = lang.getId();
                }
            }

            // Create an AdminEngineMgr, if possible (we need Enterprise Extensions for this)
            AdminEngineMgrIf adEngineMgr = getAdminEngMgr();

            if (adEngineMgr == null)
            {
                // This shouldn't happen
                log.warn("AdminEngineMgr is null");
                return null;
            }

            PdfOptions options = new PdfOptions();
            options.setId(order.getId());
            options.setType(KKConstants.HTML_ORDER_INVOICE);
            options.setLanguageId(langId);
            options.setReturnFileName(true);
            options.setReturnBytes(false);
            options.setCreateFile(true);

            PdfResult pdfResult = adEngineMgr.getPdf(getEng().getEngConf(), options);

            if (pdfResult != null)
            {
                order.setInvoiceFilename(pdfResult.getFileNameAfterBase());

                // Update the invoice filename on the order
                KKCriteria updateC = getNewCriteria();
                KKCriteria selectC = getNewCriteria();
                updateC.addForInsert(BaseOrdersPeer.INVOICE_FILENAME, order.getInvoiceFilename());
                selectC.add(BaseOrdersPeer.ORDERS_ID, order.getId());
                KKBasePeer.doUpdate(selectC, updateC);
                if (log.isDebugEnabled())
                {
                    log.debug("Updated invoice filename (" + order.getInvoiceFilename()
                            + ") on order " + order.getId());
                }
            } else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("pdfResult returned = null");
                }
            }

            return pdfResult;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * We get the order from the database and check whether any of the ordered products are digital
     * downloads. For each digital download product we insert a DigitalDownload object in the DB.
     * This is read by the application in order to provide the download link for the customer.
     * <p>
     * The status of the order is modified if DigitalDownloads exist.
     * 
     * @param order
     */
    private void manageDigitalDownloads(OrderIf order)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Entered manageDigitalDownloads()");
        }
        try
        {
            int numDownloadsCreated = 0;
            ProductMgrIf productMgr = null;
            OrderMgrIf orderMgr = getOrderMgr();

            // Check the order products to see whether any of them are digital downloads
            if (order != null && order.getOrderProducts() != null)
            {
                for (int i = 0; i < order.getOrderProducts().length; i++)
                {
                    OrderProductIf op = order.getOrderProducts()[i];
                    if (op.getType() == ProductMgr.DIGITAL_DOWNLOAD)
                    {
                        if (productMgr == null)
                        {
                            productMgr = getProdMgr();
                        }
                        productMgr.insertDigitalDownload(order.getCustomerId(), op.getProductId());
                        numDownloadsCreated++;
                    }
                }

                /*
                 * If the order only consisted of digital downloads and we have created the links
                 * for all of the downloads, then we can change the order state to DELIVERED. If it
                 * consisted partially of digital downloads then we change the state to
                 * PARTIALLY_DELIVERED
                 */
                OrderUpdateIf updateOrder = new OrderUpdate();
                updateOrder.setUpdatedById(order.getCustomerId());

                if (numDownloadsCreated == order.getOrderProducts().length)
                {
                    orderMgr.updateOrder(order.getId(), OrderMgr.DELIVERED_STATUS, /* customerNotified */
                            false, "Download Link Available", updateOrder);
                } else if (numDownloadsCreated > 0)
                {
                    orderMgr.updateOrder(order.getId(), OrderMgr.PARTIALLY_DELIVERED_STATUS, /* customerNotified */
                            false, "Download Link Available", updateOrder);
                }
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method allows you to introduce a proprietary algorithm for creating the order number for
     * an order just before the order is saved. It is called by the <code>saveOrder()</code> method.
     * The value returned by this method populates the <code>orderNumber</code> attribute of the
     * order when it is saved.
     * <p>
     * If a null value is returned, then the order number of the order is left unchanged.<br>
     * If an exception is thrown, the exception will be also thrown by the saveOrder() method and
     * the order will not be saved.
     * 
     * @param order
     * @return Return the order number for the new order
     * @throws Exception
     */
    public String createOrderNumber(OrderIf order) throws Exception
    {
        // Example implementation
        return Long.toString(System.currentTimeMillis());
    }

    /**
     * This method allows you to generate a tracking number for an order just before the order is
     * saved. It is called by the <code>saveOrder()</code> method. The value returned by this method
     * populates the <code>trackingNumber</code> attribute of the order when it is saved.<br>
     * If a null value is returned, then the tracking number of the order is left unchanged.<br>
     * If an exception is thrown, the exception will be also thrown by the saveOrder() method and
     * the order will not be saved.
     * 
     * @param order
     * @return Return the tracking number for the new order
     * @throws Exception
     */
    public String createTrackingNumber(OrderIf order) throws Exception
    {
        return null;
    }

    /**
     * We get the order from the database and check whether any of the ordered products were ordered
     * from a gift registry. If they were, we have to update the gift registry item (wish list item)
     * quantity received attribute to reflect how many items have been received.
     * 
     * @param order
     */
    private void manageGiftRegistries(OrderIf order)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Entered manageGiftRegistries()");
        }
        try
        {
            Boolean enableGiftRegistry = getConfigMgr().getConfigurationValueAsBool(false,
                    ConfigConstants.ENABLE_GIFT_REGISTRY, false);

            if (enableGiftRegistry)
            {
                // Check the order products to see whether any of them are gift registry items
                if (order != null && order.getOrderProducts() != null)
                {
                    for (int i = 0; i < order.getOrderProducts().length; i++)
                    {
                        OrderProductIf op = order.getOrderProducts()[i];
                        if (op.getWishListItemId() > 0)
                        {
                            getWishListMgr().updateWishListItemQuantityBought(
                                    op.getWishListItemId(), op.getQuantity());
                        }
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * We get the order from the database and check whether any of the ordered products are gift
     * certificates. If they are we need to process them:
     * <ol>
     * <li>Find the promotion attached to the gift certificate</li>
     * <li>Create a coupon code</li>
     * <li>Create a coupon with the new code</li>
     * <li>Attach the coupon to the promotion</li>
     * <li>Insert the coupon code into a downloadable product that can be downloaded by the
     * customer. This becomes the gift certificate that the customer can forward on to the receiver
     * of the gift.</li>
     * </ol>
     * 
     * @param order
     */
    private void manageGiftCertificates(OrderIf order)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Entered manageGiftCertificates()");
        }

        try
        {
            int numCertificates = 0;
            int numDownloads = 0;
            OrderMgrIf orderMgr = getOrderMgr();
            PromotionMgrIf promMgr = getPromMgr();

            // Check the order products to see whether any of them are gift certificate products
            int promotionId = -1;
            if (order != null && order.getOrderProducts() != null)
            {
                for (int i = 0; i < order.getOrderProducts().length; i++)
                {
                    OrderProductIf op = order.getOrderProducts()[i];
                    if (op.getType() == ProductMgr.GIFT_CERTIFICATE_PRODUCT_TYPE)
                    {
                        numCertificates++;
                        for (int j = 0; j < op.getQuantity(); j++)
                        {
                            promotionId = promMgr.getPromotionIdForGiftCertificate(op);
                            if (promotionId > 0)
                            {
                                // We now create a coupon code
                                String couponCode = getCouponCode();

                                // Create a coupon
                                CouponIf coupon = new Coupon();
                                coupon.setCouponCode(couponCode);
                                coupon.setMaxUse(1);
                                coupon.setTimesUsed(0);
                                coupon.setName(order.getId() + " - " + order.getCustomerId()
                                        + " - " + order.getCustomerName()); // Set this to whatever
                                // you like

                                // Insert the coupon and Associate it with the promotion
                                promMgr.insertCoupon(coupon, promotionId);

                                /*
                                 * The coupon code could be sent to the customer in whichever way
                                 * the merchant sees fit. In this example we will make it available
                                 * as a digital download product. Alternatively it could be sent as
                                 * an eMail etc.
                                 */
                                String filePath = getGiftCertificateFilePath(op, couponCode);

                                getProdMgr().insertGiftCertificateDigitalDownload(
                                        order.getCustomerId(), op.getProductId(), filePath);

                            } else
                            {
                                log.warn("A promotion was not found for Gift Certificate product id = "
                                        + op.getProductId()
                                        + " so it was not processed for order id = "
                                        + op.getOrderId());
                            }
                        }
                    } else if (op.getType() == ProductMgr.DIGITAL_DOWNLOAD)
                    {
                        /*
                         * Digital downloads should already have been processed. We keep track of
                         * all gift certificates and digital downloads in order to set the state of
                         * the order.
                         */
                        numDownloads++;
                    }
                }

                /*
                 * If the order only consisted of digital downloads and gift certificates and we
                 * have created the links for all of the downloads, then we can change the order
                 * state to DELIVERED. If it consisted partially of digital downloads and gift
                 * certificates then we change the state to PARTIALLY_DELIVERED
                 */
                OrderUpdateIf updateOrder = new OrderUpdate();
                updateOrder.setUpdatedById(order.getCustomerId());

                if (numCertificates + numDownloads == order.getOrderProducts().length
                        && numCertificates > 0)
                {
                    orderMgr.updateOrder(order.getId(), OrderMgr.DELIVERED_STATUS, /* customerNotified */
                            false, "Download Link Available", updateOrder);
                } else if (numCertificates > 0)
                {
                    orderMgr.updateOrder(order.getId(), OrderMgr.PARTIALLY_DELIVERED_STATUS, /* customerNotified */
                            false, "Download Link Available", updateOrder);
                }
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Creates and returns the path of the file that contains the code of the gift certificate that
     * can be downloaded. This example creates a simple text file. For your store you may want to
     * create a stylish pdf document or an HTML file that the customer can download and send on.
     * 
     * @param op
     * @param couponCode
     * @return Return the file path of the file containing the certificate code
     * @throws IOException
     */
    private String getGiftCertificateFilePath(OrderProductIf op, String couponCode)
            throws IOException
    {
        String outputFilename = couponCode + ".txt";
        File myOutFile = new File(outputFilename);

        BufferedWriter bw = new BufferedWriter(new FileWriter(myOutFile));
        bw.write(couponCode);
        bw.close();

        return myOutFile.getAbsolutePath();
    }

    /**
     * Method returns a random coupon code. You may change this code to implement a different
     * algorithm or to call out to an external system.
     * 
     * @return Returns the coupon code
     */
    private String getCouponCode()
    {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }

    /**
     * We get the order from the database, and depending on the state of the order, reward points
     * are assigned to the customer and / or removed from the customer's total. The language for the
     * description of the reward point transaction may be determined by the <code>locale</code>
     * attribute of the order.
     * 
     * @param order
     */
    private void manageRewardPoints(OrderIf order)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Entered manageRewardPoints()");
        }
        try
        {
            KKConfiguration conf = getConfigMgr().getConfiguration(false,
                    ConfigConstants.ENABLE_REWARD_POINTS);

            if (conf != null && conf.getValue() != null && conf.getValue().equalsIgnoreCase("true"))
            {
                OrderMgrIf orderMgr = getOrderMgr();

                // Check the state
                if (order.getStatus() == OrderMgr.PENDING_STATUS
                        || order.getStatus() == OrderMgr.WAITING_PAYMENT_STATUS)
                {
                    /*
                     * If the customer is redeeming points then we need to reserve those points so
                     * they aren't used again until the order is either paid for or cancelled.
                     */
                    if (order.getPointsRedeemed() > 0 && order.getPointsReservationId() <= 0)
                    {
                        int reservationId = getRewardPointMgr().reservePoints(
                                order.getCustomerId(), order.getPointsRedeemed());
                        // Save the reservation id
                        orderMgr.setRewardPointReservationId(order.getCustomerId(), order.getId(),
                                reservationId);
                    }
                } else if (order.getStatus() == OrderMgr.PAYMENT_RECEIVED_STATUS)
                {
                    /*
                     * We need to commit any reserved points that were reserved when the order was
                     * saved. If the points weren't reserved because the order was saved directly in
                     * the "PAYMENT RECEIVED" state, then we must just delete the points.
                     */
                    if (order.getPointsRedeemed() > 0)
                    {
                        if (order.getPointsReservationId() >= 0)
                        {
                            getRewardPointMgr().deleteReservedPoints(order.getCustomerId(),
                                    order.getPointsReservationId(), "ORDER",
                                    "Points redeemed in order #" + order.getId());
                        } else
                        {
                            getRewardPointMgr().deletePoints(order.getCustomerId(),
                                    order.getPointsRedeemed(), "ORDER",
                                    "Points redeemed in order #" + order.getId());
                        }
                    }
                    /*
                     * If points were awarded for the order, then they must be assigned to the
                     * customer
                     */
                    if (order.getPointsAwarded() > 0)
                    {
                        getRewardPointMgr().addPoints(order.getCustomerId(),
                                order.getPointsAwarded(), "ORDER",
                                "Points assigned for order #" + order.getId());
                    }
                } else if (order.getStatus() == OrderMgr.CANCELLED_STATUS)
                {
                    /*
                     * If the order is cancelled and some points have been reserved, then these
                     * points must be returned to the customer.
                     */
                    if (order.getPointsRedeemed() > 0 && order.getPointsReservationId() >= 0)
                    {
                        getRewardPointMgr().freeReservedPoints(order.getCustomerId(),
                                order.getPointsReservationId());
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Used to save a subscription object in the database in order to manage recurring billing for
     * products. Only used when recurring billing isn't managed by a payment gateway.
     * 
     * @param order
     */
    @SuppressWarnings("unused")
    private void manageSubscriptions(OrderIf order)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Entered manageSubscriptions()");
        }
        try
        {
            BillingMgrIf billingMgr = getBillingMgr();
            ProductMgrIf prodMgr = getProdMgr();

            /*
             * Check the order products to see whether any of them require recurring billing. We
             * achieve this by detecting whether they have a valid PaymentScheduleId
             */
            if (order != null && order.getOrderProducts() != null)
            {
                for (int i = 0; i < order.getOrderProducts().length; i++)
                {
                    OrderProductIf op = order.getOrderProducts()[i];
                    int paymentScheduleId = prodMgr.getPaymentScheduleId(op.getProductId());
                    if (paymentScheduleId > -1)
                    {
                        ProductIf prod = prodMgr.getProduct(null, op.getProductId(),
                                LanguageMgr.DEFAULT_LANG);
                        PaymentScheduleIf schedule = null;
                        if (prod != null)
                        {
                            schedule = prod.getPaymentSchedule();
                        }
                        if (schedule != null && prod != null)
                        {
                            /*
                             * The product requires recurring billing so we insert a Subscription.
                             * Some of the optional attributes are commented and may be set
                             * depending on your business requirements.
                             */
                            Subscription sub = new Subscription();
                            sub.setOrderId(order.getId());
                            sub.setPaymentScheduleId(schedule.getId());
                            sub.setProductId(prod.getId());
                            sub.setCustomerId(order.getCustomerId());
                            sub.setOrderNumber(order.getOrderNumber());
                            sub.setProductSku(op.getSku());
                            sub.setAmount(op.getFinalPriceIncTax());
                            sub.setActive(true);
                            sub.setStartDate(new GregorianCalendar());
                            // sub.setLastBillingDate();
                            // sub.setNextBillingDate();
                            // sub.setTrialAmount();
                            int subscriptionId = billingMgr.insertSubscription(sub);

                        }
                    }
                }
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Creates an Order Export file for the order.
     * <p>
     * Typically this would be used to create an import file for the purposes of integrating
     * KonaKart with a 3rd Party system such as UPS WorldShip
     * 
     * @param order
     * @return a ExportOrderResponse object which can be null if the export was unsuccessful
     */
    @SuppressWarnings("unused")
    private ExportOrderResponseIf createOrderExportForShipping(OrderIf order)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Entered createOrderExportForShipping()");
        }
        try
        {
            // Create an AdminEngineMgr, if possible (we need Enterprise Extensions for this)
            AdminEngineMgrIf adEngineMgr = getAdminEngMgr();

            if (adEngineMgr == null)
            {
                // This shouldn't happen
                log.warn("AdminEngineMgr is null");
                return null;
            }

            ExportOrderOptionsIf options = new ExportOrderOptions();
            options.setOrderId(order.getId());
            options.setCode(KKConstants.EXP_ORDER_BY_SHIPPING_MODULE);

            ExportOrderResponseIf response = adEngineMgr
                    .exportOrder(getEng().getEngConf(), options);

            if (response != null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug(response.toString());
                }
            } else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("exportOrder returned = null");
                }
            }

            return response;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates an XML Order Export file for the order.
     * 
     * @param order
     * @return a ExportOrderResponse object which can be null if the export was unsuccessful
     */
    @SuppressWarnings("unused")
    private ExportOrderResponseIf createXmlOrderExport(OrderIf order)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Entered createXmlOrderExport()");
        }
        try
        {
            // Create an AdminEngineMgr, if possible (we need Enterprise Extensions for this)
            AdminEngineMgrIf adEngineMgr = getAdminEngMgr();

            if (adEngineMgr == null)
            {
                // This shouldn't happen
                log.warn("AdminEngineMgr is null");
                return null;
            }

            ExportOrderOptionsIf options = new ExportOrderOptions();
            options.setOrderId(order.getId());
            options.setCode(KKConstants.EXP_ORDER_FULL_ORDER_TO_XML);

            ExportOrderResponseIf response = adEngineMgr
                    .exportOrder(getEng().getEngConf(), options);

            if (response != null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug(response.toString());
                }
            } else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("exportOrder returned = null");
                }
            }

            return response;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Posts the order to the Order Message Queue
     * 
     * @param order
     */
    @SuppressWarnings("unused")
    private void postOrderToQueue(OrderIf order)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Entered postOrderToQueue()");
        }
        try
        {
            // Create an AdminEngineMgr, if possible (we need Enterprise Extensions for this)
            AdminEngineMgrIf adEngineMgr = getAdminEngMgr();

            if (adEngineMgr == null)
            {
                // This shouldn't happen
                log.warn("AdminEngineMgr is null");
                return;
            }

            ExportOrderOptionsIf exportOptions = new ExportOrderOptions();
            exportOptions.setOrderId(order.getId());
            exportOptions.setCode(KKConstants.EXP_ORDER_RETURN_XML_STRING);

            ExportOrderResponseIf response = adEngineMgr.exportOrder(getEng().getEngConf(),
                    exportOptions);

            if (response == null)
            {
                if (log.isWarnEnabled())
                {
                    log.warn("exportOrder returned = null");
                }
                return;
            }

            if (log.isDebugEnabled())
            {
                log.debug(response.toString());
            }

            MqOptionsIf mqOptions = new MqOptions();
            mqOptions.setQueueName(KKEng.getKonakartConfig().getString("mq.orders.queue"));
            mqOptions.setBrokerUrl(KKEng.getKonakartConfig().getString("mq.broker.uri"));
            mqOptions.setUsername(KKEng.getKonakartConfig().getString("mq.username"));
            mqOptions.setPassword(KKEng.getKonakartConfig().getString("mq.password"));

            mqOptions.setMsgText(response.getOrderAsXml());
            MqMgrIf mgr = getMqMgr();
            mgr.postMessageToQueue(mqOptions);

            if (log.isInfoEnabled())
            {
                log.info("Order " + order.getId() + " Posted Successfully to Message Queue");
            }
        } catch (Exception e)
        {
            log.warn("Problem posting Order to Message Queue");
            e.printStackTrace();
        }
    }

    /**
     * The order details are sent to the tax cloud tax service.
     * 
     * @param order
     */
    @SuppressWarnings("unused")
    private void manageTaxCloud(OrderIf order)
    {
        // Use manageTax instead.
        
        if (log.isDebugEnabled())
        {
            log.debug("Entered manageTaxCloud()");
        }

    }

    /**
     * The order details are sent to the defined set of tax services.
     * 
     * @param order
     */
    @SuppressWarnings("unused")
    private void manageTax(OrderIf order)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Entered manageTax()");
        }

        if (order.getOrderTotals() == null)
        {
            // No OrderTotals... that's unexpected
            log.warn("Order " + order.getId() + " has no order totals");
            return;
        }
        // Cycle through the Order totals looking for module codes in our tax module map

        for (OrderTotalIf ot : order.getOrderTotals())
        {
            TaxModuleData tmd = taxModuleMap.get(ot.getClassName());

            if (tmd != null)
            {
                String moduleName = tmd.getModuleName();
                String commitConfKey = tmd.getCommitConfigKey();

                try
                {
                    boolean checkReturnByApi = false;
                    boolean doCommit = getConfigMgr().getConfigurationValueAsBool(checkReturnByApi,
                            commitConfKey, false /* default is false */);

                    if (doCommit)
                    {
                        // Call Tax Service Module to commit the order
                        if (log.isDebugEnabled())
                        {
                            log.debug("Create the " + moduleName + " tax module");
                        }
                        OrderTotalMgrIf otm = getOrderTotalMgr();
                        OrderTotalInterface module = otm.getOrderTotalModuleForName(moduleName);
                        if (log.isDebugEnabled())
                        {
                            log.debug("Commit order (id " + order.getId() + ") to " + moduleName);
                        }
                        module.commitOrder(order);
                        if (log.isDebugEnabled())
                        {
                            log.debug("Committed order (id " + order.getId() + ") to " + moduleName);
                        }
                    }
                } catch (Exception e)
                {
                    log.warn("Problem committing order (id " + order.getId() + ") to " + moduleName);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * We get the order from the database and check whether any of the ordered products are bookable
     * products. For each bookable product we insert a Booking object in the DB. At the moment we
     * assume that the name of the person in the Booking object is the name of the customer. For the
     * case where a customer may make multiple bookings providing the names of the people, these
     * names will have to be encoded into a custom field of the order product and read from the
     * custom field in this method, so that a Booking object may be inserted for each person.
     * 
     * @param order
     */
    @SuppressWarnings("unused")
    private void manageBookings(OrderIf order)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Entered manageBookings()");
        }
        try
        {
            // Check the order products to see whether any of them are bookable products
            if (order != null && order.getOrderProducts() != null)
            {
                for (int i = 0; i < order.getOrderProducts().length; i++)
                {
                    OrderProductIf op = order.getOrderProducts()[i];
                    if (op.getType() == ProductMgr.BOOKABLE_PRODUCT_TYPE)
                    {
                        BookingIf booking = new Booking();
                        booking.setCustomerId(order.getCustomerId());
                        booking.setOrderId(order.getId());
                        booking.setOrderProductId(op.getId());
                        booking.setQuantity(op.getQuantity());
                        booking.setProductId(op.getProductId());
                        CustomerIf customer = getCustMgr().getCustomerForId(order.getCustomerId());
                        if (customer != null)
                        {
                            booking.setFirstName(customer.getFirstName());
                            booking.setLastName(customer.getLastName());
                        }
                        getBookableProductMgr().insertBooking(booking, null);
                    }
                }
            }

        } catch (Exception e)
        {
            if (order == null)
            {
                log.warn("Problem creating bookings");
            } else
            {
                log.warn("Problem creating bookings for order id = " + order.getId());
            }
            e.printStackTrace();
        }
    }

    /**
     * Utility method that gets the vendor orders for the parent order and adds the latest state
     * change of the parent order to the vendor orders.
     * 
     * @param order
     */
    @SuppressWarnings("unused")
    private void addParentStatusChangeToVendorOrders(OrderIf order)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Entered manageChildren()");
        }
        
        EngineConfigIf conf = getEng().getEngConf();
        if (conf == null)
        {
            return;
        }
        
        String mainStoreId = conf.getStoreId();

        // Get latest status history object from the parent order
        if (order.getStatusTrail() == null || order.getStatusTrail().length == 0)
        {
            return;
        }

        try
        {
            // Find children
            KKCriteria selectC = getNewCriteria(/* AllStores */true);
            selectC.add(BaseOrdersPeer.PARENT_ID, order.getId());
            selectC.addSelectColumn(BaseOrdersPeer.ORDERS_ID);
            selectC.addSelectColumn(BaseOrdersPeer.STORE_ID);
            List<Record> rows = KKBasePeer.select(selectC);
            if (rows.isEmpty())
            {
                return;
            }

            OrderStatusHistoryIf latestStatus = order.getStatusTrail()[order.getStatusTrail().length - 1];

            // Change the state for each child
            for (Iterator<Record> iterator = rows.iterator(); iterator.hasNext();)
            {
                Record rec = iterator.next();
                int childId = rec.getValue(1).asInt();
                String storeId = rec.getValue(2).asString();
                conf.setStoreId(storeId);
                KKEng eng = new KKEng(conf);
                OrderUpdateIf orderUpdate = new OrderUpdate();
                orderUpdate.setUpdatedById(latestStatus.getUpdatedById());

                eng.getMgrFactory()
                        .getOrderMgr(/* createNew */true)
                        .updateOrder(childId, latestStatus.getOrderStatusId(),
                                latestStatus.isCustomerNotified(), latestStatus.getComments(),
                                orderUpdate);
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            conf.setStoreId(mainStoreId);
        }
    }

    /**
     * @return the taxModuleMap
     */
    public HashMap<String, TaxModuleData> getTaxModuleMap()
    {
        if (taxModuleMap == null)
        {
            synchronized (mutex)
            {
                if (taxModuleMap == null)
                {
                    taxModuleMap = new HashMap<String, TaxModuleData>();

                    // Populate the Map with all the tax modules that have a commitOrder
                    // implementation. Not all Tax modules will have one and even if they do, you
                    // may not wish to call them. If you don't want to call them, do not add them to
                    // this HashMap.

                    // Include a configuration variable to check and only call the module if this
                    // configuration variable has a true value.

                    TaxModuleData tmd = new TaxModuleData();
                    tmd.setModuleName("Avalara");
                    tmd.setCommitConfigKey("MODULE_ORDER_TOTAL_AVALARA_COMMIT_STATUS");
                    taxModuleMap.put("ot_tax_avalara", tmd);

                    tmd = new TaxModuleData();
                    tmd.setModuleName("TaxCloud");
                    tmd.setCommitConfigKey("MODULE_ORDER_TOTAL_TAX_CLOUD_STATUS");
                    taxModuleMap.put("ot_tax_cloud", tmd);
                }
            }
        }
        return taxModuleMap;
    }

    /**
     * Used to store the static data of this module
     */
    protected class TaxModuleData
    {
        private String moduleName;

        private String commitConfigKey;

        /**
         * @return the moduleName
         */
        public String getModuleName()
        {
            return moduleName;
        }

        /**
         * @param moduleName
         *            the moduleName to set
         */
        public void setModuleName(String moduleName)
        {
            this.moduleName = moduleName;
        }

        /**
         * @return the commitConfigKey
         */
        public String getCommitConfigKey()
        {
            return commitConfigKey;
        }

        /**
         * @param commitConfigKey
         *            the commitConfigKey to set
         */
        public void setCommitConfigKey(String commitConfigKey)
        {
            this.commitConfigKey = commitConfigKey;
        }
    }
}
