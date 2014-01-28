package com.vodimo.core.util;

import java.util.HashMap;
import java.util.Map;

public class VodimoModelConstants {

	/* 
	 * Sets the memory length of the entity transition model. 
	 * Too long and no PA candidates. Too short and too many 
	 * interim "false neighbors".  
	 * 
	 * The number of possible transitions is (2^N)^2 = 2^2N, 
	 * where N is the memory length. So a memory length of 5 
	 * would be 32 states and 32^2 = 1024 possible state 
	 * transitions. 
	 * 
	 */
	public static int MEMORY_LENGTH = 5;
	
	/*
	 * The frequency at which trand predictions will be generated. 
	 */
	public static Map<String, Long> PREDICTION_FREQUENCY = new HashMap<String, Long>();
	static {
		PREDICTION_FREQUENCY.put("5 SECS", (long) (1000*5));
		PREDICTION_FREQUENCY.put("10 SECS", (long) (1000*10));
		PREDICTION_FREQUENCY.put("30 SECS", (long) (1000*30));
		PREDICTION_FREQUENCY.put("1 MIN", (long) (1000*60));
		PREDICTION_FREQUENCY.put("2 MINS", (long) (1000*60*2));
		PREDICTION_FREQUENCY.put("3 MINS", (long) (1000*60*3));
		PREDICTION_FREQUENCY.put("5 MINS", (long) (1000*60*5));
		PREDICTION_FREQUENCY.put("15 MINS", (long) (1000*60*15));
		PREDICTION_FREQUENCY.put("30 MINS", (long) (1000*60*30));
		PREDICTION_FREQUENCY.put("1 HOUR", (long) (1000*60*60));
		PREDICTION_FREQUENCY.put("1 DAY", (long) (1000*60*60*24));
	}
	
	/*
	 * How much time to lookback. Should mimic the tick time.  
	*/
	public static Map<String, Long> TRANSITION_LOOKBACK = new HashMap<String, Long>();
	static {
		TRANSITION_LOOKBACK.put("5 SECS", (long) (1000*5));
		TRANSITION_LOOKBACK.put("10 SECS", (long) (1000*10));
		TRANSITION_LOOKBACK.put("30 SECS", (long) (1000*30));
		TRANSITION_LOOKBACK.put("1 MIN", (long) (1000*60));
		TRANSITION_LOOKBACK.put("2 MINS", (long) (1000*60*2));
		TRANSITION_LOOKBACK.put("3 MINS", (long) (1000*60*3));
		TRANSITION_LOOKBACK.put("5 MINS", (long) (1000*60*5));
		TRANSITION_LOOKBACK.put("15 MINS", (long) (1000*60*15));
		TRANSITION_LOOKBACK.put("30 MINS", (long) (1000*60*30));
		TRANSITION_LOOKBACK.put("1 HOUR", (long) (1000*60*60));
		TRANSITION_LOOKBACK.put("1 DAY", (long) (1000*60*60*24));
	}
	
	/*
	 * Number of candidates to attached to at each preferential
	 * attachment step. 
	 */
	public static int CANDIDATE_ATTACHMENTS = 3;
	
	/*
	 * How often the network generation process should run.
	 *  
	 */
	public static int NETWORK_UPDATE_FREQUENCY = 1000 * 30; // Set to 30 seconds
	
	/*
	 * Minimal source weight used to bootstrap network
	 */
	public static double MINIMUM_SRCWEIGHT = 0.01D;
	
}
