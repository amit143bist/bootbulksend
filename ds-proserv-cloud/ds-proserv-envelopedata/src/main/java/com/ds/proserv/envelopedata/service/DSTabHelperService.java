package com.ds.proserv.envelopedata.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.ds.proserv.envelopedata.model.DSTab;
import com.ds.proserv.envelopedata.repository.DSTabRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DSTabHelperService {

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Transactional(isolation = Isolation.DEFAULT)
	public void updatedDSTabs(List<DSTab> updateDSTabList, String processId) {

		log.info("Size of updateDSTabList is {} for processId -> {}", updateDSTabList.size(), processId);

		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(convertListToMapForUpdated(updateDSTabList));
		int[] totalUpdates = namedParameterJdbcTemplate.batchUpdate(DSTabRepository.UPDATE_SQL, batch);

		if (log.isDebugEnabled()) {

			log.debug("Successfully updated total {} dstab rows and totalUpdates is {}", updateDSTabList.size(),
					totalUpdates);
		}

	}

	private List<Map<String, Object>> convertListToMapForUpdated(List<DSTab> updateDSTabList) {

		List<Map<String, Object>> rowDataMapList = new ArrayList<>(updateDSTabList.size());
		for (DSTab updateDSTab : updateDSTabList) {

			Map<String, Object> columnDataMap = new HashMap<String, Object>();

			columnDataMap.put("updatedby", "DSTabUpdate");
			columnDataMap.put("updateddatetime", LocalDateTime.now().toString());
			columnDataMap.put("envelopeid", updateDSTab.getEnvelopeId());
			columnDataMap.put("recipientid", updateDSTab.getRecipientId());
			columnDataMap.put("tablabel", updateDSTab.getTabLabel());
			columnDataMap.put("tabname", updateDSTab.getTabName());
			columnDataMap.put("taboriginalvalue", updateDSTab.getTabOriginalValue());
			columnDataMap.put("tabstatus", updateDSTab.getTabStatus());
			columnDataMap.put("tabvalue", updateDSTab.getTabValue());
			columnDataMap.put("id", updateDSTab.getId());

			rowDataMapList.add(columnDataMap);
		}

		return rowDataMapList;
	}

	@Transactional(isolation = Isolation.DEFAULT)
	public void insertDSTabs(List<DSTab> insertDSTabList, String processId) {

		log.info("Size of insertDSTabList is {} for processId -> {}", insertDSTabList.size());

		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(convertListToMapForInsert(insertDSTabList));
		int[] totalInserts = namedParameterJdbcTemplate.batchUpdate(DSTabRepository.INSERT_SQL, batch);

		if (log.isDebugEnabled()) {

			log.debug("Successfully inserted total {} dstab rows and totalInserts is {}", insertDSTabList.size(),
					totalInserts);
		}

	}

	private List<Map<String, Object>> convertListToMapForInsert(List<DSTab> insertDSTabList) {

		List<Map<String, Object>> rowDataMapList = new ArrayList<>(insertDSTabList.size());
		for (DSTab insertDSTab : insertDSTabList) {

			Map<String, Object> columnDataMap = new HashMap<String, Object>();

			columnDataMap.put("createdby", "DSTabUpdate");
			columnDataMap.put("createddatetime", LocalDateTime.now().toString());
			columnDataMap.put("envelopeid", insertDSTab.getEnvelopeId());
			columnDataMap.put("recipientid", insertDSTab.getRecipientId());
			columnDataMap.put("tablabel", insertDSTab.getTabLabel());
			columnDataMap.put("tabname", insertDSTab.getTabName());
			columnDataMap.put("taboriginalvalue", insertDSTab.getTabOriginalValue());
			columnDataMap.put("tabstatus", insertDSTab.getTabStatus());
			columnDataMap.put("tabvalue", insertDSTab.getTabValue());
			columnDataMap.put("id", UUID.randomUUID().toString());

			rowDataMapList.add(columnDataMap);
		}

		return rowDataMapList;
	}
}