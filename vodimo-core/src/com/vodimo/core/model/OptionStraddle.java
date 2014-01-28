package com.vodimo.core.model;

import java.io.Serializable;

public class OptionStraddle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -83783317667781885L;
	
	private Option put;
	private Option call;
	
	public Option getPut() {
		return put;
	}
	public void setPut(Option put) {
		this.put = put;
	}
	public Option getCall() {
		return call;
	}
	public void setCall(Option call) {
		this.call = call;
	}
	
}
