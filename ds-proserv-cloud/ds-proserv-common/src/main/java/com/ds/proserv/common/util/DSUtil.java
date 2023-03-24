package com.ds.proserv.common.util;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.ConnectProcessorType;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class DSUtil {

	// get envelopeId from string using regex
	public static String getEnvelopeId(String xml) {

		final Pattern pattern = Pattern.compile("<EnvelopeID>(.+?)</EnvelopeID>", Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(xml);
		boolean matchFound = matcher.find();

		if (matchFound) {

			return matcher.group(1);
		} else {

			return null;
		}

	}

	// get getTimezone from string using regex
	public static boolean isTimezoneAvailable(String xml) {

		final Pattern pattern = Pattern.compile("<TimeZone>(.+?)</TimeZone>", Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(xml);

		return matcher.find();

	}

	public static ConnectProcessorType getConnectProcessorType(String connectProcessorType) {

		return EnumUtils.getEnum(ConnectProcessorType.class, connectProcessorType.toUpperCase());
	}

	public static List<String> extractFirstPageQueryParamValueAsList(List<PageQueryParam> pageQueryParams) {

		PageQueryParam applicationStatusesParam = pageQueryParams.stream().findFirst().get();

		if (null == applicationStatusesParam || StringUtils.isEmpty(applicationStatusesParam.getParamName())
				|| StringUtils.isEmpty(applicationStatusesParam.getParamValue())) {

			throw new InvalidInputException("Param Name or value cannot be null");
		}

		log.info("{} param value is {}", applicationStatusesParam.getParamName(), applicationStatusesParam);
		List<String> appStatusList = Stream
				.of(applicationStatusesParam.getParamValue().trim().split(AppConstants.COMMA_DELIMITER))
				.collect(Collectors.toList());
		return appStatusList;
	}

	public static String extractPageQueryParamValue(List<PageQueryParam> pageQueryParams, final String paramName) {

		PageQueryParam filteredPageQueryParam = pageQueryParams.stream()
				.filter(pageQueryParam -> paramName.equalsIgnoreCase(pageQueryParam.getParamName())).findAny()
				.orElse(null);

		if (null == filteredPageQueryParam || StringUtils.isEmpty(filteredPageQueryParam.getParamValue())) {

			throw new InvalidInputException(paramName + " param cannot be null");
		}

		log.info("{} param value is {}", paramName, filteredPageQueryParam);
		return filteredPageQueryParam.getParamValue();
	}

	public static String extractPageQueryOptionalParamValue(List<PageQueryParam> pageQueryParams,
			final String paramName) {

		PageQueryParam filteredPageQueryParam = pageQueryParams.stream()
				.filter(pageQueryParam -> paramName.equalsIgnoreCase(pageQueryParam.getParamName())).findAny()
				.orElse(null);

		if (null != filteredPageQueryParam && !StringUtils.isEmpty(filteredPageQueryParam.getParamValue())) {

			return filteredPageQueryParam.getParamValue();
		}

		log.info("{} param value is {}", paramName, filteredPageQueryParam);
		return null;
	}

	public static List<String> extractPageQueryParamValueAsList(List<PageQueryParam> pageQueryParams,
			final String paramName) {

		PageQueryParam applicationStatusesParam = pageQueryParams.stream()
				.filter(pageQueryParam -> paramName.equalsIgnoreCase(pageQueryParam.getParamName())).findAny()
				.orElse(null);

		if (null == applicationStatusesParam || StringUtils.isEmpty(applicationStatusesParam.getParamValue())) {

			throw new InvalidInputException(paramName + " param cannot be null");
		}

		log.info("{} param value is {}", paramName, applicationStatusesParam);
		List<String> appStatusList = Stream
				.of(applicationStatusesParam.getParamValue().trim().split(AppConstants.COMMA_DELIMITER))
				.collect(Collectors.toList());
		return appStatusList;
	}

	public static List<String> extractPageQueryParamValueOptionalAsList(List<PageQueryParam> pageQueryParams,
			final String paramName) {

		PageQueryParam applicationStatusesParam = pageQueryParams.stream()
				.filter(pageQueryParam -> paramName.equalsIgnoreCase(pageQueryParam.getParamName())).findAny()
				.orElse(null);

		log.info("{} param value is {}", paramName, applicationStatusesParam);

		if (null != applicationStatusesParam) {

			List<String> appStatusList = Stream
					.of(applicationStatusesParam.getParamValue().trim().split(AppConstants.COMMA_DELIMITER))
					.collect(Collectors.toList());
			return appStatusList;
		} else {

			return null;
		}

	}

	public static String[] convertStringToArray(String stringLine) {

		log.debug("Inside convertStringToArray for headerLine {}", stringLine);
		List<String> headerColumnList = Stream.of(stringLine.split(AppConstants.COMMA_DELIMITER)).map(String::trim)
				.collect(Collectors.toList());

		String[] headerArray = new String[headerColumnList.size()];
		return headerColumnList.toArray(headerArray);
	}

	public static List<String> getFieldsAsList(String propertyName) {

		return Stream.of(propertyName.split(AppConstants.COMMA_DELIMITER)).map(String::trim)
				.collect(Collectors.toList());

	}

	public static List<Object> getFieldsAsObjectList(String propertyName) {

		return getFieldsAsObjectList(propertyName, null);
	}

	public static List<Object> getFieldsAsObjectList(String propertyName, String delimiter) {

		if (StringUtils.isEmpty(delimiter)) {

			return Stream.of(propertyName.split(AppConstants.COMMA_DELIMITER)).map(String::trim)
					.collect(Collectors.toList());
		} else {

			return Stream.of(propertyName.split(delimiter)).map(String::trim).collect(Collectors.toList());
		}
	}


	public  static String getRoleName(String input) {
		if (input.contains("::")){
			return input.substring(0, input.indexOf("::"));
		} else {
			return null;
		}

	}

	public  static String getDataLabel(String input) {
		if (input.contains("::")){
			return input.substring(input.indexOf("::") + 2);
		} else {
			return input;
		}

	}
	/**
	 * This method give a list of table header like the one use when sending via
	 * bulkSend role::tabLabel, role::tablabel2 will generate a Map containing on
	 * each key a list of tab labels, the Map key represents a rolename in a
	 * template.
	 *
	 * @param allRoleColumnNames separate by comma value of the form String::String
	 * @return the map containing a list for each rolename
	 */
	public static Map<String, Map<String, String>> buildRoleTabLabelMap(String allRoleColumnNames) {

		Map<String, Map<String, String>> rolesTabLabelMap = new HashMap<>();
		Map<String, String> tabs = null;

		StringTokenizer csvHeaders = new StringTokenizer(allRoleColumnNames, AppConstants.COMMA_DELIMITER);
		log.info("buildRoleTabLabelMap: Number of bulksend header are: {}", csvHeaders.countTokens());

		while (csvHeaders.hasMoreTokens()) {
			StringTokenizer csvHeader = new StringTokenizer(csvHeaders.nextToken(), AppConstants.BULK_CSV_DELIMITER);
			String roleName = csvHeader.nextToken();
			if (!rolesTabLabelMap.containsKey(roleName)) {

				tabs = new HashMap<>();
				rolesTabLabelMap.put(roleName, tabs);

			}
			String roleTabLabel = csvHeader.nextToken();
			rolesTabLabelMap.get(roleName).put(roleTabLabel, "");
		}

		log.info("buildRoleTabLabelMap: Number of roles read -> {}", rolesTabLabelMap.size());
		if (log.isDebugEnabled()){
			log.debug("buildRoleTabLabelMap: Values in role tab label map -> {}", rolesTabLabelMap.toString());
		}
		return rolesTabLabelMap;
	}
}