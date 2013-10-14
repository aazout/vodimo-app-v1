package com.vodimo.core.util;

import java.util.Date;
import com.vodimo.core.model.VodimoEntity;
import com.vodimo.core.model.ApplicationStats;

import static org.junit.Assert.*;
import org.junit.Test;

public class MongoDBConnectorTest {
	
	@Test
	public void testMongoDBInsertion() {		
		MongoDBConnector connector = MongoDBConnector.newInstance();
		
		// Test Entity insertion
		VodimoEntity entity = new VodimoEntity();
		entity.setEntityId("Entity1");
		entity.setSrcWeight(1.00);
		//entity.setEdge("Entity2", 0.50);		
		
		connector.saveEntity(entity);
		
		assertTrue(true);
	}

	@Test
	public void testMongoDBQuery() {
		MongoDBConnector connector = MongoDBConnector.newInstance();
		
		ApplicationStats stats = new ApplicationStats();
		stats.setLastTimeTransitionsRan(new Date());
		stats.setLastTimeModelRan(new Date());
		
		connector.updateVodimoStats(stats);	
		
		stats = connector.getVodimoStats();
		System.out.println(stats.toString());
		
		assertTrue(true);		
	}
}
