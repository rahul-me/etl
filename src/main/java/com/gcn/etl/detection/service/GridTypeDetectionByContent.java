package com.gcn.etl.detection.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gcn.etl.detection.IGridTypeDetectionApproach;

@Service(value="byContent")
public class GridTypeDetectionByContent implements IGridTypeDetectionApproach {

	@Override
	public boolean detectGrid(List<String[]> file) {		
		return false;
	}

}
