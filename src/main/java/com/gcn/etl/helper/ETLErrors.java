
package com.gcn.etl.helper;

public enum ETLErrors {
	INVALID_FILE_FORMAT_UPLOAD(3000,"Invalid file format"),
	MISSING_FILE_UPLOAD(3001,"Missing File"),
	UNABLE_TO_DETECT_INPUTFILETYPEID(3002, "Unable to detect input file type id"),
	INVALID_DATA_UNITS_UPLOAD(3003,"Invalid data units"),
	MISSING_DATA_UNITS_UPLOAD(3004,"Missing data units"),
	UNABLE_TO_DETECT_TIMESTAMPFORMAT(3006, "Unable to detect timestampFormat"),
	INVALID_INTERVAL_LENGTH_UPLOAD(3007,"Invalid interval length"),
	UNABLE_TO_DETECT_INTERVAL_LENGTH(3008, "Unable to detect interval length"),
	MISSING_TIMEZONE_UPLOAD(3009,"Missing timezone"),
	INVALID_DATATYPE_UPLOAD(3010,"Invalid dataType"),
	MISSING_DATATYPE_UPLOAD(3011,"Missing dataType"),
	INVALID_DATA_TYPE_UPLOAD(3012,"Invalid data point in file"),
	MISSING_METERNAME_UPLOAD(3013,"Missing meter name"),
	INVALID_METER_NAME_UPLOAD(3014,"Invalid meter name"),
	MISSING_ISINTERVALSTART_UPLOAD(3015,"Missing isIntervalStart"),
	FILE_ID_NOTFOUND_TRANSFORMATION(3101,"FileID Not Found "),
	INVALID_FILE_ID_FOR_SPACE_TRANSFORMATION(3102,"FileID invalid for Space "),
	JOB_ID_NOTFOUND_TRANSFORMATION(3103,"JobID Not Found "),
	INVALID_JOB_ID_FOR_SPACE_TRANSFORMATION(3104,"JobId invalid for Space"),
	MORE_THAN_4_MISSING_DATAPOINTS_TRANSFORMATION(3202,"More than 4 consecutive missing data points"),
	SPIKE_FOUND_TRANSFORMATION(3200,"Spike is found"),
	KETTLE_TRANFORMATION(3103,"Kettle Tranformation not found "),
	SPACE_NOT_FOUND_GENERAL_ERROR(3900,"Space Not Found "),
	PMT_FAILED_MISC(3901,"PMT call failed"),
	DATA_INPUTFILE_NOT_FOUND_GENERAL_ERROR(3902,"DataInputFiles Not Found"),
	FILE_NOT_FOUND(3903,"File not found");
	

	
	private final int errorCode;
	private final String errorMessage;
	
	ETLErrors(int errorCode,String errorMessage){
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}		
	
	public int errorCode(){
        return this.errorCode;
    }
	
    public String errorMessage(){
        return this.errorMessage;
    }

}
	
