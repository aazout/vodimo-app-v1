package com.vodimo.core.util.volatility;

import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

public class TreasuryBillRiskFreeRate implements RiskFreeRate {

	private static Logger logger = LogManager.getLogger(TreasuryBillRiskFreeRate.class.getName());
	
	private static TreasuryBillRiskFreeRate riskFreeRate;
	
	private static Map<Long, Double> rfrMap = new HashMap<Long, Double>();
	
	private TreasuryBillRiskFreeRate() {}
	
	public static TreasuryBillRiskFreeRate newInstance() {
		if(riskFreeRate == null) {
			riskFreeRate = new TreasuryBillRiskFreeRate();
			return riskFreeRate;
		} else {
			return riskFreeRate;
		}
	}
	
	
	// FIXME: This needs to be fixed with real data
	@Override
	public double getRiskFreeRate(Date d) {
		logger.debug("Trying to get... " + d + " from rfrMap " + rfrMap.size());		
		return rfrMap.get(d.getTime());
	}
	
	//Load the spreadsheet
	static {
		
		final CellProcessor[] processors = new CellProcessor[] { 
				new ParseDate("MM/dd/yy"), // Expiration
				//new ParseDate("MM/dd/yyyy hh:mm:ss a"), // Quote Date
				new NotNull(new ParseDouble()), // 1 month
				new NotNull(new ParseDouble()), // 3 month
				new NotNull(new ParseDouble()), // 6 month
				new NotNull(new ParseDouble()), // 1 year
				new NotNull(new ParseDouble()), // 2 year
				new NotNull(new ParseDouble()), // 3 year
				new NotNull(new ParseDouble()), // 5 year
				new NotNull(new ParseDouble()), // 7 year
				new NotNull(new ParseDouble()), // 10 year
				new NotNull(new ParseDouble()), // 20 year
				new NotNull(new ParseDouble()), // 30 year
			};		
		
		String[] fieldNames = new String[] {
				"date",
				"1month",
				"3month",
				"6month",
				"1year",
				"2year",
				"3year",
				"5year",
				"7year",
				"10year",
				"20year",
				"30year"
			};		
		
		ICsvMapReader mapReader = null;
        try {
            mapReader = new CsvMapReader(
            		new InputStreamReader(TreasuryBillRiskFreeRate.class.getClassLoader().getResourceAsStream("TreasuryYields2012.csv")), 
            		CsvPreference.STANDARD_PREFERENCE);		
            
            @SuppressWarnings("unused")
			final String[] header = mapReader.getHeader(true);            
            
            Map<String, Object> entryMap; 
            while((entryMap = mapReader.read(fieldNames, processors)) != null) {
            	rfrMap.put(
            			((Date) entryMap.get(fieldNames[0])).getTime(), 
            			((Double) entryMap.get(fieldNames[4]) / 100));
            	logger.debug("Putting in map {} {}", entryMap.get(fieldNames[0]), ((Double) entryMap.get(fieldNames[4]) / 100));    
            }
            
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

}
