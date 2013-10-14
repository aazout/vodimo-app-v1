package com.vodimo.core.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity("VodimoEntity")
public class VodimoEntity {
	
    @Id
    private ObjectId id;	
	
	/*
	 * The unique identifier for the entity (i.e. symbol)
	 */
	public String entityId;
	
	/*
	 * The source score for this entity.
	 */
	public double srcWeight = -1D;	
	
	@Embedded(concreteClass = java.util.ArrayList.class)
	public List<Edge> sources = new ArrayList<Edge>();	
	
	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public double getSrcWeight() {
		return srcWeight;
	}

	public void setSrcWeight(double srcWeight) {
		this.srcWeight = srcWeight;
	}		

	public List<Edge>  getEdges() {
		return sources;
	}

	public void setEdges(List<Edge>  edges) {
		this.sources = edges;
	}
	
	public void addEdge(Edge e) {
		this.sources.add(e);
	}

	public boolean hasEdge(String srcId) {
		for(Edge e : getEdges()) {
			if(e.getSrcId().equals(srcId)) return true;
		}
		return false;
	}
	
	public void incrementEdge(String srcId) {
		Edge e;
		if((e = getEdge(srcId)) == null) {
			e = new Edge();
			e.setSrcId(srcId);
			e.increment();
			addEdge(e);
		} else {
			e.increment();
		}
	}
	
	public Edge getEdge(String srcId) {
		for(Edge e : getEdges()) {
			if(e.getSrcId().equals(srcId)) return e;
		}
		return null;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public int getEdgeCount() {
		int sum = 0;
		for(Edge e : getEdges()) {
			sum += e.getFrequency();
		}
		return sum;
	}
	
	public void addToSourceWeight(double weight) {
		this.srcWeight += weight;
	}
	
	public List<String> getSources() {
		List<String> entityIds = new ArrayList<String>();
		for(Edge e : getEdges()) {
			entityIds.add(e.getSrcId());
		}
		return entityIds;
	}
		
}
