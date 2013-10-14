package com.vodimo.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vodimo.core.model.BackTestRunConfiguration;
import com.vodimo.core.util.MongoDBConnector;
import com.vodimo.core.util.VodimoUtils;
import com.vodimo.io.HistoricalOptionDataCSVReader;
import com.vodimo.io.LocalFileLoader;

/*
 * This is the core Vodimo application class. The class allows you to set whether you want to reload the data, run backtest, or run normally. 
 */
public class VodimoApplication {
	
	private static Logger logger = LogManager.getLogger(VodimoApplication.class.getName());
	
	public static void main(String[] args) {
		
		// Reload historical data into VodimoDataBase 
		if(args[0].equals("reload")) {
			logger.info("Loading VodimoApplication -Reload"); 
			
			// (1) Launch historical options data populator			
			try {				
				new LocalFileLoader(
					args[1], 
					MongoDBConnector.newInstance(), 
					new HistoricalOptionDataCSVReader()
					).load();
				/*
				new S3FileLoader(
						args[1], 
						MongoDBConnector.newInstance(), 
						new HistoricalOptionDataCSVReader()
						).load();
						*/
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Options File Load Failed", e);
				System.exit(0);
			}			
		} 
		else if (args[0].equals("backtest")) {
			logger.info("Loading VodimoApplication -Backtest");
			
			// Create run configuraton
			BackTestRunConfiguration config = new BackTestRunConfiguration();
			try {
				// Set the start back date
				config.setStartBackDate(VodimoUtils.parseSimpleDate(args[1]));
				
				/* 
					Set the # of training steps - this is the number of timesteps
					for running the network generation (i.e. ticks). 
				*/
				config.setTrainingSteps(Integer.parseInt(args[2]));
			} catch (Exception e) {
				logger.error("BackTestRunConfiguration Load Failed", e);
				e.printStackTrace();
				System.exit(0);
			}	
			
			// Start the entity network generator thread
			EntityNetworkGenerator entityGenerator = new EntityNetworkGenerator(MongoDBConnector.newInstance(), config);
			new Thread(entityGenerator).start();
			
			// Start the transition generator thread and pass in referer to entity network generator
			// TODO: Does this need to be its own thread?
			(new Thread(new TransitionGenerator(MongoDBConnector.newInstance(), config, entityGenerator))).start();
						
			// Start the prediction generator
			(new Thread(new PredictionGenerator(MongoDBConnector.newInstance(), config))).start();
			
		} 
		else {
			// Run normally
			// (1) Get contract prices from last population timestamps in VodimoDatabase
			
			// (2) Build/update entity graph (asyncrhronous process) 
			// (3) Deliver predictions
		}				
	}
	
}
