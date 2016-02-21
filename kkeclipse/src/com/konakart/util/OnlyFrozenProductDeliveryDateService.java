package com.konakart.util;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.konakart.al.KKAppEng;
import com.konakart.appif.OrderIf;

public class OnlyFrozenProductDeliveryDateService extends AbstractDeliveryDateService implements DeliveryDateServiceIf {

	public String getDeliveryDate(KKAppEng eng, OrderIf checkoutOrder, Map<String, Boolean> slotsMap) {
		String deliveryDay = null;
		
		Date today = new Date();
    	Time now = new Time(today.getTime());

    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.HOUR_OF_DAY, 00); // 12 AM
    	cal.set(Calendar.MINUTE, 00);
    	cal.set(Calendar.SECOND, 00);
   	
    	cal.set(Calendar.HOUR_OF_DAY, 7); //7 AM
    	Time sevenAm = new Time(cal.getTime().getTime());
    	
     	cal.set(Calendar.HOUR_OF_DAY, 17); //5 PM
    	Time fivePm = new Time(cal.getTime().getTime());
    	
    	cal.set(Calendar.HOUR_OF_DAY, 12); //12.05 PM
    	cal.set(Calendar.MINUTE, 5);
    	Time fivePastTwelvePm = new Time(cal.getTime().getTime());
    	
    	if(now.before(sevenAm)) {
    		deliveryDay = getDate(new Date());
    	} else if (now.after(sevenAm) && now.before(fivePastTwelvePm)) {
    		if(isAfternoonSlotEnabled(eng) || isEveningSlotEnabled(eng)) {
    			slotsMap.put(Constants.MORNING_SLOT, false);
        		deliveryDay = getDate(new Date());	
    		} else {
    			deliveryDay = getDateTomorrow();	
    		}    		
    	} else if (now.after(fivePastTwelvePm) && now.before(fivePm)) {
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
