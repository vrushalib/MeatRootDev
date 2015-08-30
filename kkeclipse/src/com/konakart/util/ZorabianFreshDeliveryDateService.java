package com.konakart.util;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.konakart.al.KKAppEng;
import com.konakart.appif.OrderIf;

public class ZorabianFreshDeliveryDateService extends AbstractDeliveryDateService implements DeliveryDateServiceIf {

	public String getDeliveryDate(KKAppEng eng, OrderIf checkoutOrder, Map<String, Boolean> slotsMap) {
		String deliveryDay = null;
		
		Date today = new Date();
    	Time now = new Time(today.getTime());

    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.HOUR_OF_DAY, 00); // 12 AM
    	cal.set(Calendar.MINUTE, 00);
    	cal.set(Calendar.SECOND, 00);
   	
    	cal.set(Calendar.HOUR_OF_DAY, 20); //8 PM
    	Time eightPm = new Time(cal.getTime().getTime());
		
		if(now.before(eightPm))
			deliveryDay = getDateTomorrow();
		else{
			slotsMap.put(Constants.ZORABIAN_AFTER_EIGHT, true);
    		deliveryDay = getDateAfterTomorrow();
		}
		return deliveryDay;
	}

}
