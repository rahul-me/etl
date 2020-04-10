package com.gcn.etl.pojo;

public class TimeDataLocation {
	
	public TimeDataLocation(Integer rowIndex, Integer columnIndex) {
		setRowIndex(rowIndex);
		setColumnIndex(columnIndex);
	}
	
	private Integer rowIndex;
	
	private Integer columnIndex;

	public Integer getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(Integer rowIndex) {
		this.rowIndex = rowIndex;
	}

	public Integer getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(Integer columnIndex) {
		this.columnIndex = columnIndex;
	}
	
	
}
