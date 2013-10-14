package com.vodimo.core;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ib.client.Contract;
import com.vodimo.core.model.HistoricalDataRequest;
import com.vodimo.core.model.TimeSeries;
import com.vodimo.core.util.FileIOManager;
import com.vodimo.core.util.VodimoConstants;

public class HistoricalDataTest {
	
	private int MAX_TIMEOUT = 3000 * 5; //15 seconds
	
	@Test
	public void testHistoricalData() {
		IBClientWrapper client = IBClientWrapper.newInstance();
		//(1) Connect the client
		client.connect();		
		//(2) Create HistoricalDataRequest
		//System.out.println("Is connected? " + client.isConnected());
		System.out.println("Creating historical data request...");
		HistoricalDataRequest hdr = createHistoricalDataRequest();
		//(3) Add historical data listener		
		IBClientHistoricalDataListener hdl = new IBClientHistoricalDataListener();
		System.out.println("Attaching listener...");
		client.setListener(hdl);
		//(4) Request historical data
		System.out.println("Sending request for historical data for " + hdr.getContract().m_symbol + "...");
		client.requestHistoricalData(hdr);
		
		int wait = 0;
		while(hdl.isEmpty()) {
			try {
				//Sleep for 3 seconds
				Thread.sleep(3000);
				wait += 3000;
				if(wait >= MAX_TIMEOUT) {
					fail("Request took longer than 15 seconds");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		assertTrue(true);
	}
	
	//This will not work because of multithreading issues and JUnit testing
	@Test
	public void testHistoricalDataPopulator() {
		IBHistoricalDataPopulator pop = new IBHistoricalDataPopulator(VodimoConstants.getDefaultConfig());
		List<String> symbols = new ArrayList<String>();
		symbols.add("AAPL");		
		pop.populateHistoricalData(symbols);
		
		//Sleep for 5 seconds
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		FileIOManager filemanager = FileIOManager.newInstance();
		Map<String, TimeSeries> results = filemanager.loadAllTimeSeriesData(symbols);
		TimeSeries resSeries = results.get("AAPL");	
		
		assertNotNull(resSeries);
		
		//Default config is 1 trading day, at 30 second intervals
		double count = 6.5 * 60 * 2;
		assertTrue(resSeries.size() == count);
						
	}
	
	@Test
	public void testSaveAndReadFile() {
		
		Calendar cal = Calendar.getInstance();
		int samples = 500000; //We want it to be enough to create more than 1 file					
		TimeSeries series = new TimeSeries();		
		for(int i=0;i<samples;i++) {
			series.add(cal.getTime(), new Double(Math.round((Math.random()*100))));
			cal.add(Calendar.DAY_OF_YEAR, -1);
			if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)//Check for Sunday 
				cal.add(Calendar.DAY_OF_YEAR, -1);				
		}
				
		FileIOManager filemanager = FileIOManager.newInstance();
		try {
			filemanager.saveToFile("AAPL", series);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error occured");
		}
		
		//System.out.println(series.toString());
		
		List<String> symbols = new ArrayList<String>();
		symbols.add("AAPL");
		Map<String, TimeSeries> results = filemanager.loadAllTimeSeriesData(symbols);
		TimeSeries resSeries = results.get("AAPL");
		
		//System.out.println(resSeries.toString());
		
		assertNotNull(resSeries);		
		assertTrue(series.equals(resSeries));
			
	}
	
	@Test
	public void testReadFiles() {
		FileIOManager filemanager = FileIOManager.newInstance();
		Map<String, TimeSeries> map = null;
		try {
			map = filemanager.loadAllTimeSeriesData(new File(VodimoConstants.SYMBOLS_FILE));
			for(String symbol : map.keySet()) {
				TimeSeries series = map.get(symbol);
				System.out.println(symbol + " timeseries: " + series.size());
				long lastKey = 0;
				for(long key : series.keySet()){
					//System.out.println("Date: " + (new Date(key)).toString());
					if(key < lastKey) {
						fail("The timestamps are not properly ordered");
					}
					lastKey = key;
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not load symbols file");
		}		
				
		assert(map == null | !map.isEmpty());		
	}
	
	private HistoricalDataRequest createHistoricalDataRequest() {
		HistoricalDataRequest hdr = new HistoricalDataRequest();		
		Contract c = new Contract(0, "AAPL", "STK", "", 0.0, "", "", "SMART", "USD", "", null, "ISLAND", false, "", "");
		hdr.setContract(c);		
		hdr.setBarSizeSetting("1 hour");
		hdr.setDurationStr("1 M");
		hdr.setId(1);
		hdr.setUseRTH(1);
		hdr.setWhatToShow("TRADES");
		hdr.setEndDateTime("20120112 19:20:10 GMT");//yyyymmdd hh:mm:ss
		hdr.setFormatDate(1);
		return hdr;
	}

}
