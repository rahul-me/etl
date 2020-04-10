package com.gcn.etl.thread;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Repository;

import com.gcn.etl.database.models.DataInputFile;
import com.gcn.etl.database.models.ErrorDetails;
import com.gcn.etl.database.models.JobStatus;
import com.gcn.etl.database.models.JobValidationStatus;
import com.gcn.etl.database.models.Jobs;
import com.gcn.etl.database.repository.JobRepository;
import com.gcn.etl.database.repository.JobStatusRepository;
import com.gcn.etl.database.service.ErrorDetailsService;
import com.gcn.etl.database.service.JobService;
import com.gcn.etl.database.service.JobValidationStatusService;
import com.gcn.etl.helper.PMTServiceHelper;
import com.gcn.etl.helper.ValidateDataInputFiles;
import com.gcn.etl.kettle.TransformationWithRemoteServer;
import com.gcn.etl.lib.s3Aws.S3AwsService;
import com.gcn.etl.propertiesHelper.AppConfigProperties;

@Repository
@EnableAsync
public class ValidationAndJobExecution {

	private static Logger logger = LogManager.getLogger(ValidationAndJobExecution.class);

	@Autowired
	private JobValidationStatusService jobValidationAndStatusRepository;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobService jobService;

	@Autowired
	private JobStatusRepository jobStatusRepository;

	@Autowired
	private ErrorDetailsService errorDetailsService;

	@Autowired
	private TransformationWithRemoteServer jobexec;

	@Autowired
	private S3AwsService s3Service;

	@Autowired
	private AppConfigProperties appConfigProperties;

	@Autowired
	private ValidateDataInputFiles validateDataInputFiles;

	@Autowired
	private PMTServiceHelper pmtService;

	@Async
	public void validateAndExecuteJob(Jobs obj, DataInputFile dif) {
		logger.info("validateAndExecuteJob start");
		String jobUploadPath =  dif.getSpaceId().getGeneratedUUID()+appConfigProperties.getJobsUploadPath()+"/"+obj.getJobId();
		String realPath = System.getProperty("user.dir") + "/";
		obj.setOutputFileLocation(jobUploadPath+"/");
		jobHandler(dif, obj, jobUploadPath, realPath);
	}

	public Map<String, Object> jobHandler(DataInputFile dif, Jobs obj, String jobUploadPath, String realPath) {
		Map<String, Object> map = new HashMap<String, Object>();
		Long timeStamp = System.currentTimeMillis();
		String inputFileLocation = dif.getLocation() + dif.getFileName();
		String jobId = obj.getJobId();
		String fileName = null;
		String jobOutputFileName = null;
		String outputFileLocation = null;
		String ktrFilePath = appConfigProperties.getKtrFilePath();
		if (dif.getInputFileTypeId() == 1 && dif.getIntervalLength() == 900) {
			fileName = timeStamp + "_" + jobId + "_grid_format_output";
			outputFileLocation = jobUploadPath + "/" + fileName;
			ktrFilePath = realPath + ktrFilePath + "grid_format.ktr";
		} else if (dif.getInputFileTypeId() == 1 && dif.getIntervalLength() == 3600) {
			fileName = timeStamp + "_" + jobId + "_grid_format_output";
			outputFileLocation = jobUploadPath + "/" + fileName;
			ktrFilePath = realPath + ktrFilePath + "grid_1_hour_format.ktr";
		} else if (dif.getInputFileTypeId() == 2 && dif.getIntervalLength() == 900) {
			fileName = timeStamp + "_" + jobId + "_column_format_output";
			outputFileLocation = jobUploadPath + "/" + fileName;
			ktrFilePath = realPath + ktrFilePath + "column_format.ktr";
		} else if (dif.getInputFileTypeId() == 2 && dif.getIntervalLength() == 3600) {
			fileName = timeStamp + "_" + jobId + "_column_format_output";
			outputFileLocation = jobUploadPath + "/" + fileName;
			ktrFilePath = realPath + ktrFilePath + "column_1_hour_format.ktr";
		}
		jobOutputFileName = fileName + ".csv";
		obj.setOutputFileLink(outputFileLocation + ".csv");
		obj.setOutputFileName(jobOutputFileName);
		obj = jobRepository.save(obj);
		map.put("jobs", obj);
		this.executeJob(obj, outputFileLocation, inputFileLocation, ktrFilePath, realPath, dif);
		return map;
	}

