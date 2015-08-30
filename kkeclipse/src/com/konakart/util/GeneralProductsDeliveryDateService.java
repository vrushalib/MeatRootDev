package com.konakart.util;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.konakart.al.KKAppEng;
import com.konakart.appif.OrderIf;

public class GeneralProductsDeliveryDateService extends AbstractDeliveryDateService implements DeliveryDateServiceIf {

	public String getDeliveryDate(KKAppEng eng, OrderIf checkoutOrder, Map<String, Boolean> slotsMap) {
		String deliveryDay = null;
		
		Date today = new Date();
    	Time now = new Time(today.getTime());

    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.HOUR_OF_DAY, 00); // 12 AM
    	cal.set(Calendar.MINUTE, 00);
    	cal.set(Calendar.SECOND, 00);
   	
    	cal.set(Calendar.HOUR_OF_DAY, 8); //8.30 AM
    	cal.set(Calendar.MINUTE, 30);
    	Time eightThirtyAm = new Time(cal.getTime().getTime());
    	
    	cal.set(Calendar.HOUR_OF_DAY, 15); //3 PM
    	cal.set(Calendar.MINUTE, 00);
    	Time threePm = new Time(cal.getTime().getTime());
    	
    	cal.set(Calendar.HOUR_OF_DAY, 20); //8.30 PM
    	cal.set(Calendar.MINUTE, 30);
    	Time eightThirtyPm = new Time(cal.getTime().getTime());
    	
    	if(now.before(eightThirtyAm)) {
    		if(isAfternoonSlotEnabled(eng) || isEveningSlotEnabled(eng)) {
    			deliveryDay = getDate(new Date());
        		slotsMap.put(Constants.MORNING_SLOT, false);	
    		} else {
    			deliveryDay = getDateTomorrow();
    		}    		
    	} else if (now.after(eightThirtyAm) && now.before(threePm)) {
    		if(isEveningSlotEnabled(eng)) {
    			slotsMap.put(Constants.MORNING_SLOT, false);
        		slotsMap.put(Constants.AFTERNOON_SLOT, false);
        		deliveryDay = getDate(new Date());	
    		} else {
    			deliveryDay = getDateTomorrow();
    		}    		
    	} else if (now.after(threePm) && now.before(eightThirtyPm)) {
    		deliveryDay = getDateTomorrow();
    	} else {
    		// this is case for time after 8.30pm upto midnight
    		if(isAfternoonSlotEnabled(eng) || isEveningSlotEnabled(eng)) {
    			deliveryDay = getDateTomorrow();
        		slotsMap.put(Constants.MORNING_SLOT, false);	
    		} else {
    			deliveryDay = getDateAfterTomorrow();
    		}    		
    	}
    	return deliveryDay;
	}

}
