package com.gcn.etl.detection.service;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gcn.etl.constant.Constant;
import com.gcn.etl.detection.IColumnDetection;
import com.gcn.etl.detection.IGridDetection;
import com.gcn.etl.detection.IRequestParameterDetector;
import com.gcn.etl.helper.ETLErrors;

@Service
public class InputParameterDetector implements IRequestParameterDetector {

	private static final Logger logger = LogManager.getLogger(InputParameterDetector.class);

	@Autowired
	private IColumnDetection columnDetection;

	@Autowired
	private IGridDetection gridDetection;

	@Override
	public int detectFileTypeId(List<String[]> file) {

		if (columnDetection.isColumnType(file)) {
			return Constant.FILE_TYPE_ID_COLUMN;
		}

		if (gridDetection.isGridType(file)) {
			return Constant.FILE_TYPE_ID_GRID;
		}

		return -1;
	}

	@Override
	public int detectIntervalLength(List<String[]> file, String detectedDateFormat, boolean isDetected, Map<String, Object> errorMap) {

		int fileTypeId = detectFileTypeId(file);

		switch (fileTypeId) {
		case Constant.FILE_TYPE_ID_GRID:
			return gridDetection.getIntervalLength(file);
		case Constant.FILE_TYPE_ID_COLUMN:
			if(detectedDateFormat == null) {
				if(!isDetected) {
					detectedDateFormat = columnDetection.getDateTimeFormat(file);
					if (detectedDateFormat != null) {
						return columnDetection.getIntervalLength(file, detectedDateFormat);
					} else {
						logger.info("Couldn't able to detect interval length");
						errorMap.put(ETLErrors.UNABLE_TO_DETECT_TIMESTAMPFORMAT.errorMessage(),
								ETLErrors.UNABLE_TO_DETECT_TIMESTAMPFORMAT.errorCode());
					}
				}
			} else {
				return columnDetection.getIntervalLength(file, detectedDateFormat);
			}			
		}
		return -1;
	}

	@Override
	public String detectDateTimeFormart(List<String[]> file) {

		int fileTypeId = detectFileTypeId(file);

		switch (fileTypeId) {
		case Constant.FILE_TYPE_ID_GRID:
			return gridDetection.getDateTimeFormat(file);
		case Constant.FILE_TYPE_ID_COLUMN:
			return columnDetection.getDateTimeFormat(file);
		}
		return null;
	}

}
