package com.gcn.etl.database.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "spaces")
@JsonIgnoreProperties
public class Space extends BaseEntity {
	@Column(name = "name")
	private String spaceName;
	
	@Column(name = "generated_uuid")
	private String generatedUUID;
	
	@Column(name="timestamp")
	private String timestamp;
	

	@Column(name="is_deleted")
	@JsonIgnore
	private boolean isDeleted;
	
	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getSpaceName() {
		return spaceName;
	}

	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}

	public String getGeneratedUUID() {
		return generatedUUID;
	}

	public void setGeneratedUUID(String generatedUUID) {
		this.generatedUUID = generatedUUID;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	 
}
