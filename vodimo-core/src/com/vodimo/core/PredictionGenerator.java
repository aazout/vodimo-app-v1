package com.vodimo.core;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vodimo.core.model.Edge;
import com.vodimo.core.model.IRunConfiguration;
import com.vodimo.core.model.TransitionIndex;
import com.vodimo.core.model.VodimoEntity;
import com.vodimo.core.util.VodimoDataBase;
import com.vodimo.core.util.VodimoModelConstants;

public class PredictionGenerator implements Runnable {

	private static Logger logger = LogManager.getLogger(PredictionGenerator.class.getName());
	
	private VodimoDataBase db;
	
	//private IRunConfiguration config;	
	
	public PredictionGenerator(VodimoDataBase _db, IRunConfiguration config) {
		this.db = _db;
		//this.config = config;
	}
	
	@Override
	public void run() {
		logger.info("Starting PredictionGenerator");
		while(true) {
			List<VodimoEntity> entities = this.db.getEntities();	
			//logger.debug("Getting entities " + entities.size());
			if(entities != null & entities.size() > 0) {
				// Loop through all entities
				for(VodimoEntity entity : entities) {
					
					// FIXME: Needs to be updated to allow for a time-window
					// Get the entity's sources' last transitions					
					logger.debug("Getting sources for entity " + entity.getEntityId() + " sources " + entity.getSources().size());
					Map<String, Integer> sourceTransitions = this.db.getLastTransitions(entity.getSources());
					
					int[] bits = getLastBits(sourceTransitions);
					logger.debug("getLastBits() " + Arrays.toString(bits));
					
					double[] weights = getSourceWeights(entity);
					logger.debug("getSourceWeights() " + Arrays.toString(weights));
					
					double prediction = innerProduct(bits, weights);	
					logger.debug("Prediction {}", prediction);
				}
				try {
					//Thread.sleep(VodimoModelConstants.PREDICTION_FREQUENCY.get("1 DAY"));
					Thread.sleep(VodimoModelConstants.PREDICTION_FREQUENCY.get("5 SECS")); // For testing
				} catch (InterruptedException e) {
					logger.error(e);
				}				
			}
		}		
	}
	
	private double innerProduct(int[] bits, double[] weights) {
		double scalar = 0;
		for(int i=0;i<bits.length;i++) {
			scalar += bits[i] * weights[i];
		}
		return scalar;
	}
	
	private double[] getSourceWeights(VodimoEntity entity) {
		List<Edge> edges = entity.getEdges();
		double[] sourceWeights = new double[entity.getEdges().size()]; 
		int sum = 0;
		// Get sums
		int counter = 0;
		for(Edge e : edges) {
			sum += e.getFrequency();
			sourceWeights[counter] = e.getFrequency();
			counter++;
		}
		// Normalize sums
		for(int i=0;i<sourceWeights.length;i++) {
			sourceWeights[i] = sourceWeights[i] / sum;
  		}
		return sourceWeights;
	}
	
	private int[] getLastBits(Map<String, Integer> sourceTransitions) {
		Iterator<String> it = sourceTransitions.keySet().iterator();
		int[] bits = new int[sourceTransitions.size()];
		int counter = 0;
		while(it.hasNext()) {
			String entityId = it.next();
			int transition = sourceTransitions.get(entityId);
			TransitionIndex index = new TransitionIndex(VodimoModelConstants.MEMORY_LENGTH, transition);
			int bit = index.getLastBit();
			bits[counter] = bit;
			counter++;
		}
		return bits;
	}
	
}
