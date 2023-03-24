package com.ds.proserv.feign.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.domain.DateRange;

public class DateRangeUtil {

	public static DateRange createDateRange(String fromDate, String toDate) {

		LocalDateTime startDateTime = null;
		if (DateTimeUtil.isValidDateTimeByPatternNano(fromDate)) {

			startDateTime = LocalDateTime.parse(fromDate,
					DateTimeFormatter.ofPattern(DateTimeUtil.DATE_TIME_PATTERN_NANO));
		} else {

			startDateTime = LocalDateTime.parse(fromDate);
		}

		LocalDateTime endDateTime = null;
		if (DateTimeUtil.isValidDateTimeByPatternNano(toDate)) {

			endDateTime = LocalDateTime.parse(toDate, DateTimeFormatter.ofPattern(DateTimeUtil.DATE_TIME_PATTERN_NANO));
		} else {

			endDateTime = LocalDateTime.parse(toDate);
		}

		return new DateRange(startDateTime, endDateTime);
	}
	
}