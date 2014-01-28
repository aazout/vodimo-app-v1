package com.vodimo.core.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vodimo.core.model.VodimoEntity;
import com.vodimo.core.model.Transition;

public class ProbabilisticAttachmentHelper {

	//private static Logger logger = LogManager.getLogger(ProbabilisticAttachmentHelper.class.getName());
	
	private VodimoDataBase db;
	
	//private Set<VodimoEntity> entitySet = new HashSet<VodimoEntity>();
	
	public ProbabilisticAttachmentHelper(VodimoDataBase db) {
		this.db = db;
	}
	
	/*
	 * Get a bunch of attachment candidates from prior transitions
	 */
	public void generateAttachment(String sinkEntityId, Map<String, List<Transition>> priorTransitions, int numOfAttachments) {
		
		//List<VodimoEntity> entities = new ArrayList<VodimoEntity>();		
		Map<String, VodimoEntity> entityMap = new HashMap<String, VodimoEntity>();
		
		double maxSourceWeight = 0;
		Iterator<String> it = priorTransitions.keySet().iterator();
		while(it.hasNext()) {			
			
			String entityId = it.next();
			
			double srcWeight = VodimoModelConstants.MINIMUM_SRCWEIGHT;
			VodimoEntity entity = this.db.getEntity(entityId);
						
			if(entity != null) {								
				if(entity.getSrcWeight() != -1D) { // TODO: Should this be 0
					srcWeight = entity.getSrcWeight();
				} else {
					entity.setSrcWeight(srcWeight);	
					// Save the new source weight
					this.db.saveEntity(entity);
				}
			} else {
				//TODO: Will this ever happen?
				entity = new VodimoEntity();
				entity.setEntityId(entityId);
				entity.setSrcWeight(srcWeight);
				this.db.saveEntity(entity);
			}
			
			if(srcWeight > maxSourceWeight) maxSourceWeight = srcWeight;
			entityMap.put(entityId, entity);
		}
		
		// Normalize by maximum
		Iterator<String> it_entities = entityMap.keySet().iterator();
		while(it_entities.hasNext()) {
			
			String sourceEntityId = it_entities.next();			
			
			// TODO: Add dates to Edge
			VodimoEntity sourceEntity = entityMap.get(sourceEntityId);
			double weight = sourceEntity.getSrcWeight() / maxSourceWeight;
			
			//System.out.println("Testing probabilistic attachment for entityId: " + sourceEntityId + " srcWeight: " + weight);
			
			double random = Math.random();
			//System.out.println("Testing against random: " + random + " srcWeight: " + weight);
			
			// Probabilistically attach
			if(weight >= random) {
				// (1) Update sink's edges and save
				//System.out.println("Incrementing source on sink sourceEntityId: " + sourceEntity.getEntityId());
				VodimoEntity sinkEntity = this.db.getEntity(sinkEntityId);
				sinkEntity.incrementEdge(sourceEntity.getEntityId());
				this.db.saveEntity(sinkEntity);

				// (2) Update source score and save
				double newSourceWeight = sinkEntity.getEdge(sourceEntity.getEntityId()).getFrequency() / sinkEntity.getEdgeCount();
				sourceEntity.addToSourceWeight(newSourceWeight);				
				this.db.saveEntity(sourceEntity);
				//logger.debug("Incrementing source weight on {} sourceEntityId: {} sourceWeih" + sourceEntityId + " adding to srcWeight: " + newSourceWeight);
			}
		}
	}

}
