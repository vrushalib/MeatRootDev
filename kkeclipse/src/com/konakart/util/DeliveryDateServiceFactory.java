package com.konakart.util;

import com.konakart.appif.OrderIf;

public class DeliveryDateServiceFactory {
	public static DeliveryDateServiceIf getDeliveryDateService (OrderIf checkoutOrder) {
		DeliveryDateServiceIf deliveryDateService = null;
		
		if(orderContainsZorabianProduct(checkoutOrder)) {
			deliveryDateService = new ZorabianFreshDeliveryDateService();
		} else if (orderContainsOnlyFrozenProducts(checkoutOrder)) {
			deliveryDateService = new OnlyFrozenProductDeliveryDateService();
		} else {
			deliveryDateService = new GeneralProductsDeliveryDateService();
		}
		
		return deliveryDateService;
	}
		
	public static boolean orderContainsZorabianProduct(com.konakart.appif.OrderIf checkoutOrder) {
    	boolean flag = false;
    	com.konakart.appif.OrderProductIf[] products = checkoutOrder.getOrderProducts();
    	for (com.konakart.appif.OrderProductIf prod : products) {
    		if (prod.getProduct().getManufacturerName()
    				.equals("Zorabian's Fresh")) {
    			flag = true;
    		}
    	}
    	return flag;
    }
	
	public static boolean orderContainsOnlyFrozenProducts(com.konakart.appif.OrderIf checkoutOrder) {
    	boolean flag = true;
    	com.konakart.appif.OrderProductIf[] products = checkoutOrder.getOrderProducts();
    	for (com.konakart.appif.OrderProductIf prod : products) {
    		if (prod.getProduct().getManufacturerName()
    				.toLowerCase().contains("fresh")) {
    			flag = false;
    		}
    	}
    	return flag;
    }
}

