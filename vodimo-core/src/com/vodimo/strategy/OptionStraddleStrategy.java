package com.vodimo.strategy;

import java.util.List;

import com.vodimo.core.model.Option;

public class OptionStraddleStrategy implements OptionStrategy {

	public static long TIME_TO_EXPIRY = 90L * 24L * 60L * 60L * 1000L; //90 days
	public static long MAX_TIME_TO_EXPIRY = 120L * 24L * 60L * 60L * 1000L; //120 days
	public static long TIME_TO_CLOSE = 30L * 24L * 60L * 60L * 1000L; //30 days
	
	//private static double MONEYNESS_MAX = 1.50D;
	//private static double MONEYNESS_MIN = 0.50D;
	
	//private static double MONEYNESS_MAX = 1.10D;
	//private static double MONEYNESS_MIN = 0.90D;	
	
	//private static double MONEYNESS_RANGE = 0.10D;
		
	
	public static Option getNearestATMOption(double underlyingPrice, List<Option> optionsList, String optionType) {
		int nearestMatchIndex = 0;
	    for (int i = 1; i < optionsList.size(); i++) {
	        if (Math.abs(underlyingPrice - optionsList.get(nearestMatchIndex).getStrike())
	                > Math.abs(underlyingPrice - optionsList.get(i).getStrike()) 
	                && optionsList.get(i).getOptionType().equals(optionType)) {
	            nearestMatchIndex = i;
	        }
	    }
	    return optionsList.get(nearestMatchIndex);		
	}
	
	/*
	 * Filters the options that are being loaded (to conserve time/space). They are filtered (a) by level of moneyness 
	 * S/K as well as by expiration (i.e. > 120 days). 
	 */
	public static boolean filterOptionChainEntry(Option o) {
		/*
		if(((o.getUnderlyingLastPrice() / o.getStrike()) > MONEYNESS_MAX) | ((o.getUnderlyingLastPrice() / o.getStrike()) < MONEYNESS_MIN)) 
			return false;*/		
		if((o.getExpirationDate().getTime() - o.getQuoteDate().getTime()) > MAX_TIME_TO_EXPIRY) 
			return false;
		return true;
	}
	
}
