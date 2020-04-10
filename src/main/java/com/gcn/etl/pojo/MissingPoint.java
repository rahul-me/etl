package com.gcn.etl.pojo;

public class MissingPoint {
	private String meterNo;
	private String date;
	private String interval;
	private int rowIndex;
	private int colIndex;
	private Float value;
	
	
	public Float getValue() {
		return value;
	}
	public void setValue(Float value) {
		this.value = value;
	}
	public String getMeterNo() {
		return meterNo;
	}
	public void setMeterNo(String meterNo) {
		this.meterNo = meterNo;
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
	@Override
	public String toString() {
		return "MissingPoint [meterNo=" + meterNo + ", date=" + date + ", interval=" + interval + ", rowIndex="
				+ rowIndex + ", colIndex=" + colIndex + "]";
	}
	
	
}
