package com.gcn.etl.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.gcn.etl.database.models.AdditionalProperties;
import com.gcn.etl.database.models.Jobs;

@Service
public class JobsHelper {
	public String getTimeStamp() {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		return timeStamp;
	}

	public String getJobId() {
		String randomString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random random = new Random();
		StringBuilder sb = new StringBuilder(8);
		for (int i = 0; i < 8; i++) {
			sb.append(randomString.charAt(random.nextInt(randomString.length())));
		}
		return sb.toString();
	}	
	
	public Jobs getCopyJobObjWithoutId(Jobs obj) {
		Jobs temp = new Jobs();
		temp.setAction(obj.getAction());
		temp.setJobId(obj.getJobId());
		temp.setInputFileId(obj.getInputFileId());
		temp.setSkipTransformation(obj.getSkipTransformation());
		temp.setCallbackUrl(obj.getCallbackUrl());
		temp.setStatus(obj.getStatus());
		temp.setOutputFileLink(obj.getOutputFileLink());
		temp.setOutputFileLocation(obj.getOutputFileLocation());
		temp.setOutputFileName(obj.getOutputFileName());
		List<AdditionalProperties> list = new ArrayList<AdditionalProperties>();
		if (obj.getAdditionalProperties().size() > 0) {
			AdditionalProperties ap = null;
			for (AdditionalProperties apObj : obj.getAdditionalProperties()) {
				ap = new AdditionalProperties();
				ap.setMeterId(apObj.getMeterId());
				ap.setUtilityId(apObj.getUtilityId());
				list.add(ap);
			}
			temp.setAdditionalProperties(list);
		}
		return temp;
	}

}
