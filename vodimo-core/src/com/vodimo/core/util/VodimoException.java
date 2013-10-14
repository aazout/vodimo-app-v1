package com.vodimo.core.util;

public class VodimoException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public VodimoException(Exception e) {
		this.message = e.getMessage();
	}	
	
	public VodimoException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}
	
}
