package com.vodimo.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

public class VodimoUtils {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	private static SimpleDateFormat sdf_in = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	
	private static SimpleDateFormat sdf_simpledate = new SimpleDateFormat("MM/dd/yyyy");
	private static SimpleDateFormat sdf_simpledateandtime = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
	
	public static Date parseSimpleDate(String date) throws ParseException {
		return sdf_simpledate.parse(date);
	}
	
	public static Date parseSimpleDateAndTime(String date) throws ParseException {
		return sdf_simpledateandtime.parse(date);
	}	
	
	public static String timestampToDateFormatted(long timestamp) {
		return sdf.format(new Date(timestamp));
	}
	
	public static String timestampToIBDateFormatted(long timestamp) {
		return sdf.format(new Date(timestamp));
	}	
	
	public static Date parseIBDateReturn(String str) throws ParseException {
		return sdf_in.parse(str);
	}
	
	public static Date parseIBDateStore(String str) throws ParseException {
		//return sdf_file.parse(str);
		return new Date(new Long(str));
	}
	
	public String getJSON(Object o) {
		Gson gson = new Gson();
		return gson.toJson(o); 		
	}
	
	public static Date getBeginningOfTime() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		// 100 years ago from today
		cal.add(Calendar.YEAR, -100);
		return cal.getTime();
	}
		
	public static String toStringAsBits(List<Integer> delayEmbedding) {
		StringBuffer sb = new StringBuffer();
		for(int i : delayEmbedding) {
			sb.append(Integer.toBinaryString(i));
			if(i < delayEmbedding.size()) sb.append("\n"); 
		}
		return sb.toString();
	}
	
	public static byte[] serialize(Object obj) {	        
	    try {
	    	ByteArrayOutputStream out = new ByteArrayOutputStream();	
	    	ObjectOutputStream os = new ObjectOutputStream(out);
			os.writeObject(obj);
			return out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	    
	}
	
	public static Object deserialize(byte[] data) {
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    
	    try {
	    	ObjectInputStream is = new ObjectInputStream(in);
			return is.readObject();
		} catch (Exception e) {		
			e.printStackTrace();
			return null;
		}
	}			
	
}
