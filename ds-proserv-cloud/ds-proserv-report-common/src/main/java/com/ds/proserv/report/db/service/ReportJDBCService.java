package com.ds.proserv.report.db.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.feign.report.domain.ManageDataAPI;
import com.ds.proserv.feign.report.domain.ReportData;
import com.ds.proserv.feign.report.domain.TableColumnMetaData;
import com.ds.proserv.feign.report.domain.TableCreationRequest;
import com.ds.proserv.feign.report.domain.TableDefinition;
import com.ds.proserv.feign.util.ReportAppUtil;
import com.ds.proserv.feign.util.ReportDataManageUtil;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Service
@Slf4j
public class ReportJDBCService {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Transactional
	public void createDynamicTables(TableCreationRequest tableCreationRequest) {

		log.info("<<<<<<<<<<<<<<<<<<<< Creating Dynamic Tables >>>>>>>>>>>>>>>>>>>>");

		List<TableDefinition> tableDefinitions = tableCreationRequest.getTableDefinitions();

		List<String> ddlQueries = new ArrayList<String>();
		tableDefinitions.forEach(tableDefinition -> {

			log.info("Creating table -> {}", tableDefinition.getTableName());
			ddlQueries.add(ReportAppUtil.createTableQuery(tableDefinition));

		});

		ddlQueries.forEach(ddl -> {

			log.debug(" :::::: ddlQuery :::::: " + ddl);

		});

		log.info(" <<<<<<<<<<<<<<<<<<<<< Calling JDBC to create table in the database >>>>>>>>>>>>>>>>>> ");
		String[] ddlArr = new String[ddlQueries.size()];
		createTables(ddlQueries.toArray(ddlArr));

	}

	@Transactional
	public void createTables(String[] ddlQueries) {

		int[] batchUpdateResponse = jdbcTemplate.batchUpdate(ddlQueries);

		Arrays.asList(batchUpdateResponse)
				.forEach(response -> log.info(" :::::::::::: response :::::::::::: " + response));
	}

	@Transactional(readOnly = true)
	public TableColumnMetaData getTableColumns(String tableName) {

		SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from " + tableName + " where 0=1");

		int columnCount = sqlRowSet.getMetaData().getColumnCount();

		Map<String, String> columnNameTypeMap = new HashMap<String, String>(columnCount);
		Map<String, Integer> columnNameIndexMap = new HashMap<String, Integer>(columnCount);

		StringBuilder columnNames = new StringBuilder();
		columnNames.append("(");

		StringBuilder positionPlaceHolders = new StringBuilder();
		positionPlaceHolders.append("(");

		StringBuilder namedPlaceHolders = new StringBuilder();
		namedPlaceHolders.append("(");

		for (int i = 1; i <= columnCount; i++) {

			String columnName = sqlRowSet.getMetaData().getColumnName(i).toLowerCase();

			log.debug("ColumnName " + columnName + " ColumnTypeName " + sqlRowSet.getMetaData().getColumnTypeName(i));

			columnNameTypeMap.put(columnName, sqlRowSet.getMetaData().getColumnTypeName(i));

			columnNameIndexMap.put(columnName, i);

			columnNames.append("`");
			columnNames.append(columnName);
			columnNames.append("`");

			columnNames.append(",");

			positionPlaceHolders.append("?");
			positionPlaceHolders.append(",");

			namedPlaceHolders.append(":" + columnName);
			namedPlaceHolders.append(",");

		}

		columnNames.deleteCharAt(columnNames.length() - 1);
		columnNames.append(")");

		positionPlaceHolders.deleteCharAt(positionPlaceHolders.length() - 1);
		positionPlaceHolders.append(")");

		namedPlaceHolders.deleteCharAt(namedPlaceHolders.length() - 1);
		namedPlaceHolders.append(")");

		StringBuilder insertSqlBuilder = new StringBuilder();

		insertSqlBuilder.append("insert into " + tableName + " ");
		insertSqlBuilder.append(columnNames.toString());
		insertSqlBuilder.append(" values ");
		insertSqlBuilder.append(positionPlaceHolders.toString());

		StringBuilder insertNamedSqlBuilder = new StringBuilder();

		insertNamedSqlBuilder.append("insert into " + tableName + " ");
		insertNamedSqlBuilder.append(" values ");
		insertNamedSqlBuilder.append(namedPlaceHolders.toString());

		log.debug("InsertQuery::::::::: {}", insertSqlBuilder.toString());
		log.debug("columnNameTypeMap::::::::: {}", columnNameTypeMap);
		log.debug("columnNameIndexMap::::::::: {}", columnNameIndexMap);

		return createTableColumnMetaData(tableName, columnNameTypeMap, columnNameIndexMap, insertSqlBuilder,
				insertNamedSqlBuilder);

	}

