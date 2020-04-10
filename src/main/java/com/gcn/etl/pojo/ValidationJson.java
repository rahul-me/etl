package com.gcn.etl.pojo;

import java.util.List;

public class ValidationJson {

	private String errorMessage;
	
	private List<MissingPoint> missingDataPoint;

	public List<MissingPoint> getMissingDataPoint() {
		return missingDataPoint;
	}

	public void setMissingDataPoint(List<MissingPoint> missingDataPoint) {
		this.missingDataPoint = missingDataPoint;
	}

	public String geterrorMessage() {
		return errorMessage;
	}

	public void seterrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
}
