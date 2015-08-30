package com.konakart.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.konakart.al.KKAppEng;

public abstract class AbstractDeliveryDateService {
	public String getDateTomorrow() {
    	Calendar c = new GregorianCalendar();
    	c.add(Calendar.DATE, 1);
    	return getDate(c.getTime());
    }	

    public String getDateAfterTomorrow() {
    	Calendar c = new GregorianCalendar();
    	c.add(Calendar.DATE, 2);
    	return getDate(c.getTime());
    }
    
    public String getDate(Date date) {
    	return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }
    
    public boolean isEveningSlotEnabled(KKAppEng eng){
    	if(eng.getConfigAsBoolean("ENABLE_EVENING_SLOT", true)){
    		return true;
    	}
    	return false;
    }
    
    public boolean isMorningSlotEnabled(KKAppEng eng){
    	if(eng.getConfigAsBoolean("ENABLE_MORNING_SLOT", false)){
    		return true;
    	}
    	return false;
    }

    public boolean isAfternoonSlotEnabled(KKAppEng eng){
    	if(eng.getConfigAsBoolean("ENABLE_AFTERNOON_SLOT", false)){
    		return true;
    	}
    	return false;
    }

}
