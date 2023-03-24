package com.ds.proserv.cachedata.repository;

import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.cachedata.model.CacheDataLog;

@Lazy
@Repository(value = "cacheDataLogPagingAndSortingRepository")
public interface CacheDataLogPagingAndSortingRepository extends PagingAndSortingRepository<CacheDataLog, String> {

	Optional<CacheDataLog> findByCacheKey(String cacheKey);

	Iterable<CacheDataLog> findAllByCacheReference(String cacheReference);

	Optional<CacheDataLog> findByCacheKeyAndCacheValue(String cacheKey, String cacheValue);

	Optional<CacheDataLog> findByCacheKeyAndCacheValueAndCacheReference(String cacheKey, String cacheValue,
			String cacheReference);

	Optional<CacheDataLog> findByCacheKeyAndCacheReference(String cacheKey, String cacheReference);

	Optional<CacheDataLog> findByCacheValueAndCacheReference(String cacheValue, String cacheReference);

	Iterable<CacheDataLog> findAllByCacheValue(String cacheValue);

	void deleteByCacheKey(String cacheKey);

}