	public Jobs executeJob(Jobs obj, String outputFileLocation, String inputFileLocation, String ktlPath,
			String realPath, DataInputFile dif) {
		try {
			this.callTransformation(obj, dif, outputFileLocation, ktlPath);
			this.callPMTService(obj.getJobId(), dif.getSpaceId().getGeneratedUUID(), realPath, dif);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return obj;
	}

	private void callPMTService(String jobId, String space, String realPath, DataInputFile dif) {
		logger.info("PMT Call start....");
		Jobs job = jobService.findJobByJobId(jobId);
		int index = 0;
		Map<String, Object> map = new HashMap<String, Object>();
		boolean flag = false;
		File dir = null;
		String localOutputFilePath = null;
		String localDataInputFilePath = null;
		try {
			JobStatus jobStatus = null;
			if (job != null) {
				while (true) {
					jobStatus = jobService.getJobStatusByJobId(jobId);
					logger.info("\n\n");
					logger.info(jobStatus + "\n\n");
					logger.info(jobStatus + "\n\n");
					if (jobStatus != null) {
						logger.info("transformation id ==> " + jobStatus.getTransformationId());
						List<Object[]> listObj = jobRepository.findTransformationById(jobStatus.getTransformationId());
						if (listObj.size() > 0) {
							String status = null;
							for (Object[] obj : listObj) {
								status = obj[3].toString();
								break;
							}
							logger.info("current job status = " + status);
							if (status.equals("end")) {
								flag = true;
								break;
							} else if (status.equals("stop")) {
								break;
							} else {
								logger.info("status = " + status);
								index++;
								if (index > 10) {
									break;
								} else {
									try {
										Thread.sleep(10000);
									} catch (InterruptedException e) {
										logger.error(e.getMessage(), e);
									}
								}
							}
						} else {
							logger.info("ID_BATCH " + jobStatus.getTransformationId()
									+ " transformation status not found.");
						}
					} else {
						logger.error("We don't have status of the job id = " + jobId);
						index++;
						if (index > 10) {
							break;
						} else {
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								logger.error(e.getMessage(), e);
							}
						}
					}
				}
				logger.info("flag ==> " + flag);
				if (flag) {
					// validate output file before send to the PMT
					localDataInputFilePath = appConfigProperties.getLocalDataInputFilePath();
					Map<String, Object> validDataMap = validateDataInputFiles.validateData(dif, job,
							localDataInputFilePath, s3Service);
					if (validDataMap.size() > 0) {
						String status = validDataMap.get("status").toString();
						logger.info("validation status = " + status);
						if (status.equals("error")) {
							Object objData = validDataMap.get("validationStatus");
							JobValidationStatus jvs = (JobValidationStatus) objData;
							jvs.setJobId(job.getJobId());
							if (jvs != null && jvs.getIsDataValid() != null && jvs.getIsDataValid() == false) {
								jvs = jobValidationAndStatusRepository.saveAndFlush(jvs);
								objData = validDataMap.get("errorDetails");
								@SuppressWarnings("unchecked")
								List<ErrorDetails> errorDetailsList = (List<ErrorDetails>) objData;
								for (ErrorDetails ed : errorDetailsList) {
									if (ed != null) {
										ed.setJobValidationStatusId(jvs);
										errorDetailsService.saveOrUpdate(ed);
									}
								}
							} else {
								logger.info("JobValidationStatus obj = null obj");
							}
						} else {
							// data is repaired
							Object objData = validDataMap.get("validationStatus");
							JobValidationStatus jvs = (JobValidationStatus) objData;
							jvs.setJobId(job.getJobId());
							objData = validDataMap.get("errorDetails");
							@SuppressWarnings("unchecked")
							List<ErrorDetails> errorDetailsList = (List<ErrorDetails>) objData;
							jvs.setErrorDetailsList(errorDetailsList);
							jvs = jobValidationAndStatusRepository.saveAndFlush(jvs);
							logger.info("JobValidationStatus object data = " + jvs.toString());
							if (jvs.getIsSpike() != null && jvs.getIsSpike()) {
								logger.info("Spike error.!");
							} else {
								// run job validation success
								jobStatus = jobService.getJobStatusByJobId(jobId);
								String localOutputPath = realPath + localDataInputFilePath + dif.getSpaceId().getGeneratedUUID() + "/"
										+ job.getJobId();
								localOutputFilePath = localOutputPath + "/" + job.getOutputFileName();
								logger.info("localOutputFilePath = " + localOutputFilePath);
								dir = new File(localOutputPath);
								if (!dir.exists()) {
									dir.mkdirs();
									logger.info("Dir is create");
								}
								String sourcePath = job.getOutputFileLocation() + job.getOutputFileName();
								sourcePath = sourcePath.replace(".", "_final.");
								s3Service.saveToLocal(sourcePath, localOutputFilePath);
								jobStatus.setIsPmtServiceCall(true);
								logger.info("jobStatus obj data = " + jobStatus.toString());
								jobStatus = jobStatusRepository.save(jobStatus);
								logger.info("File SourcePath =" + sourcePath);
								map = pmtService.PMTHistoricalAPI(dif,localOutputFilePath);
								if(map.get("status") != null){

									jobStatus.setPmtStatus(map.get("status").toString());
									jobStatus.setPmtResponse(map.get("detailedMsg").toString());
								}
								if(map.containsKey("pmtError")){
									jobStatus.setPmtStatus("pmtError");
									jobStatus.setPmtResponse("PMT Call failed");
								}
								jobStatus = jobStatusRepository.save(jobStatus);
							}
						}
					} else {
						logger.info("output file not validated");
					}
				}
			} else {
				logger.error("Job id not found");
			}
			logger.info("PMT Call end....");
		} catch (Exception e) {
			logger.info(e.getMessage(), e);
		} finally {
			try {
				if( (dif != null) && (dif.getSpaceId()!= null)){
					String localOutputPath = realPath + localDataInputFilePath + dif.getSpaceId().getGeneratedUUID() + "/"
							+ job.getJobId();
					if (localOutputPath != null) {
						dir = new File(localOutputPath);
						logger.info("localOutputPath = " + localOutputPath);
						logger.info("localOutputPath exits = " + dir.exists());
						FileUtils.deleteDirectory(dir);
					}
				}
			} catch (Exception e) {
				logger.info(e.getMessage(), e);
			}
		}
	}

	private void callTransformation(Jobs jobObj, DataInputFile dif, String outputFileLocation, String ktrFilePath) {
		jobexec.executeTransformation(ktrFilePath, dif, outputFileLocation, jobObj, appConfigProperties);
	}
}
