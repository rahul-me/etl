package com.gcn.etl.helper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gcn.etl.database.models.DataInputFile;
import com.gcn.etl.propertiesHelper.AppConfigProperties;

@Service
@SuppressWarnings("deprecation")
public class PMTServiceHelper {
	private static Logger logger = LogManager.getLogger(PMTServiceHelper.class);

	@Autowired
	private AppConfigProperties appConfigProperties;

	@SuppressWarnings({ "resource" })
	public Map<String, Object> PMTHistoricalAPI(DataInputFile dif, String sourceFilePath) {
		HttpClient httpclient = new DefaultHttpClient();
		Map<String, Object> map = new HashMap<String, Object>();
		logger.info("PMT Service call start");
		String body = null;
		logger.info("PMT URL = " + appConfigProperties.getPmtUploadUrl());
		HttpPost httppost = new HttpPost(appConfigProperties.getPmtUploadUrl());
		try {
			httppost.addHeader("meterName", dif.getMeterName());
			httppost.addHeader("timezone", dif.getTimeZone());
			httppost.addHeader("dataType", dif.getDataType());
			//logger.debug("file path = " + sourceFilePath);
			File file = new File(sourceFilePath);
			logger.debug("file exits = " + file.exists());
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.addBinaryBody("file", file);
			HttpEntity entity = builder.build();
			httppost.setEntity(entity);
			HttpResponse response = httpclient.execute(httppost);
			logger.info("Http status : "+response.getStatusLine().getStatusCode());
			ResponseHandler<String> handler = new BasicResponseHandler();
			body = handler.handleResponse(response);
			logger.debug("PMT response :" + body);
			//logger.info(response.getStatusLine());
			httpclient.getConnectionManager().shutdown();
			JSONObject jsonObj = new JSONObject(body);
			logger.info("HT status = " +  jsonObj.get("status"));
			logger.info("HT detailedMsg = " +  jsonObj.get("detailedMsg"));

			map.put("status", jsonObj.get("status"));
			map.put("detailedMsg", jsonObj.get("detailedMsg"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("pmtError", "true");
		} finally {
			try {
				if (sourceFilePath != null) {
					File file = new File(sourceFilePath);
					boolean isDelete = file.delete();
					logger.info("isDelete = " + isDelete);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return map;
	}

}
