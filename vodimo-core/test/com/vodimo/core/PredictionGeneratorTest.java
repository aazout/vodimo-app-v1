package com.vodimo.core;

import org.junit.Test;

import com.vodimo.core.util.MongoDBConnector;

public class PredictionGeneratorTest {

	@Test
	public void testPredictionGenerator() {	
		PredictionGenerator generator = new PredictionGenerator(MongoDBConnector.newInstance(), null);		
		generator.run();
	}
	
}
