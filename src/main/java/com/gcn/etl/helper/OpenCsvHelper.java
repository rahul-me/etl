package com.gcn.etl.helper;

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.gcn.etl.pojo.Column15Min;
import com.gcn.etl.pojo.Column1Hour;
import com.gcn.etl.pojo.Grid15Min;
import com.gcn.etl.pojo.Grid1Hour;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

@Service
public class OpenCsvHelper {

	private static Logger logger = LogManager.getLogger(OpenCsvHelper.class);

	public List<Grid15Min> readGrid15MinCsv(String filePath) {
		try {
			String[] columnMapping = this.grid15MinColumnMappingAsArray();
			CsvToBean<Grid15Min> csvToBean = new CsvToBean<Grid15Min>();
			ColumnPositionMappingStrategy<Grid15Min> strategy = new ColumnPositionMappingStrategy<Grid15Min>();
			strategy.setType(Grid15Min.class);
			strategy.setColumnMapping(columnMapping);
			List<Grid15Min> list = null;
			CSVReader reader = null;
			FileReader fileReader = null;
			try {
				fileReader = new FileReader(filePath);
				reader = new CSVReader(fileReader);
				list = csvToBean.parse(strategy, reader);
			} catch (Exception e) {
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
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private String[] grid15MinColumnMappingAsArray() {
		try {
			List<String> columnMapping = new ArrayList<String>();
			columnMapping.add("date");
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MINUTE, 00);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
			for (int index = 0; index <= 95; index++) {
				   Date date = calendar.getTime();
				   String timeFormat = formatter.format(date);
				   String method = "t" + timeFormat.replace(":", "");
				   columnMapping.add(method);
				   calendar.add(Calendar.MINUTE, 15);
			}
			return columnMapping.toArray(new String[0]);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private String[] grid1HourColumnMappingAsArray() {
		try {
			  List<String> columnMapping = new ArrayList<String>();
		        columnMapping.add("date");
		        Calendar calendar = Calendar.getInstance();
		        calendar.set(Calendar.MILLISECOND, 0);
		        calendar.set(Calendar.SECOND, 0);
		        calendar.set(Calendar.MINUTE, 00);
		        calendar.set(Calendar.HOUR_OF_DAY, 0);
		        SimpleDateFormat formatter = new SimpleDateFormat("H:mm");
		        for (int index = 0; index < 24; index++) {
		                Date date = calendar.getTime();
		                String timeFormat = formatter.format(date);
		                String method = "t" + timeFormat.replace(":", "");
		                columnMapping.add(method);
		                calendar.add(Calendar.HOUR, 1);
		        }
		        return columnMapping.toArray(new String[0]);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public List<Grid1Hour> readGrid1HourCsv(String filePath) {
		try {
			String[] columnMapping = this.grid1HourColumnMappingAsArray();
			CsvToBean<Grid1Hour> csvToBean = new CsvToBean<Grid1Hour>();
			ColumnPositionMappingStrategy<Grid1Hour> strategy = new ColumnPositionMappingStrategy<Grid1Hour>();
			strategy.setType(Grid1Hour.class);
			strategy.setColumnMapping(columnMapping);
			List<Grid1Hour> list = null;
			CSVReader reader = null;
			FileReader fileReader = null;
			try {
				fileReader = new FileReader(filePath);
				reader = new CSVReader(fileReader);
				list = csvToBean.parse(strategy, reader);
			} catch (Exception e) {
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
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public Map<String, String> grid15MinColumnMapping() {
		Map<String, String> columnMapping = new HashMap<String, String>();
		columnMapping.put("Date", "date");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		for (int index = 0; index <= 95; index++) {
			Date date = calendar.getTime();
			String timeFormat = formatter.format(date);
			String method = "t" + timeFormat.replace(":", "");
			columnMapping.put(timeFormat, method);
			calendar.add(Calendar.MINUTE, 15);
		}
		return columnMapping;
	}

	private Map<String, String> grid1HourColumnMapping() {
		Map<String, String> columnMapping = new HashMap<String, String>();
		columnMapping.put("Date", "date");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		SimpleDateFormat formatter = new SimpleDateFormat("H:mm");
		for (int index = 0; index < 24; index++) {
			Date date = calendar.getTime();
			String timeFormat = formatter.format(date);
			String method = "t" + timeFormat.replace(":", "");
			columnMapping.put(timeFormat, method);
			calendar.add(Calendar.HOUR, 1);
		}
		return columnMapping;
	}

	public List<String> getGrid15MinHeader(String filePath) {
		logger.info("filePath = " + filePath);
		List<String> headers = new ArrayList<String>();
		Map<String, String> columnMapping = this.grid15MinColumnMapping();
		HeaderColumnNameTranslateMappingStrategy<Grid15Min> strategy = new HeaderColumnNameTranslateMappingStrategy<Grid15Min>();
		strategy.setType(Grid15Min.class);
		strategy.setColumnMapping(columnMapping);
		CSVReader headerReader = null;
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(filePath);
			headerReader = new CSVReader(fileReader);
			String[] nextLine;
			int index = 0;
			while ((nextLine = headerReader.readNext()) != null) {
				if (index == 0) {
					for (String header : nextLine) {
						// logger.info(header);
						headers.add(header);
					}
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				headerReader.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			try {
				fileReader.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return headers;
	}

	public List<String> getGrid1HourHeader(String filePath) {
		logger.info("filePath = " + filePath);
		List<String> headers = new ArrayList<String>();
		Map<String, String> columnMapping = this.grid1HourColumnMapping();
		HeaderColumnNameTranslateMappingStrategy<Grid1Hour> strategy = new HeaderColumnNameTranslateMappingStrategy<Grid1Hour>();
		strategy.setType(Grid1Hour.class);
		strategy.setColumnMapping(columnMapping);
		CSVReader headerReader = null;
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(filePath);
			headerReader = new CSVReader(fileReader);
			String[] nextLine;
			int index = 0;
			while ((nextLine = headerReader.readNext()) != null) {
				if (index == 0) {
					for (String header : nextLine) {
						// logger.info(header);
						headers.add(header);
					}
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				headerReader.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			try {
				fileReader.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return headers;
	}

	public List<String> getColumn15MinHeader(String localDataFilePath) {
		logger.info("filePath = " + localDataFilePath);
		List<String> headers = new ArrayList<String>();
		Map<String, String> columnMapping = this.grid1HourColumnMapping();
		HeaderColumnNameTranslateMappingStrategy<Column15Min> strategy = new HeaderColumnNameTranslateMappingStrategy<Column15Min>();
		strategy.setType(Column15Min.class);
		strategy.setColumnMapping(columnMapping);
		CSVReader headerReader = null;
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(localDataFilePath);
			headerReader = new CSVReader(fileReader);
			String[] nextLine;
			int index = 0;
			while ((nextLine = headerReader.readNext()) != null) {
				if (index == 0) {
					for (String header : nextLine) {
						// logger.info(header);
						headers.add(header);
					}
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				headerReader.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			try {
				fileReader.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return headers;
	}

	public Map<String, String> column15MinMapping() {
		Map<String, String> columnMapping = new HashMap<String, String>();
		columnMapping.put("TS", "ts");
		columnMapping.put("Header", "header");
		return columnMapping;
	}

	public List<Column15Min> readColumn15MinCsv(String localDataFilePath) {
		CsvToBean<Column15Min> csvToBean = new CsvToBean<Column15Min>();

		ColumnPositionMappingStrategy<Column15Min> mappingStrategy = 
        		new ColumnPositionMappingStrategy<Column15Min>();
        mappingStrategy.setType(Column15Min.class);

        String[] columns = new String[]{"ts","header"};
        mappingStrategy.setColumnMapping(columns);
		List<Column15Min> list = null;
		CSVReader reader = null;
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(localDataFilePath);
			reader = new CSVReaderBuilder(fileReader)
            .withSkipLines(1)
            .build();
			list = csvToBean.parse(mappingStrategy, reader);
		} catch (Exception e) {
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

	public Map<String, String> column1HourMapping() {
		Map<String, String> columnMapping = new HashMap<String, String>();
		columnMapping.put("TS", "ts");
		columnMapping.put("value", "value");
		return columnMapping;
	}

	public List<String> getColumn1HourHeader(String localDataFilePath) {
		logger.info("filePath = " + localDataFilePath);
		List<String> headers = new ArrayList<String>();
		Map<String, String> columnMapping = this.column1HourMapping();
		HeaderColumnNameTranslateMappingStrategy<Column1Hour> strategy = new HeaderColumnNameTranslateMappingStrategy<Column1Hour>();
		strategy.setType(Column1Hour.class);
		strategy.setColumnMapping(columnMapping);
		CSVReader headerReader = null;
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(localDataFilePath);
			headerReader = new CSVReader(fileReader);
			String[] nextLine;
			int index = 0;
			while ((nextLine = headerReader.readNext()) != null) {
				if (index == 0) {
					for (String header : nextLine) {
						// logger.info(header);
						headers.add(header);
					}
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				headerReader.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			try {
				fileReader.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return headers;
	}

	public List<Column1Hour> readColumn1HourCsv(String localDataFilePath) {
		CsvToBean<Column1Hour> csvToBean = new CsvToBean<Column1Hour>();
		ColumnPositionMappingStrategy<Column1Hour> mappingStrategy = 
        		new ColumnPositionMappingStrategy<Column1Hour>();
        mappingStrategy.setType(Column1Hour.class);

        String[] columns = new String[]{"ts","value"};
        mappingStrategy.setColumnMapping(columns);
		List<Column1Hour> list = null;
		CSVReader reader = null;
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(localDataFilePath);
			reader = new CSVReaderBuilder(fileReader)
            .withSkipLines(1)
            .build();
			list = csvToBean.parse(mappingStrategy, reader);
		} catch (Exception e) {
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

	public Date getSecondSundayOfMarch(int year, String timeZone) {
		Calendar cal = Calendar.getInstance();
		//cal.setTimeZone(TimeZone.getTimeZone(timeZone));
		cal.set(Calendar.MONTH, Calendar.MARCH);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 2);
		return cal.getTime();
	}

	public Date getFirstSundayOfNovember(int year, String timeZone) {
		Calendar cal = Calendar.getInstance();
		//cal.setTimeZone(TimeZone.getTimeZone(timeZone));
		cal.set(Calendar.MONTH, Calendar.NOVEMBER);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
		return cal.getTime();
	}
}
