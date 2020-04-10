package com.gcn.etl.pojo;

import java.util.Date;

import com.opencsv.bean.CsvBind;

public class Column15Min {
	@CsvBind
	public String ts;

	@CsvBind
	public String header;
	
	public Date date;

	public Column15Min() {
	}

	public Column15Min(String ts, String header) {
		super();
		this.ts = ts;
		this.header = header;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Column15Min [ts=" + ts + ", header=" + header + ", date=" + date + "]";
	}
}
