package com.gcn.etl.database.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gcn.etl.database.models.JobValidationStatus;

public interface JobValidationStatusRepository extends JpaRepository<JobValidationStatus, Serializable> {

	public JobValidationStatus findByJobId(String jobId);

	public List<JobValidationStatus> findAllByJobId(String jobId);
	
	public List<JobValidationStatus> findAllByJobIdOrderByIdDesc(String jobId);
	
	public JobValidationStatus findFirstByJobIdOrderByIdDesc(String jobId);

}
