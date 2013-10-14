package com.vodimo.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vodimo.core.model.IRunConfiguration;
import com.vodimo.core.model.VodimoEntity;
import com.vodimo.core.model.Transition;
import com.vodimo.core.util.OptionsMarketCalendarUtil;
import com.vodimo.core.util.ProbabilisticAttachmentHelper;
import com.vodimo.core.util.VodimoDataBase;
import com.vodimo.core.util.VodimoModelConstants;

public class EntityNetworkGenerator implements Runnable, EntityNetworkUpdateListener {

	private static Logger logger = LogManager.getLogger(EntityNetworkGenerator.class.getName());
	
	private VodimoDataBase db;
	
	//private IRunConfiguration config;
	
	private boolean update = false;
	
	public EntityNetworkGenerator(VodimoDataBase _db) {
		this.db = _db;
	}
	
	public EntityNetworkGenerator(VodimoDataBase _db, IRunConfiguration config) {
		this.db = _db;
		//this.config = config;
	}		
	
	/*
	 * Get the unmodeled transitions and update network. This should run at whatever frequency
	 * predictions will be run at. 
	 */
	@Override
	public void run() {		
		
		logger.info("Starting EntityNetworkGenerator Thread");
		
		Map<Long, List<Transition>> transitions;
		while(true) {	
			/*
			 * Gets a map of transitions, where transition time -> List<Transition> 
			 */
			if(update) {
				//logger.debug("Getting unmodeled transitions");
				if(!(transitions = db.getUnModeledTransitions()).isEmpty()) {
					// Get the latest transitions
					processTransitions(transitions);				
				} else {
					//logger.debug("No unmodeled transitions");
				}				
			}				
			//logger.debug("Sleeping EntityNetworkGenerator Thread");
			try {
				Thread.sleep(VodimoModelConstants.NETWORK_UPDATE_FREQUENCY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}		
	}
	
	private void processTransitions(Map<Long, List<Transition>> transitions) {
		
		// Resort the timesteps in ascending order
		Object[] timesteps = (Object[]) transitions.keySet().toArray();
		Arrays.sort(transitions.keySet().toArray(), Collections.reverseOrder());
				
		logger.debug("Processing transitions {}", transitions.size());
		
		// Each iteration is a time step
		//while(it.hasNext()) {
		for(int i=0;i<timesteps.length;i++) {
						
			long timestep = (Long) timesteps[i];			
			List<Transition> entityTransitions = transitions.get(timestep);
						
			if(entityTransitions != null && !entityTransitions.isEmpty()) {
				for(Transition t : entityTransitions) {
					
					VodimoEntity sinkEntity = this.db.getEntity(t.getEntityId());				
					
					logger.debug("Running network construction for sinkEntityId {} at time {}", t.getEntityId(), new Date(timestep));
					
					// If doesn't exist, create new entity and save
					if(sinkEntity == null) {
						sinkEntity = new VodimoEntity();
						sinkEntity.setEntityId(t.getEntityId());
						this.db.saveEntity(sinkEntity);						
					}					
					
					//TODO: MapReduce is probably a good candidate method here
					// (1) Search to see if any other entity had transitions at t - 1
					//TODO: Do we migrate calendar function to QuantLib
					Map<String, List<Transition>> priorTransitions = 
							this.db.getTransitionsByDateAndTransition(
									OptionsMarketCalendarUtil.subtractTradingDay(t.getTransitionDate()),
									t.getTransition());				
										
										
					// (2) Generate and save attachments
					ProbabilisticAttachmentHelper paHelper = new ProbabilisticAttachmentHelper(this.db);
					paHelper.generateAttachment(sinkEntity.getEntityId(), priorTransitions, VodimoModelConstants.CANDIDATE_ATTACHMENTS);
					
					this.db.updateTransitionToModeled(t);										
														
				}
			}
		}
		
		this.update = false;
	}
	
	@Override
	public void update() {
		this.update = true;
	}
		
}
