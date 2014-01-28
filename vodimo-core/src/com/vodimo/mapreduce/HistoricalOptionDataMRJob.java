package com.vodimo.mapreduce;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.vodimo.core.model.Option;
import com.vodimo.io.HistoricalOptionDataCSVReader;

public class HistoricalOptionDataMRJob {
	
	public static class OptionsDataMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Writable> {

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<Text, Writable> output, Reporter reporter)
				throws IOException {
			
			//String line = value.toString();
			//[1] Get the row from the file
			//[2] Parse the row to create the Option object
			ICsvMapReader mapReader = null;
	        try {
	            mapReader = new CsvMapReader(new StringReader(value.toString()), CsvPreference.STANDARD_PREFERENCE);
	            
	            Map<String, Object> optionMap = mapReader.read(
	            		HistoricalOptionDataCSVReader.getNameMapping(), 
	            		HistoricalOptionDataCSVReader.getProcessors());
	            
	            Option o = HistoricalOptionDataCSVReader.getOption(optionMap);
	            
	            Text eid = new Text();
	            eid.set(o.getEntityId());	            
	            
	            output.collect(eid, o);	            
	            
	        } finally {
	        	mapReader.close();
	        }
								
		}
			
	}
}
