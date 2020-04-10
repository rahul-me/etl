package com.gcn.etl.pojo;

public class Load {
	private Boolean s3Aws;

	public Boolean getS3Aws() {
		return s3Aws;
	}

	public void setS3Aws(Boolean s3Aws) {
		this.s3Aws = s3Aws;
	}

	@Override
	public String toString() {
		return "Load [s3Aws=" + s3Aws + "]";
	}
}
