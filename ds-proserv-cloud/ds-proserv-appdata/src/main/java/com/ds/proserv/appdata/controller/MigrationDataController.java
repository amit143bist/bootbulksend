package com.ds.proserv.appdata.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.SerializationUtils;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.ResourceConditionFailedException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.feign.appdata.domain.MigrationDataDefinition;
import com.ds.proserv.feign.appdata.domain.MigrationReportDataDefinition;
import com.ds.proserv.feign.appdata.domain.MigrationReportDataInformation;
import com.ds.proserv.feign.appdata.domain.MigrationReportDataRequest;
import com.ds.proserv.feign.appdata.domain.MigrationReportDataResponse;
import com.ds.proserv.feign.appdata.service.MigrationDataService;
import com.ds.proserv.feign.domain.ProcessSPDefinition;
import com.ds.proserv.feign.report.domain.ManageDataAPI;
import com.ds.proserv.feign.util.ReportDataManageUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
@Transactional
@Slf4j
public class MigrationDataController implements MigrationDataService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private static Map<String, String> tableNamedQueryMap = new HashMap<String, String>();

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<String> bulkUpdateSaveMigrationData(MigrationDataDefinition migrationDataDefinition) {

		try {

			log.info("Called bulkUpdateSaveMigrationData for tableName -> {} in processId -> {}",
					migrationDataDefinition.getApiDataTableName(), migrationDataDefinition.getProcessId());

			if (null == tableNamedQueryMap
					|| StringUtils.isEmpty(tableNamedQueryMap.get(migrationDataDefinition.getApiDataTableName()))) {

				tableNamedQueryMap.put(migrationDataDefinition.getApiDataTableName(),
						getNamedQuery(migrationDataDefinition.getApiDataTableName()));
			}

			log.info("tableNamedQueryMap size is {}", tableNamedQueryMap.size());

			SqlParameterSource[] batch = SqlParameterSourceUtils
					.createBatch(migrationDataDefinition.getRowDataMapList());
			int[] updateCounts = namedParameterJdbcTemplate
					.batchUpdate(tableNamedQueryMap.get(migrationDataDefinition.getApiDataTableName()), batch);

			log.info("updateCounts in saveReportData is {}", updateCounts);

			return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.OK);
		} catch (Exception exp) {

			log.error(
					"Exception {} occurred in saving ReportData in tableName -> {} and rowSize is {} for  processId -> {} and batchId -> {}",
					exp, migrationDataDefinition.getApiDataTableName(),
					migrationDataDefinition.getRowDataMapList().size(), migrationDataDefinition.getProcessId(),
					migrationDataDefinition.getBatchId());
			exp.printStackTrace();
			throw new ResourceNotSavedException("Report Data not saved in local database for " + " processId -> "
					+ migrationDataDefinition.getProcessId() + " batchId -> " + migrationDataDefinition.getBatchId());
		}
	}

	private String getNamedQuery(String tableName) {

		log.info("Calling getNamedQuery for tableName -> {}", tableName);
		SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from " + tableName + " where 0=1");

		int columnCount = sqlRowSet.getMetaData().getColumnCount();

		StringBuilder namedPlaceHolders = new StringBuilder();
		namedPlaceHolders.append("(");

		for (int i = 1; i <= columnCount; i++) {

			String columnName = sqlRowSet.getMetaData().getColumnName(i).toLowerCase();

			log.debug("ColumnName " + columnName + " ColumnTypeName " + sqlRowSet.getMetaData().getColumnTypeName(i));

			namedPlaceHolders.append(":" + columnName);
			namedPlaceHolders.append(",");

		}

		namedPlaceHolders.deleteCharAt(namedPlaceHolders.length() - 1);
		namedPlaceHolders.append(")");

		log.info("namedPlaceHolders value is {}", namedPlaceHolders);

		StringBuilder insertNamedSqlBuilder = new StringBuilder();

		insertNamedSqlBuilder.append("insert into " + tableName + " ");
		insertNamedSqlBuilder.append(" values ");
		insertNamedSqlBuilder.append(namedPlaceHolders.toString());

		log.info("insertNamedSqlBuilder value is {}", insertNamedSqlBuilder);
		return insertNamedSqlBuilder.toString();
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<Long> countTotalRecords(String tableName) {

		Long totalCount = jdbcTemplate.queryForObject("select count(1) from " + tableName, Long.class);

		return new ResponseEntity<Long>(totalCount, HttpStatus.OK);
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<List<Map<String, Object>>> runOnDemandQuery(String tableName,
			PageInformation pageInformation) {

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			String sqlQuery = DSUtil.extractPageQueryParamValue(pageQueryParams, AppConstants.SQL_QUERY_CLAUSE);

			List<Map<String, Object>> selectDataMapList = jdbcTemplate.queryForList(sqlQuery);

			return new ResponseEntity<List<Map<String, Object>>>(selectDataMapList, HttpStatus.OK);
		} else {

			return new ResponseEntity<List<Map<String, Object>>>(HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<MigrationReportDataResponse> createHeaderList(String tableName,
			MigrationReportDataRequest migrationReportDataRequest) {

		List<String> selectColumnList = null;
		if (!StringUtils.isEmpty(migrationReportDataRequest.getCsvColumns())) {

			selectColumnList = Stream.of(migrationReportDataRequest.getCsvColumns().split(AppConstants.COMMA_DELIMITER))
					.map(String::trim).collect(Collectors.toList());
		} else {

			log.info("Creating headers for the CSV for selectSql -> {}", migrationReportDataRequest.getSelectSql());

			selectColumnList = extractSelectColumnList(tableName, migrationReportDataRequest.getSelectSql());
		}

		log.info("selectColumnList is {}", selectColumnList);
		if (null == selectColumnList || selectColumnList.isEmpty()) {

			throw new ResourceConditionFailedException(
					"Columns list cannot be empty or null, please check the select query");
		}

		Map<String, String> columnNameHeaderMap = new LinkedHashMap<String, String>();

		selectColumnList.forEach(column -> {

			try {

				String keyValue = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(column,
						PropertyCacheConstants.CSV_COLUMN_HEADER_REFERENCE);
				columnNameHeaderMap.put(column, keyValue);

			} catch (ResourceNotFoundException exp) {

				log.warn("No cache value exists for key (columnName) -> {}", column);

				try {
					String keyValue = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(column.toUpperCase(),
							PropertyCacheConstants.CSV_COLUMN_HEADER_REFERENCE);
					columnNameHeaderMap.put(column.toLowerCase(), keyValue);
				} catch (ResourceNotFoundException upperExp) {

					log.warn("No cache value exists for key (columnName) with uppercase -> {}", column);
				}
			}

		});

		MigrationReportDataResponse migrationReportDataResponse = new MigrationReportDataResponse();
		migrationReportDataResponse.setCsvHeaderMap(columnNameHeaderMap);
		return new ResponseEntity<MigrationReportDataResponse>(migrationReportDataResponse, HttpStatus.OK);
	}

	private List<String> extractSelectColumnList(String tableName, String selectSql) {

		selectSql = selectSql.trim().replaceAll("\\s{2,}", " ").toLowerCase();
		if (selectSql.indexOf("select ") == -1) {

			throw new InvalidInputException(
					"Select query does not have select statement properly set, it should have space after select");
		}

		if (selectSql.indexOf(" from " + tableName) == -1) {

			throw new InvalidInputException(
					"Select query does not have select statement properly set, it should have space after before and after from");
		}

		String[] selectColumns = selectSql.split("select ");
		String sqlColumns = selectColumns[1].split(" from " + tableName)[0];

		return Stream.of(sqlColumns.split(AppConstants.COMMA_DELIMITER)).map(String::trim).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<MigrationReportDataInformation> readReportData(
			MigrationReportDataDefinition migrationReportDataDefinition) {

		ManageDataAPI csvReportDataExport = migrationReportDataDefinition.getCsvReportDataExport();
		Map<String, Object> inputParams = migrationReportDataDefinition.getInputParams();
		Map<String, String> columnNameHeaderMap = migrationReportDataDefinition.getColumnNameHeaderMap();

		String selectSql = csvReportDataExport.getSelectSql();
		selectSql = ReportDataManageUtil.prepareSelectSql(csvReportDataExport, selectSql);

		List<Map<String, Object>> selectDataMapList = null;

		if (isMigDataFetchSPEnabled()) {

			List<String> envelopeIds = (List<String>) inputParams
					.get(csvReportDataExport.getSqlParams().get(0).getParamName());

			List<ProcessSPDefinition> processSPDefinitionList = new ArrayList<ProcessSPDefinition>();

			for (String recordId : envelopeIds) {

				ProcessSPDefinition processSPDefinition = new ProcessSPDefinition();
				processSPDefinition.setRecordId(recordId);

				processSPDefinitionList.add(processSPDefinition);
			}

			try {

				String spName = null;
				if (!StringUtils.isEmpty(selectSql)) {

					spName = selectSql;
				} else {

					spName = "{call sproc_getrppmigrationdataforenvid(?)}";
				}

				String spJSON = objectMapper.writeValueAsString(processSPDefinitionList);

				if (log.isDebugEnabled()) {

					log.info("Calling sp -> {} with json -> {}", spName, spJSON);
				}

				selectDataMapList = jdbcTemplate.queryForList(spName, spJSON);

			} catch (JsonProcessingException e) {

				log.error("JsonProcessingException thrown in readReportData");
				e.printStackTrace();
			}

		} else {

			if (null != csvReportDataExport.getSqlParams() && !csvReportDataExport.getSqlParams().isEmpty()) {

				log.info("Calling namedParameterJdbcTemplate DB for query -> {} in readReportData", selectSql);
				selectDataMapList = namedParameterJdbcTemplate.queryForList(selectSql, inputParams);

				if (log.isDebugEnabled()) {

					columnNameHeaderMap.forEach((key, value) -> {

						log.debug("ColumnName as Key {} and HeaderName as {}", key, value);

					});
				}
			} else {

				log.info("Calling jdbcTemplate DB for query -> {} without any params", selectSql);
				selectDataMapList = jdbcTemplate.queryForList(selectSql);
			}
		}

		if (null != selectDataMapList && !selectDataMapList.isEmpty()) {

			Map<String, String> deepCopyColumnNameHeaderMap = (Map<String, String>) SerializationUtils
					.deserialize(SerializationUtils.serialize(columnNameHeaderMap));

			List<Map<String, Object>> reportDataList = ReportDataManageUtil.readReportData(selectDataMapList,
					columnNameHeaderMap, deepCopyColumnNameHeaderMap, csvReportDataExport);

			MigrationReportDataInformation migrationReportDataInformation = new MigrationReportDataInformation();
			migrationReportDataInformation.setReportDataList(reportDataList);
			migrationReportDataInformation.setTotalRecords(Long.valueOf(reportDataList.size()));
			return new ResponseEntity<MigrationReportDataInformation>(migrationReportDataInformation, HttpStatus.OK);
		} else {

			MigrationReportDataInformation migrationReportDataInformation = new MigrationReportDataInformation();
			migrationReportDataInformation.setTotalRecords(0L);
			return new ResponseEntity<MigrationReportDataInformation>(migrationReportDataInformation,
					HttpStatus.NO_CONTENT);
		}

	}

	private boolean isMigDataFetchSPEnabled() {

		String enableMigDataFetchBySP = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.MIGDATA_SELECTBYENVIDS_STOREDPROC);

		if (!StringUtils.isEmpty(enableMigDataFetchBySP)) {

			return Boolean.parseBoolean(enableMigDataFetchBySP);
		}

		return true;
	}

}