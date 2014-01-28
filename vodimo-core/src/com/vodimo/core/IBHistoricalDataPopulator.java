package com.vodimo.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.ib.client.Contract;
import com.vodimo.core.model.HistoricalData;
import com.vodimo.core.model.HistoricalDataConfiguration;
import com.vodimo.core.model.HistoricalDataRequest;
import com.vodimo.core.model.TimeSeries;
import com.vodimo.core.util.FileIOManager;
import com.vodimo.core.util.VodimoUtils;

public class IBHistoricalDataPopulator implements HistoricalDataPopulator {

	private int id_count = 0;
	private IBClientHistoricalDataListener listener;
	private IBClientWrapper client;
	
	//IB API Call limitations
	//Max history
	//private static int MAX_YEAR = 1;
	//Identical request lag is 1 call every 15 seconds
	private static int MAX_LAG_MS = 1000 * 15;
	//private static int MAX_LAG_NUM = 1;
	//Identical contract request 6 calls every 2 seconds
	private static int MAX_LAG_SECURITY_MS = 1000 * 2;
	//private static int MAX_LAG_SECURITY_NUM = 6;
	private static int MAX_LAG_TOTALREQUEST_MS = 1000 * 60 * 10; //10 minutes	
	private HistoricalDataRequest lastRequest;
	private List<HistoricalDataRequest> hdrQueue = new LinkedList<HistoricalDataRequest>();
	
	private static String SECURITY_TYPE = "STK";
	private static Double DEFAULT_STRIKE = 0.0;
	private static String DEFAULT_EXCHANGE = "SMART";
	private static String DEFAULT_PRIMARY_EXCHANGE = "ISLAND";
	private static String DEFAULT_CURRENCY = "USD";
	private static String DEFAULT_WTS = "TRADES";
	private static int DEFAULT_USE_RTH = 1;
	private static int DEFAULT_FORMAT_DATE = 1;
	private static int DEFAULT_TRADING_DAYS = 6;
	
	private String barsize;
	private String duration;
	private int days;
	
	private Date now = new Date();
	private Calendar cal = Calendar.getInstance();
	
	protected Map<Integer, String> reqIDtoSymbol = new HashMap<Integer, String>();
	protected Map<String, TimeSeries> data = new HashMap<String, TimeSeries>();
		
	private int THREAD_SLEEP_TIME = 5000; //5 seconds
		
	public IBHistoricalDataPopulator(HistoricalDataConfiguration config) {
		this.barsize = config.getBarSize();
		this.duration = config.getDuration();
		this.days = (config.getNumDays() != 0 ? config.getNumDays() : DEFAULT_TRADING_DAYS);
		this.client = IBClientWrapper.newInstance();
		this.client.connect();	
		this.listener = new IBClientHistoricalDataListener();
		this.client.setListener(this.listener);			
		
		(new Thread(new HistoricalDataOrganizer(this.listener))).start();					
	}
			
	//TODO: Build contract manager to manage contracts
	public void populateHistoricalData(List<String> symbols) {
		System.out.println("Fetching historical data...");
		for(String symbol : symbols) {
			cal.setTime(now);//Reset calendar			
			for(int i=0; i<days; i++) {
				this.id_count++;
				HistoricalDataRequest hdr = new HistoricalDataRequest();
				Contract c = new Contract(id_count, symbol, SECURITY_TYPE, "", DEFAULT_STRIKE, "", "", 
						DEFAULT_EXCHANGE, DEFAULT_CURRENCY, "", null, DEFAULT_PRIMARY_EXCHANGE, false, "", "");
				hdr.setContract(c);		
				hdr.setBarSizeSetting((this.barsize != null ? this.barsize : "30 secs"));
				hdr.setDurationStr((this.duration != null ? this.duration : "1 D"));
				hdr.setId(id_count);
				hdr.setUseRTH(DEFAULT_USE_RTH);
				hdr.setWhatToShow(DEFAULT_WTS);
				String endDate = getCurrentTimeInFormat(1);				
				hdr.setEndDateTime(endDate);//yyyymmdd hh:mm:ss
				hdr.setFormatDate(DEFAULT_FORMAT_DATE);		
				this.reqIDtoSymbol.put(this.id_count, symbol);				
				this.hdrQueue.add(hdr);
				//System.out.println("Fetching security " + symbol + " for " + endDate.toString());
			}			
		}		
		runHDRQueue();
	}
	
