package com.gcn.etl.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExcelToCsvHelper {

	private static Logger logger = LogManager.getLogger(ExcelToCsvHelper.class.getName());

	public String convertGridExcelToCsv(String localDataFilePath, String orignalFileName, String timeStampFormat, String timeFormat) {
		// TODO Auto-generated method stub
		File inputFilePath = new File(localDataFilePath + "/" + orignalFileName);
		String fName = orignalFileName.substring(0, orignalFileName.lastIndexOf("."));
		String outputFilePath = localDataFilePath + "/" + fName + ".csv";
		File outputFilePathObj = new File(outputFilePath);
		StringBuffer data = new StringBuffer();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outputFilePathObj);
			// Get the workbook object for XLSX file
			XSSFWorkbook wBook = new XSSFWorkbook(new FileInputStream(inputFilePath));
			// Get first sheet from the workbook
			XSSFSheet sheet = wBook.getSheetAt(0);
			Row row;
			Cell cell;
			// Iterate through each rows from first sheet
			Iterator<Row> rowIterator = sheet.iterator();
			int colIndex = 0, rowIndex = 0;
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				// For each row, iterate through each columns
				Iterator<Cell> cellIterator = row.cellIterator();
				colIndex = 0;
				while (cellIterator.hasNext()) {
					cell = cellIterator.next();
					String cellVal = cell.toString();
					SimpleDateFormat formatTime = new SimpleDateFormat(timeFormat);
					if (rowIndex == 0 && cellVal.toString().contains("31-Dec-1899")) {
						String timeStamp = formatTime.format(cell.getDateCellValue());
						data.append(timeStamp);
						data.append(",");
					} else {
						if (colIndex == 0) {
							String dt = cell.toString();
							if (dt.length() > 0 && !dt.equals("Date")) {
								DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
								Date startDate = df.parse(dt);
								SimpleDateFormat dt1 = new SimpleDateFormat(timeStampFormat);
								data.append(dt1.format(startDate));
								data.append(",");	
							} else {
								data.append(cell);
								data.append(",");
							}
						} else {
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
								data.append(cell.getNumericCellValue());
								data.append(",");
							} else {
								data.append(cell);
								data.append(",");
							}
						}
					}
					colIndex++;
				}
				data.deleteCharAt(data.length() - 1);
				data.append("\r\n");
				rowIndex++;
			}
			fos.write(data.toString().getBytes());
			return outputFilePath;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return outputFilePath;
	}

	public String convertColumnExcelToCsv(String localDataFilePath, String orignalFileName, String timeStampFormat) {
		// TODO Auto-generated method stub
		File inputFilePath = new File(localDataFilePath + "/" + orignalFileName);
		String fName = orignalFileName.substring(0, orignalFileName.lastIndexOf("."));
		String outputFilePath = localDataFilePath + "/" + fName + ".csv";
		File outputFilePathObj = new File(outputFilePath);
		StringBuffer data = new StringBuffer();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outputFilePathObj);
			// Get the workbook object for XLSX file
			XSSFWorkbook wBook = new XSSFWorkbook(new FileInputStream(inputFilePath));
			// Get first sheet from the workbook
			XSSFSheet sheet = wBook.getSheetAt(0);
			Row row;
			Cell cell;
			// Iterate through each rows from first sheet
			Iterator<Row> rowIterator = sheet.iterator();
			int colIndex = 0, rowIndex = 0;
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				// For each row, iterate through each columns
				Iterator<Cell> cellIterator = row.cellIterator();
				colIndex = 0;
				while (cellIterator.hasNext()) {
					cell = cellIterator.next();
					String cellVal = cell.toString();
					// logger.info(cell);
					if (rowIndex == 0) {
						data.append(cellVal);
						data.append(",");
					} else {
						if (colIndex == 0) {
							Date dt = cell.getDateCellValue();
							if (dt != null) {
								SimpleDateFormat dt1 = new SimpleDateFormat(timeStampFormat);
								data.append(dt1.format(dt));
								data.append(",");
							} else {
								data.append(cell);
								data.append(",");
							}
						} else {
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
								data.append(cell.getNumericCellValue());
								data.append(",");
							} else {
								data.append(cell);
								data.append(",");
							}
						}
					}
					colIndex++;
				}
				data.deleteCharAt(data.length() - 1);
				data.append("\r\n");
				rowIndex++;
			}
			fos.write(data.toString().getBytes());
			return outputFilePath;
		} catch (Exception ioe) {
			logger.error(ioe);
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return outputFilePath;
	}
}
