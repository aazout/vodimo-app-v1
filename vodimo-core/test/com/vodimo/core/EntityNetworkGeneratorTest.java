package com.vodimo.core;

import org.junit.Test;

import com.vodimo.core.util.MongoDBConnector;

public class EntityNetworkGeneratorTest {

	@Test
	public void testEntityNetworkGenerator() {
		EntityNetworkGenerator generator = new EntityNetworkGenerator(MongoDBConnector.newInstance());
		generator.update(); // Set to update mode
		generator.run();
	}
	
}
