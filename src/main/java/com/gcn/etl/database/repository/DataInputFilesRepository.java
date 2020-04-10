package com.gcn.etl.database.repository;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gcn.etl.database.models.DataInputFile;

public interface DataInputFilesRepository extends JpaRepository<DataInputFile, Serializable> {

	public DataInputFile findById(long id);

	public DataInputFile findByFileId(String fileId);

	@Query("SELECT t FROM DataInputFile t WHERE t.spaceId.generatedUUID = ?1 and t.spaceId.isDeleted != true")
	public Page<DataInputFile> findbySpaceID(Pageable pageable, String spaceId);	

}