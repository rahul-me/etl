package com.gcn.etl.pojo;

import java.util.Collections;
import java.util.List;

public class Errors {

	private Integer applicationErrorCode;
	private String applicationErrorMsg;
	private List<ApplicationErrorDetails> errorDetails = Collections.emptyList();
	
	public Integer getApplicationErrorCode() {
		return applicationErrorCode;
	}
	public void setApplicationErrorCode(Integer applicationErrorCode) {
		this.applicationErrorCode = applicationErrorCode;
	}
	public String getApplicationErrorMsg() {
		return applicationErrorMsg;
	}
	public void setApplicationErrorMsg(String applicationErrorMsg) {
		this.applicationErrorMsg = applicationErrorMsg;
	}
	public List<ApplicationErrorDetails> getErrorDetails() {
		return errorDetails;
	}
	public void setErrorDetails(List<ApplicationErrorDetails> errorDetails) {
		this.errorDetails = errorDetails;
	}
	
}
