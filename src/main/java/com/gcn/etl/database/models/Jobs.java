package com.gcn.etl.database.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "jobs")
public class Jobs extends BaseEntity {

	@Column(name = "action")
	private String action;

	@Column(name = "job_id")
	private String jobId;

	@Column(name = "input_file_id")
	private String inputFileId;

	@Column(name = "skip_transformation")
	private Boolean skipTransformation;

	@Column(name = "callback_url")
	private String callbackUrl;

	@Column(name = "status")
	private String status;

	@Column(name = "output_file_name")
	private String outputFileName;

	@Column(name = "output_file_link")
	private String outputFileLink;

	@Column(name = "output_file_location")
	private String outputFileLocation;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "job_id")
	@JsonManagedReference
	private List<AdditionalProperties> additionalProperties;

	@OneToOne
	@JoinColumn(name = "space_id")
	private Space spaceId;
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

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

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public String getOutputFileLink() {
		return outputFileLink;
	}

	public void setOutputFileLink(String outputFileLink) {
		this.outputFileLink = outputFileLink;
	}

	public String getOutputFileLocation() {
		return outputFileLocation;
	}

	public void setOutputFileLocation(String outputFileLocation) {
		this.outputFileLocation = outputFileLocation;
	}

	public List<AdditionalProperties> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(List<AdditionalProperties> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	public Space getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(Space spaceId) {
		this.spaceId = spaceId;
	}

	@Override
	public String toString() {
		return "Jobs [action=" + action + ", jobId=" + jobId + ", inputFileId=" + inputFileId + ", skipTransformation="
				+ skipTransformation + ", callbackUrl=" + callbackUrl + ", status=" + status + ", outputFileName="
				+ outputFileName + ", outputFileLink=" + outputFileLink + ", outputFileLocation=" + outputFileLocation
				+ "]";
	}	
}
