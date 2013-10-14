package com.vodimo.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.vodimo.core.model.BackTestRunConfiguration;
import com.vodimo.core.model.Transition;
import com.vodimo.core.model.TransitionIndex;
import com.vodimo.core.util.MongoDBConnector;
import com.vodimo.core.util.VodimoDataBase;
import com.vodimo.core.util.VodimoModelConstants;
import com.vodimo.core.util.VodimoUtils;

public class TransitionGeneratorTest {

	@Test
	public void testTransitionsOnTestOptionsData() {
		BackTestRunConfiguration config = new BackTestRunConfiguration();
		try {
			config.setStartBackDate(VodimoUtils.parseSimpleDateAndTime("12/1/2011 04:00:00 PM"));
			config.setTrainingSteps(7); // 7 = two transitions
		} catch (Exception e) {
			System.out.println("BackTestRunConfiguration Load Failed: " + e.getMessage());
			System.exit(0);
		}			
		VodimoDataBase db = MongoDBConnector.newInstance();
		TransitionGenerator generator = new TransitionGenerator(db, config, new EntityNetworkGenerator(db));
		generator.run();
	}
	
	@Test
	public void testDelayEmbedding() {
		int[] volBitChain = {0,1,1,1,0,1}; //Memory Length 5
		List<Integer> transitionHashes = TransitionGenerator.createDelayEmbedding(volBitChain, VodimoModelConstants.MEMORY_LENGTH);
				
		int[] compare = {0,1,1,1,0,1,1,1,0,1}; //Memory Length 5
		
		TransitionIndex index = new TransitionIndex(VodimoModelConstants.MEMORY_LENGTH);
		for(int i=0;i<compare.length;i++) {
			if(compare[i] == 0) {				
			} else {
				index.flip(i);
			}
		}
		
		System.out.println("index = " + index.hashCode() + " : " + Integer.toBinaryString(index.hashCode()));
		
		int t1 = index.getFirstTransition();
		int t2 = index.getSecondTransition();
		
		System.out.println("t1 = " + t1 + " : " + Integer.toBinaryString(t1));
		System.out.println("t2 = " + t2 + " : "  + Integer.toBinaryString(t2));
		
		assertTrue(index.hashCode() == transitionHashes.get(0));	
	
	}	
	
	@Test
	public void testGetTransitions() {
		
		// Create transitions
		System.out.println("Getting DB...");
		VodimoDataBase db = MongoDBConnector.newInstance();		
		
		int numTransitionsPerEntity = 10;
		int numEntities = 3;
		
		Date now = new Date();
		
		Map<String, List<Transition>> transitionsInsert = new HashMap<String, List<Transition>>();
		System.out.println("Generating Fake Transitions...");
		for(int i=0;i<numEntities;i++) {
			String eid = "entity" + i;
			List<Transition> transitions = new ArrayList<Transition>();
			transitionsInsert.put(eid, transitions);
			for(int j=0;j<numTransitionsPerEntity;j++) {
				Transition t = new Transition();
				t.setEntityId(eid);
				t.setTransitionDate(now);
				int randomTransition = (int) Math.round(10000 * Math.random());
				t.setTransition(randomTransition);
				transitionsInsert.get(eid).add(t);
				
				System.out.println("Saving Transition " + t.toString());				
				db.saveTransition(t);
			}
		}
		
		Map<String, List<Transition>> transitions = db.getTransitions(new Date());
		Iterator<String> it = transitions.keySet().iterator();
		while(it.hasNext()){
			String eid = it.next();
			if(!transitions.get(eid).equals(transitionsInsert.get(eid))) assertTrue(false); 
		}
		
		assertTrue(true);
		
	}
	
	/*
	 * Test transition generation and multiple transitions chains (cross sessions). 
	 * And checks lots of bitwise operations for transition indices. 
	 */
	
	@Test
	public void testGenerateAndSaveTransitions() {		
		Map<String, Map<Long, Integer>> rawVolMap = new HashMap<String, Map<Long, Integer>>();
		String entityId = "entity1"; 
		
		Map<Long, Integer> volMap = new TreeMap<Long, Integer>();
				
		int numOfVols = 6;
		
		// Create date and roll back numOfVols days
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_YEAR, -numOfVols);
		
		int[] volBitChain = {0,1,1,1,0,1,0}; //Memory Length 5		
				
		for(int i=0;i<volBitChain.length;i++) {
			volMap.put(cal.getTimeInMillis(), volBitChain[i]);
			// Add a day
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		// To compare 
		String volBitCompare = "1110111011110111010";
		//System.out.println("volBitCompare = " + volBitCompare);		
		
		rawVolMap.put(entityId, volMap);
		
		TransitionGenerator generator = new TransitionGenerator(MongoDBConnector.newInstance(), null, null);
		Map<String, List<Integer>> entityTransitions = generator.generateAndSaveTransitions(rawVolMap);
		
		List<Integer> transitions = entityTransitions.get(entityId);
		StringBuffer sb = new StringBuffer();
		for(int t : transitions) {
			sb.append(Integer.toBinaryString(t));
		}
				
		assertTrue(volBitCompare.equals(sb.toString()));
		
		// Test prior transition
		
		int[] volBitChainNewTransition = {1};
		//volBitCompare += "1101010101";
		volBitCompare = "1101010101";
		System.out.println("\n\nvolBitCompare Step 2 = " + volBitCompare);
		
		volMap.clear();
		for(int i=0;i<volBitChainNewTransition.length;i++) {
			volMap.put(cal.getTimeInMillis(), volBitChainNewTransition[i]);
			// Add a day
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}		
		
		rawVolMap.clear();
		rawVolMap.put(entityId, volMap);
		entityTransitions = generator.generateAndSaveTransitions(rawVolMap);
		
		transitions = entityTransitions.get(entityId);
		sb = new StringBuffer();
		for(int t : transitions) {
			sb.append(Integer.toBinaryString(t));
		}
		
		assertTrue(volBitCompare.equals(sb.toString()));
		
	}
		
}
