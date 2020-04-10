package com.gcn.etl.pojo;

import java.util.Date;

import com.opencsv.bean.CsvBind;

public class Column1Hour {
	@CsvBind
	public String ts;

	@CsvBind
	public String value;

	public Date date;
	
	public Column1Hour() {
	}

	public Column1Hour(String ts, String value) {
		super();
		this.ts = ts;
		this.value = value;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Column1Hour [ts=" + ts + ", value=" + value + "]";
	}
}
