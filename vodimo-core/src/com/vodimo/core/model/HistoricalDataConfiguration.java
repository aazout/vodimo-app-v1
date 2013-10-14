package com.vodimo.core.model;

public class HistoricalDataConfiguration {

	private int num_days = 0;
	private String bar_size;
	private String duration;
	
	public int getNumDays() {
		return num_days;
	}
	public void setNumDays(int num_days) {
		this.num_days = num_days;
	}
	public String getBarSize() {
		return bar_size;
	}
	public void setBarSize(String bar_size) {
		this.bar_size = bar_size;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	
}
