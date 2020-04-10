package com.gcn.etl.detection;

import java.util.List;

public interface IGridDetection {
	boolean isGridType(List<String[]> file);
	
	int getIntervalLength(List<String[]> file);
	
	String getDateTimeFormat(List<String[]> file);
}