	private void runHDRQueue() {
		int count = 1;
		for(HistoricalDataRequest hdr : this.hdrQueue) {			
			int waitMillis = getWaitTime(hdr, count);
			if(waitMillis == 0) {
				System.out.println("Requesting HistoricalData " + hdr);
				this.client.requestHistoricalData(hdr);
				this.lastRequest = hdr;
			} else {
				try {
					System.out.println("Sleeping runHDRQueue " + (waitMillis/1000)  + " seconds for pacing...");
					Thread.sleep(waitMillis);
					System.out.println("Requesting HistoricalData " + hdr);
					this.client.requestHistoricalData(hdr);
					this.lastRequest = hdr;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			count++;
		}			
	}
	
	private int getWaitTime(HistoricalDataRequest hdr, int count) {
		if(hdr.equals(this.lastRequest)) { //Probably should not happen
			return MAX_LAG_MS;
		} else if (hdr.equalsContract(this.lastRequest)) { //Should happen for bootstrap
			return MAX_LAG_SECURITY_MS;
		} else if (count%60 == 0) {
			return MAX_LAG_TOTALREQUEST_MS;
		} else {
			return 0;
		}
	}
	
	private String getCurrentTimeInFormat(int rewind) {		
		if(rewind == 0) {
			return VodimoUtils.timestampToDateFormatted(now.getTime());
		} else {
			//cal.setTime(now);
			cal.add(Calendar.DAY_OF_YEAR, -1 * rewind);
			if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)//Check for Sunday 
				cal.add(Calendar.DAY_OF_YEAR, -1);			
			return VodimoUtils.timestampToDateFormatted(cal.getTime().getTime());
		}
	}
		
	class HistoricalDataOrganizer implements Runnable {
		
		private IBClientHistoricalDataListener listener;
		private FileIOManager filemanager; 
		
		public HistoricalDataOrganizer(IBClientHistoricalDataListener listener) {
			System.out.println("Running HistoricalDataOrganizer thread...");
			this.listener = listener;
			this.filemanager = FileIOManager.newInstance();
		}
		
		@Override
		public void run() {
			while(true) {
				//if(!listener.isEmpty()) {
				//System.out.println("Listener has data: " + listener.hasMoreData());
				if(listener.hasMoreData()) {
					for(int key : this.listener.getAllKeys()) {
						String symbol = reqIDtoSymbol.get(key);						
						List<HistoricalData> list = this.listener.get(key);
						List<HistoricalData> clone = new ArrayList<HistoricalData>();
						clone.addAll(list);
						ListIterator<HistoricalData> it = clone.listIterator();						
						if(clone.size() == 0) continue;
						System.out.println("Processing data for symbol: " + symbol + " total ticks: " + clone.size());
						try{
							while(it.hasNext()) {
								HistoricalData item = it.next();
								if(!item.getDate().contains("finished")) { //Skip the finished book-end
									if(reqIDtoSymbol.containsKey(key)) {										
										try {
											if(data.containsKey(symbol)) {
												data.get(symbol).add(VodimoUtils.parseIBDateReturn(item.getDate()), item.getClose());
											} else {
												TimeSeries series = new TimeSeries();
												series.add(VodimoUtils.parseIBDateReturn(item.getDate()), item.getClose());
												data.put(symbol, series);
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}	
									/*
									try {
										System.out.println("Added " + symbol + " " + VodimoUtils.parseIBDateReturn(item.getDate()).getTime() + 
												" " + item.getClose());
									} catch(Exception e){}
									*/
								} 
								this.listener.get(key).remove(item);
							}
							//System.out.print("Got historical data for request: " + reqIDtoSymbol.get(key));
						} catch (ConcurrentModificationException cme) {
							cme.printStackTrace();
							break;
						}
					}					
				} else {
					try {
						Set<String> keys = new HashSet<String>();
						keys.addAll(data.keySet()); //Clone to avoid concurrency issues
						for(String key : keys) {
							TimeSeries series = data.get(key);
							try {
								if(series != null && series.size() > 0) {
									this.filemanager.saveToFile(key, series);
									//Reset values
									data.put(key, new TimeSeries()); 
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						//System.out.println("Sleeping thread for " + THREAD_SLEEP_TIME + " seconds...");
						Thread.sleep(THREAD_SLEEP_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}			
	}
	
}
