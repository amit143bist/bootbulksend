package com.ds.proserv.bulksend.sourcedata.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.bulksend.sourcedata.domain.BulkSendSqlDefinition;
import com.ds.proserv.bulksend.sourcedata.domain.SqlOption;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.JSONConversionException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.feign.bulksend.sourcedata.domain.BulkSendPrepareDefinition;
import com.ds.proserv.feign.bulksend.sourcedata.service.BulkSendDataSourceService;
import com.ds.proserv.feign.domain.ProcessSPDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BulkSendDataSourceController implements BulkSendDataSourceService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private BulkSendSqlDefinition bulkSendSqlDefinition;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public ResponseEntity<BulkSendPrepareDefinition> findBulkSendRecordIds(PageInformation pageInformation) {

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();

		Map<String, Object> inputParams = prepareInputParams(pageQueryParams);
		// APP_RECORDIDS_QUERY_TYPE
		// APP_RECORDDATA_QUERY_TYPE
		SqlOption filteredSqlOption = findFilteredSqlOption(pageQueryParams);

		List<Map<String, Object>> selectDataMapList = null;
		try {

			int pageNumber = getPageNumber(pageQueryParams);
			int limit = getPaginationLimit(pageQueryParams);
			int offset = (limit * pageNumber) - limit;
			inputParams.put(AppConstants.SQL_QUERY_OFFSET, offset);
			inputParams.put(AppConstants.SQL_QUERY_LIMIT, limit);

			log.info("offset -> {} and limit -> {}", offset, limit);
			selectDataMapList = namedParameterJdbcTemplate.queryForList(prepareSelectSql(filteredSqlOption),
					inputParams);
			List<Object> recordIds = new ArrayList<Object>();

			if (null != selectDataMapList && !selectDataMapList.isEmpty()) {

				log.info("PageNumber to be fetched -> {}", pageNumber);
				selectDataMapList.forEach(selectDataMapRow -> {

					recordIds.add(selectDataMapRow.entrySet().stream().findFirst().get().getValue());
				});
			}

			if (null != recordIds && !recordIds.isEmpty()) {

				log.info("Total RecordIds size is {} for pageQueryParams -> {}", recordIds.size(), pageQueryParams);
				BulkSendPrepareDefinition bulkSendPrepareDefinition = new BulkSendPrepareDefinition();
				bulkSendPrepareDefinition.setRecordIds(recordIds);
				bulkSendPrepareDefinition.setTotalRecords(recordIds.size());

				return new ResponseEntity<BulkSendPrepareDefinition>(bulkSendPrepareDefinition, HttpStatus.OK);
			} else {

				BulkSendPrepareDefinition bulkSendPrepareDefinition = new BulkSendPrepareDefinition();
				bulkSendPrepareDefinition.setTotalRecords(0);
				return new ResponseEntity<BulkSendPrepareDefinition>(bulkSendPrepareDefinition, HttpStatus.NO_CONTENT);
			}
		} catch (ArrayIndexOutOfBoundsException exp) {

			log.error(
					"Placeholder mismatch between sql and sql params list, verify sql and/or sql params list sent to findBulkSendRecordIds");
			exp.printStackTrace();
			throw new InvalidInputException(
					"Placeholder mismatch between sql and sql params list, verify sql and/or sql params list sent to findBulkSendRecordIds");
		}
	}

	private int getPageNumber(List<PageQueryParam> pageQueryParams) {

		String pageNumberStr = DSUtil.extractPageQueryParamValue(pageQueryParams, AppConstants.PAGENUMBER_PARAM_NAME);

		if (!StringUtils.isEmpty(pageNumberStr)) {

			return Integer.parseInt(pageNumberStr);
		} else {

			return 1;
		}
	}

	private int getPaginationLimit(List<PageQueryParam> pageQueryParams) {

		String paginationLimitStr = DSUtil.extractPageQueryOptionalParamValue(pageQueryParams,
				AppConstants.PAGINATIONLIMIT_PARAM_NAME);

		if (!StringUtils.isEmpty(paginationLimitStr)) {

			return Integer.parseInt(paginationLimitStr);
		} else {

			return 5000;
		}
	}

	private SqlOption findFilteredSqlOption(List<PageQueryParam> pageQueryParams) {

		String queryIdentifierValue = DSUtil.extractPageQueryParamValue(pageQueryParams,
				AppConstants.APP_QUERY_IDENTIFIER);

		String queryType = DSUtil.extractPageQueryParamValue(pageQueryParams, AppConstants.APP_QUERY_TYPE);

		SqlOption filteredSqlOption = bulkSendSqlDefinition.getSqlOptions().stream()
				.filter(sqlOption -> queryIdentifierValue.equalsIgnoreCase(sqlOption.getQueryIdentifier())
						&& queryType.equalsIgnoreCase(sqlOption.getQueryType()))
				.findFirst().orElse(null);

		if (null == filteredSqlOption) {

			throw new InvalidInputException("filteredSqlOption cannot be null for queryType -> " + queryType
					+ " queryIdentifier -> " + queryIdentifierValue + " in findBulkSendRecordIds");
		}
		return filteredSqlOption;
	}

	private Map<String, Object> prepareInputParams(List<PageQueryParam> pageQueryParams) {

		Map<String, Object> inputParams = new HashMap<String, Object>(pageQueryParams.size());
		pageQueryParams.forEach(pageQueryParam -> {

			if (!AppConstants.PAGINATIONLIMIT_PARAM_NAME.equalsIgnoreCase(pageQueryParam.getParamName())
					&& !AppConstants.PAGENUMBER_PARAM_NAME.equalsIgnoreCase(pageQueryParam.getParamName())) {

				if (!AppConstants.APP_QUERY_IDENTIFIER.equalsIgnoreCase(pageQueryParam.getParamName())
						&& !AppConstants.APP_QUERY_TYPE.equalsIgnoreCase(pageQueryParam.getParamName())) {

					if (null != pageQueryParam.getDelimitedList() && pageQueryParam.getDelimitedList()) {

						String delimiter = AppConstants.COMMA_DELIMITER;
						if (StringUtils.isEmpty(pageQueryParam.getDelimiter())) {

							delimiter = pageQueryParam.getDelimiter();
						}
						inputParams.put(pageQueryParam.getParamName(),
								DSUtil.getFieldsAsObjectList(pageQueryParam.getParamValue(), delimiter));
					} else {

						inputParams.put(pageQueryParam.getParamName(), pageQueryParam.getParamValue());
					}
				}
			}
		});
		return inputParams;
	}

	private String prepareSelectSql(SqlOption filteredSqlOption) {

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(filteredSqlOption.getSelectSql());
		if (!StringUtils.isEmpty(filteredSqlOption.getWhereClause())) {

			stringBuilder.append(" ");
			stringBuilder.append(filteredSqlOption.getWhereClause());
		}

		if (!StringUtils.isEmpty(filteredSqlOption.getOrderByClause())) {

			stringBuilder.append(" ");
			stringBuilder.append(filteredSqlOption.getOrderByClause());
		}
		return stringBuilder.toString();
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public ResponseEntity<BulkSendPrepareDefinition> findBulkSendSelectedRows(PageInformation pageInformation) {

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();

		Map<String, Object> inputParams = prepareInputParams(pageQueryParams);
		SqlOption filteredSqlOption = findFilteredSqlOption(pageQueryParams);

		String primaryKey = filteredSqlOption.getPrimaryKey();

		List<Map<String, Object>> selectDataMapList = null;

		List<Object> primaryKeyList = new ArrayList<Object>();
		try {

			selectDataMapList = namedParameterJdbcTemplate.queryForList(prepareSelectSql(filteredSqlOption),
					inputParams);

			if (null != selectDataMapList && !selectDataMapList.isEmpty()) {

				DateFormat sourceFormat = new SimpleDateFormat(getSqlDateSourceFormat());
				DateFormat targetFormat = new SimpleDateFormat(getSqlDateTargetFormat());

				selectDataMapList.forEach(selectDataMap -> {

					selectDataMap.forEach((key, value) -> {

						if (null != value) {

							String className = value.getClass().getName();
							if ("java.sql.Date".equalsIgnoreCase(className)) {

								String formattedDate = null;
								try {

									Date date = sourceFormat.parse(((java.sql.Date) value).toString());
									formattedDate = targetFormat.format(date);
								} catch (ParseException e) {

									log.error("Cannot parse value -> {} from sourceFormat -> {} to targetFormat -> {}",
											value, getSqlDateSourceFormat(), getSqlDateTargetFormat());
									e.printStackTrace();
								}

								selectDataMap.put(key, formattedDate);
							}

							if ("java.math.BigDecimal".equalsIgnoreCase(className)) {

								selectDataMap.put(key, value.toString());
							}
						}

						if (!StringUtils.isEmpty(primaryKey) && primaryKey.equalsIgnoreCase(key)) {

							primaryKeyList.add(value);
						}
					});
				});

				log.info("Total RecordIds size is {} for pageQueryParams -> {}", selectDataMapList.size(),
						pageQueryParams);

				BulkSendPrepareDefinition bulkSendPrepareDefinition = new BulkSendPrepareDefinition();
				bulkSendPrepareDefinition.setSelectedRows(selectDataMapList);
				bulkSendPrepareDefinition.setTotalRecords(selectDataMapList.size());

				if (null != primaryKeyList && !primaryKeyList.isEmpty()) {

					if (log.isDebugEnabled()) {

						log.debug("Fetched primaryKeyIds are {} for pageQueryParams -> {}", primaryKeyList,
								pageQueryParams);
					}
					bulkSendPrepareDefinition.setRecordIds(primaryKeyList);
				}

				return new ResponseEntity<BulkSendPrepareDefinition>(bulkSendPrepareDefinition, HttpStatus.OK);
			} else {

				BulkSendPrepareDefinition bulkSendPrepareDefinition = new BulkSendPrepareDefinition();
				bulkSendPrepareDefinition.setTotalRecords(0);
				return new ResponseEntity<BulkSendPrepareDefinition>(bulkSendPrepareDefinition, HttpStatus.NO_CONTENT);
			}
		} catch (ArrayIndexOutOfBoundsException exp) {

			log.error(
					"Placeholder mismatch between sql and sql params list, verify sql and/or sql params list sent to findBulkSendSelectedRows");
			exp.printStackTrace();
			throw new InvalidInputException(
					"Placeholder mismatch between sql and sql params list, verify sql and/or sql params list sent to findBulkSendSelectedRows");
		}
	}

	private String getSqlDateSourceFormat() {

		String sourceFormat = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DSBULKSEND_SQLDATE_SOURCE_FORMAT);

		if (StringUtils.isEmpty(sourceFormat)) {

			return "yyyy-MM-dd";
		} else {

			return sourceFormat;
		}
	}

	private String getSqlDateTargetFormat() {

		String targetFormat = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DSBULKSEND_SQLDATE_TARGET_FORMAT);

		if (StringUtils.isEmpty(targetFormat)) {

			return "MM/dd/yyyy";
		} else {

			return targetFormat;
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional
	public ResponseEntity<String> updateBulkSendSelectedRows(PageInformation pageInformation) {

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();

		SqlOption filteredSqlOption = findFilteredSqlOption(pageQueryParams);
		String updateSQL = filteredSqlOption.getUpdateSql();

		Assert.assertNotNull("updateSQL cannot be null in the sqlDefinition.json", updateSQL);

		String idParamValue = findParamValue(pageQueryParams);
		log.info("IdParam value in updateBulkSendSelectedRows is {}", idParamValue);

		if (updateSQL.toLowerCase().contains("{call")) {

			List<String> recordIds = Stream.of(idParamValue.trim().split(AppConstants.COMMA_DELIMITER))
					.collect(Collectors.toList());

			List<ProcessSPDefinition> processSPDefinitionList = new ArrayList<ProcessSPDefinition>();
			recordIds.forEach(recordId -> {

				ProcessSPDefinition processSPDefinition = new ProcessSPDefinition();
				processSPDefinition.setRecordId(recordId);

				processSPDefinitionList.add(processSPDefinition);
			});

			String appStatus = DSUtil.extractPageQueryParamValue(pageQueryParams, AppConstants.APP_PROCESS_STATUS);

			try {

				String spJSON = objectMapper.writeValueAsString(processSPDefinitionList);

				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("json", spJSON);
				paramMap.put("status", appStatus);

				int result = namedParameterJdbcTemplate.update(updateSQL, paramMap);

				if (log.isDebugEnabled()) {

					log.debug(
							"InsertUpdate result with SP in updateBulkSendSelectedRows is {} for pageInformation -> {}",
							result, pageInformation);
				}
			} catch (JsonProcessingException exp) {
				exp.printStackTrace();
				throw new JSONConversionException(exp.getMessage());
			} catch (Exception exp) {
				exp.printStackTrace();
				throw exp;
			}
		} else {

			Map<String, Object> inputParams = prepareInputParams(pageQueryParams);
			int result = namedParameterJdbcTemplate.update(updateSQL, inputParams);
			if (log.isDebugEnabled()) {

				log.debug(
						"InsertUpdate result with update sql in updateBulkSendSelectedRows is {} for pageInformation -> {}",
						result, pageInformation);
			}
		}

		return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.OK);
	}

	private String findParamValue(List<PageQueryParam> pageQueryParams) {

		PageQueryParam filteredPageQueryParam = pageQueryParams.stream().filter(
				pageQueryParam -> (!pageQueryParam.getParamName().equalsIgnoreCase(AppConstants.APP_QUERY_IDENTIFIER)
						&& !pageQueryParam.getParamName().equalsIgnoreCase(AppConstants.APP_QUERY_TYPE)
						&& !pageQueryParam.getParamName().equalsIgnoreCase(AppConstants.APP_PROCESS_STATUS)))
				.findFirst().orElse(null);
		return filteredPageQueryParam.getParamValue();
	}

}