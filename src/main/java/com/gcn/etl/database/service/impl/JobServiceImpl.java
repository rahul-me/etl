package com.gcn.etl.database.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.gcn.etl.database.models.DataInputFile;
import com.gcn.etl.database.models.ErrorDetails;
import com.gcn.etl.database.models.JobStatus;
import com.gcn.etl.database.models.JobValidationStatus;
import com.gcn.etl.database.models.Jobs;
import com.gcn.etl.database.repository.JobRepository;
import com.gcn.etl.database.repository.JobStatusRepository;
import com.gcn.etl.database.service.DataInputFileService;
import com.gcn.etl.database.service.ErrorDetailsService;
import com.gcn.etl.database.service.JobService;
import com.gcn.etl.database.service.JobValidationStatusService;
import com.gcn.etl.helper.ETLErrors;
import com.gcn.etl.helper.JobStatusValues;
import com.gcn.etl.lib.s3Aws.S3AwsService;
import com.gcn.etl.pojo.ApplicationErrorDetails;
import com.gcn.etl.pojo.Content;
import com.gcn.etl.pojo.Errors;
import com.gcn.etl.pojo.JobDetails;
import com.gcn.etl.pojo.JobReport;
import com.gcn.etl.pojo.Load;
import com.gcn.etl.pojo.MissingPoint;
import com.gcn.etl.pojo.Pmt;
import com.gcn.etl.pojo.Transformation;
import com.gcn.etl.pojo.ValidationStatus;
import com.gcn.etl.propertiesHelper.AppConfigProperties;

@Service
public class JobServiceImpl implements JobService {

	private static Logger logger = LogManager.getLogger(JobServiceImpl.class);

	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private AppConfigProperties appConfig;

	@Autowired
	private JobStatusRepository jobStatusRepository;

	@Autowired
	private S3AwsService s3Service;
	
	@Autowired
	private ErrorDetailsService errorDetailsService;

	@Autowired
	private JobValidationStatusService jobValidationService;
	
	@Autowired
	private DataInputFileService  dataInputFileService;

	@Autowired
	private AppConfigProperties appConfigProperties;


	private JobDetails jobDetails;

	public List<Jobs> findAllJobs() {
		return jobRepository.findAll();
	}

	public Page<Jobs> findAllJobs(Pageable pageable) {
		return jobRepository.findAll(pageable);
	}

	public Jobs save(Jobs obj) {
		return jobRepository.saveAndFlush(obj);
	}

	public Jobs findJobById(long id) {
		return jobRepository.findById(id);
	}

	public Jobs findJobByJobId(String jobId) {
		logger.info("find job using job id = " + jobId);
		List<Jobs> list = jobRepository.findAllByJobId(jobId);
		if (list.size() > 0) {
			return list.get(list.size() - 1);
		}
		return null;
	}

	public JobStatus save(JobStatus obj) {
		return jobStatusRepository.save(obj);
	}

	public boolean isUniqueJobId(String jobId) {
		Jobs obj = this.findJobByJobId(jobId);
		if (obj != null) {
			return false;
		}
		return true;
	}

	public Boolean deleteJob(String jobId) {
		logger.info("delete request for job id = " + jobId);
		List<Jobs> list = jobRepository.findAllByJobId(jobId);
		Boolean status = false;
		if (list.size() > 0) {
			for (Jobs job : list) {
				jobRepository.delete(job.getId());
			}
			status = true;
		}
		return status;
	}

	public String getStatus(String jobId) {
		logger.info("find job status for jobId = " + jobId);
		List<JobStatus> jobStatusList = jobStatusRepository
				.findStatusByJobIdOrderByIdDesc(jobId);
		String status = null;
		if (jobStatusList.size() > 0) {
			JobStatus obj = jobStatusList.get(0);
			List<Object[]> transformationObj = jobRepository
					.findTransformationById(obj.getTransformationId());
			for (Object objData[] : transformationObj) {
				status = objData[0].toString();
				break;
			}
			// status = transformationObj.getSTATUS();
		}
		return status;
	}

