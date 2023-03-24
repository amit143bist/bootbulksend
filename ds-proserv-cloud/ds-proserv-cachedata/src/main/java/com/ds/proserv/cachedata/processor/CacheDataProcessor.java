package com.ds.proserv.cachedata.processor;

import static com.ds.proserv.common.lambda.LambdaExceptionWrappers.throwingConsumerWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ds.proserv.cachedata.model.CacheDataLog;
import com.ds.proserv.cachedata.repository.CacheDataLogPagingAndSortingRepository;
import com.ds.proserv.cachedata.transformer.CacheLogTransformer;
import com.ds.proserv.feign.cachedata.domain.CacheLogDefinition;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Component
@Slf4j
public class CacheDataProcessor {

	@Autowired
	private CacheLogTransformer cacheLogTransformer;

	@Autowired
	private CacheDataLogPagingAndSortingRepository cacheDataLogPagingAndSortingRepository;

	public boolean isDataAvailableForProcessing(String processId, List<CacheLogDefinition> cacheLogDefinitionList) {

		if (null != cacheLogDefinitionList && !cacheLogDefinitionList.isEmpty()) {

			return true;
		} else {

			log.warn("No cachedata identified in CacheDataProcessor.isDataAvailableForProcessing for processId -> {}",
					processId);
			return false;
		}
	}

	@Transactional(propagation = Propagation.SUPPORTS, isolation = Isolation.READ_UNCOMMITTED)
	public List<CacheDataLog> compareAndPrepareData(String processId, List<CacheLogDefinition> cacheLogDefinitionList) {

		List<CacheDataLog> prepareToSaveCacheDataLogList = new ArrayList<CacheDataLog>(cacheLogDefinitionList.size());

		List<String> cacheIds = cacheLogDefinitionList.stream()
				.filter(cacheLogDefinition -> cacheLogDefinition.getCacheId() != null)
				.map(CacheLogDefinition::getCacheId).collect(Collectors.toList());

		try {

			log.info("Preparing all to be saved/updated cacheLogList for processId -> {}", processId);

			Iterable<CacheDataLog> savedCacheDataLogIterable = cacheDataLogPagingAndSortingRepository
					.findAllById(cacheIds);

			List<CacheDataLog> savedCacheDataLogList = StreamSupport
					.stream(savedCacheDataLogIterable.spliterator(), false).collect(Collectors.toList());

			cacheLogDefinitionList.forEach(
					throwingConsumerWrapper(toBeSavedCacheData -> processCacheData(prepareToSaveCacheDataLogList,
							toBeSavedCacheData, savedCacheDataLogList, processId)));

			log.info("Total CacheIds to be saved/updated for processId -> {} is {}", processId,
					prepareToSaveCacheDataLogList.size());
		} catch (Throwable exp) {

			log.error("Error {} occurred in compareAndPrepareData for processId -> {}", exp, processId);
			exp.printStackTrace();
			throw exp;
		}

		return prepareToSaveCacheDataLogList;
	}

	private void processCacheData(List<CacheDataLog> prepareToSaveCacheDataLogList,
			CacheLogDefinition toBeSavedCacheData, List<CacheDataLog> savedCacheDataLogList, String processId) {

		CacheDataLog filterSavedCacheDataLog = savedCacheDataLogList.stream().filter(savedCacheDataLog -> {

			if (null != savedCacheDataLog && null != toBeSavedCacheData
					&& savedCacheDataLog.getCacheId().equalsIgnoreCase(toBeSavedCacheData.getCacheId())) {

				return true;
			} else {

				return false;
			}
		}).findFirst().orElse(null);

		if (null != filterSavedCacheDataLog && !StringUtils.isEmpty(filterSavedCacheDataLog.getCacheId())) {

			log.debug("CacheId -> {} will be updated for processId -> {}", toBeSavedCacheData.getCacheId(), processId);

			prepareToSaveCacheDataLogList
					.add(cacheLogTransformer.transformToCacheLogAsUpdate(toBeSavedCacheData, filterSavedCacheDataLog));
		}
	}

	public boolean isDataAvailableForSave(String processId, List<CacheDataLog> prepareToSaveCacheDataLogList) {

		if (null != prepareToSaveCacheDataLogList && !prepareToSaveCacheDataLogList.isEmpty()) {

			return true;
		} else {

			log.error("No cache identified in isDataAvailableForSave for saving in processId -> {}", processId);

			return false;
		}
	}

	@Transactional(isolation = Isolation.DEFAULT)
	public List<CacheDataLog> callRepositorySaveOperations(String processId,
			List<CacheDataLog> prepareToSaveCacheDataLogList) {

		log.info("callRepositorySaveOperations to save cache triggered for processId -> {}", processId);

		Iterable<CacheDataLog> savedCacheDataLogIterable = cacheDataLogPagingAndSortingRepository
				.saveAll(prepareToSaveCacheDataLogList);
		List<CacheDataLog> savedCacheDataLogList = StreamSupport.stream(savedCacheDataLogIterable.spliterator(), false)
				.collect(Collectors.toList());

		return savedCacheDataLogList;
	}

}