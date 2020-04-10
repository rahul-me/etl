package com.gcn.etl.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gcn.etl.database.models.Space;
import com.gcn.etl.database.service.SpaceService;

@Controller
public class SpaceController {
	private static Logger logger = LogManager.getLogger(SpaceController.class.getName());
	
	@Autowired
	SpaceService spaceService;

	@RequestMapping(value = "/spaces", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> save(HttpServletRequest req,@RequestBody Space space) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			
			Space spaceName = spaceService.findBySpaceName(space.getSpaceName());
			if (spaceName == null) {
				Date date = new Date();  
			    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ssXXX");  
			    String strDate= formatter.format(date);  
			    space.setTimestamp(strDate);
		        String randomUUIDString = RandomStringUtils.random(8, true, true).toLowerCase();
				space.setGeneratedUUID("etl_"+randomUUIDString);
				spaceService.save(space);
				map.put("space", space);
			} else {
				map.put("errorMessage", "Space existing with name " + space.getSpaceName());
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
				
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("errorMessage",e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/spaces/{uuid}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object>  findByUUID(@PathVariable String uuid) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Space space = spaceService.findByGeneratedUUID(uuid);
			if (space != null) {
				map.put("space", space);
			} else {
				map.put("errorMessage", "Space Not Found");
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("errorMessage",e.getMessage() );
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/spaces/{uuid}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Object> deleteByGeneratedUUID(@PathVariable("uuid") String uuid) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Space space = spaceService.findByGeneratedUUID(uuid);
			if (space != null) {
				space.setDeleted(true);
				spaceService.save(space);
				map.put("message", "Space deleted successfully");
			} else {
				map.put("errorMessage", "Space Not Found");
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("errorMessage",e.getMessage() );
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/allspaces", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> getAll(Pageable pageable) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Page<Space> spaceList = spaceService.findAllSpace(pageable);
			if (spaceList.getContent().size() > 0) {
				map.put("spaces", spaceList);
			} else {
				map.put("errorMessage", "Space Not Found");
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("errorMessage",e.getMessage() );
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
}
