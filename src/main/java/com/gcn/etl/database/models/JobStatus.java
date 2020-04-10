package com.gcn.etl.database.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="job_status")
public class JobStatus extends BaseEntity{

	@Column(name="job_id")
	private String jobId;
	
	@Column(name="transformation_id")
	private long transformationId;	
	
	@Column(name="is_pmt_service_call")
	private Boolean isPmtServiceCall;
	
	@Column(name="pmt_status")
	private String pmtStatus;
	
	@Column(name="pmt_response")
	private String pmtResponse;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public long getTransformationId() {
		return transformationId;
	}

	public void setTransformationId(long transformationId) {
		this.transformationId = transformationId;
	}

	public Boolean getIsPmtServiceCall() {
		return isPmtServiceCall;
	}

	public void setIsPmtServiceCall(Boolean isPmtServiceCall) {
		this.isPmtServiceCall = isPmtServiceCall;
	}

	public String getPmtStatus() {
		return pmtStatus;
	}

	public void setPmtStatus(String pmtStatus) {
		this.pmtStatus = pmtStatus;
	}

	public String getPmtResponse() {
		return pmtResponse;
	}

	public void setPmtResponse(String pmtResponse) {
		this.pmtResponse = pmtResponse;
	}

	@Override
	public String toString() {
		return "JobStatus [jobId=" + jobId + ", transformationId=" + transformationId + ", is_pmt_service_call="
				+ isPmtServiceCall + ", pmt_status=" + pmtStatus + ", pmt_response=" + pmtResponse + "]";
	}	
}
