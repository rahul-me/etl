package com.gcn.etl.propertiesHelper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource({ "file:config/appConfig.properties" })
public class AppConfigProperties {
	@Value("${localDataInputFilePath}")
	private String localDataInputFilePath;
	@Value("${dataInputFilesPath}")
	private String dataInputFilesPath;
	@Value("${pmtUploadUrl}")
	private String pmtUploadUrl;
	@Value("${jobsUploadPath}")
	private String jobsUploadPath;
	@Value("${ktrFilePath}")
	private String ktrFilePath;
	// s3 configuration properties
	@Value("${s3.bucket}")
	private String s3Bucket;
	@Value("${s3.accessKeyID}")
	private String s3AccessKeyID;
	@Value("${s3.secretAccessKey}")
	private String s3SecretAccessKey;
	// kettle database configuration properties
	@Value("${db.host}")
	private String dbHost;
	@Value("${db.port}")
	private String dbPort;
	@Value("${db.schema}")
	private String dbSchema;
	@Value("${db.login}")
	private String dbLogin;
	@Value("${db.password}")
	private String dbPassword;
	// cluster configuration
	@Value("${carte.master.name}")
	private String carteMasterName;
	@Value("${carte.master.host}")
	private String carteMasterHost;
	@Value("${carte.master.port}")
	private String carteMasterPort;
	@Value("${carte.master.login}")
	private String carteMasterLogin;
	@Value("${carte.master.password}")
	private String carteMasterPassword;
	@Value("${targetIntervalLength}")
	private int targetIntervalLength;
	@Value("${maxNoOfMissingDataPointsAllowed}")
	private int maxNoOfMissingDataPointsAllowed;
	@Value("${minSpikeKw}")
	private int minSpikeKw;
	@Value("${maxNumberOfMissingPointsToBeReturned}")
	private int maxNumberOfMissingPointsToBeReturned;
	@Value("${application}")
	private String application;
	@Value("${datePartSeparator}")
	private String datePartSeparator;
	@Value("${timePartSeparator}")
	private String timePartSeparator;	
	@Value("${locateTimeDataInGridLimit}")
	private int locateTimeDataInGridLimit;	
	public int getLocateTimeDataInGridLimit() {
		return locateTimeDataInGridLimit;
	}

	public String getDatePartSeparator() {
		return datePartSeparator;
	}

	public String getTimePartSeparator() {
		return timePartSeparator;
	}

	public String getLocalDataInputFilePath() {
		return localDataInputFilePath;
	}

	public String getDataInputFilesPath() {
		return dataInputFilesPath;
	}

	public String getPmtUploadUrl() {
		return pmtUploadUrl;
	}

	public String getJobsUploadPath() {
		return jobsUploadPath;
	}

	public String getKtrFilePath() {
		return ktrFilePath;
	}

	public String getS3Bucket() {
		return s3Bucket;
	}

	public String getS3AccessKeyID() {
		return s3AccessKeyID;
	}

	public String getS3SecretAccessKey() {
		return s3SecretAccessKey;
	}

	public String getDbHost() {
		return dbHost;
	}

	public String getDbPort() {
		return dbPort;
	}

	public String getDbSchema() {
		return dbSchema;
	}

	public String getDbLogin() {
		return dbLogin;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public String getCarteMasterName() {
		return carteMasterName;
	}

	public String getCarteMasterHost() {
		return carteMasterHost;
	}

	public String getCarteMasterPort() {
		return carteMasterPort;
	}

	public String getCarteMasterLogin() {
		return carteMasterLogin;
	}

	public String getCarteMasterPassword() {
		return carteMasterPassword;
	}

	public int getTargetIntervalLength() {
		return targetIntervalLength;
	}

	public int getMaxNoOfMissingDataPointsAllowed() {
		return maxNoOfMissingDataPointsAllowed;
	}

	public int getMinSpikeKw() {
		return minSpikeKw;
	}

	public int getMaxNumberOfMissingPointsToBeReturned() {
		return maxNumberOfMissingPointsToBeReturned;
	}

	public String getApplication() {
		return application;
	}

}
