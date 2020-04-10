package com.gcn.etl.detection;

import java.util.List;

public interface IGridTypeDetectionApproach {
	boolean detectGrid(List<String[]> file);
}
