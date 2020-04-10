package com.gcn.etl.controllers;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.amazonaws.services.dynamodbv2.datamodeling.S3ClientCache;
import com.gcn.etl.database.models.DataInputFile;
import com.gcn.etl.database.models.Jobs;
import com.gcn.etl.database.models.Space;
import com.gcn.etl.database.service.DataInputFileService;
import com.gcn.etl.database.service.JobService;
import com.gcn.etl.database.service.SpaceService;
import com.gcn.etl.detection.IRequestParameterDetector;
import com.gcn.etl.helper.DataFilesUploadHelper;
import com.gcn.etl.helper.ETLErrors;
import com.gcn.etl.helper.JobStatusValues;
import com.gcn.etl.helper.JobsHelper;
import com.gcn.etl.helper.PMTServiceHelper;
import com.gcn.etl.lib.s3Aws.S3AwsService;
import com.gcn.etl.pojo.Errors;
import com.gcn.etl.pojo.JobDetails;
import com.gcn.etl.propertiesHelper.AppConfigProperties;
import com.gcn.etl.thread.ValidationAndJobExecution;
import com.opencsv.CSVReader;

@Controller
@RequestMapping("/datauploaderetl/spaces/{spaceId}")
public class DataUploaderEtlController {

	private static Logger logger = LogManager.getLogger(DataUploaderEtlController.class);

	@Autowired
	private DataInputFileService dataInputFileService;

	@Autowired
	private SpaceService spaceService;

	@Autowired
	private JobService jobService;

	@Autowired
	private S3AwsService s3Service;

	@Autowired
	private AppConfigProperties appConfigProperties;

	@Autowired
	private DataFilesUploadHelper dataFileUploadHelper;

	@Autowired
	private ValidationAndJobExecution validationAndJobExecution;

	@Autowired
	private JobsHelper jobHelper;

	@Autowired
	private PMTServiceHelper pmtService;

	@Autowired
	private IRequestParameterDetector requestParameterDetector;

