package com.vodimo.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vodimo.core.model.IRunConfiguration;
import com.vodimo.core.model.Option;
import com.vodimo.core.model.Transition;
import com.vodimo.core.model.TransitionIndex;
import com.vodimo.core.util.OptionsMarketCalendarUtil;
import com.vodimo.core.util.VodimoDataBase;
import com.vodimo.core.util.VodimoException;
import com.vodimo.core.util.VodimoModelConstants;
import com.vodimo.core.util.VodimoUtils;
import com.vodimo.core.util.volatility.BlackScholesImpliedVolatility;
import com.vodimo.core.util.volatility.ImpliedVolatilityHelper;
import com.vodimo.core.util.volatility.RiskFreeRate;
import com.vodimo.core.util.volatility.TreasuryBillRiskFreeRate;
import com.vodimo.strategy.OptionStraddleStrategy;

public class TransitionGenerator implements Runnable {
	
	private static Logger logger = LogManager.getLogger(TransitionGenerator.class.getName());
	
	private VodimoDataBase db;
	
	private IRunConfiguration config;	
	
	private Map<String, Map<Long, Integer>> rawVolMap = new HashMap<String, Map<Long, Integer>>();
	private Map<String, Double> lastVolMap = new HashMap<String, Double>();
	
	private EntityNetworkUpdateListener entityNetworkListener;
	
	public TransitionGenerator(VodimoDataBase _db, IRunConfiguration config, EntityNetworkUpdateListener entityNetworkListener) {
		this.db = _db;
		this.config = config;
		this.entityNetworkListener = entityNetworkListener; 
	}	
	
