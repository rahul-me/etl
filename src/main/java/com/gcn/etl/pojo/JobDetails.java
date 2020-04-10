package com.gcn.etl.pojo;

import java.util.List;


public class JobDetails {
	private String jobId;
	private String inputFileId;
	private Boolean skipTransformation;
	private String callbackUrl;
	private String rawCsv;
	private String transformationCsv;
	private String validationCsv;
	private String status;
	private String application;
	private String requestId;
	private long timestamp;
	private List<Errors> errors;
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getInputFileId() {
		return inputFileId;
	}
	public void setInputFileId(String inputFileId) {
		this.inputFileId = inputFileId;
	}
	public List<Errors> getErrors() {
		return errors;
	}
	public void setErrors(List<Errors> errors) {
		this.errors = errors;
	}
	public Boolean getSkipTransformation() {
		return skipTransformation;
	}
	public void setSkipTransformation(Boolean skipTransformation) {
		this.skipTransformation = skipTransformation;
	}
	public String getCallbackUrl() {
		return callbackUrl;
	}
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getRawCsv() {
		return rawCsv;
	}
	public void setRawCsv(String rawCsv) {
		this.rawCsv = rawCsv;
	}
	public String getTransformationCsv() {
		return transformationCsv;
	}
	public void setTransformationCsv(String transformationCsv) {
		this.transformationCsv = transformationCsv;
	}
	public String getValidationCsv() {
		return validationCsv;
	}
	public void setValidationCsv(String validationCsv) {
		this.validationCsv = validationCsv;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
}
