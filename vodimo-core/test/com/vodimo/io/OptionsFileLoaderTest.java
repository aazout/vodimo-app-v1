package com.vodimo.io;

import org.junit.Test;

import com.vodimo.core.util.MongoDBConnector;

public class OptionsFileLoaderTest {

	@Test
	public void testS3FileLoader() {

		OptionsFileLoader loader = new S3FileLoader(
				"OptionsData2012", 
				MongoDBConnector.newInstance(), 
				new HistoricalOptionDataCSVReader());
		
		try {
			loader.load();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}
	
}
