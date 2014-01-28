package com.vodimo.io;

import java.io.File;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vodimo.core.util.VodimoDataBase;

public class LocalFileLoader extends OptionsFileLoader {
				
	private static Logger logger = LogManager.getLogger(LocalFileLoader.class.getName());
	
	private File dir;
	
	public LocalFileLoader(String file, VodimoDataBase db, IOptionsReader reader) {
		super(db, reader);
		this.dir = new File(file);	
	}

	@Override
	public void load() throws Exception {
		logger.info("Loading LocalFileLoader");
		if(dir.isDirectory()) {
			File[] fileList = dir.listFiles();
			for(int i=0;i<fileList.length;i++) {
				String fileName = fileList[i].getName();				
				int index = fileName.lastIndexOf(".");
				if (index > 0) {
				    String extension = fileName.substring(index+1);
				    if(extension.equalsIgnoreCase("csv")) {
				    	logger.info("Reading file name {} ", fileName);
				    	FileReader fileReader = new FileReader(fileList[i]);
				    	reader.read(fileReader, db);
				    	fileReader.close();
				    }
				}								
			}
		} else {
			FileReader fileReader = new FileReader(dir);
			reader.read(fileReader, db);
			fileReader.close();
		}			
	}
			
}
