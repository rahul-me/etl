package com.gcn.etl.helper;

import java.io.*;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gcn.etl.constant.Constant;
import com.gcn.etl.database.models.DataInputFile;
import com.gcn.etl.pojo.ApplicationErrorDetails;
import com.gcn.etl.pojo.Column15Min;
import com.gcn.etl.pojo.Column1Hour;
import com.gcn.etl.pojo.Errors;
import com.gcn.etl.pojo.Grid15Min;
import com.gcn.etl.pojo.Grid1Hour;
import com.gcn.etl.propertiesHelper.AppConfigProperties;
import com.opencsv.CSVReader;

@Service
public class DataFileValidator {

	private static Logger logger = LogManager.getLogger(DataFileValidator.class);

	@Autowired
	private OpenCsvHelper openCsvHelper;
	@Autowired
	private AppConfigProperties appConfigProperties;

	public Map<String, Object> isValidDataFile(String localDataFilePath, DataInputFile tempObj) throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		if (tempObj.getIntervalLength() == 900 && tempObj.getInputFileTypeId() == 1) {
			map = validateGridFifteenMinutesFile(localDataFilePath, tempObj.getTimestampFormat());
		} else if (tempObj.getIntervalLength() == 3600 && tempObj.getInputFileTypeId() == 1) {
			map = validateGrid1HourFile(localDataFilePath, tempObj.getTimestampFormat());
		} else if (tempObj.getIntervalLength() == 900 && tempObj.getInputFileTypeId() == 2) {
			map = validateColumnFifteenMinutesFile(localDataFilePath, tempObj.getTimestampFormat());
		} else if (tempObj.getIntervalLength() == 3600 && tempObj.getInputFileTypeId() == 2) {
			map = validateColumn1HourFile(localDataFilePath, tempObj.getTimestampFormat());
		} else {
			logger.info("Invalid column length");
			map.put("isValid", false);
			map.put("errorMessage", "Invalid interval length");
		}
		return map;
	}

	private Map<String, Object> validateColumn1HourFile(String localDataFilePath, String timestampFormat) {

		Map<String, Object> map = new HashMap<String, Object>();
		try {
			List<String> header = openCsvHelper.getColumn1HourHeader(localDataFilePath);
			logger.info("header size = " + header.size());
			if (header.size() == 2) {
				List<Column1Hour> list = openCsvHelper.readColumn1HourCsv(localDataFilePath);
				map.put("isValid", true);
				for (int i = 0; i < list.size(); i++) {
					Column1Hour column1Hour = list.get(i);
					Boolean isValidDateFormat = isValidDateFormat(timestampFormat, column1Hour.getTs());
					if (isValidDateFormat == false) {
						logger.info("Invalid date format Date : " + column1Hour.getTs());
						map.put("isValid", false);
						map.put("errorMessage", "Invalid date format Date : " + column1Hour.getTs());
						break;
					}
					String isValidNumericData = getInValidNumericData(column1Hour.getValue());
					if (isValidNumericData != null) {
						logger.info(
								"Invalid data point in file Value : " + isValidNumericData + ",Date : " + column1Hour.getTs());
						String[] dateTime = column1Hour.getTs().split(" ");
						map.put("isValid", false);
						map.put("application", appConfigProperties.getApplication());
						map.put("requestId", "");
						map.put("status", "Failed");
						map.put("Timestamp", new Date().toString());
						Errors errors = new Errors();
						errors.setApplicationErrorMsg(ETLErrors.INVALID_DATA_TYPE_UPLOAD.errorMessage() +" Value :" +isValidNumericData);
						errors.setApplicationErrorCode(ETLErrors.INVALID_DATA_TYPE_UPLOAD.errorCode());
						List<ApplicationErrorDetails> appErrorDetailsList =  new ArrayList<>();
						ApplicationErrorDetails appErrorDetails = new ApplicationErrorDetails();
						appErrorDetails.setDate(dateTime[0]);
						appErrorDetails.setInterval(dateTime[1]);
						appErrorDetails.setRowIndex(i+1);
						appErrorDetails.setColIndex(2);
						appErrorDetailsList.add(appErrorDetails);
						errors.setErrorDetails(appErrorDetailsList);
						map.put("errors", errors);
						break;
					}
				}

			} else {
				logger.info("Invalid column length");
				map.put("isValid", false);
				map.put("errorMessage", "Invalid column length");
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return map;
	}

	private Map<String, Object> validateColumnFifteenMinutesFile(String localDataFilePath, String timestampFormat) {

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		try {
			List<String> header = openCsvHelper.getColumn15MinHeader(localDataFilePath);
			logger.info("header size = " + header.size());
			map.put("isValid", true);
			if (header.size() == 2) {
				List<Column15Min> list = openCsvHelper.readColumn15MinCsv(localDataFilePath);
				for (int i = 0; i < list.size(); i++) {
					Column15Min column15Min = list.get(i);
					Boolean isValidDateFormat = isValidDateFormat(timestampFormat, column15Min.getTs());
					if (isValidDateFormat == false) {
						logger.info("Invalid date format Date : " + column15Min.getTs());
						map.put("isValid", false);
						map.put("errorMessage", "Invalid date format Date : " + column15Min.getTs());
						break;
					}
					String isValidNumericData = getInValidNumericData(column15Min.getHeader());
					if (isValidNumericData != null) {
						logger.info(
								"Invalid data point in file Value : " + isValidNumericData + ",Date : " + column15Min.getTs());
						String[] dateTime = column15Min.getTs().split(" ");
						map.put("isValid", false);
						map.put("application", appConfigProperties.getApplication());
						map.put("requestId", "");
						map.put("status", "Failed");
						map.put("Timestamp", new Date().toString());
						Errors errors = new Errors();
						errors.setApplicationErrorMsg(ETLErrors.INVALID_DATA_TYPE_UPLOAD.errorMessage() +" Value :" + isValidNumericData);
						errors.setApplicationErrorCode(ETLErrors.INVALID_DATA_TYPE_UPLOAD.errorCode());
						List<ApplicationErrorDetails> appErrorDetailsList =  new ArrayList<>();
						ApplicationErrorDetails appErrorDetails = new ApplicationErrorDetails();
						appErrorDetails.setDate(dateTime[0]);
						appErrorDetails.setInterval(dateTime[1]);
						appErrorDetails.setRowIndex(i+1);
						appErrorDetails.setColIndex(2);
						appErrorDetailsList.add(appErrorDetails);
						errors.setErrorDetails(appErrorDetailsList);
						map.put("errors", errors);
						break;
					}
				}

			} else {
				logger.info("column length is not valid");
				map.put("isValid", false);
				map.put("errorMessage", "Invalid column length");
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return map;
	}

	private Map<String, Object> validateGrid1HourFile(String localDataFilePath, String timestampFormat) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		try {
			List<String> header = openCsvHelper.getGrid1HourHeader(localDataFilePath);
			logger.info("header size = " + header.size());
			map.put("isValid", true);
			if (header.size() == 25) {
				List<Grid1Hour> list = openCsvHelper.readGrid1HourCsv(localDataFilePath);
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						Grid1Hour grid1HourData = list.get(i);
						Boolean isValidDateFormat = isValidDateFormat(timestampFormat, grid1HourData.getDate());
						if (isValidDateFormat == false) {
							if (i > 0) {
								logger.info("Invalid date format Date : " + grid1HourData.getDate());
								map.put("isValidDateFormat", false);
								map.put("errorMessage", "Invalid date format Date : " + grid1HourData.getDate());
								map.put("Date",grid1HourData.getDate());
								map.put("RowIndex", i);
								map.put("ColIndex",1);
								break;
							}
						}
						String isValidNumericData = getInValidNumericDataGridFormat(grid1HourData);
						if (isValidNumericData != null) {
							if (i > 0) {
								logger.info(
										"Invalid data point in file Value : " + isValidNumericData + ",Date : " + grid1HourData.getDate());
								String[] dataTimeColumn= isValidNumericData.split(",");
								// dataTimeColumn[0] = data , dataTimeColumn[1] = interval , dataTimeColumn[2] = columnIndex
								map.put("isValid", false);
								map.put("application", appConfigProperties.getApplication());
								map.put("requestId", "");
								map.put("status", "Failed");
								map.put("Timestamp", new Date().toString());
								Errors errors = new Errors();
								errors.setApplicationErrorMsg(ETLErrors.INVALID_DATA_TYPE_UPLOAD.errorMessage() +" Value :" + dataTimeColumn[0]);
								errors.setApplicationErrorCode(ETLErrors.INVALID_DATA_TYPE_UPLOAD.errorCode());
								List<ApplicationErrorDetails> appErrorDetailsList =  new ArrayList<>();
								ApplicationErrorDetails appErrorDetails = new ApplicationErrorDetails();
								appErrorDetails.setDate(grid1HourData.getDate());
								appErrorDetails.setInterval(dataTimeColumn[1]);
								appErrorDetails.setRowIndex( i);
								appErrorDetails.setColIndex(Integer.parseInt(dataTimeColumn[2]));
								appErrorDetailsList.add(appErrorDetails);
								errors.setErrorDetails(appErrorDetailsList);
								map.put("errors", errors);
								break;
							}
						}
					}
				}

			} else {
				logger.info("column length is not valid");
				map.put("isValid", false);
				map.put("errorMessage", "Invalid column length");
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return map;
	}

	private Map<String, Object> validateGridFifteenMinutesFile(String localDataFilePath, String timeStampFormat) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			List<String> header = openCsvHelper.getGrid15MinHeader(localDataFilePath);
			logger.info("header size of Grid format = " + header.size());
			map.put("isValid", true);
			if (header.size() == 97) {
				List<Grid15Min> list = openCsvHelper.readGrid15MinCsv(localDataFilePath);
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						Grid15Min grid15MinData = list.get(i);
						Boolean isValidDateFormat = isValidDateFormat(timeStampFormat, grid15MinData.getDate());
						if (isValidDateFormat == false) {
							if (i > 0) {
								logger.info("Invalid date format Date : " + grid15MinData.getDate());
								map.put("isValid", false);
								map.put("errorMessage", "Invalid date format");
								map.put("Date",grid15MinData.getDate());
								break;
							}
						}
						String isValidNumericData = getInValidNumericDataGridFormat(grid15MinData);
						if (isValidNumericData != null) {
							if (i > 0) {
								logger.info(
										"Invalid data point in file Value : " + isValidNumericData + ",Date : " + grid15MinData.getDate());
								String[] dataTimeColumn= isValidNumericData.split(",");
								// dataTimeColumn[0] = data , dataTimeColumn[1] = interval , dataTimeColumn[2] = columnIndex

								map.put("isValid", false);
								map.put("application", appConfigProperties.getApplication());
								map.put("requestId", "");
								map.put("status", "Failed");
								map.put("Timestamp", new Date().toString());
								Errors errors = new Errors();
								errors.setApplicationErrorMsg(ETLErrors.INVALID_DATA_TYPE_UPLOAD.errorMessage() +" Value :" + dataTimeColumn[0]);
								errors.setApplicationErrorCode(ETLErrors.INVALID_DATA_TYPE_UPLOAD.errorCode());
								List<ApplicationErrorDetails> appErrorDetailsList =  new ArrayList<>();
								ApplicationErrorDetails appErrorDetails = new ApplicationErrorDetails();
								appErrorDetails.setDate(grid15MinData.getDate());
								appErrorDetails.setInterval(dataTimeColumn[1]);
								appErrorDetails.setRowIndex( i);
								appErrorDetails.setColIndex(Integer.parseInt(dataTimeColumn[2]));
								appErrorDetailsList.add(appErrorDetails);
								errors.setErrorDetails(appErrorDetailsList);
								map.put("errors", errors);
								break;
							}
						}
					}
				}

			} else {
				logger.info("column length is not valid");
				map.put("isValid", false);
				map.put("errorMessage", "Invalid column length");
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return map;
	}

	private String getInValidNumericData(String value) {
		try {
			if (value != null) {
				if (!value.matches("[-+]?[0-9]*\\.?[0-9]+")) {
					return value;
				}
			}
		} catch (Exception ex) {
			logger.error(ex);
		}
		return null;
	}

	private String getInValidNumericDataGridFormat(Object obj) {
		try {
			int i =0;
			for (Field f : obj.getClass().getDeclaredFields()) {
				String name = f.getName();
				if (name.startsWith("t")) {
					String value = (String) f.get(obj);
					if (value != null) {
						String time = name.replaceFirst("t", "");
						if ((time != null) && (!time.equals(""))) {
							time = time.replaceAll("(.*)(..)$", "$1.$2");
						}
						if (!value.matches("[-+]?[0-9]*\\.?[0-9]+")) {
							return  value+","+ time +","+i;
						}
					}
				}
				++i;
			}
		} catch (Exception ex) {
			logger.error(ex);
		}
		return null;
	}

	private Boolean isValidDateFormat(String format, String dateVal) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			date = sdf.parse(dateVal);
			if (!dateVal.equals(sdf.format(date))) {
				date = null;
			}
		} catch (Exception ex) {
			date = null;
		}
		return date != null;
	}

	public Map<String, Object> removeBlankRows(String localDataFilePath) {
		Map<String, Object> map = new HashMap<>();
		try {
			File file = new File(localDataFilePath);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			boolean requiredNewFile = false;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					stringBuffer.append(line);
					stringBuffer.append("\r\n");
				} else {
					requiredNewFile = true;
				}
			}
			bufferedReader.close();
			fileReader.close();

			if (requiredNewFile) {
				String[] oldPathArray = localDataFilePath.split("/");
				String oldFilePath = "";
				for (int i = 0; i < oldPathArray.length - 1; i++) {
					oldFilePath += oldPathArray[i] + "\\";
				}
				map.put("oldPath", oldFilePath);
				oldFilePath +=  Constant.PRE_REMOVE_ROWS + oldPathArray[oldPathArray.length - 1];
				map.put("oldFileName", Constant.PRE_REMOVE_ROWS + oldPathArray[oldPathArray.length - 1]);
				File newFile = new File(oldFilePath);
				FileUtils.moveFile(file, newFile);
				map.put("oldPathFile", oldFilePath);
				BufferedWriter bw = null;
				FileWriter fw = null;
				fw = new FileWriter(localDataFilePath);
				bw = new BufferedWriter(fw);
				bw.write(stringBuffer.toString());
				map.put("newPathfile", localDataFilePath);
				bw.close();
				fw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;

	}

	public Map<String, Object> convertToFloat(String localDataFilePath) {
		Map<String, Object> map = new HashMap<>();
		try {
			
			File file = new File(localDataFilePath);
			FileReader fileReader = new FileReader(file);
			CSVReader reader = new CSVReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			boolean requiredNewFile = false;
			// Read CSV all lines and use the list of string array 
			List<String[]> allRows = reader.readAll();
			  for(String[] nextLine : allRows) {
				// Verifying the read data here
				if (nextLine != null) {
					// i starting from 1 because first element of array is date.
					for (int i = 1; i < nextLine.length; i++) {
						
						if (nextLine[i].contains(",")) {
						// ex : 1,200 
							nextLine[i] = nextLine[i].replaceAll(",", "");
							requiredNewFile = true;
						}
					}
				}
				
				String output = "";
				for(String str: nextLine)
				{
					  output+=str+ ",";
				}
				output = output.substring(0, output.length() - 1);
				stringBuffer.append(output);
				stringBuffer.append("\r\n");
				
			}
			  reader.close();
				fileReader.close();
				
			  if(requiredNewFile)
			  {
				  String[] oldPathArray = localDataFilePath.split("/");
					String oldFilePath = "";
					for (int i = 0; i < oldPathArray.length - 1; i++) {
						oldFilePath += oldPathArray[i] + "\\";
					}
					map.put("oldPath", oldFilePath);
					oldFilePath += "pre_converted_to_float" + oldPathArray[oldPathArray.length - 1];
					map.put("oldFileName", Constant.PRE_CONVERTED_TO_FLOAT + oldPathArray[oldPathArray.length - 1]);
					File newFile = new File(oldFilePath);
					FileUtils.moveFile(file, newFile);
					map.put("oldPathFile", oldFilePath);
			  }
			  BufferedWriter bw = null;
				FileWriter fw = null;
				fw = new FileWriter(localDataFilePath);
				bw = new BufferedWriter(fw);
				bw.write(stringBuffer.toString());
				map.put("newPathfile", localDataFilePath);
				bw.close();
			  
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;

	}

}
