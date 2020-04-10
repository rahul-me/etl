package com.gcn.etl.database.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.gcn.etl.controllers.DataUploaderEtlController;
import com.gcn.etl.database.models.ErrorDetails;
import com.gcn.etl.database.repository.ErrorDetailsRepository;
import com.gcn.etl.database.service.ErrorDetailsService;

@Service
public class ErrorDetailsServiceImpl implements ErrorDetailsService {
	private static Logger logger = LogManager.getLogger(ErrorDetailsServiceImpl.class);

	@Autowired
	ErrorDetailsRepository errorDetailsRepository;

	public ErrorDetails saveOrUpdate(ErrorDetails obj) {
		return errorDetailsRepository.saveAndFlush(obj);
	}

	public List<ErrorDetails> findByJobValidationStatusId(long id) {
		return errorDetailsRepository.findByJobValidationStatusId(id);
	}

	@Override
	public Page<ErrorDetails> findByJobValidationStatusId(Pageable pageable,
			long id) {
		try {
			return errorDetailsRepository.findByJobValidationStatusId(pageable,id);
		}
		catch(Exception e){
			logger.error(e);
		}
		return null;
	}

}
