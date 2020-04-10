package com.gcn.etl.detection.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gcn.etl.constant.Constant;
import com.gcn.etl.detection.IColumnTypeDetectionApproach;

@Service
public class ColumnTypeDetectionByColumnCount implements IColumnTypeDetectionApproach {

	@Override
	public boolean detectColumn(List<String[]> file) {
		if(file != null && !file.isEmpty()) {
			String[] firstRow = file.get(0);
			if(firstRow != null && firstRow.length == Constant.FILE_TYPE_COLUMN_NO_OF_COLUMNS) {
				return true;
			} else if(firstRow != null && firstRow.length > Constant.FILE_TYPE_COLUMN_NO_OF_COLUMNS) {
				return isValidDataRow(firstRow);
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
		
		return emptycount == row.length - 2;
	}

}
