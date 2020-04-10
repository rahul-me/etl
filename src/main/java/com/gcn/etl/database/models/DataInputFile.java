package com.gcn.etl.database.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "datainputfiles")
public class DataInputFile extends BaseEntity {

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "file_id")
	private String fileId;

	@Column(name = "input_file_type_id")
	private Integer inputFileTypeId;

	@Column(name = "file_link")
	private String fileLink;

	@OneToOne
	@JoinColumn(name = "space_id")
	private Space spaceId;

	@Column(name = "location")
	private String location;

	@Column(name = "meter_name")
	private String meterName;

	@Column(name = "time_zone")
	private String timeZone;

	@Column(name = "data_type")
	private String dataType;

	@Column(name = "timestamp_format")
	private String timestampFormat;

	@Column(name = "interval_length")
	private Integer intervalLength;

	@Column(name = "units")
	private String units;

	@Column(name = "is_interval_start")
	private Boolean isIntervalStart;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public Integer getInputFileTypeId() {
		return inputFileTypeId;
	}

	public void setInputFileTypeId(Integer inputFileTypeId) {
		this.inputFileTypeId = inputFileTypeId;
	}

	public String getFileLink() {
		return fileLink;
	}

	public void setFileLink(String fileLink) {
		this.fileLink = fileLink;
	}

	

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getMeterName() {
		return meterName;
	}

	public void setMeterName(String meterName) {
		this.meterName = meterName;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getTimestampFormat() {
		return timestampFormat;
	}

	public void setTimestampFormat(String timestampFormat) {
		this.timestampFormat = timestampFormat;
	}

	public Integer getIntervalLength() {
		return intervalLength;
	}

	public void setIntervalLength(Integer intervalLength) {
		this.intervalLength = intervalLength;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public Boolean getIsIntervalStart() {
		return isIntervalStart;
	}

	public void setIsIntervalStart(Boolean isIntervalStart) {
		this.isIntervalStart = isIntervalStart;
	}

	public Space getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(Space spaceId) {
		this.spaceId = spaceId;
	}

	@Override
	public String toString() {
		return "DataInputFile [fileName=" + fileName + ", fileId=" + fileId + ", inputFileTypeId=" + inputFileTypeId
				+ ", fileLink=" + fileLink + ", location=" + location + ", meterName="
				+ meterName + ", timeZone=" + timeZone + ", dataType=" + dataType + ", timestampFormat="
				+ timestampFormat + ", intervalLength=" + intervalLength + ", units=" + units + ", isIntervalStart="
				+ isIntervalStart + "]";
	}
}
