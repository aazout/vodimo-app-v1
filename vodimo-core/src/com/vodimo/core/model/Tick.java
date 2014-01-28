package com.vodimo.core.model;

import java.util.Date;

import com.vodimo.core.util.VodimoConstants;

public class Tick {

	private Date timestamp;
	private Double ticker;
	
	public Tick(Date timestamp, Double ticker) {
		setTimestamp(timestamp);
		setTicker(ticker);
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public Double getTicker() {
		return ticker;
	}
	
	public void setTicker(Double ticker) {
		this.ticker = ticker;
	}
	
	public String serialize() {
		return (getTimestamp().getTime() + VodimoConstants.TICK_SEPARATOR_DATEPRICE + getTicker());
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Tick){
			Tick t = (Tick) o;
			if((t.getTimestamp().getTime() == getTimestamp().getTime()) && (t.getTicker().equals(getTicker()))) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
		
}
