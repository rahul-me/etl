package com.gcn.etl.database.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.gcn.etl.database.models.JobStatus;
import com.gcn.etl.database.models.Jobs;
import com.gcn.etl.pojo.JobDetails;

public interface JobService {
	public List<Jobs> findAllJobs();
	
	public Page<Jobs> findAllJobs(Pageable pageable);

	public Jobs save(Jobs obj);

	public Jobs findJobById(long id);

	public Jobs findJobByJobId(String jobId);

	public boolean isUniqueJobId(String jobId);

	public Boolean deleteJob(String jobId);

	public String getStatus(String jobId);

	public JobStatus save(JobStatus obj);

	public JobStatus getJobStatusByJobId(String jobId);

	public String getTransformationStatus(long transformationId);

	public List<Object[]> getTransformationById(long transformationId);

	public List<Object[]> findStepListByTransformationId(long transformationId);
	
	public Map<String, Object> getJobStatus(String jobId, String spaceId);

	public List<Jobs> findAll(Sort sort);

	public Page<Jobs> findAllJobsbySpaceID(Pageable pageable, String spaceId);

	public JobDetails getJobDetailsForJob(Jobs obj);

	public Map<String, Object> getJobReport(String jobId, String spaceId, Pageable pageable);

}