	public JobStatus getJobStatusByJobId(String jobId) {
		List<JobStatus> list = jobStatusRepository
				.findAllByJobIdOrderByIdDesc(jobId);
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	public String getTransformationStatus(long transformationId) {
		List<Object[]> transformationObj = jobRepository
				.findTransformationStatusByTransformationId(transformationId);
		String status = null;
		for (Object[] obj : transformationObj) {
			status = obj[0].toString();
			break;
		}
		return status;
	}

	public List<Object[]> getTransformationById(long transformationId) {
		return jobRepository.findTransformationById(transformationId);
	}

	public List<Object[]> findStepListByTransformationId(long transformationId) {
		return jobRepository.findStepListByTransformationId(transformationId);
	}

	@Override
	public Map<String, Object> getJobStatus(String jobId, String spaceId) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			com.gcn.etl.pojo.JobStatus jobStatus = new com.gcn.etl.pojo.JobStatus();
			jobStatus.setJobId(jobId);
			Jobs job = this.findJobByJobId(jobId);
			if (job != null) {
				if ((job.getSpaceId() != null)
						&& (job.getSpaceId().getGeneratedUUID().equals(spaceId))) {
					jobStatus = setJobStatusFromJob(job);
					map.put("jobStatus", jobStatus);
				} else {
					map.put("errorMessage","Invalid = " + spaceId + " space id");
				}
			} else {
				map.put("errorMessage","Job id " + jobId + " not found");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return map;
	}
	
	@Override
	public Map<String, Object> getJobReport(String jobId, String spaceId,Pageable pageable) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			JobReport jobReport = new JobReport();
			Jobs job = this.findJobByJobIdAndSpaceId(jobId,spaceId);
			if (job != null) {
				if ((job.getSpaceId() != null)
						&& (job.getSpaceId().getGeneratedUUID().equals(spaceId))) {
					jobReport = setJobReportFromJob(job,pageable);
					map.put("DataInputFiles", jobReport);
				} else {
					//Invalid = " + spaceId + " space id
					map.put("application", appConfigProperties.getApplication());
					map.put("requestId", "");
					map.put("status", "failed");
					map.put("Timestamp", new Date().toString());
					Errors errors = new Errors();
					errors.setApplicationErrorCode(ETLErrors.INVALID_JOB_ID_FOR_SPACE_TRANSFORMATION.errorCode());
					errors.setApplicationErrorMsg(ETLErrors.INVALID_JOB_ID_FOR_SPACE_TRANSFORMATION.errorMessage()+spaceId);
					map.put("errors", errors);  
					map.put("errorMessage","Invalid = " + spaceId + " space id");
				}
			} else {
				//Job id " + jobId + " not found
				map.put("application", appConfigProperties.getApplication());
				map.put("requestId", "");
				map.put("status", "failed");
				map.put("Timestamp", new Date().toString());
				Errors errors = new Errors();
				errors.setApplicationErrorCode(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorCode());
				errors.setApplicationErrorMsg(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorMessage()+" id:"+jobId);
				map.put("errors", errors);  
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return map;
	}

	private Jobs findJobByJobIdAndSpaceId(String jobId, String spaceId) {
		logger.info("find job using job id = " + jobId);
		List<Jobs> list = jobRepository.findJobByJobIdAndSpaceId(jobId,spaceId);
		if (list.size() > 0) {
			return list.get(list.size() - 1);
		}
		return null;
	}

	private JobReport setJobReportFromJob(Jobs job, Pageable pageable) {
		try {
			JobReport jobReport = new JobReport();
			JobValidationStatus jvs = jobValidationService
					.findFirstByJobIdOrderByIdDesc(job.getJobId());
			if (jvs != null) {
				if (jvs.getErrorDetailsList() != null
						&& jvs.getErrorDetailsList().size() > 0) {
					Page<ErrorDetails> list = errorDetailsService.findByJobValidationStatusId(pageable,jvs.getId());
					List<Content> jobReportContentList = new ArrayList<Content>();
					for (ErrorDetails obj : list) {
						Content jobReportContent = new Content();
						jobReportContent.setDate(obj.getDate());
						jobReportContent.setInterval(obj.getIntervalTime());
						jobReportContent.setMeterNo(obj.getMeterNo());
						jobReportContent.setColIndex(obj.getColIndex());
						jobReportContent.setRowIndex(obj.getRowIndex());
						jobReportContent.setExtractedValue(obj.getValue());
						jobReportContent.setTransformedValue(obj.getTransformedValue());
						jobReportContent.setIsMissingPointRepaired(jvs.getIsMissingPointRepaired());
						jobReportContent.setIsSpike(jvs.getIsSpike());
						jobReportContent.setMessage(jvs.getMessage());
						jobReportContentList.add(jobReportContent);
					}
					jobReport.setContent(jobReportContentList);
					jobReport.setFirst(list.isFirst());
					jobReport.setLast(list.isLast());
					jobReport.setNumber(list.getNumber());
					jobReport.setNumberOfElements(list.getNumberOfElements());
					jobReport.setSize(list.getSize());
					jobReport.setSort(list.getSort().DEFAULT_DIRECTION.name());
					jobReport.setTotalElements((int)list.getTotalElements());
					jobReport.setTotalPages(list.getTotalPages());
				}
				
				return jobReport;
			} else {
				List<Content> jobReportContentList = new ArrayList<Content>();
				jobReport.setContent(jobReportContentList);
			}
			return jobReport;
		}
		catch(Exception e){
			logger.error(e);
		}
		return null;
	}

	private com.gcn.etl.pojo.JobStatus setJobStatusFromJob(Jobs job) {
		try {
			com.gcn.etl.pojo.JobStatus jobStatus = new com.gcn.etl.pojo.JobStatus();
			jobStatus.setJobId(job.getJobId());
			JobValidationStatus jvs = jobValidationService
					.findFirstByJobIdOrderByIdDesc(job.getJobId());
			if (jvs != null) {
				if (jvs.getIsDataValid()) {
					JobStatus obj = this.getJobStatusByJobId(job.getJobId());
					if (obj != null) {
						jobStatus = getJobStatus(obj, jobStatus, jvs,
								job);
					} else {
						ValidationStatus vStatus = getValidationStatus(jvs);
						jobStatus.setValidation(vStatus);
						jobStatus.setMessage("Job status not found");
						logger.info("Job status not found");
					}
				} else {
					ValidationStatus vStatus = getValidationStatus(jvs);
					jobStatus.setStatus(JobStatusValues.Failed.toString());
					jobStatus.setValidation(vStatus);
					jobStatus.setTransformation(new Transformation());
					jobStatus.setMessage("Data file not valid");
					jobStatus.setApplicationErrorCode(jvs.getApplicationErrorCode());
					jobStatus.setApplicationErrorMsg(jvs.getApplicationErrorMsg());
					logger.info("Data file not valid");
				}
			} else {
				jobStatus.setMessage("Validation status not found");
				jobStatus.setStatus(JobStatusValues.Loading.toString());
				JobStatus obj = this.getJobStatusByJobId(job.getJobId());
				
				if (obj != null) {
					jobStatus = getJobStatus(obj, jobStatus, jvs, job);
					if(obj.getTransformationId() != 0){
						jobStatus.setStatus(JobStatusValues.Queued.toString());
					}
					
				}
			}
			return jobStatus;
		}
		catch(Exception e){
			logger.error(e);
		}
		return null;
	}

	private com.gcn.etl.pojo.JobStatus getJobStatus(JobStatus obj,
			com.gcn.etl.pojo.JobStatus jobStatus, JobValidationStatus jvs,
			Jobs job) {
		if (obj != null) {
			logger.info("Job Status entity = "+obj);
			List<Object[]> list = this.getTransformationById(obj
					.getTransformationId());
			String status = null;
			if (list.size() > 0) {
				for (Object row[] : list) {
					status = row[3].toString();
					if (jvs != null) {
						ValidationStatus vStatus = getValidationStatus(jvs);
						jobStatus.setValidation(vStatus);
					}
					Transformation trans = new Transformation();
					trans.setStatus(status);
					jobStatus.setTransformation(trans);										
					jobStatus.setMessage("success");
					break;
				}
				// verify output file uploaded on S3Aws server
				boolean exits = s3Service.isFileExists(job.getOutputFileLink());
				Load load = new Load();
				load.setS3Aws(exits);
				jobStatus.setLoad(load);
				Pmt pmt = new Pmt();
				pmt.setStatus(obj.getPmtStatus());
				pmt.setResponse(obj.getPmtResponse());
				jobStatus.setPmt(pmt);
				
				if(jvs != null){
					jobStatus.setStatus(jvs.getJobStatus());
					if(jvs.getApplicationErrorMsg() != null){
						jobStatus.setApplicationErrorCode(jvs.getApplicationErrorCode());
						jobStatus.setApplicationErrorMsg(jvs.getApplicationErrorMsg());
					}
				}
			}
		}
		return jobStatus;
	}

	private ValidationStatus getValidationStatus(JobValidationStatus jvs) {
		ValidationStatus vStatus = new ValidationStatus();
		if (jvs.getErrorDetailsList() != null
				&& jvs.getErrorDetailsList().size() > 0) {
			List<ErrorDetails> list = jvs.getErrorDetailsList();
			List<MissingPoint> missingIntervalOrUsage = new ArrayList<MissingPoint>();
			MissingPoint missingPoint = null;
			for (ErrorDetails obj : list) {
				missingPoint = new MissingPoint();
				missingPoint.setDate(obj.getDate());
				missingPoint.setInterval(obj.getIntervalTime());
				missingPoint.setMeterNo(obj.getMeterNo());
				missingPoint.setColIndex(obj.getColIndex());
				missingPoint.setRowIndex(obj.getRowIndex());
				missingPoint.setValue(obj.getValue());
				missingIntervalOrUsage.add(missingPoint);
			}
			vStatus.setMissingIntervalOrUsage(missingIntervalOrUsage);
		}
		vStatus.setIsDataValid(jvs.getIsDataValid());
		vStatus.setIsMissingPointRepaired(jvs.getIsMissingPointRepaired());
		vStatus.setIsSpike(jvs.getIsSpike());
		vStatus.setMessage(jvs.getMessage());
		return vStatus;
	}

	@Override
	public List<Jobs> findAll(Sort sort) {
		// TODO Auto-generated method stub
		return jobRepository.findAll(sort);
	}

	@Override
	public Page<Jobs> findAllJobsbySpaceID(Pageable pageable, String spaceId) {
		return jobRepository.findAllJobsbySpaceID(pageable, spaceId);
	}

	public JobDetails getJobDetailsForJob(Jobs job) {
		try {
			JobDetails jobDetails = new JobDetails();
			if(job != null){
				if(job.getCreated_at() != null){
					jobDetails.setTimestamp(job.getCreated_at().getTime());
				}
				DataInputFile dataInputFile = dataInputFileService.findByFileId(job.getInputFileId());
				jobDetails.setSkipTransformation(job.getSkipTransformation());
				jobDetails.setStatus(job.getStatus());
				jobDetails.setRawCsv(dataInputFile.getLocation()+dataInputFile.getFileName());
				String transformationCsv = job.getOutputFileLink();
				jobDetails.setTransformationCsv(transformationCsv);
				String validationCsv = job.getOutputFileLink();
				validationCsv = validationCsv.replace(".", "_final.");
				jobDetails.setValidationCsv(validationCsv);
				jobDetails.setApplication(appConfig.getApplication());
				jobDetails.setRequestId("");
				jobDetails.setTimestamp(job.getCreated_at().getTime());
				jobDetails.setCallbackUrl(job.getCallbackUrl());
				jobDetails.setInputFileId(job.getInputFileId());
				jobDetails.setJobId(job.getJobId());
				com.gcn.etl.pojo.JobStatus jobStatus = setJobStatusFromJob(job);
				if(jobStatus != null){
					
					List<Errors> errors = new ArrayList<Errors>();
					if(jobStatus.getTransformation() == null){
						
						Errors error = new Errors();
						error.setApplicationErrorCode(ETLErrors.KETTLE_TRANFORMATION.errorCode());
						error.setApplicationErrorMsg(ETLErrors.KETTLE_TRANFORMATION.errorMessage());
						List<ApplicationErrorDetails> jobErrorDetailsList = new ArrayList<ApplicationErrorDetails>();
						error.setErrorDetails(jobErrorDetailsList);
						errors.add(error);
					}
					if(jobStatus.getApplicationErrorCode() != 0){
						
						Errors error = new Errors();
						error.setApplicationErrorCode(jobStatus.getApplicationErrorCode());
						error.setApplicationErrorMsg(jobStatus.getApplicationErrorMsg());
						List<ApplicationErrorDetails> jobErrorDetailsList = new ArrayList<ApplicationErrorDetails>();
						
						if(jobStatus.getApplicationErrorCode() == ETLErrors.MORE_THAN_4_MISSING_DATAPOINTS_TRANSFORMATION.errorCode()){
							if(jobStatus.getValidation() != null){

								if(jobStatus.getValidation().getMissingIntervalOrUsage() != null){
								if(jobStatus.getValidation().getMissingIntervalOrUsage().size() > 0){
									for(MissingPoint missingPoint: jobStatus.getValidation().getMissingIntervalOrUsage()){
										ApplicationErrorDetails errorDetails = new ApplicationErrorDetails();
										errorDetails.setColIndex(missingPoint.getColIndex());
										errorDetails.setRowIndex(missingPoint.getRowIndex());
										errorDetails.setDate(missingPoint.getDate());
										errorDetails.setInterval(missingPoint.getInterval());
										jobErrorDetailsList.add(errorDetails);

									}
								}
							}
							}
						}
						
						if(jobStatus.getApplicationErrorCode() == ETLErrors.SPIKE_FOUND_TRANSFORMATION.errorCode()){
							if(jobStatus.getValidation() != null){
								if(jobStatus.getValidation().getMissingIntervalOrUsage() != null){
									if(jobStatus.getValidation().getMissingIntervalOrUsage().size() > 0){
									
										for(MissingPoint missingPoint: jobStatus.getValidation().getMissingIntervalOrUsage()){
											ApplicationErrorDetails errorDetails = new ApplicationErrorDetails();
											errorDetails.setColIndex(missingPoint.getColIndex());
											errorDetails.setRowIndex(missingPoint.getRowIndex());
											errorDetails.setDate(missingPoint.getDate());
											errorDetails.setInterval(missingPoint.getInterval());
											errorDetails.setValue(missingPoint.getValue());
											jobErrorDetailsList.add(errorDetails);
										}
									}
								}
							
							}
						}
						error.setErrorDetails(jobErrorDetailsList);
						errors.add(error);
					}
					jobDetails.setStatus(jobStatus.getStatus());
					if(jobStatus.getTransformation() != null){
						if(jobStatus.getTransformation().getStatus() != null){
							if(jobStatus.getTransformation().getStatus().equals("running")){
								jobDetails.setStatus(JobStatusValues.Queued.toString());
							}
						}
					}
					
					Pmt pmt = new Pmt();
					if(jobStatus.getPmt() != null){
						pmt.setStatus(jobStatus.getPmt().getStatus());
						pmt.setResponse(jobStatus.getPmt().getResponse());
						if(jobStatus.getPmt().getStatus() != null){
							if(jobStatus.getPmt().getStatus().equals("pmtError")){
							
								Errors applicationError = new Errors();
								applicationError.setApplicationErrorCode(ETLErrors.PMT_FAILED_MISC.errorCode());
								applicationError.setApplicationErrorMsg(ETLErrors.PMT_FAILED_MISC.errorMessage());
								List<ApplicationErrorDetails> applicationErrorDetailsList = new ArrayList<ApplicationErrorDetails>();
								applicationError.setErrorDetails(applicationErrorDetailsList);
								errors.add(applicationError);
							}
						}	
					}
					jobDetails.setErrors(errors);
				}
				
			}
			return jobDetails;
		}
			
		catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}
