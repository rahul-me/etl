package com.gcn.etl.pojo;

public class JobStatus {
	private String jobId;
	private String fileId;
	private String message;
	private ValidationStatus validation;
	private Transformation transformation;
	private Pmt pmt;
	private Load load;
	private int applicationErrorCode;
	private String applicationErrorMsg;
	private String status;
	
	public Pmt getPmt() {
		return pmt;
	}
	public void setPmt(Pmt pmt) {
		this.pmt = pmt;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public ValidationStatus getValidation() {
		return validation;
	}
	public void setValidation(ValidationStatus validation) {
		this.validation = validation;
	}
	public Transformation getTransformation() {
		return transformation;
	}
	public void setTransformation(Transformation transformation) {
		this.transformation = transformation;
	}
	public Load getLoad() {
		return load;
	}
	public void setLoad(Load load) {
		this.load = load;
	}
	public int getApplicationErrorCode() {
		return applicationErrorCode;
	}
	public void setApplicationErrorCode(int applicationErrorCode) {
		this.applicationErrorCode = applicationErrorCode;
	}
	public String getApplicationErrorMsg() {
		return applicationErrorMsg;
	}
	public void setApplicationErrorMsg(String applicationErrorMsg) {
		this.applicationErrorMsg = applicationErrorMsg;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "JobStatus [jobId=" + jobId + ", fileId=" + fileId + ", message=" + message + ", validation="
				+ validation + ", transformation=" + transformation + ", load=" + load + "]";
	}	
}
