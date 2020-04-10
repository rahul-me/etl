package com.gcn.etl.detection;

import java.util.List;

public interface IColumnTypeDetectionApproach {
	boolean detectColumn(List<String[]> file);
}
