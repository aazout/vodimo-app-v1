package com.vodimo.core.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

// FIXME: Use QuantLib tools for this
public class OptionsMarketCalendarUtil { 
	
	public static List<Date> CLOSEDATES_2012 = new ArrayList<Date>();
	static {
		try {
			CLOSEDATES_2012.add(VodimoUtils.parseSimpleDate("1/2/2012")); //New Year's
			CLOSEDATES_2012.add(VodimoUtils.parseSimpleDate("1/16/2012")); //Martin Luther King, Jr. Day
			CLOSEDATES_2012.add(VodimoUtils.parseSimpleDate("2/20/2012")); //Presidents' Day
			CLOSEDATES_2012.add(VodimoUtils.parseSimpleDate("4/6/2012")); //Good Friday
			CLOSEDATES_2012.add(VodimoUtils.parseSimpleDate("5/28/2012")); //Memorial Day
			CLOSEDATES_2012.add(VodimoUtils.parseSimpleDate("7/4/2012")); //Independence Day
			CLOSEDATES_2012.add(VodimoUtils.parseSimpleDate("9/3/2012")); //Labor Day
			CLOSEDATES_2012.add(VodimoUtils.parseSimpleDate("11/22/2012")); //Thanksgiving Day
			CLOSEDATES_2012.add(VodimoUtils.parseSimpleDate("12/25/2012")); //Christmas Day (observed)
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public static List<Date> EARLYDATES_2012 = new ArrayList<Date>();
	static {
		try {
			EARLYDATES_2012.add(VodimoUtils.parseSimpleDate("7/3/2012 1:00 PM")); //New Year's
			EARLYDATES_2012.add(VodimoUtils.parseSimpleDate("11/23/2012 1:00 PM")); //Martin Luther King, Jr. Day
			EARLYDATES_2012.add(VodimoUtils.parseSimpleDate("12/24/2012 1:00 PM")); //Presidents' Day
		} catch (ParseException e) {
			e.printStackTrace();
		}		
	}	
	
	public static List<Integer> WEEKEND = new ArrayList<Integer>();
	static {
		WEEKEND.add(Calendar.SATURDAY);
		WEEKEND.add(Calendar.SUNDAY);
	}
	
	public static Date addTradingDay(Date d) {
		Calendar cal = Calendar.getInstance();
		//d = DateUtils.truncate(d, Calendar.DATE);
		cal.setTime(d);		
		cal.add(Calendar.DAY_OF_YEAR, 1);
		while(CLOSEDATES_2012.contains(cal.getTime()) | WEEKEND.contains(cal.get(Calendar.DAY_OF_WEEK))) {
			cal.add(Calendar.DAY_OF_YEAR, 1);			
		}		
		return cal.getTime();
	}
	
	public static Date subtractTradingDay(Date d) {
		Calendar cal = Calendar.getInstance();
		//d = DateUtils.truncate(d, Calendar.DATE);
		cal.setTime(d);		
		cal.add(Calendar.DAY_OF_YEAR, -1);
		while(CLOSEDATES_2012.contains(d) | WEEKEND.contains(cal.get(Calendar.DAY_OF_WEEK))) {
			cal.add(Calendar.DAY_OF_YEAR, -1);
		}
		return cal.getTime();
	}	
	
}
