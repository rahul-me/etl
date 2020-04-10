package com.gcn.etl.database.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gcn.etl.database.models.Space;

public interface SpaceService {

	public void save(Space obj);

	public Space findBySpaceName(String spaceName);

	public Space findByGeneratedUUID(String uuid);

	public void deleteByGeneratedUUID(String uuid);

	public Page<Space> findAllSpace(Pageable pageable);
}
