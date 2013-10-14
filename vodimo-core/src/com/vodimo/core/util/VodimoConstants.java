package com.vodimo.core.util;

import com.vodimo.core.model.HistoricalDataConfiguration;

public class VodimoConstants {

	public static String MONGODB_URL = "localhost";
		
	public static String ENV_ARCHIVE_DIR = "vodimo_archive_path";
	public static String TICK_SEPARATOR_DATEPRICE = "&";
	public static String TICK_SEPARATOR = "\t";
	//TODO: Should be passed in at terminal
	public static String SYMBOLS_FILE = 
			"/Users/aazout/Dropbox/Vodimo/Code/java/vodimo-core/resources/boostrap.symbols";	

	public static HistoricalDataConfiguration getBootrstrapConfig() {
		HistoricalDataConfiguration config = new HistoricalDataConfiguration();
		config.setBarSize("30 secs");
		config.setDuration("1 D");
		//config.setNumDays(60);
		config.setNumDays(120);
		return config;
	}
	
	public static HistoricalDataConfiguration getDefaultConfig() {
		HistoricalDataConfiguration config = new HistoricalDataConfiguration();
		config.setBarSize("30 secs");
		//config.setBarSize("1 secs");
		//config.setBarSize("15 mins");
		//config.setDuration("1800 S");
		config.setDuration("1 D");
		config.setNumDays(30);
		return config;	
	}	
	
}
