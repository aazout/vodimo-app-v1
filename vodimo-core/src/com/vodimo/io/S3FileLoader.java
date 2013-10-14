package com.vodimo.io;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.vodimo.core.util.VodimoDataBase;

public class S3FileLoader extends OptionsFileLoader {

	private static Logger logger = LogManager.getLogger(S3FileLoader.class.getName());
	
	private AmazonS3 s3;
	private String bucketName;
		
	public S3FileLoader(String bucketName, VodimoDataBase db, IOptionsReader reader) {
		super(db, reader);    
		this.bucketName = bucketName;
		try {
			this.s3 = new AmazonS3Client(new PropertiesCredentials(
					S3FileLoader.class.getClassLoader().getResourceAsStream("AwsCredentials.properties")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void load() throws Exception {
		logger.info("Loading S3FileLoader");
		ObjectListing objectListing = s3.listObjects(this.bucketName);   
        for(S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
        	String key = objectSummary.getKey();
        	S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
        	reader.read(new InputStreamReader(object.getObjectContent()), db);
        }  		
	}

}
