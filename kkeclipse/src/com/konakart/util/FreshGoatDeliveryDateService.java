package com.konakart.util;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.konakart.al.KKAppEng;
import com.konakart.appif.OrderIf;

public class FreshGoatDeliveryDateService extends AbstractDeliveryDateService
		implements DeliveryDateServiceIf {

	public String getDeliveryDate(KKAppEng eng, OrderIf checkoutOrder,
			Map<String, Boolean> slotsMap) {
		String deliveryDay = null;
		
		Date today = new Date();
    	Time now = new Time(today.getTime());

    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.HOUR_OF_DAY, 00); // 12 AM
    	cal.set(Calendar.MINUTE, 00);
    	cal.set(Calendar.SECOND, 00);    	
    	
    	cal.set(Calendar.HOUR_OF_DAY, 21); //9 PM
    	Time ninePm = new Time(cal.getTime().getTime());
    	
    	cal.set(Calendar.HOUR_OF_DAY, 15); //3 PM
    	Time threePm = new Time(cal.getTime().getTime());
    	
    	cal.set(Calendar.HOUR_OF_DAY, 23); // Midnight
    	cal.set(Calendar.MINUTE, 59);
    	cal.set(Calendar.SECOND, 59);
    	Time midnight = new Time(cal.getTime().getTime());
    	    	
    	cal.set(Calendar.HOUR_OF_DAY, 9); //9 AM 
    	cal.set(Calendar.MINUTE, 00);
    	cal.set(Calendar.SECOND, 00);
    	Time nineAm = new Time(cal.getTime().getTime());
    	
    	//cal.add(Calendar.DATE, 1);
   /* 	cal.set(Calendar.HOUR_OF_DAY, 15); //3 PM 
    	Time threePmNextDay = new Time(cal.getTime().getTime());*/
    	
    	if(now.after(threePm) && now.before(ninePm)) {
    		deliveryDay = getDateTomorrow();
    	} else if (now.after(ninePm) && now.before(midnight)) {
    		if(isAfternoonSlotEnabled(eng) || isEveningSlotEnabled(eng)) {
    			slotsMap.put(Constants.MORNING_SLOT, false);
        		deliveryDay = getDateTomorrow();	
    		} else {
    			deliveryDay = getDateAfterTomorrow();
    		}
    	} else if (now.before(nineAm)) {
    		if(isAfternoonSlotEnabled(eng) || isEveningSlotEnabled(eng)) {
    			slotsMap.put(Constants.MORNING_SLOT, false);
        		deliveryDay = getDate(new Date());	
    		} else {
    			deliveryDay = getDateTomorrow();
    		}
    	} else if (now.after(nineAm) && now.before(threePm)) {
    		if(isEveningSlotEnabled(eng)) {
    			slotsMap.put(Constants.MORNING_SLOT, false);
        		slotsMap.put(Constants.AFTERNOON_SLOT, false);
        		deliveryDay = getDate(new Date());	
    		} else {
    			deliveryDay = getDateTomorrow();
    		}    		
    	} else {
    		deliveryDay = getDateTomorrow();
    	}
    	
		return deliveryDay;
	}

}