	@RequestMapping(value = "/data-files", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> getDataFiles(HttpServletRequest req, Pageable pageable,
			@PathVariable("spaceId") String spaceId) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String direction = req.getParameter("order");
			String properties = req.getParameter("sort");
			if (properties == null || properties.equals("")) {
				properties = "id";
			}
			if (direction != null && direction.equals("desc")) {
				pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
						new Sort(Sort.Direction.DESC, properties));
			} else {
				pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
						new Sort(Sort.Direction.ASC, properties));
			}
			Page<DataInputFile> list = dataInputFileService.findbySpaceID(pageable, spaceId);
			if (list.getSize() > 0) {
				map.put("DataInputFiles", list);
			} else {
				map.put("application", appConfigProperties.getApplication());
				map.put("requestId", "");
				map.put("status", "Success");
				map.put("Timestamp", new Date().toString());
				Errors errors = new Errors();
				errors.setApplicationErrorCode(ETLErrors.DATA_INPUTFILE_NOT_FOUND_GENERAL_ERROR.errorCode());
				errors.setApplicationErrorMsg(ETLErrors.DATA_INPUTFILE_NOT_FOUND_GENERAL_ERROR.errorMessage());
				map.put("errors", errors);
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "/data-files/sort", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> dataFileList(HttpServletRequest req, @PathVariable("spaceId") String spaceId) {
		Map<String, Object> map = new HashMap<String, Object>();
		String direction = req.getParameter("order");
		String properties = req.getParameter("sort");
		Sort sort = null;
		if (direction != null && direction.equals("desc")) {
			sort = new Sort(Sort.Direction.DESC, properties);
		} else {
			sort = new Sort(Sort.Direction.ASC, properties);
		}
		List<DataInputFile> list = dataInputFileService.findAll(sort);
		for (DataInputFile file : list) {
			if (!file.getSpaceId().getGeneratedUUID().equals(spaceId)) {
				list.remove(file);
			}
		}
		map.put("DataInputFiles", list);
		return map;
	}

	@RequestMapping(value = "/data-files/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> getDataFilesById(@PathVariable("spaceId") String spaceId,
			@PathVariable("id") String fileId, HttpServletRequest req, HttpServletResponse res) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			DataInputFile obj = dataInputFileService.findByFileId(fileId);
			if (obj != null) {
				if ((obj.getSpaceId() != null) && (obj.getSpaceId().getGeneratedUUID().equals(spaceId))) {
					map.put("data-file", obj);
				} else {
					map.put("application", appConfigProperties.getApplication());
					map.put("requestId", "");
					map.put("status", "failed");
					map.put("Timestamp", new Date().toString());
					Errors errors = new Errors();
					errors.setApplicationErrorCode(ETLErrors.INVALID_FILE_ID_FOR_SPACE_TRANSFORMATION.errorCode());
					errors.setApplicationErrorMsg(ETLErrors.INVALID_FILE_ID_FOR_SPACE_TRANSFORMATION.errorMessage() +spaceId);
					map.put("errors", errors);
					return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
				}

			} else {
				map.put("application", appConfigProperties.getApplication());
				map.put("requestId", "");
				map.put("status", "failed");
				map.put("Timestamp", new Date().toString());
				Errors errors = new Errors();
				errors.setApplicationErrorCode(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorCode());
				errors.setApplicationErrorMsg(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorMessage() +"id :"+ fileId);
				map.put("errors", errors);  
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("errorMessage", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "/data-files/download/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> downloadFilesById(@PathVariable("id") String fileId, HttpServletRequest req,
			HttpServletResponse res) {
		Map<String, Object> map = new HashMap<String, Object>();
		DataInputFile obj = dataInputFileService.findByFileId(fileId);
		if (obj != null) {
			String key = obj.getLocation() + obj.getFileName();
			try {
				s3Service.downloadFile(key, res, req, obj.getFileName(), map);
				logger.info(obj.getFileName() + "File download successfully");
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				map.put("errorMessage", e.getMessage());
				return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			map.put("application", appConfigProperties.getApplication());
			map.put("requestId", "");
			map.put("status", "failed");
			map.put("Timestamp", new Date().toString());
			Errors errors = new Errors();
			errors.setApplicationErrorCode(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorCode());
			errors.setApplicationErrorMsg(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorMessage() +"id :"+ fileId);
			map.put("errors", errors);  
			return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "/data-files", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> postGridColumnDataFiles(HttpServletRequest req,
			@PathVariable("spaceId") String spaceId) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> errorMap = new HashMap<String, Object>();
		List<Errors> errorArray = new ArrayList<Errors>();
		try {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) req;
			Map<String, MultipartFile> files = multiRequest.getFileMap();

			List<String[]> fileData = null;

			if (files.isEmpty()) {
				errorMap.put(ETLErrors.MISSING_FILE_UPLOAD.errorMessage(), ETLErrors.MISSING_FILE_UPLOAD.errorCode());
			} else {
				MultipartFile file = new ArrayList<Entry<String, MultipartFile>>(files.entrySet()).get(0).getValue();
				if (file != null) {
					try (Reader reader = new InputStreamReader(file.getInputStream());
							CSVReader csvReader = new CSVReader(reader)) {
						fileData = csvReader.readAll();
					}
				}
			}
			String inputFileTypeId = req.getParameter("inputFileTypeId");
			inputFileTypeId = inputFileTypeId.equals("") ? null : inputFileTypeId;
			if (inputFileTypeId == null) {
				if (!files.isEmpty() && fileData != null) {
					int inputFileType = requestParameterDetector.detectFileTypeId(fileData);
					logger.info("Getting " + inputFileType + " as input file type id.");
					if (inputFileType != -1) {
						inputFileTypeId = String.valueOf(inputFileType);
					} else {
						logger.error("Couldn't detect input file type id");
						errorMap.put(ETLErrors.UNABLE_TO_DETECT_INPUTFILETYPEID.errorMessage(),
								ETLErrors.UNABLE_TO_DETECT_INPUTFILETYPEID.errorCode());
					}
				} else {
					logger.info("Couldn't detect input file type id having no file in place.");
				}
			}

			String meterName = req.getParameter("meterName");
			meterName = meterName.equals("") ? null : meterName;
			if (meterName == null) {
				errorMap.put(ETLErrors.MISSING_METERNAME_UPLOAD.errorMessage(),
						ETLErrors.MISSING_METERNAME_UPLOAD.errorCode());
			}
			String timeZone = req.getParameter("timeZone");
			timeZone = timeZone.equals("") ? null : timeZone;
			if (timeZone == null) {
				errorMap.put(ETLErrors.MISSING_TIMEZONE_UPLOAD.errorMessage(),
						ETLErrors.MISSING_TIMEZONE_UPLOAD.errorCode());
			}
			String dataType = req.getParameter("dataType");
			dataType = dataType.equals("") ? null : dataType;
			if (dataType != null) {
				Pattern dataTypePattern = Pattern.compile("^[a-zA-Z0-9_]*$");
				Matcher dataTypematcher = dataTypePattern.matcher(dataType);
				if (!dataTypematcher.find()) {
					errorMap.put(ETLErrors.INVALID_DATATYPE_UPLOAD.errorMessage(),
							ETLErrors.INVALID_DATATYPE_UPLOAD.errorCode());
					dataType = null;
				}
			} else {
				errorMap.put(ETLErrors.MISSING_DATATYPE_UPLOAD.errorMessage(),
						ETLErrors.MISSING_DATATYPE_UPLOAD.errorCode());

			}

			String detectedTimeStampFormat = null;
			boolean isTimeStampFormatDetected = false;
			String timestampFormat = req.getParameter("timestampFormat");

			timestampFormat = timestampFormat.equals("") ? null : timestampFormat;
			if (timestampFormat == null) {
				if(fileData != null) {
					String dateTimeFormat = requestParameterDetector.detectDateTimeFormart(fileData);
					isTimeStampFormatDetected = true;
					logger.info("Got date time format "+dateTimeFormat);
					if(dateTimeFormat != null) {
						timestampFormat = dateTimeFormat;
						detectedTimeStampFormat = timestampFormat;
					} else {
						logger.info("Couldn't able to detect timestamp format");
						errorMap.put(ETLErrors.UNABLE_TO_DETECT_TIMESTAMPFORMAT.errorMessage(),
								ETLErrors.UNABLE_TO_DETECT_TIMESTAMPFORMAT.errorCode());
						timestampFormat = null;
					}
				}
			}

			String intervalLength = req.getParameter("intervalLength");
			intervalLength = intervalLength.equals("") ? null : intervalLength;
			if(intervalLength != null)
			{	try{
				int interval = Integer.parseInt(intervalLength);
				if(interval % 900 != 0)
				{
					intervalLength = " Invalid interval length";
				}
				}catch(Exception e)
					{
						intervalLength = " Invalid interval length";
						logger.error(e.getMessage(),e);	
					}
			}
			if (intervalLength != req.getParameter("intervalLength")) {
				if (intervalLength == null) {
					int intervalGap = requestParameterDetector.detectIntervalLength(fileData, detectedTimeStampFormat,
							isTimeStampFormatDetected, errorMap);
					logger.info("Detected " + intervalGap + " as interval gap");
					if (intervalGap != -1) {
						intervalLength = String.valueOf(intervalGap);
					} else {
						errorMap.put(ETLErrors.UNABLE_TO_DETECT_INTERVAL_LENGTH.errorMessage(),
								ETLErrors.UNABLE_TO_DETECT_INTERVAL_LENGTH.errorCode());
						logger.info("UNABLE_TO_DETECT_INTERVAL_LENGTH");
					}
				} else {
					errorMap.put(ETLErrors.INVALID_INTERVAL_LENGTH_UPLOAD.errorMessage(),
							ETLErrors.INVALID_INTERVAL_LENGTH_UPLOAD.errorCode());
					intervalLength = null;
					logger.info("Couldn't detect interval length having no file in place.");

				}
				
			}

			String units = req.getParameter("units");
			units = units.equals("") ? null
					: ((units.equalsIgnoreCase("kW") || units.equalsIgnoreCase("kWh"))) ? units : "invalid data units";
			if (units != req.getParameter("units")) {
				if (units == null) {
					errorMap.put(ETLErrors.MISSING_DATA_UNITS_UPLOAD.errorMessage(),
							ETLErrors.MISSING_DATA_UNITS_UPLOAD.errorCode());
				} else {
					errorMap.put(ETLErrors.INVALID_DATA_UNITS_UPLOAD.errorMessage(),
							ETLErrors.INVALID_DATA_UNITS_UPLOAD.errorCode());
					units = null;
				}
			}

			String isIntervalStart = req.getParameter("isIntervalStart");
			isIntervalStart = isIntervalStart.equals("") ? null : isIntervalStart;
			if (isIntervalStart == null) {
				errorMap.put(ETLErrors.MISSING_ISINTERVALSTART_UPLOAD.errorMessage(),
						ETLErrors.MISSING_ISINTERVALSTART_UPLOAD.errorCode());
			}

			Space space = spaceService.findByGeneratedUUID(spaceId);
			if (inputFileTypeId != null && spaceId != null && meterName != null && timeZone != null && dataType != null
					&& timestampFormat != null && intervalLength != null && units != null && isIntervalStart != null
					&& files != null && files.size() > 0) {

				DataInputFile tempObj = new DataInputFile();
				tempObj.setInputFileTypeId(Integer.parseInt(inputFileTypeId));
				tempObj.setSpaceId(space);
				tempObj.setMeterName(meterName);
				tempObj.setTimeZone(timeZone);
				tempObj.setDataType(dataType);
				tempObj.setTimestampFormat(timestampFormat);
				tempObj.setIntervalLength(Integer.parseInt(intervalLength));
				tempObj.setUnits(units);
				tempObj.setIsIntervalStart(Boolean.parseBoolean(isIntervalStart));

				logger.info("Request body obj =" + tempObj);
				Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]*$");
				Matcher matcher = pattern.matcher(meterName);
				boolean isValidMeterName = matcher.find();
				logger.info("meter name is valid = " + isValidMeterName);
				if (isValidMeterName) {
					if (space != null) {
						String fileId = req.getParameter("fileId");
						logger.info("fileId = " + fileId);
						if (fileId != null) {
							tempObj.setFileId(fileId);
							// update input file request
							DataInputFile dif = dataInputFileService.findByFileId(fileId);
							if (dif != null) {
								if ((dif.getSpaceId() != null)
										&& (dif.getSpaceId().getGeneratedUUID().equals(spaceId))) {
									// remove exiting file from s3 server
									String key = dif.getLocation() + dif.getFileName();
									logger.info("s3 key = " + key);
									s3Service.deleteFile(key);
									logger.info(dif.getFileName() + " file delete sucessfully from the s3 server");
									// update input file and additional
									// parameter
									dif.setInputFileTypeId(tempObj.getInputFileTypeId());
									dif.setInputFileTypeId(tempObj.getInputFileTypeId());
									dif.setSpaceId(tempObj.getSpaceId());
									dif.setFileId(tempObj.getFileId());
									dif.setMeterName(tempObj.getMeterName());
									dif.setTimeZone(tempObj.getTimeZone());
									dif.setDataType(tempObj.getDataType());
									dif.setTimestampFormat(tempObj.getTimestampFormat());
									dif.setIntervalLength(tempObj.getIntervalLength());
									dif.setUnits(tempObj.getUnits());
									dif.setIsIntervalStart(tempObj.getIsIntervalStart());
									map = dataFileUploadHelper.postDataFile(files, dif);
									if (map.containsKey("errors")) {
										return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
									}
								} else {
								//	DataInputFile not valid for Space
									map.put("application", appConfigProperties.getApplication());
									map.put("requestId", "");
									map.put("status", "Failed");
									map.put("Timestamp", new Date().toString());
									Errors errors = new Errors();
									errors.setApplicationErrorCode(ETLErrors.INVALID_FILE_ID_FOR_SPACE_TRANSFORMATION.errorCode());
									errors.setApplicationErrorMsg(ETLErrors.INVALID_FILE_ID_FOR_SPACE_TRANSFORMATION.errorMessage() + spaceId);
									map.put("errors", errors);
									return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
								}
							} else {
								//DataInputFile with id " + fileId + " Not Found
								map.put("application", appConfigProperties.getApplication());
								map.put("requestId", "");
								map.put("status", "Failed");
								map.put("Timestamp", new Date().toString());
								Errors errors = new Errors();
								errors.setApplicationErrorCode(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorCode());
								errors.setApplicationErrorMsg(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorMessage() + " id :"+fileId);
								map.put("errors", errors);
								return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
							}
						} else {
							map = dataFileUploadHelper.postDataFile(files, tempObj);
							if (map.containsKey("errors")) {
								map.remove("isValid");
								return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
							}
						}
					} else {
						map.put("application", appConfigProperties.getApplication());
						map.put("requestId", "");
						map.put("status", "Failed");
						map.put("Timestamp", new Date().toString());
						Errors errors = new Errors();
						errors.setApplicationErrorCode(ETLErrors.SPACE_NOT_FOUND_GENERAL_ERROR.errorCode());
						errors.setApplicationErrorMsg(ETLErrors.SPACE_NOT_FOUND_GENERAL_ERROR.errorMessage());
						map.put("errors", errors);
						return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
					}
				} else {
					map.put("application", appConfigProperties.getApplication());
					map.put("requestId", "");
					map.put("status", "Failed");
					map.put("Timestamp", new Date().toString());
					Errors errors = new Errors();
					errors.setApplicationErrorCode(ETLErrors.INVALID_METER_NAME_UPLOAD.errorCode());
					errors.setApplicationErrorMsg(ETLErrors.INVALID_METER_NAME_UPLOAD.errorMessage());
					map.put("errors", errors);
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}

			} else {
				map.put("application", appConfigProperties.getApplication());
				map.put("requestId", "");
				map.put("status", "Failed");
				map.put("Timestamp", new Date().toString());
				List<String> errorMessageList = new ArrayList<String>(errorMap.keySet());
				for (int i = 0; i < errorMessageList.size(); ++i) {
					Errors errors = new Errors();
					errors.setApplicationErrorCode(Integer.parseInt(errorMap.get(errorMessageList.get(i)).toString()));
					errors.setApplicationErrorMsg(errorMessageList.get(i).toString());
					errorArray.add(i, errors);
				}
				map.put("errors", errorArray);
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("errorMessage", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "batchupload", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> multipleUpload(HttpServletRequest req, @PathVariable("spaceId") String spaceId) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> errorMap = new HashMap<String, Object>();
		List<Errors> errorArray = new ArrayList<Errors>();
		try {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) req;
			Map<String, MultipartFile> files = multiRequest.getFileMap();

			List<List<String[]>> filesData = new ArrayList<>();

			if (files.isEmpty()) {
				errorMap.put(ETLErrors.MISSING_FILE_UPLOAD.errorMessage(), ETLErrors.MISSING_FILE_UPLOAD.errorCode());
			} else {
				for (Entry<String, MultipartFile> entry : files.entrySet()) {
					MultipartFile file = entry.getValue();
					if (file != null) {
						try (Reader reader = new InputStreamReader(file.getInputStream());
								CSVReader csvReader = new CSVReader(reader)) {
							List<String[]> fileData = csvReader.readAll();
							filesData.add(fileData);
						}
					}

				}
			}
			String inputFileTypeId = req.getParameter("inputFileTypeId");
			inputFileTypeId = inputFileTypeId.equals("") ? null : inputFileTypeId;
			if (inputFileTypeId == null) {
				if (!files.isEmpty() && !filesData.isEmpty()) {
					if (filesData.size() > 1) {
						int lastTypeId = -1;
						Map<Integer, Integer> fileDataMap = new HashMap<>();
						for (List<String[]> fileData : filesData) {
							int fileTypeId = requestParameterDetector.detectFileTypeId(fileData);
							fileDataMap.put(fileTypeId, fileTypeId);
							lastTypeId = fileTypeId;
						}
						if (fileDataMap.size() > 1 || fileDataMap.get(lastTypeId) == -1) {
							logger.info("Couldn't detect file type id");
							errorMap.put(ETLErrors.UNABLE_TO_DETECT_INPUTFILETYPEID.errorMessage(),
									ETLErrors.UNABLE_TO_DETECT_INPUTFILETYPEID.errorCode());
						} else {
							inputFileTypeId = String.valueOf(lastTypeId);
						}
					}
				} else {
					logger.info("Couldn't detect file type id due to no file data available.");
				}
			}

			String meterName = req.getParameter("meterName");
			meterName = meterName.equals("") ? null : meterName;
			if (meterName == null) {
				errorMap.put(ETLErrors.MISSING_METERNAME_UPLOAD.errorMessage(),
						ETLErrors.MISSING_METERNAME_UPLOAD.errorCode());
			}

			String timeZone = req.getParameter("timeZone");
			timeZone = timeZone.equals("") ? null : timeZone;
			if (timeZone == null) {
				errorMap.put(ETLErrors.MISSING_TIMEZONE_UPLOAD.errorMessage(),
						ETLErrors.MISSING_TIMEZONE_UPLOAD.errorCode());
			}

			String dataType = req.getParameter("dataType");
			dataType = dataType.equals("") ? null : dataType;
			if (dataType != null) {
				Pattern dataTypePattern = Pattern.compile("^[a-zA-Z0-9_]*$");
				Matcher dataTypematcher = dataTypePattern.matcher(dataType);
				if (!dataTypematcher.find()) {
					errorMap.put(ETLErrors.INVALID_DATATYPE_UPLOAD.errorMessage(),
							ETLErrors.INVALID_DATATYPE_UPLOAD.errorCode());
					dataType = null;
				}
			} else {
				errorMap.put(ETLErrors.MISSING_DATATYPE_UPLOAD.errorMessage(),
						ETLErrors.MISSING_DATATYPE_UPLOAD.errorCode());

			}

			String detectedTimeStampFormat = null;
			boolean isTimeStampFormatDetected = false;
			String timestampFormat = req.getParameter("timestampFormat");
			timestampFormat = timestampFormat.equals("") ? null : timestampFormat;
			if (timestampFormat == null) {
				if (!files.isEmpty() && !filesData.isEmpty()) {
					Map<String, String> fileDataMap = new HashMap<>();
					String lastDateFormat = null;
					for (List<String[]> fileData : filesData) {
						String dateFormat = requestParameterDetector.detectDateTimeFormart(fileData);
						fileDataMap.put(dateFormat, dateFormat);
						lastDateFormat = dateFormat;
					}
					if (fileDataMap.size() > 1 || lastDateFormat == null) {
						logger.info("Couldn't detect timestampformat");
						errorMap.put(ETLErrors.UNABLE_TO_DETECT_TIMESTAMPFORMAT.errorMessage(),
								ETLErrors.UNABLE_TO_DETECT_TIMESTAMPFORMAT.errorCode());
					} else {
						timestampFormat = String.valueOf(lastDateFormat);
						logger.info("Detected " + timestampFormat + " as timestampformat by auto detection process");
					}
				}

			}

			String intervalLength = req.getParameter("intervalLength");
			intervalLength = intervalLength.equals("") ? null : intervalLength;
			if(intervalLength != null)
			{
				try{
					int interval = Integer.parseInt(intervalLength);
					if(interval % 900 != 0)
					{
						intervalLength = " Invalid interval length";
					}
				}catch (Exception e){
					intervalLength = " Invalid interval length";
					logger.error(e.getMessage(),e);
				}
				
			}
			if (intervalLength != req.getParameter("intervalLength")) {
				if (intervalLength == null) {
					if (!files.isEmpty() && !filesData.isEmpty()) {
						if (filesData.size() > 1) {
							int lastInterval = -1;
							Map<Integer, Integer> fileDataMap = new HashMap<>();
							for (List<String[]> fileData : filesData) {
								int interval = requestParameterDetector.detectIntervalLength(fileData,
										detectedTimeStampFormat, isTimeStampFormatDetected, errorMap);
								fileDataMap.put(interval, interval);
								lastInterval = interval;
							}
							if (fileDataMap.size() > 1 || fileDataMap.get(lastInterval) == -1) {
								logger.info("Couldn't detect interval length");
								errorMap.put(ETLErrors.UNABLE_TO_DETECT_INTERVAL_LENGTH.errorMessage(),
										ETLErrors.UNABLE_TO_DETECT_INTERVAL_LENGTH.errorCode());
							} else {
								intervalLength = String.valueOf(lastInterval);
								logger.info("Detected " + intervalLength + " as interval length by auto detection process");
							}
						}
					} else {
						logger.info("Couldn't detect interval length due to no file data available.");
					}
				}else {
					errorMap.put(ETLErrors.INVALID_INTERVAL_LENGTH_UPLOAD.errorMessage(),
							ETLErrors.INVALID_INTERVAL_LENGTH_UPLOAD.errorCode());
					intervalLength = null;
					logger.info("Couldn't detect interval length having no file in place.");

				}
			}
			String units = req.getParameter("units");
			units = units.equals("") ? null : units;
			if (units == null) {
				errorMap.put(ETLErrors.MISSING_DATA_UNITS_UPLOAD.errorMessage(),
						ETLErrors.MISSING_DATA_UNITS_UPLOAD.errorCode());
			}

			String isIntervalStart = req.getParameter("isIntervalStart");
			isIntervalStart = isIntervalStart.equals("") ? null : isIntervalStart;
			if (isIntervalStart == null) {
				errorMap.put(ETLErrors.MISSING_ISINTERVALSTART_UPLOAD.errorMessage(),
						ETLErrors.MISSING_ISINTERVALSTART_UPLOAD.errorCode());
			}
			Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]*$");
			Matcher matcher = pattern.matcher(meterName);
			boolean isValidMeterName = matcher.find();
			logger.info("meter name is valid = " + isValidMeterName);
			if (!isValidMeterName) {
				errorMap.put(ETLErrors.INVALID_METER_NAME_UPLOAD.errorMessage(),
						ETLErrors.INVALID_METER_NAME_UPLOAD.errorCode());
				meterName = null;
				
			}
			Space space = spaceService.findByGeneratedUUID(spaceId);

			if (inputFileTypeId != null && spaceId != null && meterName != null && timeZone != null && dataType != null
					&& timestampFormat != null && intervalLength != null && units != null && isIntervalStart != null
					&& files != null && files.size() > 0) {
				DataInputFile tempObj = new DataInputFile();
				tempObj.setInputFileTypeId(Integer.parseInt(inputFileTypeId));
				tempObj.setSpaceId(space);
				tempObj.setMeterName(meterName);
				tempObj.setTimeZone(timeZone);
				tempObj.setDataType(dataType);
				tempObj.setTimestampFormat(timestampFormat);
				tempObj.setIntervalLength(Integer.parseInt(intervalLength));
				tempObj.setUnits(units);
				tempObj.setIsIntervalStart(Boolean.parseBoolean(isIntervalStart));
				logger.info("Request body obj =" + tempObj);
				if (space != null) {
					map = dataFileUploadHelper.postDataFile(files, tempObj);
					if (map.containsKey("errors")) {
						return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
					}
				} else {
					map.put("errorMessage", "Space id " + spaceId + " Not Found");
					return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
				}
			} else {
				map.put("application", appConfigProperties.getApplication());
				map.put("requestId", "");
				map.put("status", "Failed");
				map.put("Timestamp", new Date().toString());
				List<String> errorMessageList = new ArrayList<String>(errorMap.keySet());
				for (int i = 0; i < errorMessageList.size(); ++i) {
					Errors errors = new Errors();
					errors.setApplicationErrorCode(Integer.parseInt(errorMap.get(errorMessageList.get(i)).toString()));
					errors.setApplicationErrorMsg(errorMessageList.get(i).toString());
					errorArray.add(i, errors);
				}
				map.put("errors", errorArray);
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("errorMessage", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "/data-files/{id}", method = RequestMethod.DELETE)
	public @ResponseBody ResponseEntity<Object> deleteDataFiles(@PathVariable("spaceId") String spaceId,
			@PathVariable("id") String id) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			DataInputFile dif = dataInputFileService.findByFileId(id);
			if (dif != null) {
				if ((dif.getSpaceId() != null) && (dif.getSpaceId().getGeneratedUUID().equals(spaceId))) {
					String key = dif.getLocation() + dif.getFileName();
					s3Service.deleteFile(key);
					dataInputFileService.delete(dif.getId());
					map.put("message", "Data file with id " + id + " deleted successfully");
					logger.info("File deleted successfully");
				} else {
					map.put("application", appConfigProperties.getApplication());
					map.put("requestId", "");
					map.put("status", "failed");
					map.put("Timestamp", new Date().toString());
					Errors errors = new Errors();
					errors.setApplicationErrorCode(ETLErrors.SPACE_NOT_FOUND_GENERAL_ERROR.errorCode());
					errors.setApplicationErrorMsg(ETLErrors.SPACE_NOT_FOUND_GENERAL_ERROR.errorMessage());
					map.put("errors", errors);  
					return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
				}
			} else {
				map.put("application", appConfigProperties.getApplication());
				map.put("requestId", "");
				map.put("status", "failed");
				map.put("Timestamp", new Date().toString());
				Errors errors = new Errors();
				errors.setApplicationErrorCode(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorCode());
				errors.setApplicationErrorMsg(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorMessage()+ "id:"+id);
				map.put("errors", errors);  
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("errorMessage", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "/jobs", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> jobs(@RequestBody Jobs obj, @PathVariable("spaceId") String spaceId) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			logger.info("request body obj = " + obj);
			DataInputFile dif = dataInputFileService.findByFileId(obj.getInputFileId());
			if (dif != null) {
				if (dif.getSpaceId().getGeneratedUUID().equals(spaceId)) {
					String jobUploadPath = dif.getSpaceId().getGeneratedUUID();
					jobUploadPath +=  appConfigProperties.getJobsUploadPath();
					logger.info("s3 job upload Path = " + jobUploadPath);
					boolean exits = s3Service.isPathExists(jobUploadPath);
					if (exits == false) {
						s3Service.createFolder(jobUploadPath);
					}
					String jobId = jobHelper.getJobId();
					while (true) {
						if (jobService.isUniqueJobId(jobId)) {
							break;
						}
						jobId = jobHelper.getJobId();
					}
					obj.setJobId(jobId);
					obj.setStatus(JobStatusValues.Loading.toString());
					Space space = spaceService.findByGeneratedUUID(spaceId);
					obj.setSpaceId(space);
					obj = jobService.save(obj);
					validationAndJobExecution.validateAndExecuteJob(obj, dif);
					map.put("job", obj);
				} else {
					map.put("application", appConfigProperties.getApplication());
					map.put("requestId", "");
					map.put("status", "failed");
					map.put("Timestamp", new Date().toString());
					Errors errors = new Errors();
					errors.setApplicationErrorCode(ETLErrors.INVALID_FILE_ID_FOR_SPACE_TRANSFORMATION.errorCode());
					errors.setApplicationErrorMsg(ETLErrors.INVALID_FILE_ID_FOR_SPACE_TRANSFORMATION.errorMessage());
					map.put("errors", errors);  
					return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
				}
			} else {
				map.put("application", appConfigProperties.getApplication());
				map.put("requestId", "");
				map.put("status", "failed");
				map.put("Timestamp", new Date().toString());
				Errors errors = new Errors();
				errors.setApplicationErrorCode(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorCode());
				errors.setApplicationErrorMsg(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorMessage());
				map.put("errors", errors);  
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("errorMessage", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "/jobs/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Object> jobsPUT(@PathVariable("spaceId") String spaceId, @PathVariable("id") String jobId,
			@RequestBody Jobs obj) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			logger.info("request body obj = " + obj);
			String realPath = System.getProperty("user.dir") + "/";
			if (jobId != null) {
				logger.info("Job Id = " + jobId);
				DataInputFile dif = dataInputFileService.findByFileId(obj.getInputFileId());
				if (dif != null) {
					if ((dif.getSpaceId() != null) && (dif.getSpaceId().getGeneratedUUID().equals(spaceId))) {
						String jobUploadPath = appConfigProperties.getJobsUploadPath();
						String localFilePath = appConfigProperties.getLocalDataInputFilePath();
						Jobs jobObj = jobService.findJobByJobId(jobId);
						logger.info("retrieved job object from db = " + jobObj);
						if (jobObj != null) {
							if ((jobObj.getSpaceId() != null)
									&& (jobObj.getSpaceId().getGeneratedUUID().equals(spaceId))) {
								if (jobId != null && obj.getAction() != null && obj.getAction().equals("rerun")) {
									obj.setJobId(jobId);
									jobUploadPath += dif.getSpaceId().getGeneratedUUID();
									boolean exits = s3Service.isPathExists(jobUploadPath);
									if (exits == false) {
										s3Service.createFolder(jobUploadPath);
									}
									obj.setOutputFileLink(null);
									obj.setOutputFileName(null);
									obj.setOutputFileLocation(null);
									logger.info("before update to job obj = " + obj);
									obj = jobService.save(obj);
									logger.info("after update to job obj = " + obj);
									validationAndJobExecution.validateAndExecuteJob(obj, dif);
									map.put("job", obj);
								} else if (obj.getSkipTransformation() == true && obj.getAction() == null) {
									// skip transformation and call PMT upload
									// service
									String output_path = realPath + localFilePath + dif.getSpaceId().getGeneratedUUID();
									File dir = new File(output_path);
									if (!dir.exists()) {
										dir.mkdirs();
										logger.info(" Dir path = " + output_path + " has been created");
									}
									String sourceFilePath = jobObj.getOutputFileLocation() + ""
											+ jobObj.getOutputFileName();
									String destFilePath = output_path + "/" + jobObj.getOutputFileName();
									s3Service.saveToLocal(sourceFilePath, destFilePath);
									logger.info("File Path =" + sourceFilePath);
									map = pmtService.PMTHistoricalAPI(dif, destFilePath);
									dir = new File(sourceFilePath);
									dir.deleteOnExit();
								} else if (obj.getSkipTransformation() == false && obj.getAction() == null) {
									// skip transformation is false and action
									// is not
									// provided then rerun job
									jobUploadPath += dif.getSpaceId().getGeneratedUUID();
									obj.setAction("rerun");
									obj.setJobId(jobId);
									boolean exits = s3Service.isPathExists(jobUploadPath);
									if (exits == false) {
										s3Service.createFolder(jobUploadPath);
									}
									obj.setOutputFileLink(null);
									obj.setOutputFileName(null);
									obj.setOutputFileLocation(null);
									logger.info("before update to job obj = " + obj);
									obj = jobService.save(obj);
									logger.info("after update to job obj = " + obj);
									validationAndJobExecution.validateAndExecuteJob(obj, dif);
									map.put("job", obj);
								} else {
									map.put("errorMessage", "Invalid data provided");
									return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
								}
							} else {
								map.put("application", appConfigProperties.getApplication());
								map.put("requestId", "");
								map.put("status", "failed");
								map.put("Timestamp", new Date().toString());
								Errors errors = new Errors();
								errors.setApplicationErrorCode(ETLErrors.INVALID_JOB_ID_FOR_SPACE_TRANSFORMATION.errorCode());
								errors.setApplicationErrorMsg(ETLErrors.INVALID_JOB_ID_FOR_SPACE_TRANSFORMATION.errorMessage()+spaceId);
								map.put("errors", errors);  
								return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
							}
						} else {
							map.put("application", appConfigProperties.getApplication());
							map.put("requestId", "");
							map.put("status", "failed");
							map.put("Timestamp", new Date().toString());
							Errors errors = new Errors();
							errors.setApplicationErrorCode(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorCode());
							errors.setApplicationErrorMsg(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorMessage()+" id:"+jobId);
							map.put("errors", errors);  
							return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
						}
					} else {
						//DataInputFile with id " + obj.getInputFileId() + " not valid
						map.put("application", appConfigProperties.getApplication());
						map.put("requestId", "");
						map.put("status", "failed");
						map.put("Timestamp", new Date().toString());
						Errors errors = new Errors();
						errors.setApplicationErrorCode(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorCode());
						errors.setApplicationErrorMsg(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorMessage()+" id:"+ obj.getInputFileId());
						map.put("errors", errors); 
						return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
					}
				} else {
					//DataInputFile not valid for Space " + spaceId
					map.put("application", appConfigProperties.getApplication());
					map.put("requestId", "");
					map.put("status", "failed");
					map.put("Timestamp", new Date().toString());
					Errors errors = new Errors();
					errors.setApplicationErrorCode(ETLErrors.INVALID_FILE_ID_FOR_SPACE_TRANSFORMATION.errorCode());
					errors.setApplicationErrorMsg(ETLErrors.INVALID_FILE_ID_FOR_SPACE_TRANSFORMATION.errorMessage()+spaceId);
					map.put("errors", errors); 
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}
			} else {
				map.put("application", appConfigProperties.getApplication());
				map.put("requestId", "");
				map.put("status", "failed");
				map.put("Timestamp", new Date().toString());
				Errors errors = new Errors();
				errors.setApplicationErrorCode(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorCode());
				errors.setApplicationErrorMsg(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorMessage()+" id:" +jobId);
				map.put("errors", errors);  
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("errorMessage", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "/jobs", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> getJobs(HttpServletRequest req, Pageable pageable,
			@PathVariable("spaceId") String spaceId) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {

			String direction = req.getParameter("order");
			String properties = req.getParameter("sort");

			if (properties == null || properties.equals("")) {
				properties = "id";
			}
			if (direction != null && direction.equals("desc")) {
				pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
						new Sort(Sort.Direction.DESC, properties));
			} else {
				pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
						new Sort(Sort.Direction.ASC, properties));
			}
			Page<Jobs> jobList = jobService.findAllJobsbySpaceID(pageable, spaceId);
			if (jobList.getSize() > 0) {
				map.put("jobs", jobList);
			} else {
				map.put("errorMessage", "Job Not Found");
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("application", appConfigProperties.getApplication());
			map.put("requestId", "");
			map.put("status", "failed");
			map.put("Timestamp", new Date().toString());
			map.put("errorMessage", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "/jobs/sort", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> jobs(HttpServletRequest req) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String direction = req.getParameter("order");
			String properties = req.getParameter("sort");
			Sort sort = null;
			if (direction != null && direction.equals("desc")) {
				sort = new Sort(Sort.Direction.DESC, properties);
			} else {
				sort = new Sort(Sort.Direction.ASC, properties);
			}
			List<Jobs> list = jobService.findAll(sort);
			map.put("jobs", list);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("application", appConfigProperties.getApplication());
			map.put("requestId", "");
			map.put("status", "failed");
			map.put("Timestamp", new Date().toString());
			map.put("errorMessage", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "/jobs/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> getJobById(@PathVariable("spaceId") String spaceId,
			@PathVariable("id") String jobId) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Jobs job = jobService.findJobByJobId(jobId);
			JobDetails jobDetails = jobService.getJobDetailsForJob(job);
			if (job != null) {
				if ((job.getSpaceId() != null) && (job.getSpaceId().getGeneratedUUID().equals(spaceId))) {
					map.put("job", jobDetails);
				} else {
					//Job not valid for Space " + spaceId
					map.put("application", appConfigProperties.getApplication());
					map.put("requestId", "");
					map.put("status", "failed");
					map.put("Timestamp", new Date().toString());
					Errors errors = new Errors();
					errors.setApplicationErrorCode(ETLErrors.INVALID_JOB_ID_FOR_SPACE_TRANSFORMATION.errorCode());
					errors.setApplicationErrorMsg(ETLErrors.INVALID_JOB_ID_FOR_SPACE_TRANSFORMATION.errorMessage()+ spaceId);
					map.put("errors", errors);
					return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
				}
			} else {
				//Job id " + jobId + " Not Found
				map.put("application", appConfigProperties.getApplication());
				map.put("requestId", "");
				map.put("status", "failed");
				map.put("Timestamp", new Date().toString());
				Errors errors = new Errors();
				errors.setApplicationErrorCode(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorCode());
				errors.setApplicationErrorMsg(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorMessage()+" id :"+ jobId);
				map.put("errors", errors);
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("application", appConfigProperties.getApplication());
			map.put("requestId", "");
			map.put("status", "failed");
			map.put("Timestamp", new Date().toString());
			map.put("errorMessage", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "/jobs/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Object> deleteJobs(@PathVariable("spaceId") String spaceId,
			@PathVariable("id") String jobId) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Jobs job = jobService.findJobByJobId(jobId);
			if (job != null) {
				if ((job.getSpaceId() != null) && (job.getSpaceId().getGeneratedUUID().equals(spaceId))) {
					String key = job.getOutputFileLink();
					if (key != null) {
						boolean isFileExits = s3Service.isFileExists(key);
						if (isFileExits == true) {
							s3Service.deleteFile(key);
							logger.info("Job  id  :" + jobId + " output file deleted from s3 server successfully");
						} else {
							logger.info("Job  id  :" + jobId + "  output file key does not exits on the s3");
						}
					}
					Boolean status = jobService.deleteJob(jobId);
					if (status) {
						map.put("message", "Job id " + jobId + " deleted successfully.");
					} else {
						//Job id " + jobId + " Not Found.
						map.put("requestId", "");
						map.put("status", "Failed");
						map.put("Timestamp", new Date().toString());
						Errors errors = new Errors();
						errors.setApplicationErrorCode(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorCode());
						errors.setApplicationErrorMsg(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorMessage()+" id:"+jobId);
						map.put("errors", errors);
						return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
					}
				} else {
					//Job not valid for Space " + spaceId
					map.put("requestId", "");
					map.put("status", "Failed");
					map.put("Timestamp", new Date().toString());
					Errors errors = new Errors();
					errors.setApplicationErrorCode(ETLErrors.INVALID_JOB_ID_FOR_SPACE_TRANSFORMATION.errorCode());
					errors.setApplicationErrorMsg(ETLErrors.INVALID_JOB_ID_FOR_SPACE_TRANSFORMATION.errorMessage()+ spaceId);
					map.put("errors", errors);
					return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
				}
			} else {
				//Job id " + jobId + " Not Found
				map.put("requestId", "");
				map.put("status", "Failed");
				map.put("Timestamp", new Date().toString());
				Errors errors = new Errors();
				errors.setApplicationErrorCode(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorCode());
				errors.setApplicationErrorMsg(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorMessage()+" id:"+jobId);
				map.put("errors", errors);
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("errorMessage", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "/jobs/status/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> getJobStatus(@PathVariable("spaceId") String spaceId,
			@PathVariable("id") String jobId) {

		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map = jobService.getJobStatus(jobId, spaceId);
			if (map.get("errorMessage") != null) {
				map.put("errorMessage", map.get("errorMessage"));
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
			map.put("jobStatus", map.get("jobStatus"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@RequestMapping(value = "/jobs/{id}/report", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> getJobReport(HttpServletRequest req,Pageable pageable,@PathVariable("spaceId") String spaceId,
			@PathVariable("id") String jobId) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String direction = req.getParameter("order");
			String properties = req.getParameter("sort");
			if (properties == null || properties.equals("")) {
				properties = "id";	
			}
			if (direction != null && direction.equals("desc")) {
				pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize() , new Sort(Sort.Direction.DESC, properties));
			} else {
				pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(Sort.Direction.ASC, properties));
			}
			map = jobService.getJobReport(jobId, spaceId,pageable);
			if (map.get("errorMessage") != null) {
				map.put("errorMessage", map.get("errorMessage"));
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/s3/download", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> accessFile(@PathVariable("spaceId") String spaceId, @RequestParam(value="file_name", required=true) String fileName, @RequestParam(value="job_id", required = false) String jobId, @RequestParam(value = "file_id", required = false) String fileId,HttpServletRequest req,
			HttpServletResponse res) {
		Map<String, Object> map = new HashMap<String, Object>();
		boolean isJobId = false;
		boolean isFileId = false;
		if(fileName == null) {
			map.put("errorMessage", "File name is not present in the request");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
		if(jobId != null) {
			isJobId = true;
		}
		if(fileId != null) {
			isFileId = true;
		}
		if(isFileId && isJobId) {
			map.put("errorMessage", "Both file id and job id are present in query parameter");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
		if(isFileId) {
			DataInputFile obj = dataInputFileService.findByFileId(fileId);
			if (obj != null) {
				//String key = obj.getLocation() + fileName;
				StringBuilder stringBuilder = new StringBuilder();
				String key = stringBuilder.append(spaceId).append(appConfigProperties.getDataInputFilesPath()).append("/").append(fileName).toString();
				try {
					if(s3Service.doesObjectExists(key)) {
						s3Service.downloadFile(key, res, req, fileName, map);
					} else {
						map.put("requestId", "");
						map.put("status", "Failed");
						map.put("Timestamp", new Date().toString());
						Errors errors = new Errors();
						errors.setApplicationErrorCode(ETLErrors.FILE_NOT_FOUND.errorCode());
						errors.setApplicationErrorMsg(ETLErrors.FILE_NOT_FOUND.errorMessage());
						map.put("errors", errors);
						return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					map.put("requestId", "");
					map.put("status", "Failed");
					map.put("Timestamp", new Date().toString());
					return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				map.put("requestId", "");
				map.put("status", "Failed");
				map.put("Timestamp", new Date().toString());
				Errors errors = new Errors();
				errors.setApplicationErrorCode(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorCode());
				errors.setApplicationErrorMsg(ETLErrors.FILE_ID_NOTFOUND_TRANSFORMATION.errorMessage()+ " id: "+fileId);
				map.put("errors", errors);
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
		}
		
		if(isJobId) {
			Jobs job = jobService.findJobByJobId(jobId);
			if(job != null) {
				StringBuilder jobUrlBuilder = new StringBuilder();
				//String key = job.getOutputFileLocation() + fileName;
				String key = jobUrlBuilder.append(spaceId).append(appConfigProperties.getJobsUploadPath()).append("/").append(jobId).append("/").append(fileName).toString();
				try {
					if(s3Service.doesObjectExists(key)) {
						s3Service.downloadFile(key, res, req, fileName, map);
					} else {
						StringBuilder stringBuilder = new StringBuilder();
						key = stringBuilder.append(spaceId).append(appConfigProperties.getDataInputFilesPath()).append("/").append(fileName).toString();
						if(s3Service.doesObjectExists(key)) {
							s3Service.downloadFile(key, res, req, fileName, map);
						} else {
							map.put("requestId", "");
							map.put("status", "Failed");
							map.put("Timestamp", new Date().toString());
							Errors errors = new Errors();
							errors.setApplicationErrorCode(ETLErrors.FILE_NOT_FOUND.errorCode());
							errors.setApplicationErrorMsg(ETLErrors.FILE_NOT_FOUND.errorMessage());
							map.put("errors", errors);
							return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
						}
					}									
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					map.put("requestId", "");
					map.put("status", "Failed");
					map.put("Timestamp", new Date().toString());
					return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				//Job with id " + jobId + " Not Found
				map.put("requestId", "");
				map.put("status", "Failed");
				map.put("Timestamp", new Date().toString());
				Errors errors = new Errors();
				errors.setApplicationErrorCode(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorCode());
				errors.setApplicationErrorMsg(ETLErrors.JOB_ID_NOTFOUND_TRANSFORMATION.errorMessage()+ " id: "+jobId);
				map.put("errors", errors);
				return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			}
		}
		
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
}
