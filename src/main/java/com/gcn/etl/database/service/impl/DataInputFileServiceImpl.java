package com.gcn.etl.database.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.gcn.etl.database.models.DataInputFile;
import com.gcn.etl.database.repository.DataInputFilesRepository;
import com.gcn.etl.database.service.DataInputFileService;

@Service
public class DataInputFileServiceImpl implements DataInputFileService {

	@Autowired
	DataInputFilesRepository dataInputFilesRepository;

	public List<DataInputFile> findAll() {
		return dataInputFilesRepository.findAll();
	}

	@Override
	public List<DataInputFile> findAll(Sort sort) {
		// TODO Auto-generated method stub
		return dataInputFilesRepository.findAll(sort);
	}

	@Override
	public Page<DataInputFile> findAll(Pageable pageable) {
		// TODO Auto-generated method stub
		return dataInputFilesRepository.findAll(pageable);
	}

	public DataInputFile findById(long id) {
		return dataInputFilesRepository.findById(id);
	}

	public DataInputFile save(DataInputFile obj) {
		return dataInputFilesRepository.saveAndFlush(obj);
	}

	public DataInputFile findByFileId(String fileId) {
		return dataInputFilesRepository.findByFileId(fileId);
	}

	public boolean isFileIdUnique(String file_id) {
		DataInputFile obj = this.findByFileId(file_id);
		if (obj != null) {
			return false;
		}
		return true;
	}

	public void delete(long id) {
		dataInputFilesRepository.delete(id);
	}

	
	public Page<DataInputFile> findbySpaceID(Pageable pageable,String spaceId) {
		return dataInputFilesRepository.findbySpaceID(pageable,spaceId);
	}
}
