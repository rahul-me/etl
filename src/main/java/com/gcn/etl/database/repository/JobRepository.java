package com.gcn.etl.database.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gcn.etl.database.models.Jobs;

public interface JobRepository extends JpaRepository<Jobs, Serializable> {
	public Jobs findById(long id);

	public Jobs findByJobId(String jobId);
	
	public Jobs findFirstByOrderByIdDesc(String jobId);

	@Query(value = "SELECT STATUS FROM transformation_log where ID_BATCH=:transformationId", nativeQuery = true)
	public List<Object[]> findTransformationStatusByTransformationId(@Param("transformationId") long transformationId);

	@Query(value = "SELECT * FROM transformation_log where ID_BATCH=:transformationId", nativeQuery = true)
	public List<Object[]> findTransformationById(@Param("transformationId") long transformationId);

	@Query(value = "SELECT * FROM step_log  where ID_BATCH=:transformationId", nativeQuery = true)
	public List<Object[]> findStepListByTransformationId(@Param("transformationId") long transformationId);

	public List<Jobs> findAllByJobId(String jobId);

	@Query("SELECT t FROM Jobs t WHERE t.spaceId.generatedUUID = ?1  and t.spaceId.isDeleted != true")
	public Page<Jobs> findAllJobsbySpaceID(Pageable pageable, String spaceId);

	@Query("SELECT t FROM Jobs t WHERE t.spaceId.generatedUUID = ?2  and t.spaceId.isDeleted != true and t.jobId= ?1")
	public List<Jobs> findJobByJobIdAndSpaceId(String jobId, String spaceId);
}