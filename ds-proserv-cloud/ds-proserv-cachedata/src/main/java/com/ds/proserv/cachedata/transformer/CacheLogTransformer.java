package com.ds.proserv.cachedata.transformer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.ds.proserv.cachedata.model.CacheDataLog;
import com.ds.proserv.feign.cachedata.domain.CacheLogDefinition;

@Service
public class CacheLogTransformer {

	public List<CacheDataLog> transformToCacheDataLogList(List<CacheLogDefinition> cacheLogDefinitionList) {

		List<CacheDataLog> coreCacheDataLogList = new ArrayList<CacheDataLog>(cacheLogDefinitionList.size());

		cacheLogDefinitionList.forEach(cacheLogDefinition -> {

			CacheDataLog cacheDataLog = new CacheDataLog();

			cacheDataLog.setCacheKey(cacheLogDefinition.getCacheKey());
			cacheDataLog.setCacheValue(cacheLogDefinition.getCacheValue());
			cacheDataLog.setCacheReference(cacheLogDefinition.getCacheReference());

			coreCacheDataLogList.add(cacheDataLog);
		});

		return coreCacheDataLogList;
	}

	public List<CacheLogDefinition> transformToCacheLogDefinitionList(List<CacheDataLog> cacheDataLogList) {

		List<CacheLogDefinition> cacheLogDefinitionList = new ArrayList<CacheLogDefinition>(cacheDataLogList.size());

		cacheDataLogList.forEach(cacheDataLog -> {

			CacheLogDefinition cacheLogDefinition = new CacheLogDefinition();

			cacheLogDefinition.setCacheId(cacheDataLog.getCacheId());
			cacheLogDefinition.setCacheKey(cacheDataLog.getCacheKey());
			cacheLogDefinition.setCacheValue(cacheDataLog.getCacheValue());
			cacheLogDefinition.setCacheReference(cacheDataLog.getCacheReference());

			cacheLogDefinitionList.add(cacheLogDefinition);
		});

		return cacheLogDefinitionList;
	}

	public CacheDataLog transformToCacheDataLog(CacheLogDefinition cacheLogDefinition) {

		CacheDataLog cacheDataLog = new CacheDataLog();

		cacheDataLog.setCacheKey(cacheLogDefinition.getCacheKey());
		cacheDataLog.setCacheValue(cacheLogDefinition.getCacheValue());
		cacheDataLog.setCacheReference(cacheLogDefinition.getCacheReference());

		return cacheDataLog;
	}

	public CacheLogDefinition transformToCacheLogDefinition(CacheDataLog cacheDataLog) {

		CacheLogDefinition cacheLogDefinition = new CacheLogDefinition();

		cacheLogDefinition.setCacheKey(cacheDataLog.getCacheKey());
		cacheLogDefinition.setCacheValue(cacheDataLog.getCacheValue());
		cacheLogDefinition.setCacheId(cacheDataLog.getCacheId());
		cacheLogDefinition.setCacheReference(cacheDataLog.getCacheReference());

		return cacheLogDefinition;
	}

	public CacheDataLog transformToCacheLogAsUpdate(CacheLogDefinition cacheLogDefinition, CacheDataLog cacheDataLog) {

		if (!StringUtils.isEmpty(cacheLogDefinition.getCacheKey())) {

			cacheDataLog.setCacheKey(cacheLogDefinition.getCacheKey());
		}

		if (!StringUtils.isEmpty(cacheLogDefinition.getCacheValue())) {

			cacheDataLog.setCacheValue(cacheLogDefinition.getCacheValue());
		}

		if (!StringUtils.isEmpty(cacheLogDefinition.getCacheReference())) {

			cacheDataLog.setCacheReference(cacheLogDefinition.getCacheReference());
		}

		return cacheDataLog;

	}

}