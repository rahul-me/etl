package com.gcn.etl.pojo;

public class Content {
	private String meterNo;
	private String date;
	private String interval;
	private int rowIndex;
	private int colIndex;
	private Float value;
	private Float extractedValue;
	private Float transformedValue;
	private Boolean isMissingPointRepaired;
	private Boolean isSpike;
	private String message;
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
	public Float getExtractedValue() {
		return extractedValue;
	}
	public void setExtractedValue(Float extractedValue) {
		this.extractedValue = extractedValue;
	}
	public Float getTransformedValue() {
		return transformedValue;
	}
	public void setTransformedValue(Float transformedValue) {
		this.transformedValue = transformedValue;
	}
	public Boolean getIsMissingPointRepaired() {
		return isMissingPointRepaired;
	}
	public void setIsMissingPointRepaired(Boolean isMissingPointRepaired) {
		this.isMissingPointRepaired = isMissingPointRepaired;
	}
	public Boolean getIsSpike() {
		return isSpike;
	}
	public void setIsSpike(Boolean isSpike) {
		this.isSpike = isSpike;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
