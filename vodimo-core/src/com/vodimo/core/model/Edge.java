package com.vodimo.core.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity("Edge")
public class Edge {

    @Id
    private ObjectId id;	
	
	public String srcId;
	
	public Date lastUpdatedDated;
	
	public int frequency = 0;

	public String getSrcId() {
		return srcId;
	}

	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}

	public Date getLastUpdatedDated() {
		return lastUpdatedDated;
	}

	public void setLastUpdatedDated(Date lastUpdatedDated) {
		this.lastUpdatedDated = lastUpdatedDated;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
	
	@Override
	public int hashCode(){
		return this.getSrcId().hashCode();
	}
	
	public void increment() {
		this.frequency++;
	}
}
