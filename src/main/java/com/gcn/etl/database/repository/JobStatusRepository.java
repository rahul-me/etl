package com.gcn.etl.database.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gcn.etl.database.models.JobStatus;

public interface JobStatusRepository extends JpaRepository<JobStatus, Serializable>{

	public JobStatus findByJobId(String jobId);
	
	public List<JobStatus> findAllByJobIdOrderByIdDesc(String jobId);
	
	public JobStatus findJobStatusByJobId(String jobId);	
	
	public List<JobStatus> findStatusByJobIdOrderByIdDesc(String jobId);

}