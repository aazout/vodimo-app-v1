package com.vodimo.core.model;

public class HistoricalData {

	//The ticker Id of the request to which this bar is responding.
	private int reqId;
	
	/*
	 * The date-time stamp of the start of the bar. The format is determined by the 
	 * reqHistoricalData() formatDate parameter.
	 */
	private String date;
	
	private double open;
	private double high;
	private double low;
	private double close;
	
	//The volume during the time covered by the bar.
	private int volume; 
	
	/* 
	 * When TRADES historical data is returned, represents the number of trades that 
	 * occurred during the time period the bar covers 
	*/
	private int count; 
	
	//The weighted average price during the time covered by the bar.
	private double WAP; 
	
	//Whether or not there are gaps in the data.
	private boolean hasGaps;

	public int getReqId() {
		return reqId;
	}

	public void setReqId(int reqId) {
		this.reqId = reqId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double getWAP() {
		return WAP;
	}

	public void setWAP(double wAP) {
		WAP = wAP;
	}

	public boolean isHasGaps() {
		return hasGaps;
	}

	public void setHasGaps(boolean hasGaps) {
		this.hasGaps = hasGaps;
	}
	
	@Override
	public boolean equals(Object o) {		
		if(o instanceof HistoricalData) {
			HistoricalData h = (HistoricalData) o;
			if(	h.getReqId() == getReqId() && 
				h.getDate().equals(getDate()) &&
				h.getClose() == getClose()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("HistoricalData: ");
		sb.append("{reqId: " + reqId + "} {date: ");
		sb.append(date + "} {open: ");
		sb.append(open + "} {high: ");
		sb.append(high + "} {low: ");
		sb.append(low + "} {close: ");
		sb.append(close + "}");
		return sb.toString();
	}
	
	
	
}
