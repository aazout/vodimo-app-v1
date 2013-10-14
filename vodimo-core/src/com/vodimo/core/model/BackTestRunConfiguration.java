package com.vodimo.core.model;

import java.util.Date;

public class BackTestRunConfiguration implements IRunConfiguration {
	
	private Date startBackDate;
	
	private int trainingSteps;

	public Date getStartBackDate() {
		return startBackDate;
	}

	public void setStartBackDate(Date startBackDate) {
		this.startBackDate = startBackDate;
	}

	public int getTrainingSteps() {
		return trainingSteps;
	}

	public void setTrainingSteps(int trainingSteps) {
		this.trainingSteps = trainingSteps;
	}	

}
