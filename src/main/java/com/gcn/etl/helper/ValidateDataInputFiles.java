package com.gcn.etl.helper;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gcn.etl.controllers.DataUploaderEtlController;
import com.gcn.etl.database.models.DataInputFile;
import com.gcn.etl.database.models.ErrorDetails;
import com.gcn.etl.database.models.JobValidationStatus;
import com.gcn.etl.database.models.Jobs;
import com.gcn.etl.lib.csv.CsvOutputData;
import com.gcn.etl.lib.s3Aws.S3AwsService;
import com.gcn.etl.pojo.DataPointWithIndex;
import com.gcn.etl.pojo.OutputData;

@Component
public class ValidateDataInputFiles {

	private static Logger logger = LogManager.getLogger(DataUploaderEtlController.class);

	@Autowired
	private CsvOutputData csvOutputData;

	@SuppressWarnings("unchecked")
	public Map<String, Object> validateData(DataInputFile dif, Jobs job, String localDataInputFilePath,
			S3AwsService s3Service) {
		Map<String, Object> map = new HashMap<String, Object>();
		String realPath = System.getProperty("user.dir") + "/";
		if (dif != null) {
			try {
				String outputFilePath = job.getOutputFileLink();
				String localOutputPath = realPath + localDataInputFilePath + dif.getSpaceId().getGeneratedUUID() + "/" + job.getJobId();
				String localOutputFilePath = localOutputPath + "/" + job.getOutputFileName();
				File dir = new File(localOutputPath);
				if (!dir.exists()) {
					dir.mkdirs();
					logger.debug("File is created");
				}
				s3Service.saveToLocal(outputFilePath, localOutputFilePath);
				logger.info("localOutputFilePath = " + localOutputFilePath);
				List<OutputData> list = csvOutputData.csvToBean(localOutputFilePath);
				String timeStampFormat = "MM/dd/yyyy HH:mm";
				if (list.size() > 0) {
					list = csvOutputData.setDateTimeAndMakeOrderList(list, dif.getTimeZone(), timeStampFormat);
					map = csvOutputData.findMissingIntervalOrOriginal(list, dif.getTimeZone(),job);
					logger.info("data file validation message = " + map.get("message"));
					if (map.get("message").equals("error")) {						
						JobValidationStatus jvs = new JobValidationStatus();
						jvs.setInputFileTypeId(dif.getInputFileTypeId());
						jvs.setMessage("Missing Point (Reject data files)");
						jvs.setApplicationErrorMsg(ETLErrors.MORE_THAN_4_MISSING_DATAPOINTS_TRANSFORMATION.errorMessage());
						jvs.setApplicationErrorCode(ETLErrors.MORE_THAN_4_MISSING_DATAPOINTS_TRANSFORMATION.errorCode());
						if((map.get("missingDataPoints") != null)){
							jvs.setApplicationErrorMsg(ETLErrors.MORE_THAN_4_MISSING_DATAPOINTS_TRANSFORMATION.errorMessage()+" at timestamps: "+map.get("missingDataPoints"));
						}
						jvs.setJobStatus(JobStatusValues.Failed.toString());
						jvs.setIsDataValid(false);
						jvs.setIsMissingPointRepaired(false);
						List<ErrorDetails> errorDetailsList = (List<ErrorDetails>) map.get("missingDataPoint");
						map = new HashMap<String, Object>();
						map.put("status", "error");
						map.put("validationStatus", jvs);
						map.put("errorDetails", errorDetailsList);

					} else {
						List<ErrorDetails> errorDetailsList = (List<ErrorDetails>) map.get("missingDataPoint");
						List<OutputData> outputDataList = (List<OutputData>) map.get("updatedData");
						boolean isSave = false;
						Boolean isDstSwitchExits = isDstSwitchExits(dif.getTimeZone());
						if(outputDataList != null){
							if (outputDataList.size() > 0 || isDstSwitchExits == true) {
								list = (List<OutputData>) map.get("updatedData");
								map.remove("updatedData");
								isSave = true;
							}
						}
						
						DataPointWithIndex spikeIndex = csvOutputData.getSpikeIndex(list, dif.getTimestampFormat(),dif.getTimeZone());
						logger.info("is spike in data file = " + spikeIndex);
						if (spikeIndex != null) {
							String spikeIndexInFile = "Max DataPoint = "+spikeIndex.getDataPoint() + ", at date "+spikeIndex.getDate();
							JobValidationStatus jvs = new JobValidationStatus();
							jvs.setInputFileTypeId(dif.getInputFileTypeId());
							jvs.setIsSpike(true);
							jvs.setIsDataValid(false);
							jvs.setIsMissingPointRepaired(false);
							jvs.setApplicationErrorCode(ETLErrors.SPIKE_FOUND_TRANSFORMATION.errorCode());
							jvs.setApplicationErrorMsg(ETLErrors.SPIKE_FOUND_TRANSFORMATION.errorMessage() +", "+ spikeIndexInFile);
							List<ErrorDetails> errorDetailsListForSpike = new ArrayList<ErrorDetails>();
							ErrorDetails errorDetails = new ErrorDetails();
							errorDetails.setColIndex(spikeIndex.getColumnIndex());
							errorDetails.setRowIndex(spikeIndex.getRowIndex());
							errorDetails.setDate(spikeIndex.getDate());
							errorDetails.setIntervalTime(spikeIndex.getInterval());
							errorDetails.setValue(spikeIndex.getDataPoint());
							errorDetailsListForSpike.add(errorDetails);
							jvs.setErrorDetailsList(errorDetailsListForSpike);
							jvs.setJobStatus(JobStatusValues.Failed.toString());
							map = new HashMap<String, Object>();
							map.put("status", "success");
							map.put("validationStatus", jvs);
							map.put("errorDetails", errorDetailsListForSpike);
							File file = new File(localOutputFilePath);
							file.deleteOnExit();
						} else {
							logger.debug("isSave = " + isSave);
							String fileNameAppender = "_final.";
							map = csvOutputData.repairData(list, job, localOutputFilePath, localOutputPath,fileNameAppender);
							if (isSave) {
								JobValidationStatus jvs = new JobValidationStatus();
								jvs.setInputFileTypeId(dif.getInputFileTypeId());
								jvs.setIsMissingPointRepaired(true);
								jvs.setIsDataValid(true);
								jvs.setIsSpike(false);
								jvs.setJobStatus(JobStatusValues.Completed.toString());
								jvs.setMessage("Missing Point is repaired");
								logger.info("missing data points are repair");
								map = new HashMap<String, Object>();
								
								map.put("validationStatus", jvs);
								map.put("errorDetails", errorDetailsList);
								File file = new File(localOutputPath);
								file.deleteOnExit();
							} else {
								JobValidationStatus jvs = new JobValidationStatus();
								jvs.setInputFileTypeId(dif.getInputFileTypeId());
								jvs.setIsDataValid(true);
								jvs.setIsMissingPointRepaired(false);
								jvs.setIsSpike(false);
								jvs.setJobStatus(JobStatusValues.Completed.toString());
								jvs.setMessage("Data file is valid");
								
								map.put("validationStatus", jvs);
								map.put("errorDetails", errorDetailsList);
							}
							map.put("status", "success");
						}
					}
				}
				File file = new File(localOutputFilePath);
				file.deleteOnExit();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return map;
	}

	private Boolean isDstSwitchExits(String timeZone) {
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timeZone));
		ZoneId z = now.getZone();
		ZoneRules zoneRules = z.getRules();
		Boolean isDst = zoneRules.isDaylightSavings(now.toInstant());
		return isDst;
	}
}
