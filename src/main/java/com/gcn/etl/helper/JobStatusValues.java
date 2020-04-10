package com.gcn.etl.helper;

public enum JobStatusValues {
	Queued,
	Transforming,
	Validating,
	Loading,
	Completed,
	Failed;
}
