package com.konakart.util;

import java.util.Map;

import com.konakart.al.KKAppEng;
import com.konakart.appif.OrderIf;

public interface DeliveryDateServiceIf {
	public String getDeliveryDate(KKAppEng eng, OrderIf checkoutOrder, Map<String, Boolean> slotsMap);
}
