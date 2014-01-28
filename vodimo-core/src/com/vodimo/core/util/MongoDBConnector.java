package com.vodimo.core.util;

import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import com.vodimo.core.model.OptionStraddle;
import com.vodimo.core.model.VodimoEntity;
import com.vodimo.core.model.Option;
import com.vodimo.core.model.OptionsEntry;
import com.vodimo.core.model.Transition;
import com.vodimo.core.model.ApplicationStats;

public class MongoDBConnector implements VodimoDataBase {

	private static Logger logger = LogManager.getLogger(MongoDBConnector.class.getName());
	
	private static MongoDBConnector connector;
	
	private DB db;
	
	private static String DB_NAME = "db";
	
	private MongoDBConnector(){}
	
	public static MongoDBConnector newInstance() {
		if(connector == null) {
			MongoClient mongoClient;
			try {
				mongoClient = new MongoClient(VodimoConstants.MONGODB_URL);
				DB _db = mongoClient.getDB(DB_NAME);				
				connector = new MongoDBConnector();
				connector.setDB(_db);
				return connector;
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			}						
		} else {
			return connector;
		}
	}
	
	private void setDB(DB _db) {
		this.db = _db;
	}
	
	@SuppressWarnings("rawtypes")
	private static Map<String, Object> getMapfromFields(Class clazz, Object o) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		//Get the class' keys
		Field[] fields = clazz.getFields();		
		for(int i=0;i<fields.length;i++) {
			try {				
				String name = fields[i].getName();
				map.put(name, fields[i].get(o));				
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}				
		return map;
	}
	
	/*
	 * Returns a map with collection name and BasicDBObject for insertion
	 */
	@SuppressWarnings("rawtypes")
	public static BasicDBObject convertToDBObject(Object o) {
		Class clazz = o.getClass();		
		return new BasicDBObject(getMapfromFields(clazz, o));
	}
	
