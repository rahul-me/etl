package com.gcn.etl.detection;

import java.util.List;

public interface IColumnDetection {
	boolean isColumnType(List<String[]> file);
	
	int getIntervalLength(List<String[]> file, String dateFormat);
	
	String getDateTimeFormat(List<String[]> file);
}
