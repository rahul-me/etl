package com.gcn.etl.detection;

import java.util.List;
import java.util.Map;

public interface IRequestParameterDetector {
	int detectFileTypeId(List<String[]> file);
	
	int detectIntervalLength(List<String[]> file, String dateFormat, boolean isDetected, Map<String, Object> errorMap);
	
	String detectDateTimeFormart(List<String[]> file);
	
	
}
