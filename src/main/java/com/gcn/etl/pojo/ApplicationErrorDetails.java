package com.gcn.etl.pojo;

public class ApplicationErrorDetails {
	private String date;
	private String interval;
	private int rowIndex;
	private int colIndex;
	private double value;
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
	public int getRowIndex() {
		return rowIndex;
	}
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}
	public int getColIndex() {
		return colIndex;
	}
	public void setColIndex(int colIndex) {
		this.colIndex = colIndex;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
}
