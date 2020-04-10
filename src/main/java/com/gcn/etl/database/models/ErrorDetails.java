package com.gcn.etl.database.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "error_details")
public class ErrorDetails extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "job_validation_status_id")
	@JsonBackReference
	private JobValidationStatus jobValidationStatusId;
	@Column(name = "meter_no")
	private String meterNo;
	@Column(name = "date")
	private String date;
	@Column(name = "interval_time")
	private String intervalTime;
	@Column(name = "row_index")
	private int rowIndex;
	@Column(name = "col_index")
	private int colIndex;
	@Column(name = "description")
	private String description;
	@Column(name = "value")
	private Float value;
	@Column(name = "transformedValue")
	private Float transformedValue;

	public JobValidationStatus getJobValidationStatusId() {
		return jobValidationStatusId;
	}

	public void setJobValidationStatusId(JobValidationStatus jobValidationStatusId) {
		this.jobValidationStatusId = jobValidationStatusId;
	}

	public String getMeterNo() {
		return meterNo;
	}

	public void setMeterNo(String meterNo) {
		this.meterNo = meterNo;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getIntervalTime() {
		return intervalTime;
	}

	public void setIntervalTime(String intervalTime) {
		this.intervalTime = intervalTime;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public int getColIndex() {
		return colIndex;
	}

	public void setColIndex(int colIndex) {
		this.colIndex = colIndex;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public Float getTransformedValue() {
		return transformedValue;
	}

	public void setTransformedValue(Float transformedValue) {
		this.transformedValue = transformedValue;
	}

}
