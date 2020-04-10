package com.gcn.etl.helper;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gcn.etl.constant.Constant;
import com.gcn.etl.propertiesHelper.AppConfigProperties;

@Component
public class AutoDetectHelper {
	
	private static final Logger logger = LogManager.getLogger(AutoDetectHelper.class);
	
	@Autowired
	private AppConfigProperties config;

	public boolean isTimeData(String data) {
		String timePartSeparator = config.getTimePartSeparator();
		if (data != null) {
			if (data.contains(timePartSeparator)) {
				String timeParts[] = data.split(timePartSeparator);
				if (timeParts != null && timeParts.length > 1) {
					for (String timepart : timeParts) {
						try {
							Integer.parseInt(timepart);
						} catch (NumberFormatException e) {
							return false;
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	public long getTimeInMillies(String time) {
		String[] timeParts = time.split(config.getTimePartSeparator());
		Calendar calendar = getCalendarWithDefaultTime();
		for (int i = 0; i < timeParts.length; i++) {
			setTimePart(i, Integer.parseInt(timeParts[i]), calendar);
		}
		return calendar.getTimeInMillis();
	}
	
	public void setTimeInMillies(String time, Calendar calendar) {
		String[] timeParts = time.split(config.getTimePartSeparator());		
		for (int i = 0; i < timeParts.length; i++) {
			setTimePart(i, Integer.parseInt(timeParts[i]), calendar);
		}
	}

	public String getTimeFormat(String time) {
		String timePartSeparator = config.getTimePartSeparator().trim();
		if (time != null) {
			StringBuilder builder = new StringBuilder();
			String[] timeParts = time.split(timePartSeparator);
			for (int i = 0; i < timeParts.length; i++) {
				if (i != 0) {
					builder.append(timePartSeparator);
				}
				builder.append(getTimePartSymbol(i, timeParts[i].length()));
			}
			return builder.toString();
		}
		return null;
	}

	public String getTimePartSymbol(int index, int charLength) {
		switch (index) {
		case Constant.HOUR_OF_DAY:
			switch(charLength) {
			case 2:
				return Constant.HOUR_OF_DAY_HH;
			case 1:
				return Constant.HOUR_OF_DAY_H;
			}
		case Constant.MINUTE:
			switch(charLength) {
			case 2:
				return Constant.MINUTE_MM;
			case 1:
				return Constant.MINUTE_M;
			}			
		case Constant.SECOND:
			switch(charLength) {
			case 2:
				return Constant.SECOND_SS;
			case 1:
				return Constant.SECOND_S;
			}			
		case Constant.MILLISECOND:
			return Constant.MILLISECOND_SYM;
		}
		return null;
	}

	private Calendar setTimePart(int index, int timePart, Calendar calendar) {

		switch (index) {
		case Constant.HOUR_OF_DAY:
			calendar.set(Calendar.HOUR_OF_DAY, timePart);
			break;
		case Constant.MINUTE:
			calendar.set(Calendar.MINUTE, timePart);
			break;
		case Constant.SECOND:
			calendar.set(Calendar.SECOND, timePart);
			break;
		case Constant.MILLISECOND:
			calendar.set(Calendar.MILLISECOND, timePart);
			break;
		}
		return calendar;
	}

	public Calendar getCalendarWithDefaultTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, Constant.DEFAULT_INT);
		calendar.set(Calendar.MINUTE, Constant.DEFAULT_INT);
		calendar.set(Calendar.SECOND, Constant.DEFAULT_INT);
		calendar.set(Calendar.MILLISECOND, Constant.DEFAULT_INT);
		return calendar;
	}

	public boolean isDateData(String data) {
		String datePartSeparator = config.getDatePartSeparator();
		if (data != null) {
			if (data.contains(datePartSeparator)) {
				String dateParts[] = data.split(datePartSeparator);
				if (dateParts != null) {
					for (String datepart : dateParts) {
						try {
							Integer.parseInt(datepart);
						} catch (NumberFormatException e) {
							return false;
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isDateTimeData(String data) {
		if (data != null) {
			data = data.trim();
			if (data.contains(Constant.SPACE_CONSTANT) && data.contains(config.getDatePartSeparator().trim())
					&& data.contains(config.getTimePartSeparator())) {
				String[] dateTimeDataParts = data.trim().split(Constant.SPACE_CONSTANT);
				return isDateData(dateTimeDataParts[0]) && isTimeData(dateTimeDataParts[1]);

			}
		}
		return false;
	}

	public String getTimePartFromDateTimeData(String data) {
		return data.split(Constant.SPACE_CONSTANT)[1];
	}
	
	public String getDatePartFromDateTimeData(String data) {
		return data.split(Constant.SPACE_CONSTANT)[0];
	}
	

	public int[] getDateParts(String data) {
		String[] date = data.split(config.getDatePartSeparator());
		try {
			int[] array = Arrays.asList(date).stream().mapToInt(Integer::parseInt).toArray();
			return array;
		} catch (Exception e) {
			return null;
		}
	}

	public Map<Integer, Integer> updateCounter(Map<Integer, Integer> indexCounter, int[] date, int[] nextDate) {
		for (int i = 0; i < date.length; i++) {
			int counter = nextDate[i] - date[i];
			if (counter > 0) {
				Integer mapCounter = indexCounter.get(i);
				if (mapCounter != null) {
					indexCounter.put(i, ++mapCounter);
				} else {
					indexCounter.put(i, 1);
				}
			}
		}
		return indexCounter;
	}

	public int getIndexIncrements(Map<Integer, Integer> indexCounter) {
		int indexIncrements = 0;
		for (Entry<Integer, Integer> entry : indexCounter.entrySet()) {
			if (entry.getValue() != null && entry.getValue() > 2) {
				indexIncrements++;
			}
		}
		return indexIncrements;
	}

	public int[] getDatePosition(Map<Integer, Integer> indexCounter) {
		int[] datePosition = new int[3];
		int maxIndex = Integer.MIN_VALUE;
		int secondMaxIndex = Integer.MIN_VALUE;
		int maxIndexCount = Integer.MIN_VALUE;
		int secondMaxIndexCount = Integer.MIN_VALUE;
		for (Entry<Integer, Integer> entry : indexCounter.entrySet()) {
			int indexCount = entry.getValue();
			if (indexCount > maxIndexCount) {
				secondMaxIndexCount = maxIndexCount;
				maxIndexCount = indexCount;
				maxIndex = entry.getKey();
			} else if (indexCount != maxIndexCount && indexCount > secondMaxIndexCount) {
				secondMaxIndex = entry.getKey();
			}
		}
		if (maxIndex != Integer.MAX_VALUE) {
			datePosition[maxIndex] = Constant.DAY;
		}
		if (secondMaxIndex != Integer.MAX_VALUE) {
			datePosition[secondMaxIndex] = Constant.MONTH;
		}

		return datePosition;
	}

	public int[] getNoOfChars(Map<Integer, Integer> defaultCharMap) {
		int[] noOfChar = new int[3];
		for (Entry<Integer, Integer> entry : defaultCharMap.entrySet()) {
			noOfChar[entry.getKey()] = entry.getValue();
		}
		return noOfChar;
	}

	public String getDateFormat(int[] datePosition, int[] noOfChar) {
		try {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < datePosition.length; i++) {
				if (i != 0) {
					builder.append(config.getDatePartSeparator().trim());
				}
				switch (datePosition[i]) {
				case Constant.YEAR:
					switch (noOfChar[i]) {
					case 2:
						builder.append(Constant.YEAR_YY);
						break;
					default:
						builder.append(Constant.YEAR_Y);
					}
					break;
				case Constant.MONTH:
					switch (noOfChar[i]) {
					case 2:
						builder.append(Constant.MONTH_MM);
						break;
					case 1:
						builder.append(Constant.MONTH_M);
						break;
					}
					break;
				case Constant.DAY:
					switch (noOfChar[i]) {
					case 2:
						builder.append(Constant.DAY_DD);
						break;
					case 1:
						builder.append(Constant.DAY_D);
						break;
					}				
					break;
				}
			}
			return builder.toString();
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
		
	}

	public Map<Integer, Integer> getDefaultIndexChar() {
		Map<Integer, Integer> map = new LinkedHashMap<>();
		map.put(Constant.DEFAULT_INT, Constant.DEFAULT_INT + 4);
		map.put(Constant.DEFAULT_INT + 1, Constant.DEFAULT_INT + 4);
		map.put(Constant.DEFAULT_INT + 2, Constant.DEFAULT_INT + 4);
		return map;
	}

	public void updateDefaultChar(Map<Integer, Integer> defaultCharMap, String date) {
		String[] dateParts = date.trim().split(config.getDatePartSeparator());
		for (int i = 0; i < dateParts.length; i++) {
			Integer number = defaultCharMap.get(i);
			int charLength = dateParts[i].trim().length();
			if (number > charLength) {
				defaultCharMap.put(i, charLength);
			}
		}
	}

}
