package com.gcn.etl.database.service;

import java.util.List;

import com.gcn.etl.database.models.JobValidationStatus;
	
public interface JobValidationStatusService {
	public JobValidationStatus saveAndFlush(JobValidationStatus obj);

	public JobValidationStatus findByJobId(String jobId);
	
	public List<JobValidationStatus> findAllByJobId(String jobId);
	
	public JobValidationStatus findFirstByJobIdOrderByIdDesc(String jobId);
}
