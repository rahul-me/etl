package com.gcn.etl.pojo;

public class Spike {
	private String date;
	private Float usage;
	private int rowIndex;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Float getUsage() {
		return usage;
	}

	public void setUsage(Float usage) {
		this.usage = usage;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

}
