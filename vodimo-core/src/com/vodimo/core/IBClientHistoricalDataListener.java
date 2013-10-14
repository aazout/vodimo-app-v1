package com.vodimo.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.vodimo.core.model.HistoricalData;

public class IBClientHistoricalDataListener {
	
	private Map<Integer, List<HistoricalData>> queue = new ConcurrentHashMap<Integer, List<HistoricalData>>();
	
	public void historicalDataEvent(int reqId, String date, double open, double high, 
			double low, double close, int volume, int count, double WAP, boolean hasGaps) {
		HistoricalData data = new HistoricalData();
		data.setReqId(reqId);
		data.setDate(date);
		data.setOpen(open);
		data.setHigh(high);
		data.setLow(low);
		data.setClose(close);
		data.setVolume(volume);
		data.setCount(count);
		data.setWAP(WAP);
		data.setHasGaps(hasGaps);
		if(containsKey(reqId)) {
			List<HistoricalData> list = get(reqId);
			list.add(data);
			put(reqId, list);
		} else {
			List<HistoricalData> list = Collections.synchronizedList(new ArrayList<HistoricalData>());
			list.add(data);
			put(reqId, list);
		}
	}
	
	private synchronized boolean containsKey(int i) {
		return this.queue.containsKey(i);
	}
	
	private synchronized void put(int i, List<HistoricalData> list) {
		this.queue.put(i, list);
	}
	
	public synchronized void remove(int i) {
		this.queue.remove(i);
	}
	
	public boolean hasMoreData() {
		for(int key : getAllKeys()) {
			if(get(key).size() > 0) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public int queueSize() {
		return queue.size();
	}
	
	public synchronized List<HistoricalData> get(int i) {
		return queue.get(i);
	}
	
	public synchronized Set<Integer> getAllKeys() {
		return queue.keySet();
	}
	
}
