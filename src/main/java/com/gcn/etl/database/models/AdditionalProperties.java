package com.gcn.etl.database.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="additional_properties")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalProperties extends BaseEntity{
	
	@Column(name="meter_id")
	private String meterId;
	
	@Column(name="utility_id")
	private String utilityId;
	
	@ManyToOne
	@JoinColumn(name="job_id")
	@JsonBackReference	
	private Jobs job;

	public String getMeterId() {
		return meterId;
	}

	public void setMeterId(String meterId) {
		this.meterId = meterId;
	}

	public String getUtilityId() {
		return utilityId;
	}

	public void setUtilityId(String utilityId) {
		this.utilityId = utilityId;
	}

	public Jobs getJob() {
		return job;
	}

	public void setJob(Jobs job) {
		this.job = job;
	}

	@Override
	public String toString() {
		return "AdditionalProperties [meterId=" + meterId + ", utilityId=" + utilityId + ", job=" + job + "]";
	}	
}
