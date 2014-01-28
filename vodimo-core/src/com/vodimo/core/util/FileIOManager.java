package com.vodimo.core.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.vodimo.core.model.TimeSeries;

public class FileIOManager {

	/*
	 * FileIOManager manipulates/archives time series data on disk. The idea is 
	 * to create a SequenceFile with a map entry for each day and store 120 days
	 * per file. At 30 second intervals in a given trading day (6.5 hours), there
	 * would be 780 entries per key, and at 120 days there would be 93,600. 
	 */
	
	private static FileIOManager manager;		
	private static String local_dir = System.getProperty(VodimoConstants.ENV_ARCHIVE_DIR, 
			"/Users/aazout/Dropbox/Vodimo/data");
	//private static int MAX_FILES = 9; //10 120-day files
	private static int MAX_FILE_SIZE = 10000000; //in bytes (10MB)
	//private static int MAX_FILE_SIZE = 60; //in bytes (10MB)
	private static String SYMBOL_DELIM = "\t";
	
	private FileIOManager() {}
	
	public static FileIOManager newInstance() {
		if(manager == null) {
			manager = new FileIOManager();
			return manager;
		} else {
			return manager; 
		}
	}
	
	public void saveToFile(String symbol, TimeSeries series) throws Exception {
		saveToFile(symbol, series, 0);
	}
	
	public void saveToFile(String symbol, TimeSeries series, int offset) throws Exception {	
		try {
			File symboldir = new File(local_dir + "/" + symbol);
			int counter = 0;
			if(symboldir.exists()) {
				File[] list = symboldir.listFiles(new PrivateFileFilter());
				if(list != null && list.length != 0) {					
					counter = list.length - 1;
				} 
			} else {
				symboldir.mkdir();
			}
			
			if(offset > 0) {
				counter++; //Add new file
			}
			
			//System.out.println("Writing to file '" + symboldir.toString() + "/" + symbol + "_00" + counter + "'");
			//System.out.println("Offset = " + offset);
			File file = new File(symboldir.toString() + "/" + symbol + "_00" + counter);
			boolean append = false;
			if(file.exists()) {
				append = true;
			} 
			//System.out.println("Append = " + append);
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));	
			
			//Create list for offset
			List<Long> seriesList = new ArrayList<Long>(series.keySet());
			
			for(int i=offset;i<seriesList.size();i++){		
				String str = (series.get(seriesList.get(i)).serialize());
				int bytes = (VodimoConstants.TICK_SEPARATOR + str).getBytes().length;
				if(file.length() <= (MAX_FILE_SIZE - bytes)) {						
					if(!append) {
						writer.append(str);
						append = true; //Toggle to append because file has been created 
					} else {
						writer.append(VodimoConstants.TICK_SEPARATOR + str);						
					}
					//System.out.println("Writing to file: " + str);
					writer.flush();
				} else {
					writer.close();
					saveToFile(symbol, series, i);
					break; //Break to escape current loop forever
				}
			}
			writer.close();	
			//System.out.println("File: " + file.getName() + " Size: " + file.length());

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public Map<String, TimeSeries> loadAllTimeSeriesData(File file) throws Exception {
		List<String> dirs = FileIOManager.loadSymbols(file);
		return loadAllTimeSeriesData(dirs);
	}
	
	public Map<String, TimeSeries> loadAllTimeSeriesData(List<String> dirs){
		Map<String, TimeSeries> retMap = new HashMap<String, TimeSeries>();
		try {			
			for(String dir : dirs) {
				File symboldir = new File(local_dir + "/" + dir);
				if(symboldir.exists()) {
					TimeSeries series = new TimeSeries();
					File[] list = symboldir.listFiles(new PrivateFileFilter());
					//Loop through files
					for(int i=0;i<list.length;i++) {
						File f = list[i];
						BufferedReader reader = new BufferedReader(new FileReader(f));
						String line;
						while((line = reader.readLine()) != null) {
							StringTokenizer st = new StringTokenizer(line, VodimoConstants.TICK_SEPARATOR);
							while(st.hasMoreTokens()) {
								String tick = st.nextToken();
								String[] tokens = tick.split(VodimoConstants.TICK_SEPARATOR_DATEPRICE);
								series.add((VodimoUtils.parseIBDateStore(tokens[0])), Double.parseDouble(tokens[1]));
							}
						}
						reader.close();
					}
					retMap.put(dir, series);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retMap;
	}
	
	public static List<String> loadSymbols(File file) throws Exception {		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		List<String> retList = new ArrayList<String>();
		while((line = reader.readLine()) != null) {
			String[] row = line.split(SYMBOL_DELIM);
			retList.add(row[1]);
		}
		reader.close();
		return retList;	
		
	}
	
	class PrivateFileFilter implements FilenameFilter {

		@Override
		public boolean accept(File file, String name) {
			if(name.startsWith(".")) {
				return false;
			} else {
				return true;
			}
		}
		
	}
	
}
