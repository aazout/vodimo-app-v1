package com.vodimo.core.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity("Transition")
public class Transition {
	
    @Id
    private ObjectId id;	
	
	public Date transitionDate;
	
	/*
	 * The hash of the transition hash(x1,x2)
	 */
	public int transition;
	
	/*
	 * The unique identifier for the entity (i.e. symbol)
	 */
	public String entityId;
	
	/*
	 * Whether the transition has been modeled
	 */
	public boolean modeled = false; 

	public int getTransition() {
		return transition;
	}

	public void setTransition(int transition) {
		this.transition = transition;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public boolean isModeled() {
		return modeled;
	}

	public void setModeled(boolean modeled) {
		this.modeled = modeled;
	}	
	
	public Date getTransitionDate() {
		return transitionDate;
	}

	public void setTransitionDate(Date transitionDate) {
		this.transitionDate = transitionDate;
	}

	public String toString() {
		return "entityId: " + entityId + ", transition: " + transition;
	}
	
	@Override
	public int hashCode() {
		return transition;
	}
	
}