	private TableColumnMetaData createTableColumnMetaData(String tableName, Map<String, String> columnNameTypeMap,
			Map<String, Integer> columnNameIndexMap, StringBuilder insertSqlBuilder,
			StringBuilder insertnamedSqlBuilder) {

		TableColumnMetaData tableColumnMetaData = new TableColumnMetaData();

		tableColumnMetaData.setTableName(tableName);
		tableColumnMetaData.setColumnNameTypeMap(columnNameTypeMap);
		tableColumnMetaData.setColumnNameIndexMap(columnNameIndexMap);
		tableColumnMetaData.setInsertQuery(insertSqlBuilder.toString());
		tableColumnMetaData.setInsertNamedQuery(insertnamedSqlBuilder.toString());

		return tableColumnMetaData;
	}

	@Transactional
	public void deleteReportData(Set<String> accoundIdList, String batchId, String tableName) {

		List<Object[]> batchArgs = new ArrayList<Object[]>();

		accoundIdList.forEach(accountId -> {

			batchArgs.add(new Object[] { accountId, batchId });

		});
		jdbcTemplate.batchUpdate("DELETE FROM " + tableName + " WHERE accountid = ? and batchid = ?", batchArgs);
	}

	private List<Map<String, Object>> convertListToMap(List<List<ReportData>> reportRowsList, String accountId,
			String batchId, String processId) {

		List<Map<String, Object>> rowDataMapList = new ArrayList<>(reportRowsList.size());
		for (List<ReportData> reportDataList : reportRowsList) {

			Map<String, Object> columnDataMap = new HashMap<String, Object>();

			columnDataMap.put("recordid", UUID.randomUUID().toString());
			columnDataMap.put("createddatetime", LocalDateTime.now().toString());
			columnDataMap.put("createdby",
					dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.APP_DB_AUDITOR_NAME));
			columnDataMap.put("accountid", accountId);
			columnDataMap.put("batchid", batchId);
			columnDataMap.put("processid", processId);

			reportDataList.forEach(reportData -> {

				columnDataMap.put(reportData.getReportColumnName(), reportData.getReportColumnValue());
			});

			rowDataMapList.add(columnDataMap);
		}

