package com.gcn.etl.pojo;

import java.util.Date;

import com.opencsv.bean.CsvBind;

public class OutputData {
	@CsvBind
	private String timeStamp;
	@CsvBind
	private String original;
	private Date dateTime;

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	@Override
	public String toString() {
		return "OutputData [timeStamp=" + timeStamp + ", original=" + original + ", dateTime=" + dateTime + "]";
	}

}
