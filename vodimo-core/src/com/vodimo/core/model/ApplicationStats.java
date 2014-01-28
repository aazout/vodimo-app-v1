package com.vodimo.core.model;

import java.util.Date;

public class ApplicationStats {

	/*
	 * The last time the transition model ran
	 */
	public Date lastTimeModelRan;
	
	/*
	 * The last (trading) time for which we retrieved options prices. 
	 */
	public Date lastTimestampTransitionsRan;
	
	public int globalTransitionCount;

	public Date getLastTimeModelRan() {
		return lastTimeModelRan;
	}

	public void setLastTimeModelRan(Date lastTimeModelRan) {
		this.lastTimeModelRan = lastTimeModelRan;
	}	
	
	public Date getLastTimeTransitionsRan() {
		return lastTimestampTransitionsRan;
	}

	public void setLastTimeTransitionsRan(Date lastTimeTransitionsRan) {
		this.lastTimestampTransitionsRan = lastTimeTransitionsRan;
	}

	@Override
	public String toString() {
		return "VodimoStats: lastTimeModelRan="+lastTimeModelRan.toString() + 
				" lastTimeGotOptionsProces=" + lastTimestampTransitionsRan.toString(); 
	}

	public int getGlobalTransitionCount() {
		return globalTransitionCount;
	}

	public void setGlobalTransitionCount(int globalTransitionCount) {
		this.globalTransitionCount = globalTransitionCount;
	}
	
}
