package com.gcn.etl.detection.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gcn.etl.constant.Constant;
import com.gcn.etl.detection.IColumnDetection;
import com.gcn.etl.detection.IColumnTypeDetectionApproach;
import com.gcn.etl.helper.AutoDetectHelper;
import com.gcn.etl.pojo.TimeDataLocation;
import com.gcn.etl.propertiesHelper.AppConfigProperties;

@Service
public class ColumnDetection implements IColumnDetection {
	
	private Logger logger = LogManager.getLogger(ColumnDetection.class);
	
	@Autowired
	private IColumnTypeDetectionApproach typeDetection;

	@Autowired
	private AppConfigProperties config;

	@Autowired
	private AutoDetectHelper detectHelper;

	@Override
	public boolean isColumnType(List<String[]> file) {
		return typeDetection.detectColumn(file);
	}

	@Override
	public int getIntervalLength(List<String[]> file, String dateFormat) {
		TimeDataLocation location = locateDateTimeData(file);
		if (location != null && location.getRowIndex() != null && location.getColumnIndex() != null) {
			int intervalGapInMillies = findIntervalsAndDoMode(file, location.getRowIndex(), location.getColumnIndex(), dateFormat);
			int intervalGapInSeconds = intervalGapInMillies / Constant.ONE_SECOND_IN_MILLIES;
			return intervalGapInSeconds;
		}
		return -1;
	}

	private TimeDataLocation locateDateTimeData(List<String[]> file) {
		int checklimit = config.getLocateTimeDataInGridLimit();
		int rowChecklimit = checklimit <= file.size() ? checklimit : file.size();
		for (int i = 0; i < rowChecklimit; i++) {
			String[] row = file.get(i);
			int columnCheckLimit = checklimit <= row.length ? checklimit : row.length;
			for (int j = 0; j < columnCheckLimit; j++) {
				if (detectHelper.isDateTimeData(row[j])) {
					return new TimeDataLocation(i, j);
				}
			}
		}
		return null;
	}

	private int findIntervalsAndDoMode(List<String[]> file, int rowIndex, int columnIndex, String dateFormat) {
		int checklimit = config.getLocateTimeDataInGridLimit();
		int rowChecklimit = checklimit <= file.size() ? checklimit : file.size();
		Map<Integer, Integer> map = new HashMap<>();
		int maxCount = Constant.DEFAULT_INT;
		int mode = -1;
		for (int i = rowIndex; i < rowChecklimit - 2; i++) {
			String[] row = file.get(i);
			String[] nextRow = file.get(i + 1);
			String data = row[columnIndex];
			String nextData = nextRow[columnIndex];
			if (detectHelper.isDateTimeData(data) && detectHelper.isDateTimeData(nextData)) {
				int interval = (int) (getTimeStampInMillies(nextData, dateFormat) - getTimeStampInMillies(data, dateFormat));
				Integer count = map.get(interval);
				if (count != null) {
					count++;
					map.put(interval, count);
					if (count > maxCount) {
						maxCount = count;
						mode = interval;
					}
				} else {
					map.put(interval, Constant.DEFAULT_INT);
				}
			}
		}
		return mode;
	}
	
	private long getTimeStampInMillies(String timeStamp, String dateFormat) {
		Calendar calendar = detectHelper.getCalendarWithDefaultTime();
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat.split(Constant.SPACE_CONSTANT)[0]);
		try {
			calendar.setTime(dateFormatter.parse(detectHelper.getDatePartFromDateTimeData(timeStamp)));
		} catch (ParseException e) {
			logger.error("Couldn't parse given date");
		}
		detectHelper.setTimeInMillies(detectHelper.getTimePartFromDateTimeData(timeStamp), calendar);
		return calendar.getTimeInMillis();
	}

	@Override
	public String getDateTimeFormat(List<String[]> file) {
		TimeDataLocation location = locateDateTimeData(file);
		if(location != null && location.getColumnIndex() != null && location.getRowIndex() != null) {
			int rowIndex = location.getRowIndex();
			int columnIndex = location.getColumnIndex();
			Map<Integer, Integer> indexCounter = new LinkedHashMap<>();
			Map<Integer, Integer> defaultCharMap = detectHelper.getDefaultIndexChar();
			for(int i = rowIndex ; i < file.size() - 2 ; i++) {
				int firstRowIndex = i;
				while(!detectHelper.isDateData(file.get(firstRowIndex)[columnIndex].trim().split(Constant.SPACE_CONSTANT)[0])) {
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
				while(!detectHelper.isDateData(file.get(nextRowIndex)[columnIndex].trim().split(Constant.SPACE_CONSTANT)[0])) {
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
				
				int[] date = detectHelper.getDateParts(detectHelper.getDatePartFromDateTimeData(row[columnIndex].trim()));
				int[] nextDate = detectHelper.getDateParts(detectHelper.getDatePartFromDateTimeData(nextRow[columnIndex].trim()));
				
				
				
				if(date != null && nextDate != null) {
					detectHelper.updateCounter(indexCounter, date, nextDate);
					detectHelper.updateDefaultChar(defaultCharMap, detectHelper.getDatePartFromDateTimeData(row[columnIndex].trim()));
					int increments = detectHelper.getIndexIncrements(indexCounter);
					if(increments != 0 && increments > 1) {
						break;
					}
				}								
			}
			if(indexCounter.size() > 1) {
				StringBuilder stringBuilder = new StringBuilder();
				String timeFormat = detectHelper.getTimeFormat(detectHelper.getTimePartFromDateTimeData(file.get(rowIndex)[columnIndex].trim()));
				int[] dateDataPosition = detectHelper.getDatePosition(indexCounter);
				int[] noOfChars = detectHelper.getNoOfChars(defaultCharMap);
				String dateFormat = detectHelper.getDateFormat(dateDataPosition, noOfChars);
				if(timeFormat != null && dateFormat != null) {
					return stringBuilder.append(dateFormat).append(Constant.SPACE_CONSTANT).append(timeFormat).toString();
				}				
			} else {
				logger.info("Couldn't able to detect datetime format due to lack of having enough information");
			}
						
		} else {
			logger.info("Couldn't find position of date data");
		}
		return null;
	}

}
