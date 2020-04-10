package com.gcn.etl.helper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.gcn.etl.database.models.DataInputFile;
import com.gcn.etl.database.service.DataInputFileService;
import com.gcn.etl.lib.s3Aws.S3AwsService;
import com.gcn.etl.pojo.ApplicationErrorDetails;
import com.gcn.etl.pojo.BatchFileResponse;
import com.gcn.etl.pojo.Errors;
import com.gcn.etl.propertiesHelper.AppConfigProperties;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class DataFilesUploadHelper {

	private static Logger logger = LogManager.getLogger(DataFilesUploadHelper.class);

	@Autowired
	private AppConfigProperties appConfig;

	@Autowired
	private S3AwsService s3Service;

	@Autowired
	private DataFileValidator dataFileValidator;

	@Autowired
	private DataInputFileService dataInputFileService;

	@Autowired
	private ExcelToCsvHelper excelToCsvHelper;
	
	@Autowired
	private AppConfigProperties appConfigProperties;

	public Map<String, Object> postDataFile(Map<String, MultipartFile> files, DataInputFile tempObj) {
		Map<String, Object> map = new LinkedHashMap<>();
		List<Object> fileIds = new ArrayList<Object>();
		BatchFileResponse batchFileRes = null;
		try {
			String realPath = System.getProperty("user.dir") + "/";
			DataInputFile dif = null;
			for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {

				MultipartFile file = entry.getValue();
				Boolean isValidFile = isFileCsvOrExcel(file.getOriginalFilename());
				logger.info("isValidFile = " + isValidFile);
				if (isValidFile) {
					String localDataFilePath = realPath + appConfig.getLocalDataInputFilePath()
							+ tempObj.getSpaceId().getGeneratedUUID();
					String filePath = saveFileToLocal(file, localDataFilePath);

					filePath = convertExcelToCsv(file.getOriginalFilename(), localDataFilePath, tempObj);
					Map<String, Object> convertedToFloat = dataFileValidator.convertToFloat(filePath);
					if (convertedToFloat.containsKey("oldPathFile")) {
						File fileObj = new File(convertedToFloat.get("oldPathFile").toString());
						String dirPath = tempObj.getSpaceId().getGeneratedUUID()+appConfig.getDataInputFilesPath();
						map = s3Service.uploadFileFromPath(dirPath,convertedToFloat.get("oldFileName").toString(), fileObj);						
						deleteFile(convertedToFloat.get("oldPathFile").toString());
					}
					Map<String, Object> removedBlankRows = dataFileValidator.removeBlankRows(filePath);
					if (removedBlankRows.containsKey("oldPathFile")) {
						File fileObj = new File(removedBlankRows.get("oldPathFile").toString());
						String dirPath = tempObj.getSpaceId().getGeneratedUUID()+appConfig.getDataInputFilesPath();
						map = s3Service.uploadFileFromPath(dirPath,removedBlankRows.get("oldFileName").toString(), fileObj);						
						deleteFile(removedBlankRows.get("oldPathFile").toString());
					}
					Map<String, Object> isValidDataFile = dataFileValidator.isValidDataFile(filePath, tempObj);
					if (isValidDataFile.get("errors") != null) {
						//map.put("errorMessage",isValidDataFile);
						return isValidDataFile;
					}
					if (isValidDataFile.size() > 0
							&& Boolean.parseBoolean(isValidDataFile.get("isValid").toString()) == true) {
						String fileName = getFileNameWithoutSpecialChar(file.getOriginalFilename());
						String dirPath = tempObj.getSpaceId().getGeneratedUUID()+appConfig.getDataInputFilesPath();
						logger.debug("local file path = " + filePath + "");
						File fileObj = new File(filePath);
						map = s3Service.uploadFileFromPath(dirPath, fileName + ".csv", fileObj);
						if (tempObj.getId() != 0) {
							tempObj.setFileName(map.get("fileName").toString());
							tempObj.setFileLink(map.get("fileLink").toString());
							tempObj.setLocation(map.get("location").toString());
							dataInputFileService.save(tempObj);
							map.put("application", appConfig.getApplication());
							map.put("requestId", "");
							map.put("status", "Success");
							map.put("file_id", tempObj.getFileId());

						} else {
							dif = new DataInputFile();
							dif.setFileId(getFileId());
							dif.setInputFileTypeId(tempObj.getInputFileTypeId());
							dif.setSpaceId(tempObj.getSpaceId());
							dif.setMeterName(tempObj.getMeterName());
							dif.setTimeZone(tempObj.getTimeZone());
							dif.setDataType(tempObj.getDataType());
							dif.setTimestampFormat(tempObj.getTimestampFormat());
							dif.setIntervalLength(tempObj.getIntervalLength());
							dif.setUnits(tempObj.getUnits());
							dif.setIsIntervalStart(tempObj.getIsIntervalStart());
							dif.setFileName(map.get("fileName").toString());
							dif.setFileLink(map.get("fileLink").toString());
							dif.setLocation(map.get("location").toString());
							dataInputFileService.save(dif);
							if (files.size() > 1) {
								batchFileRes = new BatchFileResponse();
								batchFileRes.setFileName(file.getOriginalFilename());
								batchFileRes.setFileId(dif.getFileId());
								fileIds.add(batchFileRes);
							} else {
								map.put("application", appConfig.getApplication());
								map.put("status", "Success");
								map.put("requestId", "");
								map.put("file_id", dif.getFileId());
							}
						}
						map.remove("fileName");
						map.remove("fileLink");
						map.remove("location");
						map.put("inputFileTypeId", tempObj.getInputFileTypeId());
						deleteFile(filePath);
						if (isFileExcel(file.getOriginalFilename())) {
							deleteFile(localDataFilePath + "/" + file.getOriginalFilename());
						}
					} else {
						map.put("application", appConfigProperties.getApplication());
						map.put("requestId", "");
						map.put("status", "Failed");
						map.put("Timestamp", new Date().toString());
						Errors errors = new Errors();
						errors.setApplicationErrorMsg(ETLErrors.INVALID_FILE_FORMAT_UPLOAD.errorMessage());
						errors.setApplicationErrorCode(ETLErrors.INVALID_FILE_FORMAT_UPLOAD.errorCode());
						List<ApplicationErrorDetails> appErrorDetailsList =  new ArrayList<>();
						ApplicationErrorDetails appErrorDetails = new ApplicationErrorDetails();
						appErrorDetailsList.add(appErrorDetails);
						errors.setErrorDetails(appErrorDetailsList);
						map.put("errors", errors);
						break;
					}
				} else {
					map.put("application", appConfigProperties.getApplication());
					map.put("requestId", "");
					map.put("status", "Failed");
					map.put("Timestamp", new Date().toString());
					Errors errors = new Errors();
					errors.setApplicationErrorMsg(ETLErrors.INVALID_FILE_FORMAT_UPLOAD.errorMessage());
					errors.setApplicationErrorCode(ETLErrors.INVALID_FILE_FORMAT_UPLOAD.errorCode());
					/*List<ApplicationErrorDetails> appErrorDetailsList =  new ArrayList<>();
					ApplicationErrorDetails appErrorDetails = new ApplicationErrorDetails();
					appErrorDetailsList.add(appErrorDetails);
					errors.setErrorDetails(appErrorDetailsList);*/
					map.put("errors", errors);
					break;
				}

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			map.put("errorMessage", e.getMessage());
		}
		if (files.size() > 1 && map.get("message") == null) {
			map.put("inputFileTypeId", tempObj.getInputFileTypeId());
			map.put("files", fileIds);
		}
		return map;
	}

	private boolean isFileExcel(String originalFileName) {
		try {
			String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
			if (ext.equals(".xlsx") || ext.equals(".xls")) {
				return true;
			}
		} catch (Exception e) {
			logger.error(e);
		}

		return false;
	}

	public boolean isFileCsvOrExcel(String originalFileName) {
		try {
			String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
			logger.info("file ext = " + ext);
			if (ext.equals(".xlsx") || ext.equals(".xls") || ext.equals(".csv")) {
				return true;
			}
		} catch (Exception e) {
			logger.error(e);
		}

		return false;
	}

	private String convertExcelToCsv(String orignalFileName, String localDataFilePath, DataInputFile tempObj) {
		try {
			String ext = orignalFileName.substring(orignalFileName.lastIndexOf("."));
			if (ext.equals(".xlsx") || ext.equals(".xls")) {
				String csvFilePath = null;
				if (tempObj.getInputFileTypeId() == 1 && tempObj.getIntervalLength() == 900) {
					csvFilePath = excelToCsvHelper.convertGridExcelToCsv(localDataFilePath, orignalFileName,
							tempObj.getTimestampFormat(), "HH:mm");
				} else if (tempObj.getInputFileTypeId() == 1 && tempObj.getIntervalLength() == 3600) {
					csvFilePath = excelToCsvHelper.convertGridExcelToCsv(localDataFilePath, orignalFileName,
							tempObj.getTimestampFormat(), "H:mm");
				} else {
					csvFilePath = excelToCsvHelper.convertColumnExcelToCsv(localDataFilePath, orignalFileName,
							tempObj.getTimestampFormat());
				}
				return csvFilePath;
			} else {
				return localDataFilePath + "/" + orignalFileName;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	private String saveFileToLocal(MultipartFile file, String localDataFilePath) throws IOException {
		BufferedOutputStream stream = null;
		try {
			String fileName = file.getOriginalFilename();
			byte[] bytes = file.getBytes();
			logger.info("localDataFilePath = " + localDataFilePath);
			File directory = new File(localDataFilePath);
			if (!directory.exists()) {
				directory.mkdirs();
			}
			String filePath = localDataFilePath + "/" + fileName;
			File fileCsv = new File(filePath);
			stream = new BufferedOutputStream(new FileOutputStream(fileCsv));
			stream.write(bytes);
			stream.close();
			logger.info("File save on local dir");
			return filePath;
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				stream.close();
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return null;

	}

	private String getFileNameWithoutSpecialChar(String orignalFileName) {
		String fName = null;
		try {
			logger.debug("orignalFileName == " + orignalFileName);
			fName = orignalFileName.substring(0, orignalFileName.lastIndexOf("."));
			Pattern pt = Pattern.compile("[^a-zA-Z0-9]");
			fName = fName.replaceAll("\\s", "");
			Matcher match = pt.matcher(fName);
			while (match.find()) {
				String s = match.group();
				fName = fName.replaceAll("\\" + s, "");
			}
			logger.debug("New fileName = " + fName);
			return fName;
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	private String getFileId() {
		String newFileId = null;
		try {
			newFileId = getRandomString();
			while (true) {
				if (dataInputFileService.isFileIdUnique(newFileId)) {
					break;
				}
				newFileId = getRandomString();
			}
		} catch (Exception e) {
			logger.error(e);
		}

		return newFileId;
	}

	private String getRandomString() {
		try {
			String randomString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			Random random = new Random();
			StringBuilder sb = new StringBuilder(8);
			for (int i = 0; i < 8; i++) {
				sb.append(randomString.charAt(random.nextInt(randomString.length())));
			}
			return sb.toString();
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	private boolean deleteFile(String filePath) {
		try {
			logger.info("delete file path = " + filePath);
			File deleteFile = new File(filePath);
			Boolean isDeleteFile = deleteFile.delete();
			logger.info("file delete = " + isDeleteFile);
			return isDeleteFile;
		} catch (Exception e) {
			logger.error(e);
		}
		return false;
	}
}