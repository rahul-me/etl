package com.gcn.etl.database.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gcn.etl.database.models.Space;
import com.gcn.etl.database.repository.SpaceRepository;
import com.gcn.etl.database.service.SpaceService;

@Service
public class SpaceServiceImpl implements SpaceService {
	
	private static Logger logger = LogManager.getLogger(SpaceServiceImpl.class.getName());

	@Autowired
	SpaceRepository spaceRepo;

	@Override
	public void save(Space space) {
		try {
			spaceRepo.saveAndFlush(space);
		}
		catch (Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public Space findBySpaceName(String spaceName) {
		// TODO Auto-generated method stub
		return spaceRepo.findBySpaceName(spaceName);
	}

	@Override
	public Space findByGeneratedUUID(String uuid) {
		// TODO Auto-generated method stub
		return spaceRepo.findByGeneratedUUID(uuid);
	}

	@Override
	@Transactional
	public void deleteByGeneratedUUID(String uuid) {
		// TODO Auto-generated method stub
		spaceRepo.deleteByGeneratedUUID(uuid);
	}

	@Override
	public Page<Space> findAllSpace(Pageable pageable) {
		// TODO Auto-generated method stub
		return spaceRepo.findAllSpace(pageable);
	}

}
