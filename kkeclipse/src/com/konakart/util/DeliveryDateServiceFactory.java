package com.konakart.util;

import com.konakart.appif.OrderIf;

public class DeliveryDateServiceFactory {
	private static final int GOAT_MEAT_CATEGORY_ID = 32;
	private static final int CHICKEN_CATEGORY_ID = 31;
	private static final int FISH_CATEGORY_ID = 33;
	private static final int PORK_CATEGORY_ID = 42;
	
	public static DeliveryDateServiceIf getDeliveryDateService (OrderIf checkoutOrder) {
		DeliveryDateServiceIf deliveryDateService = null;
		
		if(orderContainsZorabianProduct(checkoutOrder)) {
			deliveryDateService = new ZorabianFreshDeliveryDateService();
		} else if (orderContainsFreshGoatMeat(checkoutOrder)){
			deliveryDateService = new FreshGoatDeliveryDateService(); 
		} else if (orderContainsFreshPorkMeat(checkoutOrder)){
			deliveryDateService = new FreshPorkDeliveryDateService();
		} else if (orderContainsFreshChickenOrFish(checkoutOrder)) {
			deliveryDateService = new FreshChickenFishDeliveryDateService();
		} else if (orderContainsOnlyFrozenProducts(checkoutOrder)) {
			deliveryDateService = new OnlyFrozenProductDeliveryDateService();
		} else {
			deliveryDateService = new GeneralProductsDeliveryDateService();
		}
		
		return deliveryDateService;
	}
		
	private static boolean orderContainsFreshPorkMeat(OrderIf checkoutOrder) {
		boolean flag = false;
    	com.konakart.appif.OrderProductIf[] products = checkoutOrder.getOrderProducts();
    	for (com.konakart.appif.OrderProductIf prod : products) {
    		if (prod.getProduct().getCategoryId() == PORK_CATEGORY_ID && prod.getProduct().getManufacturerName()
    				.equals("MeatRoot's Fresh")) {
    			flag = true;
    			return flag;
    		}
    	}
    	return flag;
	}

	private static boolean orderContainsFreshGoatMeat(OrderIf checkoutOrder) {
		boolean flag = false;
    	com.konakart.appif.OrderProductIf[] products = checkoutOrder.getOrderProducts();
    	for (com.konakart.appif.OrderProductIf prod : products) {
    		if (prod.getProduct().getCategoryId() == GOAT_MEAT_CATEGORY_ID && prod.getProduct().getManufacturerName()
    				.equals("MeatRoot's Fresh")) {
    			flag = true;
    			return flag;
    		}
    	}
    	return flag;
	}

	private static boolean orderContainsFreshChickenOrFish(OrderIf checkoutOrder) {
		boolean flag = false;
    	com.konakart.appif.OrderProductIf[] products = checkoutOrder.getOrderProducts();
    	for (com.konakart.appif.OrderProductIf prod : products) {
    		if ((prod.getProduct().getCategoryId() == CHICKEN_CATEGORY_ID || prod.getProduct().getCategoryId() == FISH_CATEGORY_ID) 
    				&& prod.getProduct().getManufacturerName()
    				.equals("MeatRoot's Fresh")) {
    			flag = true;
    			return flag;
    		}
    	}
    	return flag;
	}

	public static boolean orderContainsZorabianProduct(com.konakart.appif.OrderIf checkoutOrder) {
    	boolean flag = false;
    	com.konakart.appif.OrderProductIf[] products = checkoutOrder.getOrderProducts();
    	for (com.konakart.appif.OrderProductIf prod : products) {
    		if (prod.getProduct().getManufacturerName()
    				.equals("Zorabian's Fresh")) {
    			flag = true;
    			return flag;
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

