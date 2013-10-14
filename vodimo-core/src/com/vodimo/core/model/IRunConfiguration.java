package com.vodimo.core.model;

import java.util.Date;

public interface IRunConfiguration {

	/*
	 * The date from which to start the training
	 */
	public Date getStartBackDate();
	
	/*
	 * The number of timesteps to train on 
	 */
	public int getTrainingSteps();
		
}
