package com.gcn.etl.helper;

import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class UserHelper {
	
	private static Logger logger = LogManager.getLogger(UserHelper.class.getName());
		
	static final String randomString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public String getUniqueToken() {
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		return uuid;
	}
	
	public String getToken(){
		try{
			Random random = new Random();				
			StringBuilder sb = new StringBuilder(8);
			for( int i = 0;i<8;i++ ){ 
			   sb.append(randomString.charAt(random.nextInt(randomString.length())));
			}
			return sb.toString();
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}
		return null;
	}
}	
