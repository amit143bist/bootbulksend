package com.ds.proserv.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StringUtils;

import com.ds.proserv.common.exception.InvalidInputException;

public class DateTimeUtil {

	public static String DATE_PATTERN = "yyyy-MM-dd";

	public static String FILE_DATE_PATTERN = "yyyyMMddHHmm";

	public static String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	public static String DATE_TIME_PATTERN_XML = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS";

	public static String DATE_TIME_PATTERN_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	public static String DATE_TIME_PATTERN_NANO = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'";

	public static boolean isValidDate(String dateAsString) {

		LocalDate.parse(dateAsString, DateTimeFormatter.ofPattern(DATE_PATTERN));

		return true;
	}

	public static boolean isValidDateTime(String dateTimeAsString) {

		DateTimeUtil.convertToLocalDateTime(dateTimeAsString);

		return true;
	}

	public static boolean isValidDateTimeByPatternNano(String dateTimeAsString) {

		try {

			LocalDateTime.parse(dateTimeAsString, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN_NANO));

		} catch (Exception exp) {

			return false;
		}

		return true;
	}

	public static boolean isValidDateTimeByPattern(String dateTimeAsString, String dateTimePattern) {

		DateTimeUtil.convertToLocalDateTimeByPattern(dateTimeAsString, dateTimePattern);

		return true;
	}

	public static Long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {

		long diffInMillies = date2.getTime() - date1.getTime();
		return timeUnit.convert(diffInMillies, timeUnit);
	}

	public static Long addHoursAndconvertToEpochTime(String dateTimeAsString, Integer numberOfHours) {

		Long epochTime = LocalDateTime.parse(dateTimeAsString, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
				.plusHours(numberOfHours).toEpochSecond(ZoneOffset.UTC);

		return epochTime;
	}

	public static Long addSecondsAndconvertToEpochTime(String dateTimeAsString, Integer numberOfSeconds) {

		Long epochTime = LocalDateTime.parse(dateTimeAsString, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
				.plusSeconds(numberOfSeconds).toEpochSecond(ZoneOffset.UTC);

		return epochTime;
	}

	public static Long convertToEpochTimeFromDateTimeAsString(String dateTimeAsString) {

		return convertToEpochTimeFromDateTimeAsString(dateTimeAsString, null);
	}

	public static Long convertToEpochTimeFromDateAsString(String dateTimeAsString, String datePattern) {

		if (StringUtils.isEmpty(datePattern)) {
			datePattern = DATE_PATTERN;
		}
		Long epochTimeInSeconds = LocalDate.parse(dateTimeAsString, DateTimeFormatter.ofPattern(datePattern))
				.atStartOfDay(TimeZone.getTimeZone("UTC").toZoneId()).toInstant().getEpochSecond();

		return epochTimeInSeconds;
	}

	public static String convertToSQLDateFromDateTimeAsString(String dateTimeAsString, String dateTimePattern) {

		if (StringUtils.isEmpty(dateTimePattern)) {

			dateTimePattern = DATE_PATTERN;
		}

		return LocalDate.parse(dateTimeAsString, DateTimeFormatter.ofPattern(dateTimePattern)).toString();
	}

	public static String convertToISOLocalDate(String dateTimeAsString, String dateTimePattern) {

		if (StringUtils.isEmpty(dateTimePattern)) {

			dateTimePattern = DATE_TIME_PATTERN_NANO;
		}

		return DateTimeFormatter.ISO_LOCAL_DATE
				.format(LocalDate.parse(dateTimeAsString, DateTimeFormatter.ofPattern(dateTimePattern)));
	}

	public static Long convertToEpochTimeFromDateTimeAsString(String dateTimeAsString, String dateTimePattern) {

		if (StringUtils.isEmpty(dateTimePattern)) {

			dateTimePattern = DATE_TIME_PATTERN;
		}
		Long epochTimeInSeconds = LocalDateTime.parse(dateTimeAsString, DateTimeFormatter.ofPattern(dateTimePattern))
				.toEpochSecond(ZoneOffset.UTC);

		return epochTimeInSeconds;
	}

	public static String convertToSQLDateTimeFromDateTimeAsString(String dateTimeAsString, String dateTimePattern) {

		if (StringUtils.isEmpty(dateTimePattern)) {

			dateTimePattern = DATE_TIME_PATTERN_NANO;
		}

		return LocalDateTime.parse(dateTimeAsString, DateTimeFormatter.ofPattern(dateTimePattern)).toString();
	}

	public static String convertToISOLocalDateTime(String dateTimeAsString, String dateTimePattern) {

		if (StringUtils.isEmpty(dateTimePattern)) {

			dateTimePattern = DATE_TIME_PATTERN_NANO;
		}

		return DateTimeFormatter.ISO_LOCAL_DATE_TIME
				.format(LocalDateTime.parse(dateTimeAsString, DateTimeFormatter.ofPattern(dateTimePattern)));
	}

	public static String currentTimeInString() {

		LocalDateTime dateTime = LocalDateTime.now(TimeZone.getTimeZone("UTC").toZoneId());

		if (null != dateTime) {

			return dateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
		}

		return null;
	}

	public static String currentLocalDateInString() {

		LocalDateTime dateTime = LocalDateTime.now();

		if (null != dateTime) {

			return dateTime.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
		}

		return null;
	}

	public static String currentDateInString(String timeZoneId) {

		if (StringUtils.isEmpty(timeZoneId)) {
			timeZoneId = "UTC";
		}

		LocalDateTime dateTime = LocalDateTime.now(TimeZone.getTimeZone(timeZoneId).toZoneId());

		if (null != dateTime) {

			return dateTime.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
		}

		return null;
	}

	public static String currentTimeInString(String timeZone) {

		LocalDateTime dateTime = convertToLocalDateTimeFromEpochTimeInSecs(currentEpochTime(), timeZone);
		if (null != dateTime) {
			return dateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
		}

		return null;
	}

	public static Long currentEpochTime() {

		Long epochTime = LocalDateTime.now(TimeZone.getTimeZone("UTC").toZoneId()).toEpochSecond(ZoneOffset.UTC);

		return epochTime;
	}

	public static LocalDateTime convertToLocalDateTime(String dateTimeAsString) {

		return Optional.ofNullable(dateTimeAsString).map(str -> {

			return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
		}).orElseThrow(() -> new InvalidInputException(dateTimeAsString + " is null"));
	}

	public static String convertToString(LocalDateTime dateTime) {

		if (null != dateTime) {
			return dateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
		}

		return null;
	}

	public static LocalDateTime convertToLocalDateTimeByPattern(String dateTimeAsString, String dateTimePattern) {

		return Optional.ofNullable(dateTimeAsString).map(str -> {

			return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(dateTimePattern));
		}).orElseThrow(() -> new InvalidInputException(dateTimeAsString + " is null"));
	}

	public static String convertToStringByPattern(LocalDateTime dateTime, String dateTimePattern) {

		if (null != dateTime) {
			return dateTime.format(DateTimeFormatter.ofPattern(dateTimePattern));
		}

		return null;
	}

	public static String convertToStringFromEpochTimeInSecs(Long epochTimeInSeconds) {

		LocalDateTime dateTime = convertToLocalDateTimeFromEpochTimeInSecs(epochTimeInSeconds, null);

		if (null != dateTime) {

			return dateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
		}

		return null;
	}

	// UTC or CST or PST
	public static LocalDateTime convertToLocalDateTimeFromEpochTimeInSecs(Long epochTimeInSeconds, String timeZone) {

		LocalDateTime localDateTime = null;

		if (!StringUtils.isEmpty(timeZone)) {

			localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTimeInSeconds),
					TimeZone.getTimeZone(timeZone).toZoneId());
		} else {
			localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTimeInSeconds),
					TimeZone.getTimeZone("UTC").toZoneId());
		}

		return localDateTime;
	}

	// UTC or CST or PST
	public static String convertToLocalDateTimeFromEpochTimeInSecs(Long epochTimeInSeconds, String timeZone,
			String datePattern) {

		LocalDateTime localDateTime = null;

		if (!StringUtils.isEmpty(timeZone)) {

			localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTimeInSeconds),
					TimeZone.getTimeZone(timeZone).toZoneId());
		} else {
			localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTimeInSeconds),
					TimeZone.getTimeZone("UTC").toZoneId());
		}

		if (StringUtils.isEmpty(datePattern)) {

			return localDateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
		} else {

			return localDateTime.format(DateTimeFormatter.ofPattern(datePattern));
		}

	}

	// UTC or CST or PST
	public static String convertToLocalDateFromEpochTimeInSecs(Long epochTimeInSeconds) {

		LocalDateTime localDateTime = convertToLocalDateTimeFromEpochTimeInSecs(epochTimeInSeconds, null);

		return localDateTime.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
	}

	// UTC or CST or PST
	public static String convertToLocalDateFromEpochTimeInSecs(Long epochTimeInSeconds, String timeZone) {

		LocalDateTime localDateTime = convertToLocalDateTimeFromEpochTimeInSecs(epochTimeInSeconds, timeZone);

		return localDateTime.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
	}

	public static String convertToLocalDateFromEpochTimeInSecs(Long epochTimeInSeconds, String timeZone,
			String datePattern) {

		LocalDateTime localDateTime = convertToLocalDateTimeFromEpochTimeInSecs(epochTimeInSeconds, timeZone);

		if (StringUtils.isEmpty(datePattern)) {

			return localDateTime.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
		} else {

			return localDateTime.format(DateTimeFormatter.ofPattern(datePattern));
		}
	}

	public static LocalDateTime checkAndconvertToLocalDateTime(String fromDate) {

		LocalDateTime startDateTime = null;
		if (DateTimeUtil.isValidDateTimeByPatternNano(fromDate)) {

			startDateTime = LocalDateTime.parse(fromDate,
					DateTimeFormatter.ofPattern(DateTimeUtil.DATE_TIME_PATTERN_NANO));
		} else {

			startDateTime = LocalDateTime.parse(fromDate);
		}

		return startDateTime;
	}

}