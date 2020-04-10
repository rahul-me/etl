package com.gcn.etl.database.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gcn.etl.database.models.JobValidationStatus;
import com.gcn.etl.database.repository.JobValidationStatusRepository;
import com.gcn.etl.database.service.JobValidationStatusService;

@Service
public class JobValidationStatusServiceImpl implements JobValidationStatusService {

	@Autowired
	private JobValidationStatusRepository jobValidationStatusRepository;

	public JobValidationStatus findByJobId(String jobId) {
		return jobValidationStatusRepository.findByJobId(jobId);
	}

	@Override
	public JobValidationStatus saveAndFlush(JobValidationStatus obj) {
		JobValidationStatus jobStatusObj = jobValidationStatusRepository.saveAndFlush(obj);
		return jobStatusObj;
	}

	@Override
	public List<JobValidationStatus> findAllByJobId(String jobId) {
		// TODO Auto-generated method stub
		List<JobValidationStatus> list = jobValidationStatusRepository.findAllByJobId(jobId);
		return list;
	}

	@Override
	public JobValidationStatus findFirstByJobIdOrderByIdDesc(String jobId) {
		// TODO Auto-generated method stub
		return jobValidationStatusRepository.findFirstByJobIdOrderByIdDesc(jobId);
	}
}
