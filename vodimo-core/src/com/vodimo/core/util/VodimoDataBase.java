package com.vodimo.core.util;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.vodimo.core.model.OptionStraddle;
import com.vodimo.core.model.VodimoEntity;
import com.vodimo.core.model.Option;
import com.vodimo.core.model.OptionsEntry;
import com.vodimo.core.model.Transition;
import com.vodimo.core.model.ApplicationStats;

public interface VodimoDataBase {

	/*
	 * Save an entity to the database
	 */
	public void saveEntity(VodimoEntity e);
	
	public VodimoEntity getEntity(String entityId);
	
	/*
	 * Save and option to the database
	 */
	public void saveOption(Option o);
	
	/*
	 * Save a transition to the database
	 */
	public void saveTransition(Transition t);
	
	/*
	 * Save VodimoStats
	 */
	public void updateVodimoStats(ApplicationStats s);
	
	public ApplicationStats getVodimoStats();
	
	//public Map<Long, Map<String, List<OptionChain>>> getOptions(Date d);
	
	public Map<String, List<Transition>> getTransitions(Date d);
	
	public Map<String, List<Transition>> getTransitionsByDateAndTransition(Date d, int transition);
	
	public Transition getLastTransition(String entityId);
	
	public void updateTransitionToModeled(Transition t);

	public void saveOptionsEntry(OptionsEntry entry);
	
	public OptionsEntry getOptionsEntry(String entityId, Date quoteDate, Date expirationDate); 
	
	public void getOptionsWithClosestTimeToExpiry(Date quoteDate, long timeToExpiry, VodimoKVStore<String, OptionStraddle> store);
	
	/*
	 * Get the options which match a particular quoteDate. This method will return only the Options
	 * that mature the fastest (i.e. the next maturity date, or the first term in the term structure).
	 * We will be building our models on these maturities as they are best representative of the 
	 * current implied volatility. 
	 */
	public Map<String, List<Option>> getOptionsEntriesWithClosestTimeToExpiry(Date quoteDate, long timeToExpiry); 
	
	public Map<String, List<Option>> getOptionsEntriesWithClosestTimeToExpiryGTDate(Date startDate, long timeToExpiry);

	public Map<Long, List<Transition>> getUnModeledTransitions(); 
	
	public List<VodimoEntity> getEntities();
	
	public Map<String, Integer> getLastTransitions(List<String> entityIds);
	
}
