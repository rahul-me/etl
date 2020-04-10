package com.gcn.etl.lib.s3Aws;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.gcn.etl.propertiesHelper.AppConfigProperties;

@Service
public class S3AwsService {

	private static Logger logger = LogManager.getLogger(S3AwsService.class.getName());

	@Autowired
	private AppConfigProperties appConfigProperties;

	AWSCredentials credentials = null;
	AmazonS3 s3client = null;
	String bucket;
	String SUFFIX = "/";

	@PostConstruct
	private void initClient() {
		try {
			this.credentials = new BasicAWSCredentials(this.appConfigProperties.getS3AccessKeyID(),
					this.appConfigProperties.getS3SecretAccessKey());
			this.s3client = new AmazonS3Client(credentials);
			this.bucket = this.appConfigProperties.getS3Bucket();
		}
		catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	public void listBuckets() {
		try {
			for (Bucket bucket : this.s3client.listBuckets()) {
				logger.info(" - " + bucket.getName());
			}
		}
		catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	public void createFolder(String folderName) {
		InputStream emptyContent = null;
		try {
			emptyContent = new ByteArrayInputStream(new byte[0]);
			// create meta-data for your folder and set content-length to 0
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(0);
			// create a PutObjectRequest passing the folder name suffixed by /
			PutObjectRequest putObjectRequest = new PutObjectRequest(this.bucket, folderName + "/", emptyContent, metadata);
			// send request to S3 to create folder
			this.s3client.putObject(putObjectRequest);
		}
		catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		finally{
			try {
				emptyContent.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("unused")
	public boolean isPathExists(String path) {
		try {
			boolean isValidFile = true;
			path += "/";
			return isExitsPathOrFile(path);
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	public boolean isFileExists(String filePath) {
		try{
			return isExitsPathOrFile(filePath);
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	@SuppressWarnings("unused")
	private boolean isExitsPathOrFile(String path) {
		boolean isValidFile = true;
		try {
			logger.info("filePath = " + path);
			ObjectMetadata objectMetadata = this.s3client.getObjectMetadata(this.bucket, path);
		} catch (Exception exception) {
			isValidFile = false;
		}
		return isValidFile;
	}

	public Map<String, Object> uploadFile(String folderName, String fileName, MultipartFile file) {
		S3Object s3Obj = null;
		InputStream is = null;
		try{
			Map<String, Object> map = new HashMap<String, Object>();
			is = file.getInputStream();
			Long timeStamp = System.currentTimeMillis();
			String location = folderName + SUFFIX + timeStamp + "_" + fileName;

			this.s3client.putObject(new PutObjectRequest(this.bucket, location, is, new ObjectMetadata()));
			s3Obj = this.s3client.getObject(new GetObjectRequest(this.bucket, location));
			map.put("fileLink", s3Obj.getObjectContent().getHttpRequest().getURI().toString());
			map.put("location", folderName + SUFFIX);
			map.put("fileName", timeStamp + "_" + fileName);
			return map;
		}
		catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		finally{
			try {
				s3Obj.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			try {
				is.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		return null;
	}

	public void downloadFile(String key, HttpServletResponse response, HttpServletRequest request, String fileName, Map<String, Object> erorrMap){				
			try(S3Object s3Obj = this.s3client.getObject(new GetObjectRequest(this.bucket, key));
					InputStreamReader inputStreamReader = new InputStreamReader(s3Obj.getObjectContent());
					BufferedReader reader = new BufferedReader(inputStreamReader);
					OutputStream outputStream = response.getOutputStream()){
				
				response.setContentType("application/application-download");
				response.setHeader("Content-Disposition", "inline; filename=" + fileName);			
				byte[] buffer = new byte[4096];
				while (true) {
					String line = reader.readLine();
					if (line == null)
						break;
					buffer = line.getBytes();
					outputStream.write(buffer, 0, line.length());
					String newLine = "\n";
					outputStream.write(newLine.getBytes(), 0, newLine.length());				
				}
				
			} catch (AmazonServiceException e1) {
				erorrMap.put("erorrMessage", e1.getMessage());
				logger.error(e1.getMessage(),e1);
			} catch (AmazonClientException e1) {
				erorrMap.put("erorrMessage", e1.getMessage());
				logger.error(e1.getMessage(),e1);
			} catch (IOException e1) {
				erorrMap.put("erorrMessage", e1.getMessage());
				logger.error(e1.getMessage(),e1);
			}		
	}
	
	public boolean doesObjectExists(String key) {
		List<S3ObjectSummary> objects = Collections.emptyList();
		try {
			ObjectListing ol = s3client.listObjects(new ListObjectsRequest().withBucketName(this.bucket).withPrefix(key));
	        objects = ol.getObjectSummaries();	        
		} catch(Exception e) {		
			logger.error(e.getMessage(),e);
		}
		return !objects.isEmpty();
	}

	public void deleteFile(String key) {
		try {
			s3client.deleteObject(this.bucket, key);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public Map<String, Object> uploadFileFromPath(String path, String fileName, File outputFile) {
		Map<String, Object> map = new HashMap<String, Object>();
		S3Object s3Obj = null;
		try {
			Long timeStamp = System.currentTimeMillis();
			String location = path + SUFFIX + timeStamp + "_" + fileName;
			logger.info("bucket = " + this.bucket);
			this.s3client.putObject(this.bucket, location, outputFile);
			s3Obj = this.s3client.getObject(new GetObjectRequest(this.bucket, location));
			map.put("fileLink", s3Obj.getObjectContent().getHttpRequest().getURI().toString());
			map.put("location", path + SUFFIX
					);
			map.put("fileName", timeStamp + "_" + fileName);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		finally{
			try {
				s3Obj.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		return map;
	}

	public Map<String, Object> uploadFileFromPathWithOutTimestamp(String path, String fileName, File sourceFileData) {
		Map<String, Object> map = new HashMap<String, Object>();
		S3Object s3Obj = null;
		try {
			// Long timeStamp = System.currentTimeMillis();
			String location = path + fileName;
			logger.info("file path location = " + location);
			this.s3client.putObject(this.bucket, location, sourceFileData);
			s3Obj = this.s3client.getObject(new GetObjectRequest(this.bucket, location));
			logger.info("file upload");
			map.put("fileLink", s3Obj.getObjectContent().getHttpRequest().getURI().toString());
			map.put("location", path + SUFFIX);
			map.put("fileName", fileName);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		finally{
			try {
				s3Obj.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		return map;
	}

	public void saveToLocal(String source_file_path, String output_file_path) {
		S3Object object = null;
		InputStream in = null;
		try {
			logger.info("download file from s3 server....");
			logger.info("sourceFilePath = " + source_file_path);
			logger.info("outputFilePath = " + output_file_path);
			object = this.s3client.getObject(new GetObjectRequest(this.bucket, source_file_path));
			in = object.getObjectContent();
			Path destinationPath = Paths.get(output_file_path);
			Files.copy(in, destinationPath,StandardCopyOption.REPLACE_EXISTING);
		}
		catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		finally{
			try {
				object.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			try {
				in.close();
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			
		}
		
		logger.info("file download successfully....");
	}
}
