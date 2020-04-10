
package com.gcn.etl.database.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;


	
@MappedSuperclass
public class BaseEntity {
	@Id
	@Column(name="id")
	@GeneratedValue
	@JsonIgnore
	private long id;
	
	@Column(name="created_at")
	@JsonIgnore
	private Date created_at = new Date()	;
	
	@Column(name="updated_at")
	@JsonIgnore
	private Date updated_at = new Date();


	@PreUpdate
	public void setLastUpdate() {  
		this.updated_at = new Date(); 
	}
		
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public Date getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(Date updated_at) {
		this.updated_at = updated_at;
	}
}
	