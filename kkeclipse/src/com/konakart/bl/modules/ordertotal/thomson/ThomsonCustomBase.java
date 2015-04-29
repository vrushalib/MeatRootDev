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

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.Country;
import com.konakart.app.OrderProduct;
import com.konakart.app.Zone;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderProductIf;
import com.konakart.appif.ShippingQuoteIf;
import com.konakart.bl.modules.ordertotal.thomson.Thomson.StaticData;
import com.konakart.util.Utils;
import com.sabrix.services.taxcalculationservice._2011_09_01.IndataLineType;
import com.sabrix.services.taxcalculationservice._2011_09_01.OutdataLineType;
import com.sabrix.services.taxcalculationservice._2011_09_01.TaxCalculationResponse;
import com.sabrix.services.taxcalculationservice._2011_09_01.UserElementType;
import com.sabrix.services.taxcalculationservice._2011_09_01.ZoneAddressType;

/**
 * Thomson Custom Utilities - Customers can provide custom classes that extend this interface to
 * provide custom functionality for the module for the interfaces that they wish to specialise.
 */
public class ThomsonCustomBase implements ThomsonCustomIf
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(ThomsonCustomBase.class);

    /**
     * An engine that can be used by the custom methods
     */
    private KKEngIf eng;

    /**
     * The module
     */
    private com.konakart.bl.modules.ordertotal.thomson.Thomson module;

    /**
     * Constructor
     * 
     * @param _eng
     *            a KKENgIf engine
     * @param _module
     *            the instance of the module
     */
    public ThomsonCustomBase(KKEngIf _eng,
            com.konakart.bl.modules.ordertotal.thomson.Thomson _module)
    {
        this.eng = _eng;
        this.module = _module;
    }

    public ZoneAddressType getShipToAddress(StaticData sd, OrderIf order, Country deliveryCountry,
            Zone deliveryZone)
    {
        String deliveryZoneType = null;

        if (deliveryZone != null)
        {
            // We assume that the custom1 field of Zone contains "S" (State) or "P" (Province)
            deliveryZoneType = deliveryZone.getCustom1();
        }

        ZoneAddressType shipToAddr = new ZoneAddressType();
        shipToAddr.setADDRESS1(order.getDeliveryStreetAddress());

        if (!Utils.isBlank(order.getDeliveryCity()))
        {
            shipToAddr.setCITY(order.getDeliveryCity());
        }
        shipToAddr.setCOUNTRY(getModule().getCountryISO2(sd, order.getDeliveryCountry()));

        if (!Utils.isBlank(order.getDeliveryPostcode()))
        {
            shipToAddr.setPOSTCODE(order.getDeliveryPostcode());
        }

        setStateAndProvince(shipToAddr, deliveryZone, deliveryZoneType, order.getDeliveryState());

        return shipToAddr;
    }

    public ZoneAddressType getBillToAddress(StaticData sd, OrderIf order, Country billingCountry,
            Zone billingZone)
    {
        String billingZoneType = null;

        if (billingZone != null)
        {
            // We assume that the custom1 field of Zone contains "S" (State) or "P" (Province)
            billingZoneType = billingZone.getCustom1();
        }

        ZoneAddressType billToAddr = new ZoneAddressType();
        billToAddr.setADDRESS1(order.getBillingStreetAddress());

        if (!Utils.isBlank(order.getBillingCity()))
        {
            billToAddr.setCITY(order.getBillingCity());
        }

        billToAddr.setCOUNTRY(getModule().getCountryISO2(sd, order.getBillingCountry()));
        if (!Utils.isBlank(order.getBillingPostcode()))
        {
            billToAddr.setPOSTCODE(order.getBillingPostcode());
        }

        setStateAndProvince(billToAddr, billingZone, billingZoneType, order.getBillingState());

        return billToAddr;
    }

    /**
     * Custom method for retrieving values from complicated places... expected to be specialised to
     * suit local requirements. In the default case we expect that the 6 tax code fields are
     * '|'-separated and set on a single custom field that is identified by the
     * MODULE_ORDER_TOTAL_THOMSON_CUSTON_CODE_FIELD configuration setting.
     * 
     * @param fieldName
     *            Name of the field we are retrieving
     * @param customCodeFieldValue
     *            the value of the custom field (could be in any String format)
     * @param sd
     *            the StaticData which caches configuration data for the store
     * @param orderProduct
     *            the order product. An order will have one or more orderProducts
     * @return the value for the fieldName specified
     */
    public String getCustomFieldValue(String fieldName, String customCodeFieldValue, StaticData sd,
            OrderProductIf orderProduct)
    {
        if (customCodeFieldValue == null)
        {
            return null;
        }

        String[] sextuplet = customCodeFieldValue.split("\\|");

        if (fieldName == Thomson.MODULE_ORDER_TOTAL_THOMSON_USCOMMODITY_CODE_FIELD)
        {
            if (sextuplet.length > 0)
            {
                return sextuplet[0];
            } else
            {
                return "";
            }
        } else if (fieldName == Thomson.MODULE_ORDER_TOTAL_THOMSON_COMMODITY_CODE_FIELD)
        {
            if (sextuplet.length > 1)
            {
                return sextuplet[1];
            } else
            {
                return "";
            }
        } else if (fieldName == Thomson.MODULE_ORDER_TOTAL_THOMSON_MOSS_CODE_FIELD)
        {
            if (sextuplet.length > 2)
            {
                return sextuplet[2];
            } else
            {
                return "";
            }
        } else if (fieldName == Thomson.MODULE_ORDER_TOTAL_THOMSON_TRANS_TYPE_FIELD)
        {
            if (sextuplet.length > 3)
            {
                return sextuplet[3];
            } else
            {
                return "";
            }
        } else if (fieldName == Thomson.MODULE_ORDER_TOTAL_THOMSON_VAT_INCLUDED_FIELD)
        {
            if (sextuplet.length > 4)
            {
                return sextuplet[4];
            } else
            {
                return "";
            }
        } else if (fieldName == Thomson.MODULE_ORDER_TOTAL_THOMSON_EXEMPTIONS_FIELD)
        {
            if (sextuplet.length > 5)
            {
                return sextuplet[5];
            } else
            {
                return "";
            }
        } else
        {
            log.warn("Unknown field : " + fieldName);
        }

        return null;
    }

    /**
     * Add zero or more User Elements to the line object
     * 
     * @param sd
     *            the StaticData which caches configuration data for the store
     * @param orderProduct
     *            the order product. An order will have one or more orderProducts
     * @param line
     *            the IndataLineType line
     */
    public void addUserElements(StaticData sd, OrderProductIf orderProduct, IndataLineType line)
    {
        String mossIndicator = getModule().getMossIndicator(sd, orderProduct);
        if (!Utils.isBlank(mossIndicator))
        {
            UserElementType ue = new UserElementType();
            ue.setNAME("ATTRIBUTE1");
            ue.setVALUE(mossIndicator);
            line.getUSERELEMENT().add(ue);
            if (log.isDebugEnabled())
            {
                log.debug("Added User Element : " + ue.getNAME() + " = " + ue.getVALUE());
            }
        }
        String vatIndicator = getModule().getVatIncludedIndicator(sd, orderProduct);
        if (!Utils.isBlank(vatIndicator))
        {
            UserElementType ue = new UserElementType();
            ue.setNAME("ATTRIBUTE4");
            ue.setVALUE(vatIndicator);
            line.getUSERELEMENT().add(ue);
            if (log.isDebugEnabled())
            {
                log.debug("Added User Element : " + ue.getNAME() + " = " + ue.getVALUE());
            }
        }
        String exemptionIndicator = getModule().getExemptionsIndicator(sd, orderProduct);
        if (!Utils.isBlank(exemptionIndicator))
        {
            UserElementType ue = new UserElementType();
            ue.setNAME("ATTRIBUTE5");
            ue.setVALUE(exemptionIndicator);
            line.getUSERELEMENT().add(ue);
            if (log.isDebugEnabled())
            {
                log.debug("Added User Element : " + ue.getNAME() + " = " + ue.getVALUE());
            }
        }
    }

    /**
     * Add zero or more User Elements to the line object for the shipping line
     * 
     * @param sd
     *            the StaticData which caches configuration data for the store
     * @param order
     *            the order.
     * @param line
     *            the IndataLineType line
     */
    public void addUserElementsShipping(StaticData sd, OrderIf order, IndataLineType line)
    {
        String vatIndicator = getModule().getShippingVatIndicator(sd, order);
        if (!Utils.isBlank(vatIndicator))
        {
            UserElementType ue = new UserElementType();
            ue.setNAME("ATTRIBUTE4");
            ue.setVALUE(vatIndicator);
            line.getUSERELEMENT().add(ue);
            if (log.isDebugEnabled())
            {
                log.debug("Added User Element : " + ue.getNAME() + " = " + ue.getVALUE());
            }
        }
    }

    public String getGROSS_AMOUNTForProduct(StaticData sd, OrderProductIf orderProduct,
            OrderIf order)
    {
        return orderProduct.getFinalPriceExTax().toString();
    }

    public String getGROSS_AMOUNTForShipping(StaticData sd, ShippingQuoteIf shipping, OrderIf order)
    {
        return shipping.getTotalIncTax().toString();
    }

    /*
     * public BigDecimal adjustmentForInclusiveShipping(StaticData sd, TaxCalculationResponse
     * response, OrderIf order) { if (response.getOUTDATA().getINVOICE() != null &&
     * response.getOUTDATA().getINVOICE().get(0) != null &&
     * response.getOUTDATA().getINVOICE().get(0).getLINE() != null) {
     * 
     * for (OutdataLineType line : response.getOUTDATA().getINVOICE().get(0).getLINE()) { if
     * (line.getCOMMODITYCODE() != null && line.getCOMMODITYCODE().equals("FREIGHT INCLUSIVE")) { if
     * (!Utils.isBlank(line.getTOTALTAXAMOUNT())) { if (log.isDebugEnabled()) {
     * log.debug("Thomson Tax reduced by " + line.getTOTALTAXAMOUNT() +
     * " for VAT-inclusive Shipping"); } return new BigDecimal(line.getTOTALTAXAMOUNT()); } } } }
     * 
     * return new BigDecimal("0.00"); }
     */

    public void adjustOrderAfterTaxCalculation(StaticData sd, TaxCalculationResponse response,
            OrderIf order)
    {
        if (response.getOUTDATA().getINVOICE() != null
                && response.getOUTDATA().getINVOICE().get(0) != null
                && response.getOUTDATA().getINVOICE().get(0).getLINE() != null)
        {
            /*
             * for (OutdataLineType line : response.getOUTDATA().getINVOICE().get(0).getLINE()) { if
             * (line.getCOMMODITYCODE() != null &&
             * line.getCOMMODITYCODE().equals("FREIGHT INCLUSIVE")) { if
             * (!Utils.isBlank(line.getTOTALTAXAMOUNT())) { if (log.isDebugEnabled()) {
             * log.debug("Thomson Tax reduced by " + line.getTOTALTAXAMOUNT() +
             * " for VAT-inclusive Shipping"); } adjustedTaxAmountBD =
             * adjustedTaxAmountBD.subtract(new BigDecimal(line .getTOTALTAXAMOUNT())); } } }
             */

            // For each line reset the tax and priceIncTax on the OrderProducts & ShippingQuote

            int lineNum = 0;
            for (OutdataLineType line : response.getOUTDATA().getINVOICE().get(0).getLINE())
            {
                lineNum++;
                if (line.getDESCRIPTION() != null)
                {
                    BigDecimal taxBD = new BigDecimal(line.getTOTALTAXAMOUNT());
                    if (line.getDESCRIPTION().equals("Shipping Charge"))
                    {
                        if (!Utils.isBlank(line.getTOTALTAXAMOUNT()))
                        {
                            order.getShippingQuote().setTax(taxBD);
                            order.getShippingQuote().setTotalExTax(
                                    order.getShippingQuote().getTotalIncTax().subtract(taxBD));
                        }
                    } else
                    {
                        order.getOrderProducts()[lineNum - 1].setTax(taxBD);
                        order.getOrderProducts()[lineNum - 1].setFinalPriceIncTax(order
                                .getOrderProducts()[lineNum - 1].getFinalPriceExTax().add(taxBD));

                        if (line.getTAXSUMMARY() != null
                                && line.getTAXSUMMARY().getTAXRATE() != null)
                        {
                            BigDecimal taxRateBD = new BigDecimal(line.getTAXSUMMARY().getTAXRATE());
                            order.getOrderProducts()[lineNum - 1].setTaxRate(taxRateBD
                                    .multiply(new BigDecimal("100")));
                        }
                    }
                }
            }
        }
    }

    public void calculateTotals(StaticData sd, OrderIf order)
    {
        order.setTotalExTax(new BigDecimal(0));
        order.setTotalIncTax(new BigDecimal(0));
        order.setTax(new BigDecimal(0));

        if (order.getOrderProducts() == null || order.getOrderProducts().length == 0)
        {
            return;
        }

        // Calculate totals for the products
        for (int i = 0; i < order.getOrderProducts().length; i++)
        {
            OrderProduct op = (OrderProduct) order.getOrderProducts()[i];
            if (op.getFinalPriceExTax() != null)
            {
                order.setTotalExTax(order.getTotalExTax().add(op.getFinalPriceExTax()));
            }
            if (op.getTax() != null)
            {
                order.setTax(order.getTax().add(op.getTax()));
            }
        }

        order.setSubTotalExTax(new BigDecimal(0).add(order.getTotalExTax()));
        order.setSubTotalIncTax(order.getSubTotalExTax().add(order.getTax()));

        // Add the shipping quote
        ShippingQuoteIf quote = order.getShippingQuote();
        if (quote != null)
        {
            order.setTotalExTax(order.getTotalExTax().add(quote.getTotalExTax()));
            order.setTax(order.getTax().add(quote.getTax()));
        }
        order.setTotalIncTax(order.getTotalExTax().add(order.getTax()));
    }

    /**
     * Logic to decide which tags to set based on the address information we have available.
     * 
     * @param addr
     *            the ZoneAddressType object to populate
     * @param zone
     *            the Zone (could be null)
     * @param zoneType
     *            the zoneType (could be null - this is the Custom1 attribute of the Zone)
     * @param rawState
     *            the state/province name we started with
     */
    protected void setStateAndProvince(ZoneAddressType addr, Zone zone, String zoneType,
            String rawState)
    {
        if (zone != null && zoneType != null && zoneType.equals("S"))
        {
            addr.setSTATE(getModule().getZoneCode(zone));
        } else if (zone != null && zoneType != null && zoneType.equals("P"))
        {
            addr.setPROVINCE(getModule().getZoneCode(zone));
        } else if (zone != null)
        {
            addr.setSTATE(getModule().getZoneCode(zone));
            addr.setPROVINCE(getModule().getZoneCode(zone));
        } else if (zone == null)
        {
            if (!Utils.isBlank(rawState))
            {
                addr.setSTATE(rawState);
                addr.setPROVINCE(rawState);
            }
        }
    }

    /**
     * @return the module
     */
    public com.konakart.bl.modules.ordertotal.thomson.Thomson getModule()
    {
        return module;
    }

    /**
     * @return the engine
     */
    public KKEngIf getEng()
    {
        return eng;
    }
}
