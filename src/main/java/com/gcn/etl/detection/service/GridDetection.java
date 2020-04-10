package com.gcn.etl.detection.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gcn.etl.detection.IGridTypeDetectionApproach;
import com.gcn.etl.helper.AutoDetectHelper;
import com.gcn.etl.pojo.TimeDataLocation;
import com.gcn.etl.propertiesHelper.AppConfigProperties;
import com.google.gwt.user.client.rpc.core.java.util.Arrays;
import com.gcn.etl.constant.Constant;
import com.gcn.etl.detection.IGridDetection;


@Service
public class GridDetection implements IGridDetection {	
	
	private static Logger logger = LogManager.getLogger(GridDetection.class);
	
	@Autowired
	@Qualifier("byContent")
	private IGridTypeDetectionApproach gridDetectionByContent;
	
	@Autowired
	@Qualifier("byColumnCount")
	private IGridTypeDetectionApproach gridDetectionByColumnCount;
	
	@Autowired
	private AppConfigProperties config;
	
	@Autowired
	private AutoDetectHelper detectHelper;
	
	@Override
	public boolean isGridType(List<String[]> file) {
		return gridDetectionByColumnCount.detectGrid(file);
	}

	@Override
	public int getIntervalLength(List<String[]> file) {
		TimeDataLocation location = locateTimeData(file);
		if(location != null && location.getColumnIndex()!= null && location.getRowIndex() != null) {
			String[] timeData = file.get(location.getRowIndex());
			int intervalGapInMillies = findIntervalsAndDoMode(timeData, location.getColumnIndex());
			int intervalGapInSeconds = intervalGapInMillies / Constant.ONE_SECOND_IN_MILLIES;
			return intervalGapInSeconds;
		} else {
			logger.error("Index for time data not found.");
		}
		return -1;
	}
	
	private TimeDataLocation locateTimeData(List<String[]> file) {
		int checklimit = config.getLocateTimeDataInGridLimit();
		int rowChecklimit = checklimit <= file.size() ? checklimit : file.size();
		for (int i = 0; i < rowChecklimit; i++) {
			String[] row = file.get(i);
			int columnCheckLimit = checklimit <= row.length ? checklimit : row.length;
			for (int j = 0; j < columnCheckLimit; j++) {
				if (detectHelper.isTimeData(row[j])) {
					if (isTimeDataRow(row, j, columnCheckLimit)) {
						return new TimeDataLocation(i, j);
					}
				}
			}
		}
		return null;
	}
	
	public boolean isTimeDataRow(String[] row, int startIndex, int endIndex) {
		for (int i = startIndex; i < endIndex; i++) {
			if (!detectHelper.isTimeData(row[i])) {
				return false;
			}
		}
		return true;
	}
	
	private int findIntervalsAndDoMode(String[] timeData, int startIndex) {
		int columnCheckLimit = config.getLocateTimeDataInGridLimit();
		Map<Integer, Integer> map = new HashMap<>();
		int maxCount = 0;
		int mode = -1;
		columnCheckLimit = columnCheckLimit <= timeData.length ? columnCheckLimit : timeData.length;
		for(int i = startIndex ; i < columnCheckLimit - 2 ; i++) {
			int interval = (int) (detectHelper.getTimeInMillies(timeData[i+1]) - detectHelper.getTimeInMillies(timeData[i]));			
			Integer count = map.get(interval);
			if(count != null) {
				count++;				
				map.put(interval, count);
				if(maxCount < count) {
					maxCount = count;
					mode = interval;
				}
			} else {
				map.put(interval, 0);
			}
		}		
		return mode;
	}

	@Override
	public String getDateTimeFormat(List<String[]> file) {
		TimeDataLocation location = locateDateData(file);
		if(location != null && location.getColumnIndex() != null && location.getRowIndex() != null) {
			int rowIndex = location.getRowIndex();
			int columnIndex = location.getColumnIndex();
			Map<Integer, Integer> indexCounter = new LinkedHashMap<>();
			Map<Integer, Integer> defaultCharMap = detectHelper.getDefaultIndexChar();
			for(int i = rowIndex ; i < file.size() - 2 ; i++) {
				int firstRowIndex = i;
				while(!detectHelper.isDateData(file.get(firstRowIndex)[columnIndex])) {
					firstRowIndex++;
					if(firstRowIndex > file.size() - 2) {
						break;
					}
					i = firstRowIndex;
				}
				if(firstRowIndex > file.size() - 2) {
					break;
				}
				int nextRowIndex = firstRowIndex + 1;
				if(nextRowIndex > file.size() -1 ) {
					break;
				}
				while(!detectHelper.isDateData(file.get(nextRowIndex)[columnIndex])) {
					nextRowIndex++;
					if(nextRowIndex > file.size() -1 ) {
						break;
					}
				}
				if(nextRowIndex > file.size() ) {
					break;
				}
				String[] row = file.get(firstRowIndex);
				String[] nextRow = file.get(nextRowIndex);
				
				int[] date = detectHelper.getDateParts(row[columnIndex]);
				int[] nextDate = detectHelper.getDateParts(nextRow[columnIndex]);
				
				
				
				if(date != null && nextDate != null) {
					detectHelper.updateCounter(indexCounter, date, nextDate);
					detectHelper.updateDefaultChar(defaultCharMap, row[columnIndex]);
					int increments = detectHelper.getIndexIncrements(indexCounter);					
					if(increments != 0 && increments > 1) {
						break;
					}
				}								
			}
			if(indexCounter.size() > 1) {
				int[] dateDataPosition = detectHelper.getDatePosition(indexCounter);
				int[] noOfChars = detectHelper.getNoOfChars(defaultCharMap);
				return detectHelper.getDateFormat(dateDataPosition, noOfChars);
			} else {
				logger.info("Couldn't able to detect datetime format");
			}
						
		} else {
			logger.info("Couldn't find position of date data");
		}
		return null;
	}
		
	private TimeDataLocation locateDateData(List<String[]> file) {
		int checklimit = config.getLocateTimeDataInGridLimit();
		int rowChecklimit = checklimit <= file.size() ? checklimit : file.size();
		for (int i = 0; i < rowChecklimit; i++) {
			String[] row = file.get(i);
			int columnCheckLimit = checklimit <= row.length ? checklimit : row.length;
			for (int j = 0; j < columnCheckLimit; j++) {
				if (detectHelper.isDateData(row[j])) {					
						return new TimeDataLocation(i, j);					
				}
			}
		}
		return null;
	}
}
