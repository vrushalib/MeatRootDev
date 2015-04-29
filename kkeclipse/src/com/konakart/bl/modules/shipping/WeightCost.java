//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is the proprietary property of
// DS Data Systems UK Ltd. and is protected by English copyright law,
// the laws of foreign jurisdictions, and international treaties,
// as applicable. No part of this document may be reproduced,
// transmitted, transcribed, transferred, modified, published, or
// translated into any language, in any form or by any means, for
// any purpose other than expressly permitted by DS Data Systems UK Ltd.
// in writing.
//
package com.konakart.bl.modules.shipping;

import java.math.BigDecimal;

/**
 * 
 * 
 */
public class WeightCost
{

    private BigDecimal weight;

    private BigDecimal cost;

    /**
     * Constructor
     */
    public WeightCost()
    {

    }

    /**
     * Constructor
     * @param weight 
     * @param cost 
     */
    public WeightCost(BigDecimal weight, BigDecimal cost)
    {
        this.weight = weight;
        this.cost = cost;
    }

    /**
     * @return Returns the weight.
     */
    public BigDecimal getWeight()
    {
        return weight;
    }

    /**
     * @param weight
     *            The weight to set.
     */
    public void setWeight(BigDecimal weight)
    {
        this.weight = weight;
    }

    /**
     * @return Returns the cost.
     */
    public BigDecimal getCost()
    {
        return cost;
    }

    /**
     * @param cost The cost to set.
     */
    public void setCost(BigDecimal cost)
    {
        this.cost = cost;
    }

}
