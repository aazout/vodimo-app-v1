package com.vodimo.core.model;

import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import java.util.SortedMap;

import com.vodimo.core.util.VodimoUtils;

public class TimeSeries {
	
	private SortedMap<Long, Double> map = new TreeMap<Long, Double>(new DateComparator());
	
	public void add(Date timestamp, Double ticker) {	
		map.put(timestamp.getTime(), ticker);		
	}
	
	public Tick get(long timestamp) {
		return new Tick(new Date(timestamp), map.get(timestamp));
	}
	
	public Set<Long> keySet() {
		return this.map.keySet();
	}
	
	public int size() {
		//return tickers.size();
		return this.map.size();
	}
	
	public Double[] toArray() {
		Double[] arr = new Double[size()];
		int counter = 0;
		for(Long key : keySet()) {
			arr[counter] = map.get(key);
			counter++;
		}
		return arr;
	}
	
	public boolean containsKey(Object o) {
		return this.map.containsKey(o);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof TimeSeries) {			
			TimeSeries s = (TimeSeries) o;
			if(s.size() != size()) return false;
			for(Long key : s.keySet()) {
				if(!containsKey(key)) return false;
				Tick tick1 = s.get(key);
				Tick tick2 = get(key);
				if(!tick1.equals(tick2)) return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	public String toString() {		
		StringBuffer sb = new StringBuffer();
		for(long key : map.keySet()) {
			sb.append("{" + VodimoUtils.timestampToIBDateFormatted(key) + " -> " + map.get(key) + "}\n");
		}
		return sb.toString();
	}
	
	static class DateComparator implements Comparator<Long> {

		@Override
		public int compare(Long d1, Long d2) {
			if(d1 < d2) {
				return -1;
			} else if(d1 > d2) {
				return 1;
			} else {
				return 0;
			}
		}
		
	}
	
}
