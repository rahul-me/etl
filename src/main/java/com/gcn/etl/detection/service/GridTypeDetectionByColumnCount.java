package com.gcn.etl.detection.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.gcn.etl.detection.IGridTypeDetectionApproach;

@Service(value="byColumnCount")
public class GridTypeDetectionByColumnCount implements IGridTypeDetectionApproach {
	
	private static Logger logger = LogManager.getLogger(GridTypeDetectionByColumnCount.class);
	
	@Override
	public boolean detectGrid(List<String[]> file) {
		if(file != null) {
			if(!file.isEmpty()) {
				String[] firstRow = file.get(0);
				return firstRow.length > 2 && isValidDataRow(firstRow);
			} else {
				logger.warn("Found empty file");
			}
		}
		return false;
	}
	
	private boolean isValidDataRow(String[] row) {
		int emptycount = 0;
		for(int i = row.length - 1 ; i > 1 ; i--) {
			String data = row[i];
			if(data != null && data.trim().length() == 0) {
				emptycount++;
			}
		}
		
		return emptycount != row.length - 2;
	}

}
