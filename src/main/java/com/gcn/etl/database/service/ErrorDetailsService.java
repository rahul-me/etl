package com.gcn.etl.database.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gcn.etl.database.models.ErrorDetails;

public interface ErrorDetailsService {
	public ErrorDetails saveOrUpdate(ErrorDetails obj);

	public List<ErrorDetails> findByJobValidationStatusId(long id);

	public Page<ErrorDetails> findByJobValidationStatusId(Pageable pageable, long id);
}