	public void run() {
		
		logger.info("Starting TransitionGenerator Thread");
		
		// Get the latest time for which transitions ran. This will be used to get latest 
		// options market data.
		Date d;
		int trainingSteps = 0;
		if(config == null) {
			d = db.getVodimoStats().getLastTimeTransitionsRan();			
		} else {
			d = config.getStartBackDate();
			trainingSteps = config.getTrainingSteps();
		}
		if (d == null) d = VodimoUtils.getBeginningOfTime();
		
		
		if(trainingSteps > 0) {
			// This is a training run			
			int timesteps = 0;
			
			while(timesteps < trainingSteps) {
				/*
				 * Get the options with with closest time to expiration. This returns a map of 
				 * entityId -> List<Option> 
				 */		
				// FIXME: Make sure it is not a weekend or holiday
				Map<String, List<Option>> optionsMap = 
					this.db.getOptionsEntriesWithClosestTimeToExpiry(d, OptionStraddleStrategy.TIME_TO_EXPIRY);				
				
				logger.debug("Running training step for date {} and processing {} options", d, optionsMap.size());
				processOptions(optionsMap, d);								
				
				// FIXME: This should be adjusted to allow for other time frequencies
				d = OptionsMarketCalendarUtil.addTradingDay(d); // Add one trading day				
				timesteps++;
			}
			
			generateAndSaveTransitions(this.rawVolMap);
			
			// Toggle update the entity network
			this.entityNetworkListener.update();
			
		} else {
			while(true) {	
				Map<String, List<Option>> optionsMap = this.db.getOptionsEntriesWithClosestTimeToExpiry(d, OptionStraddleStrategy.TIME_TO_EXPIRY);
				if(optionsMap != null && !optionsMap.isEmpty()) {
					processOptions(optionsMap, d);
					generateAndSaveTransitions(this.rawVolMap);
					rawVolMap = new HashMap<String, Map<Long, Integer>>();
					
					// TODO: Add logic for entity network update
				} else {
					try {
						Thread.sleep(VodimoModelConstants.NETWORK_UPDATE_FREQUENCY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}				
		}
		
	}
	
	private void processOptions(Map<String, List<Option>> optionsMap, Date evaluationDate) {						 
				
		//QuantLibImpliedVolatilityHelper IVHelper = new QuantLibImpliedVolatilityHelper(evaluationDate);		
		
		Iterator<String> it = optionsMap.keySet().iterator();
		while(it.hasNext()) {
			
			String eid = it.next();			
			
			List<Option> optionsList = optionsMap.get(eid);
			
			// These are all the same because it is for the same quote date
			double lastUnderlyingPrice = optionsList.get(0).getUnderlyingLastPrice();			
			
			// Get the nearest ATM option and use that implied volatility
			Option call = OptionStraddleStrategy.getNearestATMOption(lastUnderlyingPrice, optionsList, Option.OPTION_CALL); 
			Option put = OptionStraddleStrategy.getNearestATMOption(lastUnderlyingPrice, optionsList, Option.OPTION_PUT); 
			
			// Get the risk free rate helper
			RiskFreeRate rfr = TreasuryBillRiskFreeRate.newInstance();
			
			// Initiate the implied volatility helper
			ImpliedVolatilityHelper IVHelper = new BlackScholesImpliedVolatility(call, put, rfr.getRiskFreeRate(evaluationDate));
			
			double vol;
			try {
				// Calculate implied volatility
				// FIXME: What about stocks that pay dividends, the IV calc is different				
				vol = IVHelper.getImpliedVolatility();
			} catch (VodimoException e) {
				//e.printStackTrace();
				continue; // Skip, something went wrong
			}

			// Get the quote date (market quote date)
			Date d = call.getQuoteDate();
					
			/*
			logger.debug("Adding Option for Processing --> entityId: " + eid 
					+ " quoteDate: " + d 
					+ " strike: " + call.getStrike()
					+ " underlyingLastPrice: " + call.getUnderlyingLastPrice()
					+ " implied vol: " + vol);*/
			
			// FIXME: Need to do better memory management or move to KV store
			if(rawVolMap.containsKey(eid)) {
				Map<Long, Integer> volMap = rawVolMap.get(eid);
				volMap.put(d.getTime(), getQuantizedVolDiffValue(vol, lastVolMap.get(eid)));
			} else {
				double lastVol = 0;
				Map<Long, Integer> volMap = new TreeMap<Long, Integer>();
				volMap.put(d.getTime(), getQuantizedVolDiffValue(vol, lastVol));
				rawVolMap.put(eid, volMap);
			}
			
			lastVolMap.put(eid, vol);			
		}

	}
	
	/*
	 * It has been found that quantizing a time series into discrete symbols reduces its noise. 
	 * Here we quantize into binary values where if the volatility changed positively = 1
	 * and negative = 0.
	 */
	private int getQuantizedVolDiffValue(double vol1, double vol2) {
		double volDiff = vol1 - vol2;
		if(volDiff >= 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Map<String, List<Integer>> generateAndSaveTransitions(Map<String, Map<Long, Integer>> rawVolMap) {		
						
		Iterator it = rawVolMap.keySet().iterator();
		Map<String, List<Integer>> retMap = new HashMap<String, List<Integer>>();
		while(it.hasNext()) {
			
			String entityId = (String) it.next();
			
			List<Integer> transitionBits = new ArrayList<Integer>();
			retMap.put(entityId, transitionBits);
			
			// Get the last transition from the VodimoDataBase (if it
			// exists, for chaining)
			Transition lastTransition = this.db.getLastTransition(entityId);
			logger.debug("Getting last transition for {} is {}", entityId, lastTransition);
			
			// The volMap is a map of time -> volatility difference
			Map<Long, Integer> volMap = rawVolMap.get(entityId);
			
			logger.debug("Generating and saving transitions for {} total is {}", entityId, volMap.size());			
			
			// The volBitChain is a vector of volatility differences
			//Integer[] volBitChain = (Integer[]) volMap.values().toArray();	
			int[] volBitChain = ArrayUtils.toPrimitive(Arrays.copyOf(
					volMap.values().toArray(), 
					volMap.values().toArray().length, 
					Integer[].class));
			
			//System.out.println("volBitChain = " + Arrays.toString(volBitChain));
			
			// The set of time indices
			long[] times = ArrayUtils.toPrimitive(Arrays.copyOf(
					volMap.keySet().toArray(), 
					volMap.keySet().toArray().length,
					Long[].class));						
			
			// Set first transition by chaining the last transition from prior run and thus
			// add MEMORY_LENGTH - 1 bits
			if(lastTransition != null) {
				TransitionIndex lastTransitionIndex = new TransitionIndex(VodimoModelConstants.MEMORY_LENGTH, lastTransition.getTransition());
				//logger.debug("lastTransitionIndex = " + Integer.toBinaryString(lastTransitionIndex.hashCode()));
				int[] lastTransitionBits = TransitionIndex.convertToIntegerArray(lastTransitionIndex.getSecondTransition());
				//logger.debug("lastTransitionBits = " + Arrays.toString(lastTransitionBits));
				volBitChain = ArrayUtils.addAll(lastTransitionBits, volBitChain);
				//logger.debug("lastTransitionBits + volBitChain = " + Arrays.toString(volBitChain));
			}
									
			//Need to now generate the delay embedding and loop through them
			List<Integer> delayEmbedding = TransitionGenerator.createDelayEmbedding(volBitChain, VodimoModelConstants.MEMORY_LENGTH);	
			//System.out.println("delayEmbedding bits = " + VodimoUtils.toStringAsBits(delayEmbedding));
			//System.out.println("delayEmbedding ints = " + Arrays.toString(delayEmbedding.toArray()));
			
			int counter = 0;
			for(int transition : delayEmbedding) {
				Transition t = new Transition();
				t.setEntityId(entityId);
				t.setTransition(transition);
				
				// Set the time 
				// If prior transition exists then first MEMORY_LENGTH times do not exist
				if(lastTransition == null) t.setTransitionDate(new Date(times[counter + VodimoModelConstants.MEMORY_LENGTH]));
				else t.setTransitionDate(new Date(times[counter]));
				
				//Save transition in VodimoDataBase
				db.saveTransition(t);					
				
				logger.debug("Saving Transition --> date: " + t.getTransitionDate() 
						+ " entityId:" + t.getEntityId() 
						+ " transitionBits: " 
						+ Integer.toBinaryString(transition));
								
				retMap.get(entityId).add(transition);
				
				counter++;
			}
		}	
		return retMap;
	}
	
	/*
	 * Create delay embedding using Volatilty Bit Chain
	 */
	public static List<Integer> createDelayEmbedding(int[] volBitChain, int memoryLength) {
		//System.out.println("Integer[] volBitChain length = " + volBitChain.length);
		
		// Holder for the transition hashes
		List<Integer> transitionsHashes = new ArrayList<Integer>();
		
		TransitionIndex index = new TransitionIndex(VodimoModelConstants.MEMORY_LENGTH);
		//If not enough transitions, return none (nothing to model)
		if(volBitChain.length <= memoryLength) return transitionsHashes;
		
		int pos = 0;
		// For N - memory_length iteration
		int loops = (volBitChain.length - memoryLength);
		for(int i=0;i<=loops;i++) {
			
			//System.out.println("Delay embedding shift = " + i);
									
			for(int j=0;j<memoryLength;j++) {								
				if(volBitChain[i + j] == 1) {
					//System.out.println("Flipping bit position = " + (pos + j));
					index.flip(pos + j);
					//System.out.println("index = " + Integer.toBinaryString(index.hashCode()));
				}
			}
			// Flip the appropriate TransitionIndex bit	if it is even
			if(i == 0) pos = memoryLength;
			
			//If it is not the first or the last transition
			//if(i%2 != 0) {
			if(i > 0) {	
				//System.out.println("Adding transition = " + Integer.toBinaryString(index.hashCode()));
				transitionsHashes.add(index.hashCode());
				index.shift();
			}
		}
		return transitionsHashes;
	}
	
}
