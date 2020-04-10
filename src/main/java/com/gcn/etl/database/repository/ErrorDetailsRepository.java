package com.gcn.etl.database.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gcn.etl.database.models.ErrorDetails;

public interface ErrorDetailsRepository extends JpaRepository<ErrorDetails, Serializable> {

	public List<ErrorDetails> findByJobValidationStatusId(long id);
	@Query("SELECT t FROM ErrorDetails t WHERE t.jobValidationStatusId.id = ?1")
	public Page<ErrorDetails> findByJobValidationStatusId(Pageable pageable,
			long id);

}
