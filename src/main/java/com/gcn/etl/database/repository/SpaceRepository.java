package com.gcn.etl.database.repository;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gcn.etl.database.models.Space;

public interface SpaceRepository extends JpaRepository<Space, Serializable>{

	@Query("SELECT t FROM Space t WHERE t.spaceName = :spaceName and t.isDeleted != true")
	Space findBySpaceName(@Param("spaceName") String spaceName);

	@Query("SELECT t FROM Space t WHERE t.generatedUUID = :uuid and t.isDeleted != true")
	Space findByGeneratedUUID(@Param("uuid") String uuid);

	void deleteByGeneratedUUID(String uuid);

	@Query("SELECT t FROM Space t WHERE t.isDeleted != true")
	Page<Space> findAllSpace(Pageable pageable);
}
