package com.gcn.etl.database.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "job_validation_status")
public class JobValidationStatus extends BaseEntity {

	@Column(name = "job_id")
	private String jobId;

	@Column(name = "input_file_type_id")
	private int inputFileTypeId;

	@Column(name = "is_data_valid")
	private Boolean isDataValid;

	@Column(name = "is_missing_point_reoaired")
	private Boolean isMissingPointRepaired;

	@Column(name = "message")
	private String message;

	@Column(name = "is_spike")
	private Boolean isSpike;
	
	@Column(name = "application_error_code")
	private int applicationErrorCode;
	
	@Column(name = "job_status")
	private String jobStatus;
	
	@Column(name = "application_error_msg")
	private String applicationErrorMsg;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "job_validation_status_id")
	@JsonManagedReference
	private List<ErrorDetails> errorDetailsList;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public int getInputFileTypeId() {
		return inputFileTypeId;
	}

	public void setInputFileTypeId(int inputFileTypeId) {
		this.inputFileTypeId = inputFileTypeId;
	}

	public Boolean getIsDataValid() {
		return isDataValid;
	}

	public void setIsDataValid(Boolean isDataValid) {
		this.isDataValid = isDataValid;
	}

	public Boolean getIsMissingPointRepaired() {
		return isMissingPointRepaired;
	}

	public void setIsMissingPointRepaired(Boolean isMissingPointRepaired) {
		this.isMissingPointRepaired = isMissingPointRepaired;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getIsSpike() {
		return isSpike;
	}

	public void setIsSpike(Boolean isSpike) {
		this.isSpike = isSpike;
	}

	public List<ErrorDetails> getErrorDetaliList() {
		return errorDetailsList;
	}

	public void setErrorDetailsList(List<ErrorDetails> errorDetailsList) {
		this.errorDetailsList = errorDetailsList;
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

	public String getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}

	public List<ErrorDetails> getErrorDetailsList() {
		return errorDetailsList;
	}

	@Override
	public String toString() {
		return "JobValidationStatus [jobId=" + jobId + ", inputFileTypeId=" + inputFileTypeId + ", isDataValid="
				+ isDataValid +", applicationErrorCode=" +applicationErrorCode+ ", applicationErrorMessage="+applicationErrorMsg+ 
				", isMissingPointRepaired=" + isMissingPointRepaired + ", message=" + message
				+ ", isSpike=" + isSpike + ", errorDetaliList=" + errorDetailsList + "]";
	}

	
}
