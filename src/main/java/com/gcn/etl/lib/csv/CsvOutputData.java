package com.gcn.etl.lib.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gcn.etl.database.models.ErrorDetails;
import com.gcn.etl.database.models.Jobs;
import com.gcn.etl.helper.DstValidationHelper;
import com.gcn.etl.lib.s3Aws.S3AwsService;
import com.gcn.etl.pojo.DataPointWithIndex;
import com.gcn.etl.pojo.OutputData;
import com.gcn.etl.propertiesHelper.AppConfigProperties;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

@Service
public class CsvOutputData {

	private static Logger logger = LogManager.getLogger(CsvOutputData.class);
	
	@Autowired
	private AppConfigProperties appConfig;

	@Autowired
	private DstValidationHelper dstValidationHelper;

	@Autowired
	private S3AwsService s3Service;

	public List<OutputData> csvToBean(String filePath) {
		List<OutputData> list = null;
		Map<String, String> columnMapping = this.outputDataColumnMapping();
		CsvToBean<OutputData> csvToBean = new CsvToBean<OutputData>();
		HeaderColumnNameTranslateMappingStrategy<OutputData> strategy = new HeaderColumnNameTranslateMappingStrategy<OutputData>();
		strategy.setType(OutputData.class);
		strategy.setColumnMapping(columnMapping);
		CSVReader reader = null;
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(filePath);
			reader = new CSVReader(fileReader);
			list = csvToBean.parse(strategy, reader);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			try {
				fileReader.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return list;
	}

	public List<OutputData> setDateTimeAndMakeOrderList(List<OutputData> list, String timeZone,
			String timeStampFormat) {
		try {
			if (list.size() > 0) {
				SimpleDateFormat sdf = new SimpleDateFormat(timeStampFormat);
				sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
				int index = 0;
				for (OutputData obj : list) {
					if (obj.getTimeStamp() != null) {
						Date date = sdf.parse(obj.getTimeStamp());
						obj.setDateTime(date);
						list.set(index, obj);
					}
					index++;
				}
				Collections.sort(list, new Comparator<OutputData>() {
					public int compare(OutputData o1, OutputData o2) {
						return o1.getDateTime().compareTo(o2.getDateTime());
					}
				});
			} else {
				logger.info("size of output data list = " + list.size());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return list;
	}

	private Map<String, String> outputDataColumnMapping() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("Timestamp", "timeStamp");
		map.put("Original", "original");
		return map;
	}
 
	public Map<String, Object> findMissingIntervalOrOriginal(List<OutputData> list, String timeZone, Jobs job) {
		String timeStampFormat = "MM/dd/yyyy HH:mm";
		Map<String, Object> map = new HashMap<String, Object>();
		List<ErrorDetails> missingDataPointList = new ArrayList<ErrorDetails>();
		try {
			
			if (list.size() > 0) {
				SimpleDateFormat sdf = new SimpleDateFormat(timeStampFormat);
				sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
				boolean isOriginalNull = false;
				Map<String,OutputData> originalData = list.stream().collect(Collectors.toMap(OutputData::getTimeStamp, Function.identity()));
				list = addMissingDatesForTargetIntervalLength(list,sdf,missingDataPointList,timeZone);
				ArrayList<OutputData> dstSwitchStartDateTime = new ArrayList<OutputData>();
				for(OutputData outputData : list){
					Calendar cal = Calendar.getInstance();
					cal.setTimeZone(TimeZone.getTimeZone(timeZone));
					cal.setTime(outputData.getDateTime());
					int year = cal.get(Calendar.YEAR);
					Boolean isDateDstInSecondSundayOfMarch = dstValidationHelper
							.isDateDstInSecondSundayOfMarch(outputData.getTimeStamp(), year, timeZone);
					if (isDateDstInSecondSundayOfMarch) {
						dstSwitchStartDateTime.add(outputData);
					}
				}
				if(dstSwitchStartDateTime.size() > 0){
					isOriginalNull = true;
				}
				list.removeAll(dstSwitchStartDateTime);
                                
				String localDataInputFilePath = appConfig.getLocalDataInputFilePath();
				String realPath = System.getProperty("user.dir") + "/";
				String localOutputPath = realPath + localDataInputFilePath + job.getSpaceId().getGeneratedUUID() + "/" + job.getJobId();
				String localOutputFilePath = localOutputPath + "/" + job.getOutputFileName();
				
				repairData(list, job, localOutputFilePath, localOutputPath,"_with_target_interval_length.");
				
				List<Integer> missingDataPointsIndices = new ArrayList<Integer>();
				OutputData startData = null;
				OutputData endData = null;
				boolean invalidData = false;
				
				boolean isLastValueEmpty = false;
			
				if(list.get(list.size() -1).getOriginal() == null){
					backwardFill(list);
					isLastValueEmpty = true;
					isOriginalNull= true;
				}
				
				for(int i=0;i<list.size();i++){
					
					OutputData outputDataPoint = list.get(i);
					
					
					if(outputDataPoint.getOriginal() != null){
						outputDataPoint.setOriginal(String.valueOf(Float.parseFloat(outputDataPoint.getOriginal())));
						
						if(missingDataPointsIndices.size() != 0){
							if(i == list.size() - 1){
								if(isLastValueEmpty){
									endData = outputDataPoint;
									interpolateMissingValues(missingDataPointsIndices,startData,endData,list,originalData,missingDataPointList);
									missingDataPointsIndices.add(list.size() - 1);
									if(missingDataPointsIndices.size() > appConfig.getMaxNoOfMissingDataPointsAllowed()){
										invalidData = true;
										break;
									}
								}
							}
							else {
								endData = outputDataPoint;
								interpolateMissingValues(missingDataPointsIndices,startData,endData,list,originalData,missingDataPointList);
								missingDataPointsIndices.clear();
							}
							
						}
						startData = outputDataPoint;
					}
					else {
							if(i == 0){
								String original = outputDataPoint.getOriginal();
								String transformedValueString = forwardFill(list);
								Float transformedValue = Float.parseFloat((transformedValueString));
								if((original == null) || (original.equals(""))){
									missingDataPointList.add(getMissingPoint(list.get(0).getTimeStamp(),timeStampFormat, 1, 2,
											"We fill gap due to Original value missing",original,transformedValue));
								}
								
								missingDataPointsIndices.add(0);
								
								startData = outputDataPoint;
								isOriginalNull= true;
							}
							else
							{
								missingDataPointsIndices.add(i);
								isOriginalNull = true;
								
							}
							if(missingDataPointsIndices.size() > appConfig.getMaxNoOfMissingDataPointsAllowed()){
								invalidData = true;
								break;
							}
							
						}
				}
				list = removeNonQuarterHourlyDates(list,sdf);
				list = interpolateDstSwitchEndDateTime(list,timeZone,sdf);
				logger.info("total size of new updated list is = " + list.size());
				logger.info("Missing datapoints",missingDataPointList);
				if(missingDataPointList.size() > appConfig.getMaxNoOfMissingDataPointsAllowed()) {
					missingDataPointList.subList(appConfig.getMaxNoOfMissingDataPointsAllowed()+1, missingDataPointList.size()).clear();
				}
				if(invalidData){
					List<String> missingTimeStamps = new ArrayList<String>();
					for(Integer missingDataPointIndex : missingDataPointsIndices){
						missingTimeStamps.add(list.get(missingDataPointIndex).getTimeStamp());
					}
					map.put("message", "error");
					map.put("missingDataPoints", missingTimeStamps);
					map.put("missingDataPoint", missingDataPointList);
				}
				else if(isOriginalNull){
					map.put("message", "success");
					map.put("missingDataPoint", missingDataPointList);
					map.put("updatedData", list);
				} else {
					map.put("message", "success");
				}
			} else {
				map.put("message", "error");
				map.put("errorMessage", "output data not found");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return map;
	}

	private List<OutputData> removeNonQuarterHourlyDates(List<OutputData> list, SimpleDateFormat sdf) {
		try {
			List<OutputData> nonQuarterHourlyDates = new ArrayList<OutputData>();
			if(list.size() != 0){
				for(OutputData outputData : list){
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(outputData.getDateTime());

					int calendarMinutes = calendar.get(Calendar.MINUTE);
					int mod = calendarMinutes % 15;
					if(mod != 0){
						nonQuarterHourlyDates.add(outputData);
					}
				}
				list.removeAll(nonQuarterHourlyDates);
			}
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return list;
	}

	private List<OutputData> interpolateDstSwitchEndDateTime(List<OutputData> list, String timeZone, SimpleDateFormat sdf) {
		List<OutputData> newList = new ArrayList<OutputData>();
		try {
			int tempIndex = 0;
			Collections.sort(list,new Comparator<OutputData>() {
			    @Override
			    public int compare(OutputData a, OutputData b) {
			        return a.getDateTime().compareTo(b.getDateTime());
			    }
			});
			List<OutputData> valueCount = new ArrayList<OutputData>();
			for(int i = 0;i<list.size();i++){
				OutputData outputData = list.get(i);
				Date startDate = outputData.getDateTime();
				
				Calendar cal = Calendar.getInstance();
				cal.setTimeZone(TimeZone.getTimeZone(timeZone));
				cal.setTime(startDate);
				startDate = cal.getTime();
				logger.debug(startDate);
				int year = cal.get(Calendar.YEAR);
					Boolean isDateInDstOfFirstSundayInNov = dstValidationHelper
							.isDateDstInFirstOfNov(startDate, outputData.getTimeStamp(), year, timeZone);
					if (isDateInDstOfFirstSundayInNov) {
						
						logger.info("During DST we check for duplicated entry " + startDate + " date");
						boolean isDuplicatedEntryExits = isDuplicatedEntryExits(list,
								outputData.getTimeStamp());
						logger.info("is Duplicated Interval Entry Exits = " + isDuplicatedEntryExits);
						if (isDuplicatedEntryExits == false) {
							
							if(i>4){
								tempIndex = i - 4;
							}
							logger.debug("startDate = " + startDate + " tempIndex = " + tempIndex);
							logger.info("we added duplicated entry = " + list.get(tempIndex));
							newList.add(list.get(tempIndex));
							valueCount.add(outputData);
							if(valueCount.size() == 4){
								newList.addAll(valueCount);
								valueCount.clear();
							}
							
						}
					} else {
						logger.debug("startDate = " + startDate + " outputData.getTimeStamp() = "
								+ outputData.getTimeStamp());
						logger.debug(outputData);
						newList.add(outputData);
					}
			}
			
		}  catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return newList;
	}

	private String forwardFill(List<OutputData> list) {
		try {
			for(int i = 0;i<list.size();i++){
				if(list.get(i).getOriginal() != null){
					list.get(0).setOriginal(list.get(i).getOriginal());
					return list.get(i).getOriginal();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	private void backwardFill(List<OutputData> list) {
		try {
			for(int i = list.size()-1;i >= 0;i--){
				if(list.get(i).getOriginal() != null){
					list.get(list.size() - 1).setOriginal(list.get(i).getOriginal());
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void interpolateMissingValues(List<Integer> missingDataPointsIndices, OutputData begin, OutputData end,
			List<OutputData> list, Map<String,OutputData> originalData, List<ErrorDetails> missingDataPointList) {
		try {
			String timeStampFormat = "MM/dd/yyyy HH:mm";
			int count = missingDataPointsIndices.size() + 1;
			if((begin != null) && (end != null)){
				float beginOriginal = Float.valueOf(begin.getOriginal());
				float endOriginal = Float.valueOf(end.getOriginal());
				int progressIndex = 1;
				for(Integer index : missingDataPointsIndices){
					Float value = beginOriginal + (progressIndex * (endOriginal - beginOriginal)/count);
					value = BigDecimal.valueOf(value).setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
					OutputData outputDataPoint = list.get(index);
					if(originalData.containsKey(outputDataPoint.getTimeStamp())){
						OutputData existingOutputData = originalData.get(outputDataPoint.getTimeStamp());
						if((existingOutputData.getOriginal() == null) || ((existingOutputData.getOriginal().equals("")))){
							missingDataPointList.add(getMissingPoint(existingOutputData.getTimeStamp(),timeStampFormat, index+1, 2,
									"We fill gap due to Original value missing",existingOutputData.getOriginal(),value));
						}
					}
					
					list.get(index).setOriginal(value.toString());
					progressIndex++;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private List<OutputData> addMissingDatesForTargetIntervalLength(List<OutputData> list, SimpleDateFormat sdf, List<ErrorDetails> missingDataPointList, String timeZone) {
		List<OutputData> quarterlyInterpolatedDateList = null;
		try {
			String timeStampFormat = "MM/dd/yyyy HH:mm";
			if(list.size() != 0){
				Set<OutputData> originalDataSet = list.stream().collect(Collectors.toSet());
				ZonedDateTime zoneDateTimeForStartDate = LocalDateTime.parse( list.get(0).getTimeStamp() , DateTimeFormatter.ofPattern( timeStampFormat ) ).atZone( ZoneId.of( timeZone ) );
				long start = roundToNextFifteen(zoneDateTimeForStartDate);
				ZonedDateTime zoneDateTimeForEndDate = LocalDateTime.parse( list.get(list.size() - 1).getTimeStamp() , DateTimeFormatter.ofPattern( timeStampFormat ) ).atZone( ZoneId.of( timeZone ) );
				long end = roundToNextFifteen(zoneDateTimeForEndDate);
				Collection<OutputData> targetIntervalLengthDateSet = new HashSet<OutputData>();
				targetIntervalLengthDateSet.add(list.get(0));
				targetIntervalLengthDateSet.add(list.get(list.size() - 1));
				int lastFifteenMinuteIntervalMinutes = zoneDateTimeForEndDate.getMinute();
				if(lastFifteenMinuteIntervalMinutes < 45){
					end = end+((45-lastFifteenMinuteIntervalMinutes)*60*1000);
				}
				Map<String, OutputData> map = list.stream().collect(Collectors.toMap(OutputData::getTimeStamp, Function.identity()));
				int rowIndex = 2;
				Map<String,Boolean> interpolatedOutputDataMap = new HashMap<String,Boolean>();
				
				
				do{
					start = start + (appConfig.getTargetIntervalLength()*60*1000);
					OutputData outPutData = new OutputData();
					
					String time = sdf.format( new Date(start));
					Date startDate = sdf.parse(time);
					String formattedStartDate = sdf.format(startDate);
					outPutData.setTimeStamp(formattedStartDate);
					if(map.containsKey(formattedStartDate)){
						rowIndex++;
						continue;
					}
					outPutData.setDateTime(startDate);
					if(!interpolatedOutputDataMap.containsKey(outPutData.getTimeStamp())){
						targetIntervalLengthDateSet.add(outPutData);
						interpolatedOutputDataMap.put(outPutData.getTimeStamp(), true);
					}
					
					rowIndex++;
				}while( start < end );
				
				originalDataSet.addAll(targetIntervalLengthDateSet);
				quarterlyInterpolatedDateList = originalDataSet.stream()
				  .collect(Collectors.toList());
				Collections.sort(quarterlyInterpolatedDateList,new Comparator<OutputData>() {
				    @Override
				    public int compare(OutputData a, OutputData b) {
				    	return a.getDateTime().compareTo(b.getDateTime());
				    }
				});
				
				return quarterlyInterpolatedDateList;
			}
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return quarterlyInterpolatedDateList;
	}

	private boolean isDuplicatedEntryExits(List<OutputData> list, String timeStamp) {
		List<String> dateList = list.stream().map(OutputData::getTimeStamp).collect(Collectors.toList());
		String[] dates = dateList.toArray(new String[dateList.size()]);
		Set<String> allItems = new HashSet<>();
		Set<String> duplicates = Arrays.stream(dates).filter(n -> !allItems.add(n)).collect(Collectors.toSet());
		if (duplicates.size() > 0) {
			if (duplicates.contains(timeStamp)) {
				return true;
			}
		}
		return false;
	}

	private ErrorDetails getMissingPoint(String timeStamp, String timeStampFormat, int rowIndex, int colIndex,
			String description,String value,Float transformedValue) {
		ErrorDetails errorDetails = new ErrorDetails();
		try {			
			logger.info("original timeStamp = "+timeStamp);			
			String timeStampAry[] = timeStamp.split(" ");
			errorDetails.setDate(timeStampAry[0]);			
			errorDetails.setIntervalTime(timeStampAry[1]);
			errorDetails.setRowIndex(rowIndex);
			errorDetails.setColIndex(colIndex);
			errorDetails.setDescription(description);
			if(value != null){

				errorDetails.setValue(Float.parseFloat(value));
			}
			errorDetails.setTransformedValue(transformedValue);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return errorDetails;
	}

	private boolean isDstSwitchExits(String timeZone) {
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timeZone));
		ZoneId z = now.getZone();
		ZoneRules zoneRules = z.getRules();
		Boolean isDst = zoneRules.isDaylightSavings(now.toInstant());
		return isDst;
	}

	private long roundToNextFifteen(ZonedDateTime zoneDateTime) {
		long dateRoundToNextFifteen = 0;
		try{
			
			int unroundedMinutes = zoneDateTime.getMinute();
			int mod = unroundedMinutes % 15;
			zoneDateTime.plusMinutes(mod < 8 ? -mod : (15-mod));
			dateRoundToNextFifteen = zoneDateTime.toInstant().toEpochMilli();
			
		}catch (Exception e) {
				logger.error(e.getMessage(), e);
		}
		return dateRoundToNextFifteen;
	}

	public DataPointWithIndex getSpikeIndex(List<OutputData> list, String timeStampFormat, String timeZone) {
		logger.info("checking for spike");
		logger.info("total size of new list = " + list.size());
		String spikeIndexInFile = null;
		DataPointWithIndex spikeData = null;
		try {
			OutputData optData = list.get(0);
			Date startDate = optData.getDateTime();
			List<DataPointWithIndex> usageList = new ArrayList<DataPointWithIndex>();
			SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
			sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
			String tempDate = sdf.format(startDate);
			
			int i = 1;
			for (OutputData obj : list) {
				if (obj.getDateTime() != null) {
					String objectDate = sdf.format(obj.getDateTime());
					if (tempDate.equals(objectDate)) {
						if (obj.getOriginal() != null) {
							Float original = Float.parseFloat(obj.getOriginal());
							if(original < 0){
								original = 0.0f;
							}
							String timeStampAry[] = obj.getTimeStamp().split(" ");
							DataPointWithIndex dataPointWithIndex = new DataPointWithIndex();
							dataPointWithIndex.setDataPoint(original);
							dataPointWithIndex.setRowIndex(i);
							dataPointWithIndex.setColumnIndex(2);
							dataPointWithIndex.setDate(timeStampAry[0]);
							dataPointWithIndex.setInterval(timeStampAry[1]);
							usageList.add(dataPointWithIndex);
						}
					} else {
						spikeData = spikeCheck(usageList);
						if(spikeData != null)
						{
							break;
						}
						usageList.clear();
						tempDate = objectDate;
					}
				}
				i++;
			}
			if(spikeIndexInFile == null)
			{
				spikeData  = spikeCheck(usageList);
			}
				
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return spikeData;
	}

	public DataPointWithIndex spikeCheck(List<DataPointWithIndex> usageList)
	{
		String spikeIndexInFile = null;
		try{
			Collections.sort(usageList, new Comparator<DataPointWithIndex>() {
				public int compare(DataPointWithIndex o1, DataPointWithIndex o2) {
					return o1.getDataPoint().compareTo(o2.getDataPoint());
				}
			});
			DataPointWithIndex firstMaxDataPoint = usageList.get(usageList.size() - 1);
		
			DataPointWithIndex thirdMaxDataPoint = usageList.get(usageList.size() - 3);
			
			Float firstMax = firstMaxDataPoint.getDataPoint();
			Float thirdMax = thirdMaxDataPoint.getDataPoint();
		
			float spikePercentTrigger = 2.8f;
			int minSpikeKw = appConfig.getMinSpikeKw();
			float  thresholdForFirstMax  = Math.max((spikePercentTrigger * thirdMax), (thirdMax + minSpikeKw));
			if (firstMax >= thresholdForFirstMax) {
				spikeIndexInFile = "firstMax = " + firstMax + " thirdMax = "
						+ thirdMax + " thresholdForFirstMax = " + thresholdForFirstMax;
				logger.info(spikeIndexInFile);
				logger.info("spike is here.!");
				return firstMaxDataPoint;
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public Map<String, Object> repairData(List<OutputData> list, Jobs job, String path, String outputPath,String fileNameAppender) {
		logger.info("size of list = " + list.size());
		Map<String, Object> map = new HashMap<String, Object>();
		logger.info("path = " + path);
		FileWriter fileWriter = null;
		String tempOutputFilePath = null;
		File sourceFileData = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path));
			String line = "";
			String newLineSeparator = "\n";
			tempOutputFilePath = outputPath + "/temp_" + job.getOutputFileName();
			fileWriter = new FileWriter(tempOutputFilePath);
			int index = 0;
			while ((line = br.readLine()) != null) {
				fileWriter.append(line);
				fileWriter.append(newLineSeparator);
				index++;
				if (index > 0) {
					break;
				}
			}
			this.appendDataToCSVFile(fileWriter, list);
			sourceFileData = new File(tempOutputFilePath);
			String outputFileName = job.getOutputFileName().replace(".", fileNameAppender);
			s3Service.uploadFileFromPathWithOutTimestamp(job.getOutputFileLocation(), outputFileName,
					sourceFileData);
			map.put("message", "success");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if (sourceFileData != null) {
					logger.info("tempOutputFilePath = " + tempOutputFilePath);
					boolean isDelete = sourceFileData.delete();
					logger.info("tempOutputFilePath delete = " + isDelete);
				}
				if (fileWriter != null) {
					fileWriter.close();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			try {
				br.close();
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return map;
	}

	private void appendDataToCSVFile(FileWriter fileWriter, List<OutputData> list) {
		try {
			String commaDelimiter = ",";
			String newLineSeparator = "\n";
			for (OutputData obj : list) {
				if (obj != null && obj.getTimeStamp().length() > 0) {
					fileWriter.append(String.valueOf(obj.getTimeStamp()));
					fileWriter.append(commaDelimiter);
					if (obj.getOriginal() != null) {
						fileWriter.append(obj.getOriginal().toString());
					} else {
						fileWriter.append("");
					}
					fileWriter.append(newLineSeparator);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				fileWriter.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
