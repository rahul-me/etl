package com.gcn.etl.helper;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.Calendar;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class DstValidationHelper {

	private static Logger logger = LogManager.getLogger(DstValidationHelper.class);

	public boolean isDateDstInSecondSundayOfMarch(String startDateInStr, int year, String timeZone) {
		try {
			ZoneId zoneId = ZoneId.of(timeZone);
			ZoneRules rules = zoneId.getRules();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.YEAR, year);
			Instant ist = cal.getTime().toInstant();
			ZoneOffsetTransition nextTransition = rules.nextTransition(ist);
			if(nextTransition != null){
				ZonedDateTime zdt = nextTransition.getInstant().atZone(zoneId);
				if(zdt!=null){
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
					Date dstDate = Date.from(zdt.toInstant());
					String dstDateInStr = sdf.format(dstDate);
					String dateTime[] = startDateInStr.split(" ");
					String dateStr = dateTime[0];
					//logger.info("dstDateInStr = " + dstDateInStr + " timestamp = " + dateStr);
					if (dstDateInStr.equals(dateStr)) {
						String time[] = dateTime[1].split(":");
						int hour = Integer.parseInt(time[0]);
						logger.debug("dstDateInStr = " + dstDateInStr + " startDateInStr = " + startDateInStr + " hour = " + hour
								+ " dateStr = " + dateStr);
						if (hour == 2) {
							return true;
						}
					}
				}
			}
			
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	public boolean isDateDstInFirstOfNov(Date startDate, String timestamp, int year, String timeZone) {
		try {
			ZoneId zoneId = ZoneId.of(timeZone);
			ZoneRules rules = zoneId.getRules();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.YEAR, year);
			Instant ist = cal.getTime().toInstant();
			ZoneOffsetTransition nextTransition = rules.nextTransition(ist);
			nextTransition = rules.nextTransition(nextTransition.getInstant());
			ZonedDateTime zdt = nextTransition.getInstant().atZone(zoneId);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			Date dstDate = Date.from(zdt.toInstant());
			String startDateInStr = sdf.format(startDate);
			String dstDateInStr = sdf.format(dstDate);
			String dateTime[] = timestamp.split(" ");
			String dateStr = dateTime[0];
			//logger.info("dstDate = " + dstDate + " startDateInStr = " + startDateInStr + "timestamp = " + dateStr);
			if (dstDateInStr.equals(startDateInStr)) {
				String time[] = dateTime[1].split(":");
				int hour = Integer.parseInt(time[0]);
				logger.debug("dstDateInStr = " + dstDateInStr + " timestamp = " + timestamp + " hour = " + hour
						+ " dateStr = " + dateStr);
				if (hour == 2) {
					return true;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}
}
