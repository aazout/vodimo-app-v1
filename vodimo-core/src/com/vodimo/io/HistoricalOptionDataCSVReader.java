/*
 * Loads data from HistoricalOptionData.com file format
 */
package com.vodimo.io;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.vodimo.core.model.Option;
import com.vodimo.core.util.VodimoDataBase;
import com.vodimo.strategy.OptionStraddleStrategy;

public class HistoricalOptionDataCSVReader implements IOptionsReader {

	private static Logger logger = LogManager.getLogger(HistoricalOptionDataCSVReader.class.getName());
	
	//private CacheManager<Integer, OptionsEntry> cache = new CacheManager<Integer, OptionsEntry>();
	
	public void read(Reader reader, VodimoDataBase db) throws Exception {
		ICsvMapReader mapReader = null;
        try {
            //mapReader = new CsvMapReader(new FileReader(f), CsvPreference.STANDARD_PREFERENCE);
            mapReader = new CsvMapReader(reader, CsvPreference.STANDARD_PREFERENCE);
                                    
            // the header columns are used as the keys to the Map
            @SuppressWarnings("unused")
			final String[] header = mapReader.getHeader(true);
            final CellProcessor[] processors = getProcessors();
            
            Map<String, Object> optionMap; 
            while((optionMap = mapReader.read(getNameMapping(), processors)) != null) {
            	/*
            	System.out.println(String.format("lineNo=%s, rowNo=%s, customerMap=%s", 
            			mapReader.getLineNumber(), 
            			mapReader.getRowNumber(), 
            			optionMap));
            	 		*/
            	
            	//String entityId = (String) optionMap.get("entityId");
            	//Date expirationDate = (Date) optionMap.get("expirationDate");
            	//Date quoteDate = (Date) optionMap.get("quoteDate");            	
            	
            	Option o = getOption(optionMap);
            	
            	// Skip this option if does not match parameters strategy parameters (i.e. deep moneyness, far expiry)
            	// FIXME: Modify this to get the latest strategy            	
				if(!OptionStraddleStrategy.filterOptionChainEntry(o)) continue;
            	
				/*
				OptionsEntry entry = new OptionsEntry();
				entry.setEntityId(o.entityId);
				entry.setExpirationDate(o.getExpirationDate());
				entry.setQuoteDate(o.getQuoteDate());
				if(cache.get(entry) != null) {
					entry = (OptionsEntry) cache.get(entry);
				} else {
					entry = db.getOptionsEntry(entityId, quoteDate, expirationDate);   
				}
				
            	if(entry == null) {
            		// Create the OptionsEntry
            		entry = new OptionsEntry();            		
            		entry.setQuoteDate(quoteDate);
            		entry.setEntityId(entityId);
            		entry.setExpirationDate(expirationDate);  
            		
            		List<Option> optionList = new ArrayList<Option>();
            		optionList.add(o);
            		entry.setOptionList(optionList);
            		cache.addToCache(entry);
            		
            	} else {
            		entry.getOptionList().add(o);
            	}  
            	*/          	
            	logger.debug("Saving Option {entityId:" + o.getEntityId() + 
            			", quoteDate:" + o.getQuoteDate() + 
            			", expirationDate:" + o.getExpirationDate() + 
            			"}");
        		
            	db.saveOption(o);
            }
                
        } finally {
            if( mapReader != null ) {
                    mapReader.close();
            }
        }
	}
		
	/*
	 * Create the option object by mapping fields through Java
	 * reflection
	 */
	@SuppressWarnings("rawtypes")
	public static Option getOption(Map<String, Object> optionMap) {
		Field[] fields = Option.class.getFields();
		Option option = new Option();
		for(int i=0;i<fields.length;i++) {
			String field = fields[i].getName();
			Class c = fields[i].getType();
			try {				
				fields[i].set(option, c.cast(optionMap.get(field)));
			} catch (Exception e) {
				e.printStackTrace();
			} 			
		}
		return option;
	}
	
	/*
	* FILE FORMAT:
	* Underlying
	* Underlying_last
	* Exchange
	* Optionsymbol
	* Optiontype
	* Expiration
	* Quotedate
	* Strike
	* Last
	* Bid
	* Ask
	* Volume
	* Open interest
	* Implied volatility
	* Delta
	* Gamma
	* Theta
	* Vega
	* Alias
	*/
	
	public static CellProcessor[] getProcessors() {
		
		final CellProcessor[] processors = new CellProcessor[] { 
			new NotNull(), // Underlying
			new NotNull(new ParseDouble()), // Underlying Last
			new NotNull(), // Exchange
			new NotNull(), // Optionsymbol
			new Optional(), // ?
			new Optional(), // Optiontype
			new ParseDate("MM/dd/yyyy"), // Expiration
			//new ParseDate("MM/dd/yyyy hh:mm:ss a"), // Quote Date
			new ParseDate("MM/dd/yyyy"), // Quote Date
			new NotNull(new ParseDouble()), // Strike
			new NotNull(new ParseDouble()), // Last
			new NotNull(new ParseDouble()), // Bid
			new NotNull(new ParseDouble()), // Ask
			new NotNull(new ParseDouble()), // Volume
			new NotNull(new ParseDouble()), // Open Interest
			new NotNull(new ParseDouble()), // Implied Vol
			new NotNull(new ParseDouble()), // Delta
			new NotNull(new ParseDouble()), // Gamma
			new NotNull(new ParseDouble()), // Theta
			new NotNull(new ParseDouble()), // Vega
			new Optional() // Alias
		};
	        
		return processors;		
	}
	
	public static String[] getNameMapping() {		
		String[] fieldNames = new String[] {
			"entityId",
			"underlyingLastPrice",
			"exchange",
			"optionSymbol",
			"_blank",
			"optionType",
			"expirationDate",
			"quoteDate",
			"strike",
			"last",
			"bid",
			"ask",
			"volume",
			"openInterest",
			"impliedVolatility",
			"delta",
			"gamma",
			"theta",
			"vega",
			"alias"
		};
		return fieldNames;
	}
	
}