	@SuppressWarnings("rawtypes")
	public static Object convertFromDBObject(String collectionName, DBObject o) {
		if(o == null) return null;
		Map map = o.toMap();
		Iterator it = map.keySet().iterator();
		try {
			Class clazz = Class.forName("com.vodimo.core.model." + collectionName);
			Object instance = clazz.newInstance();
			while(it.hasNext()) {
				String key = (String) it.next();
				try {
					Field f = clazz.getDeclaredField(key);
					f.set(instance, map.get(key));
				} catch (NoSuchFieldException nse) {}				
			}			
			return instance;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}

	@Override
	public void saveEntity(VodimoEntity e) {		
		DBCollection coll = db.getCollection(e.getClass().getSimpleName());
		coll.save(new Morphia().toDBObject(e));				
		
	}
	
	@Override
	public VodimoEntity getEntity(String entityId) {		
		Datastore ds = new Morphia().createDatastore(this.db.getMongo(), DB_NAME);
		return ds.createQuery(VodimoEntity.class)
				.filter("entityId", entityId)
				.get();					
	}	

	@Override
	public void saveOption(Option o) {
		DBCollection coll = db.getCollection(o.getClass().getSimpleName());
		//coll.insert(convertToDBObject(o));
		coll.save(new Morphia().toDBObject(o));		
	}
	
	@Override
	public void saveOptionsEntry(OptionsEntry entry) {				
		DBCollection coll = db.getCollection(entry.getClass().getSimpleName());
		coll.save(new Morphia().toDBObject(entry));			
		
	}	
	
	@Override
	public OptionsEntry getOptionsEntry(String entityId, Date quoteDate, Date expirationDate) {
		Datastore ds = new Morphia().createDatastore(this.db.getMongo(), DB_NAME);
		return ds.createQuery(OptionsEntry.class)
				.filter("entityId", entityId)
				.filter("quoteDate", quoteDate)				
				.filter("expirationDate", expirationDate)
				.get();
	}
	
	/*
	 * This method populates the VodimoKVStore with single option straddle (put/call) with closest time to timeToExpiry. There 
	 * should be one straddle per entityId. 
	 * 
	 * TODO: Does Morphia aggregate by entityId?
	 */
	public void getOptionsWithClosestTimeToExpiry(Date quoteDate, long timeToExpiry, VodimoKVStore<String, OptionStraddle> store) {
		//Map<String, List<Option>> optionsMap = new HashMap<String, List<Option>>();
		
		Datastore ds = new Morphia().createDatastore(this.db.getMongo(), DB_NAME);
		List<Option> optionsList = ds.createQuery(Option.class)
				.field("quoteDate").equal(quoteDate)
				.order("entityId") //sorted by entityId
				.order("expirationDate") //sorted by expiration
				.asList();
		
		logger.debug("getOptionsWithClosestTimeToExpiry:: quoteDate=" 
				+ quoteDate + ", timeToExpiry=" + (int) (timeToExpiry / (24L * 60L * 60L * 1000L)) + " size=" + optionsList.size());
		
		Set<String> entitySet = new HashSet<String>();
		for(Option o : optionsList) {
			entitySet.add(o.getEntityId());
						
			if(!store.containsKey(o.getEntityId())) {
				logger.debug("getOptionsWithClosestTimeToExpiry:: for {} comparing {} and {} = {}", 
						o.getEntityId(),
						o.getExpirationDate(),
						quoteDate,
						(int) ((o.getExpirationDate().getTime() - quoteDate.getTime()) / (24L * 60L * 60L * 1000L)));	
								
				if((o.getExpirationDate().getTime() - quoteDate.getTime()) >= timeToExpiry) {	
					logger.debug("getOptionsWithClosestTimeToExpiry:: adding {} for expirationDate {}", 
							o.getEntityId(),
							o.getExpirationDate());					
					//optionsMap.put(o.getEntityId(), o.getOptionList());
				}				
			}
		}
		
		//return optionsMap;
	}
	
	/*
	 * Gets the OptionsEntry with the nearest time to expiry greater than the minimum 
	 */
	@Override
	public Map<String, List<Option>> getOptionsEntriesWithClosestTimeToExpiry(Date quoteDate, long timeToExpiry) {
		
		// This map stored the entity ID and the list of viable options
		Map<String, List<Option>> optionsMap = new HashMap<String, List<Option>>();
		Datastore ds = new Morphia().createDatastore(this.db.getMongo(), DB_NAME);
		List<OptionsEntry> oeList = (List<OptionsEntry>) ds.createQuery(OptionsEntry.class)
				.field("quoteDate").equal(quoteDate)
				.order("expirationDate")
				.asList();
		logger.debug("getOptionsEntriesWithClosestTimeToExpiry:: quoteDate=" 
				+ quoteDate + ", timeToExpiry=" + (int) (timeToExpiry / (24L * 60L * 60L * 1000L)) + " size=" + oeList.size());		
		Set<String> entitySet = new HashSet<String>();
		for(OptionsEntry oe : oeList) {
			entitySet.add(oe.getEntityId());
			if(!optionsMap.containsKey(oe.getEntityId())) {
				
				logger.debug("getOptionsEntriesWithClosestTimeToExpiry:: for {} comparing {} and {} = {}", 
						oe.getEntityId(),
						oe.getExpirationDate(),
						quoteDate,
						(int) ((oe.getExpirationDate().getTime() - quoteDate.getTime()) / (24L * 60L * 60L * 1000L)));		
				if((oe.getExpirationDate().getTime() - quoteDate.getTime()) >= timeToExpiry) {	
					logger.debug("getOptionsEntriesWithClosestTimeToExpiry:: adding {} for expirationDate {}", 
							oe.getEntityId(),
							oe.getExpirationDate());					
					optionsMap.put(oe.getEntityId(), oe.getOptionList());
				}
			} else {
				//Skip because we have the correct options for the entity already
			}
		}
		logger.debug("getOptionsEntriesWithClosestTimeToExpiry:: total unique entities {} missing {}", 
				optionsMap.keySet().size(), entitySet.size() - (optionsMap.keySet().size()));				
		return optionsMap;
	}	

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<Option>> getOptionsEntriesWithClosestTimeToExpiryGTDate(Date startDate, long timeToExpiry) {
		Map<String, List<Option>> optionsMap = new HashMap<String, List<Option>>();
		Datastore ds = new Morphia().createDatastore(this.db.getMongo(), DB_NAME);
		QueryResults<OptionsEntry> results = (QueryResults<OptionsEntry>) ds.createQuery(OptionsEntry.class)
				.filter("quoteDate > ", startDate)
				.order("expirationDate")
				.get();		
		Iterator<OptionsEntry> it = results.iterator();
		while(it.hasNext()) {
			OptionsEntry oe = it.next();
			if((oe.getExpirationDate().getTime() - oe.getQuoteDate().getTime()) >= timeToExpiry) {
				optionsMap.put(oe.getEntityId(), oe.getOptionList());
				break; // Break because we got the option list we needed (closest to time to expiry)
			}
		}			
		return optionsMap;
	}	
	

	@Override
	public void saveTransition(Transition t) {
		Morphia morphia = new Morphia();
		DBCollection coll = db.getCollection(t.getClass().getSimpleName());
		coll.save(morphia.toDBObject(t));	
	}

	@Override
	public void updateVodimoStats(ApplicationStats s) {
		DBCollection coll = db.getCollection(s.getClass().getSimpleName());
		// First drop the existing stats
		coll.drop();
		coll.insert(convertToDBObject(s));		
	}

	@Override
	public ApplicationStats getVodimoStats() {
		DBCollection coll = db.getCollection(ApplicationStats.class.getSimpleName());
		DBObject result = coll.findOne();
		return (ApplicationStats) convertFromDBObject(coll.getName(), result);
	}
		
	public Map<Long, List<Transition>> getUnModeledTransitions() {
		Map<Long, List<Transition>> transitionsMap = new TreeMap<Long, List<Transition>>();
		Datastore ds = new Morphia().createDatastore(this.db.getMongo(), DB_NAME);
		List<Transition> transitions = ds.createQuery(Transition.class)
				.field("modeled").equal(false)
				.order("transitionDate")
				.asList();		
		
		for(Transition t : transitions) {
			
			logger.debug("getUnModeledTransitions():: entityId: " + t.getEntityId() 
					+ " transitionDate: " + t.getTransitionDate());	
			
			List<Transition> transitionList;
			if(transitionsMap.containsKey(t.getTransitionDate().getTime())) {
				transitionList = transitionsMap.get(t.getTransitionDate().getTime());
				transitionList.add(t);
			} else {
				transitionList = new ArrayList<Transition>();
				transitionList.add(t);
				transitionsMap.put(t.getTransitionDate().getTime(), transitionList);
			}
		}		
		return transitionsMap;		
	}	
		
	public Map<String, List<Transition>> getTransitions(Date d) {
		
		//db.Transition.aggregate({$group:{_id:"$entityId",transitions:{$addToSet:"$transition"}}})				
		Map<String, List<Transition>> map = new HashMap<String, List<Transition>>();
		
		Datastore ds = new Morphia().createDatastore(this.db.getMongo(), DB_NAME);
		List<Transition> transitions = ds.createQuery(Transition.class)
				.field("modeled").equal(true)
				.field("transitionDate").equal(d)
				.order("-transitionDate")
				.asList();			
		
		for(Transition t : transitions) {
			String eid = t.getEntityId();
			
			if(map.containsKey(eid)) {
				map.get(eid).add(t);
			} else {
				List<Transition> list = new ArrayList<Transition>();
				list.add(t);
				map.put(eid, list);					
			}
		}			
		return map;		
	}
	
	public Map<String, List<Transition>> getTransitionsByDateAndTransition(Date d, int transition) {
		
						
		Map<String, List<Transition>> map = new HashMap<String, List<Transition>>();
		
		Datastore ds = new Morphia().createDatastore(this.db.getMongo(), DB_NAME);
		List<Transition> transitions = ds.createQuery(Transition.class)
				.field("modeled").equal(true)
				.field("transitionDate").equal(d)
				.field("transition").equal(transition)
				.order("-transitionDate")
				.asList();			
		
		System.out.println("getTransitionsByDateAndTransition():: d: " + d + " transition: " + transition + " found: " + transitions.size());
		
		for(Transition t : transitions) {
			String eid = t.getEntityId();
			
			if(map.containsKey(eid)) {
				map.get(eid).add(t);
			} else {
				List<Transition> list = new ArrayList<Transition>();
				list.add(t);
				map.put(eid, list);					
			}
		}			
		return map;		
	}	

	@Override
	public Transition getLastTransition(String entityId) {
		Datastore ds = new Morphia().createDatastore(this.db.getMongo(), DB_NAME);
		return ds.createQuery(Transition.class)
				.filter("entityId", entityId)
				.order("-timestamp")
				.get();			
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateTransitionToModeled(Transition t) {
		Datastore ds = new Morphia().createDatastore(this.db.getMongo(), DB_NAME);
		UpdateOperations<Transition> updates = (UpdateOperations<Transition>) ds.createUpdateOperations(t.getClass())
				.set("modeled", true);
		ds.update(t, updates);
	}

	@Override
	public List<VodimoEntity> getEntities() {
		Datastore ds = new Morphia().createDatastore(this.db.getMongo(), DB_NAME);
		return ds.createQuery(VodimoEntity.class)
				.asList();						
	}

	@Override
	public Map<String, Integer> getLastTransitions(List<String> entityIds) {
		Map<String, Integer> retMap = new HashMap<String, Integer>();
		Datastore ds = new Morphia().createDatastore(this.db.getMongo(), DB_NAME);
		logger.debug("getLastTransitions() " + Arrays.toString(entityIds.toArray()));
		if(entityIds != null & entityIds.size() > 0) {
			List<Transition> transitions = ds.createQuery(Transition.class)
					.field("entityId").hasAnyOf(entityIds)
					.order("-timestamp")
					.asList();				
			if(transitions != null) {
				for(Transition t : transitions) {
					String entityId = t.getEntityId();
					// Only store the first transition (the latest one)
					if(!retMap.containsKey(entityId)) {
						logger.debug("getLastTransitions() Addings transition {} {}", entityId, t.getTransitionDate());
						retMap.put(entityId, t.getTransition());
					} 
				}
			}
		}
		return retMap;
	}		
	
}
