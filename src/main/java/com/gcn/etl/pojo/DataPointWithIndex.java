package com.gcn.etl.pojo;

public class DataPointWithIndex {
	private int rowIndex;
	private Float dataPoint;
	private int columnIndex;
	private String date;
	private String interval;
	public int getRowIndex() {
		return rowIndex;
	}
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}
	public Float getDataPoint() {
		return dataPoint;
	}
	public void setDataPoint(Float dataPoint) {
		this.dataPoint = dataPoint;
	}
	public int getColumnIndex() {
		return columnIndex;
	}
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getInterval() {
		return interval;
	}
	public void setInterval(String interval) {
		this.interval = interval;
	}
	
}
