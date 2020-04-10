package com.gcn.etl.database.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.gcn.etl.database.models.DataInputFile;

public interface DataInputFileService {
	public List<DataInputFile> findAll();

	public Page<DataInputFile> findAll(Pageable pageable);
	
	public List<DataInputFile> findAll(Sort sort);

	public DataInputFile findById(long id);

	public DataInputFile save(DataInputFile obj);

	public DataInputFile findByFileId(String fileId);

	public boolean isFileIdUnique(String file_id);

	public void delete(long id);

	public Page<DataInputFile> findbySpaceID(Pageable pageable, String spaceId);

}
