package com.vodimo.core.model;

import com.ib.client.Contract;

public class HistoricalDataRequest {

	/*
	 * The Id for the request. Must be a unique value. When the data is received, 
	 * it will be identified by this Id. This is also used when canceling 
	 * the historical data request.
	 */
	private int id;
	
	//This class contains attributes used to describe the contract.
	private Contract contract;
	

	//Use the format yyyymmdd hh:mm:ss tmz, where the time zone is allowed (optionally) after a space at the end.
	private String endDateTime;
	
	/*
	 * This is the time span the request will cover, and is specified using the format: <integer> <unit>
	 */
	private String durationStr;
	
	//Specifies the size of the bars that will be returned (within IB/TWS limits)
	private String barSizeSetting;
	
	//Determines the nature of data being extracted
	private String whatToShow;
	
	/*
	 * Determines whether to return all data available during the requested
	 *  time span, or only data that falls within regular trading hours
	 */
	private int useRTH;
	
	//Determines the date format applied to returned bars.
	private int formatDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public String getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(String endDateTime) {
		this.endDateTime = endDateTime;
	}

	public String getDurationStr() {
		return durationStr;
	}

	public void setDurationStr(String durationStr) {
		this.durationStr = durationStr;
	}

	public String getBarSizeSetting() {
		return barSizeSetting;
	}

	public void setBarSizeSetting(String barSizeSetting) {
		this.barSizeSetting = barSizeSetting;
	}

	public String getWhatToShow() {
		return whatToShow;
	}

	public void setWhatToShow(String whatToShow) {
		this.whatToShow = whatToShow;
	}

	public int getUseRTH() {
		return useRTH;
	}

	public void setUseRTH(int useRTH) {
		this.useRTH = useRTH;
	}

	public int getFormatDate() {
		return formatDate;
	}

	public void setFormatDate(int formatDate) {
		this.formatDate = formatDate;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof HistoricalDataRequest) {
			HistoricalDataRequest hdr = (HistoricalDataRequest) o;
			return (hdr.getId() == getId());
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "{ symbol: " + getContract().m_symbol + " endDataTime: " + endDateTime + " }";
	}
	
	public boolean equalsContract(Object o) {
		if(o instanceof HistoricalDataRequest) {
			HistoricalDataRequest hdr = (HistoricalDataRequest) o;
			//return (hdr.getContract().equals(getContract()));
			return (hdr.getContract().m_symbol.equals(getContract().m_symbol));
		} else {
			return false;
		}		
	}
	
}