		return rowDataMapList;
	}

	@Transactional
	public String saveReportData(List<List<ReportData>> reportRowsList, TableColumnMetaData tableColumnMetaData,
			String accountId, String batchId, String processId, String nextUri, String primaryIdColumnName) {

		log.info(
				"Saving ReportData in tableName -> {} with primaryIdColumnName -> {} and rowSize is {} for accountId -> {}, batchId -> {}, processId -> {} and nextUri -> {}",
				tableColumnMetaData.getTableName(), primaryIdColumnName, reportRowsList.size(), accountId, batchId,
				processId, nextUri);

		String primaryIds = null;
		try {

			List<Map<String, Object>> rowDataMapList = convertListToMap(reportRowsList, accountId, batchId, processId);

			if (!StringUtils.isEmpty(primaryIdColumnName)) {

				primaryIds = findAndCollectPrimaryIds(rowDataMapList, primaryIdColumnName);

				if (!StringUtils.isEmpty(primaryIds)) {
					log.info("primaryIds for accountId -> {}, batchId -> {}, processId -> {} is {}", accountId, batchId,
							processId, primaryIds);
				} else {

					log.error("primaryIds is null for accountId -> {}, batchId -> {}, processId -> {}", accountId,
							batchId, processId);
				}
			}

			SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(rowDataMapList);
			int[] updateCounts = namedParameterJdbcTemplate.batchUpdate(tableColumnMetaData.getInsertNamedQuery(),
					batch);

			log.info("updateCounts in saveReportData is {}", updateCounts);

		} catch (Exception exp) {

			log.error(
					"Exception {} occurred in saving ReportData in tableName -> {} and rowSize is {} for accountId -> {}, batchId -> {}, processId -> {} and nextUri -> {}",
					exp, tableColumnMetaData.getTableName(), reportRowsList.size(), accountId, batchId, processId,
					nextUri);
			exp.printStackTrace();
			throw new ResourceNotSavedException("Report Data not saved in local database for accountId -> " + accountId
					+ " batchId -> " + batchId + " processId -> " + processId + " nextUri -> " + nextUri);
		}

		return primaryIds;
	}

	private String findAndCollectPrimaryIds(List<Map<String, Object>> rowDataMapList, String primaryIdColumnName) {

		List<String> primaryIds = new ArrayList<String>();

		for (Map<String, Object> rowMap : rowDataMapList) {

			primaryIds.add(String.valueOf(rowMap.get(primaryIdColumnName)));
		}

		return String.join(AppConstants.COMMA_DELIMITER, primaryIds);
	}

	@Transactional
	public List<Map<String, Object>> readReportData(Map<String, String> columnNameHeaderMap,
			Map<String, String> originalColumnNameHeaderMap, Map<String, Object> inputParams,
			ManageDataAPI csvReportDataExport, Integer pageNumber, Integer paginationLimit) {

		log.info("Reading ReportData with queryParams is {}", inputParams);

		String selectSql = csvReportDataExport.getSelectSql();
		selectSql = ReportDataManageUtil.prepareSelectSql(csvReportDataExport, selectSql);

		List<Map<String, Object>> selectDataMapList = null;
		if (null != csvReportDataExport.getSqlParams() && !csvReportDataExport.getSqlParams().isEmpty()) {

			Map<String, Object> paramsMap = ReportDataManageUtil.formatPathParam(inputParams, csvReportDataExport);

			// order by envelopeid asc OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
			if (selectSql.contains(AppConstants.SQL_QUERY_OFFSET)) {

				int offset = (paginationLimit * pageNumber) - paginationLimit;
				paramsMap.put(AppConstants.SQL_QUERY_OFFSET, offset);
			}

			if (selectSql.contains(AppConstants.SQL_QUERY_LIMIT)) {

				paramsMap.put(AppConstants.SQL_QUERY_LIMIT, paginationLimit);
			}

			if (log.isDebugEnabled()) {
				paramsMap.forEach((key, value) -> {

					log.debug("ParamName is {} and paramValue is {}", key, value);
				});
			}

			log.info("Calling DB for query {}", selectSql);
			selectDataMapList = namedParameterJdbcTemplate.queryForList(selectSql, paramsMap);

			if (log.isDebugEnabled()) {
				columnNameHeaderMap.forEach((key, value) -> {

					log.debug("ColumnName as Key {} and HeaderName as {}", key, value);

				});
			}
		} else {

			log.info("Calling DB for query without any params {}", selectSql);
			selectDataMapList = jdbcTemplate.queryForList(selectSql);
		}

		return ReportDataManageUtil.readReportData(selectDataMapList, columnNameHeaderMap, originalColumnNameHeaderMap,
				csvReportDataExport);
	}

	public List<Map<String, Object>> runSelectQuery(String selectSql) {

		log.info("Calling DB for select query {}", selectSql);

		List<Map<String, Object>> selectDataMapList = jdbcTemplate.queryForList(selectSql);

		log.info("Total Fetched rows are {}", selectDataMapList.size());

		return selectDataMapList;
	}

	public void runNonSelectQuery(String selectSql) {

		log.info("Calling DB for non-select query {}", selectSql);
		jdbcTemplate.batchUpdate(selectSql);
	}

	@Transactional
	public List<Map<String, Object>> readEnvelopeDataForDownload(ManageDataAPI csvReportDataExport,
			Map<String, Object> inputParams, Integer pageNumber, Integer paginationLimit) {

		log.info("Reading readEnvelopeDataForDownload with queryParams is {}", inputParams);

		String selectSql = "select * from " + csvReportDataExport.getTableName();

		selectSql = ReportDataManageUtil.prepareSelectSql(csvReportDataExport, selectSql);

		List<Map<String, Object>> selectDataMapList = null;
		if (null != csvReportDataExport.getSqlParams() && !csvReportDataExport.getSqlParams().isEmpty()) {

			Map<String, Object> paramsMap = ReportDataManageUtil.formatPathParam(inputParams, csvReportDataExport);

			// order by envelopeid asc OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
			if (selectSql.contains(AppConstants.SQL_QUERY_OFFSET)) {

				int offset = (paginationLimit * pageNumber) - paginationLimit;
				paramsMap.put(AppConstants.SQL_QUERY_OFFSET, offset);
			}

			if (selectSql.contains(AppConstants.SQL_QUERY_LIMIT)) {

				paramsMap.put(AppConstants.SQL_QUERY_LIMIT, paginationLimit);
			}

			if (log.isDebugEnabled()) {
				paramsMap.forEach((key, value) -> {

					log.debug("ParamName is {} and paramValue is {}", key, value);
				});
			}

			try {

				log.info("Calling DB for query {}", selectSql);
				selectDataMapList = namedParameterJdbcTemplate.queryForList(selectSql, paramsMap);
			} catch (ArrayIndexOutOfBoundsException exp) {

				log.error("Placeholder mismatch between sql and sql params list, verify sql and/or sql params list");
				exp.printStackTrace();
			}

		} else {

			log.info("Calling DB in readEnvelopeDataForDownload for query without any params {}", selectSql);
			selectDataMapList = jdbcTemplate.queryForList(selectSql);
		}

		log.info("Total Fetched rows are {}", selectDataMapList.size());

		return selectDataMapList;
	}

}