package com.ds.proserv.feign.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.DecorateOutputType;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.common.util.LambdaUtilities;
import com.ds.proserv.feign.report.domain.DecorateOutput;
import com.ds.proserv.feign.report.domain.ManageDataAPI;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportDataManageUtil {

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> readReportData(List<Map<String, Object>> selectDataMapList,
			Map<String, String> columnNameHeaderMap, Map<String, String> originalColumnNameHeaderMap,
			ManageDataAPI csvReportDataExport) {

		log.info("Total Fetched rows are {}", selectDataMapList.size());
		List<DecorateOutput> decorateOutputList = csvReportDataExport.getDecorateOutput();

		List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>(selectDataMapList.size());
		List<String> removeMultiValuedHeaderList = new ArrayList<String>();

		selectDataMapList.forEach(selectDataMap -> {

			Map<String, Object> csvDataMap = new HashMap<String, Object>(selectDataMap.size());
			selectDataMap.forEach((key, value) -> {

				key = key.toLowerCase();

				Object decoratedOutput = formatOutput(decorateOutputList, value, key);

				if (null != decoratedOutput && decoratedOutput instanceof List) {

					List<String> columnValueList = (List<String>) decoratedOutput;

					if (null != columnValueList && !columnValueList.isEmpty()) {

						for (int i = 0; i < columnValueList.size(); i++) {

							csvDataMap.put(originalColumnNameHeaderMap.get(key)
									+ AppConstants.RESTRICTED_CHARACTER_REPLACEMENT + i, columnValueList.get(i));
							columnNameHeaderMap.put(
									originalColumnNameHeaderMap.get(key) + AppConstants.RESTRICTED_CHARACTER_REPLACEMENT
											+ i,
									originalColumnNameHeaderMap.get(key) + AppConstants.RESTRICTED_CHARACTER_REPLACEMENT
											+ i);

						}

						prepareRemoveMultiValuedHeaderList(columnNameHeaderMap, removeMultiValuedHeaderList, key);

					}

				} else if (null != decoratedOutput && decoratedOutput instanceof Map) {

					Map<String, String> keyValueMap = (Map<String, String>) decoratedOutput;

					if (null != keyValueMap && !keyValueMap.isEmpty()) {

						for (Map.Entry<String, String> mapEntry : keyValueMap.entrySet()) {

							csvDataMap.put(originalColumnNameHeaderMap.get(key)
									+ AppConstants.RESTRICTED_CHARACTER_REPLACEMENT + mapEntry.getKey(),
									mapEntry.getValue());
							columnNameHeaderMap.put(
									originalColumnNameHeaderMap.get(key) + AppConstants.RESTRICTED_CHARACTER_REPLACEMENT
											+ mapEntry.getKey(),
									originalColumnNameHeaderMap.get(key) + AppConstants.RESTRICTED_CHARACTER_REPLACEMENT
											+ mapEntry.getKey());

						}

						prepareRemoveMultiValuedHeaderList(columnNameHeaderMap, removeMultiValuedHeaderList, key);
					}
				} else {

					csvDataMap.put(columnNameHeaderMap.get(key), decoratedOutput);
				}

			});

			rowList.add(csvDataMap);
		});

		// Remove unused header
		Iterator<Entry<String, String>> colHeaderMapIterator = columnNameHeaderMap.entrySet().iterator();
		while (colHeaderMapIterator.hasNext()) {

			Entry<String, String> mapEntry = colHeaderMapIterator.next();

			if (log.isDebugEnabled()) {

				log.debug("removeMultiValuedHeaderList is {}, mapKey is {}", removeMultiValuedHeaderList,
						mapEntry.getKey());
			}
			if (null != removeMultiValuedHeaderList && !removeMultiValuedHeaderList.isEmpty()
					&& removeMultiValuedHeaderList.contains(mapEntry.getKey())) {

				if (log.isDebugEnabled()) {

					log.debug("Removing Key {} and HeaderName as {}", mapEntry.getKey(), mapEntry.getValue());
				}
				colHeaderMapIterator.remove();
			}
		}

		return checkColumnsToRow(decorateOutputList, rowList);

	}

	private static List<Map<String, Object>> checkColumnsToRow(List<DecorateOutput> decorateOutputList,
			List<Map<String, Object>> rowList) {

		if (null != decorateOutputList && !decorateOutputList.isEmpty() && null != rowList && !rowList.isEmpty()) {

			List<DecorateOutput> decorateOutputFilterList = decorateOutputList.stream().filter(
					output -> DecorateOutputType.SPLITCOLUMNTOROW.toString().equalsIgnoreCase(output.getOutputType()))
					.collect(Collectors.toList());

			if (null != decorateOutputFilterList && !decorateOutputFilterList.isEmpty()) {

				for (DecorateOutput decorateOutput : decorateOutputFilterList) {

					log.info("For decorateOutput Col Name -> {}, rowList size before listIterator is {}",
							decorateOutput.getDbColumnName(), rowList.size());
					ListIterator<Map<String, Object>> rowListIterator = rowList.listIterator();
					while (rowListIterator.hasNext()) {

						Map<String, Object> rowColKeyValueMap = rowListIterator.next();

						rowColKeyValueMap.forEach((columnName, dbValue) -> {

							formatColumnToRowOutput(decorateOutputList, dbValue, columnName, rowListIterator,
									rowColKeyValueMap);
						});

					}

					log.info("For decorateOutput Col Name -> {}, rowList size after listIterator is {}",
							decorateOutput.getDbColumnName(), rowList.size());
				}

			}
		}

		return rowList;
	}

	private static List<Map<String, Object>> formatColumnToRowOutput(List<DecorateOutput> decorateOutputList,
			Object dbValue, String columnName, ListIterator<Map<String, Object>> rowListIterator,
			Map<String, Object> rowColKeyValueMap) {

		List<Map<String, Object>> newList = null;
		DecorateOutput decorateOutput = decorateOutputList.stream()
				.filter(output -> columnName.equalsIgnoreCase(output.getDbColumnName())).findAny().orElse(null);

		if (null != decorateOutput && null != dbValue && !StringUtils.isEmpty(decorateOutput.getOutputType())) {

			DecorateOutputType decorateOutputTypeEnum = EnumUtils.getEnum(DecorateOutputType.class,
					decorateOutput.getOutputType().toUpperCase());

			switch (decorateOutputTypeEnum) {

			case SPLITCOLUMNTOROW:

				String columnDelimiter = decorateOutput.getOutputDelimiter();

				if (StringUtils.isEmpty(columnDelimiter)) {

					columnDelimiter = AppConstants.COMMA_DELIMITER;
				}

				if (((String) dbValue).contains(columnDelimiter)) {

					Set<Entry<String, Object>> entries = rowColKeyValueMap.entrySet();
					HashMap<String, Object> shallowCopy = (HashMap<String, Object>) entries.stream()
							.collect(LambdaUtilities.toMapWithNullValues(Map.Entry::getKey, Map.Entry::getValue));

					List<String> dbValueList = Stream.of(((String) dbValue).split(columnDelimiter))
							.collect(Collectors.toList());

					rowListIterator.remove();// Removing old row which has columns

					for (String dbValueAfterSplit : dbValueList) {

						if (log.isDebugEnabled()) {

							log.debug("dbValue is {} and dbValueAfterSplit is {}", dbValue, dbValueAfterSplit);
						}
						shallowCopy.put(columnName, dbValueAfterSplit);
						rowListIterator.add(shallowCopy);// Add new rows with single value per column

						shallowCopy = (HashMap<String, Object>) shallowCopy.entrySet().stream()
								.collect(LambdaUtilities.toMapWithNullValues(Map.Entry::getKey, Map.Entry::getValue));
					}
				}

				break;

			default:
				log.warn(
						" ################################## Wrong OutputType -> {} as option in formatColumnToRowOutput ################################## ",
						decorateOutput.getOutputType());
			}
		}

		return newList;
	}

	private static void prepareRemoveMultiValuedHeaderList(Map<String, String> columnNameHeaderMap,
			List<String> removeMultiValuedHeaderList, String key) {

		if (!removeMultiValuedHeaderList.contains(key)) {
			removeMultiValuedHeaderList.add(key);
		}

		String headerValue = columnNameHeaderMap.get(key);
		if (!removeMultiValuedHeaderList.contains(headerValue)) {
			removeMultiValuedHeaderList.add(headerValue);
		}
	}

	private static Object formatOutput(List<DecorateOutput> decorateOutputList, Object dbValue, String columnName) {

		if (null != decorateOutputList && !decorateOutputList.isEmpty()) {

			DecorateOutput decorateOutput = decorateOutputList.stream()
					.filter(output -> columnName.equalsIgnoreCase(output.getDbColumnName())).findAny().orElse(null);

			if (null != decorateOutput && null != dbValue && !StringUtils.isEmpty(decorateOutput.getOutputType())) {

				DecorateOutputType decorateOutputTypeEnum = EnumUtils.getEnum(DecorateOutputType.class,
						decorateOutput.getOutputType().toUpperCase());

				switch (decorateOutputTypeEnum) {

				case DATEASEPOCHTIME:
					return DateTimeUtil.convertToLocalDateFromEpochTimeInSecs(Long.valueOf(dbValue + ""),
							decorateOutput.getOutputDateZone(), decorateOutput.getOutputDatePattern());
				case DATETIMEASEPOCHTIME:
					return DateTimeUtil.convertToLocalDateTimeFromEpochTimeInSecs(Long.valueOf(dbValue + ""),
							decorateOutput.getOutputDateZone(), decorateOutput.getOutputDatePattern());

				case DATE:

					String inputDatePattern = decorateOutput.getInputDatePattern();

					if (StringUtils.isEmpty(inputDatePattern)) {

						inputDatePattern = "yyyy-MM-dd";
					}

					LocalDate ldate = LocalDate.parse((String) dbValue, DateTimeFormatter.ofPattern(inputDatePattern));
					return DateTimeFormatter.ofPattern(decorateOutput.getOutputDatePattern()).format(ldate);

				case DATETIME:

					String inputDateTimePattern = decorateOutput.getInputDatePattern();

					if (StringUtils.isEmpty(inputDateTimePattern)) {

						inputDateTimePattern = "yyyy-MM-dd HH:mm:ss";
					}

					LocalDateTime ldateTime = LocalDateTime.parse((String) dbValue,
							DateTimeFormatter.ofPattern(inputDateTimePattern));
					if (!StringUtils.isEmpty(decorateOutput.getOutputDateZone())) {

						ldateTime.atZone(TimeZone.getTimeZone(decorateOutput.getOutputDateZone()).toZoneId());
					}

					return DateTimeFormatter.ofPattern(decorateOutput.getOutputDatePattern()).format(ldateTime);
				case ARRAY:

					List<String> columnValueList = null;
					String delimiter = decorateOutput.getOutputDelimiter();
					if (!StringUtils.isEmpty(delimiter)) {

						columnValueList = Stream.of(((String) dbValue).split(delimiter)).collect(Collectors.toList());
					}

					return columnValueList;
				case ARRAYMAP:

					Map<String, String> keyValueMap = new HashMap<String, String>();
					String outputDelimiter = decorateOutput.getOutputDelimiter();
					String keyValueDelimiter = decorateOutput.getKeyValueDelimiter();
					if (!StringUtils.isEmpty(outputDelimiter) && !StringUtils.isEmpty(keyValueDelimiter)) {

						List<String> keyValueList = Stream.of(((String) dbValue).split(outputDelimiter))
								.collect(Collectors.toList());
						for (String keyValue : keyValueList) {

							String key = null;
							String value = null;
							String[] keyValueSplitArr = keyValue.split(keyValueDelimiter);
							if (null != keyValueSplitArr && keyValueSplitArr.length == 2) {
								key = keyValueSplitArr[0];
								value = keyValueSplitArr[1];

							} else {
								key = keyValueSplitArr[0];
							}

							keyValueMap.put(key, value);
						}
					}

					return keyValueMap;
				default:
					log.warn(
							" ################################## Wrong OutputType -> {} as option in formatOutput ################################## ",
							decorateOutput.getOutputType());
				}
			}
		}

		return dbValue;
	}

	public static String prepareSelectSql(ManageDataAPI csvReportDataExport, String selectSql) {

		if (!StringUtils.isEmpty(csvReportDataExport.getWhereClause())) {

			selectSql = selectSql + " " + csvReportDataExport.getWhereClause();
		}

		if (!StringUtils.isEmpty(csvReportDataExport.getOrderByClause())) {

			selectSql = selectSql + " " + csvReportDataExport.getOrderByClause();
		}
		return selectSql;
	}

	public static Map<String, Object> formatPathParam(Map<String, Object> inputParams,
			ManageDataAPI csvReportDataExport) {

		return PathParamUtil.prepareParamValues(inputParams, csvReportDataExport.getSqlParams());
	}

}