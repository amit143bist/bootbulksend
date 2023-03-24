package com.ds.proserv.cache.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.common.constant.ValidationResult;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.feign.cachedata.domain.CacheLogDefinition;
import com.ds.proserv.feign.cachedata.domain.CacheLogInformation;
import com.ds.proserv.feign.cachedata.domain.CacheTokenRequest;
import com.ds.proserv.feign.cachedata.service.CoreCacheRefreshService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class DSCacheManager implements CoreCacheRefreshService {

	@Autowired
	private Environment env;

	@Lazy
	@Autowired
	private DSCacheService dsCacheService;

	private String SPRING_APP_NAME_CONST = "spring.application.name";

	public ResponseEntity<CacheLogInformation> prepareAndRequestAllCacheDataByCacheReference(String cacheReference) {

		CacheTokenRequest cacheTokenRequest = new CacheTokenRequest();
		cacheTokenRequest.setCacheReference(cacheReference);
		return dsCacheService.requestCacheData(cacheTokenRequest);
	}

	public String prepareAndRequestCacheDataByKey(String cacheKey) {

		return prepareAndRequestCacheDataByKeyAndReference(cacheKey, env.getProperty(SPRING_APP_NAME_CONST));
	}

	public String prepareAndRequestCacheDataByKeyAndReference(String cacheKey, String cacheReference) {

		log.debug("Finding CacheValue for cacheKey -> {} and cacheReference -> {}", cacheKey, cacheReference);
		CacheTokenRequest cacheTokenRequest = new CacheTokenRequest();
		cacheTokenRequest.setCacheReference(cacheReference);

		String cacheValue = null;
		try {
			ResponseEntity<CacheLogInformation> cacheLogInformation = dsCacheService
					.requestCacheData(cacheTokenRequest);

			if (null != cacheLogInformation && null != cacheLogInformation.getBody()
					&& null != cacheLogInformation.getBody().getCacheLogDefinitions()) {

				CacheLogInformation cacheLogInformationResp = cacheLogInformation.getBody();
				List<CacheLogDefinition> cacheLogDefinitionList = cacheLogInformationResp.getCacheLogDefinitions();
				if (null != cacheLogDefinitionList && !cacheLogDefinitionList.isEmpty()) {

					CacheLogDefinition cacheLogDefinition = cacheLogDefinitionList.stream().filter(
							savedCacheLogDefinition -> cacheKey.equalsIgnoreCase(savedCacheLogDefinition.getCacheKey()))
							.findFirst().orElse(null);

					if (null != cacheLogDefinition) {

						cacheValue = cacheLogDefinition.getCacheValue();
						log.debug("Cachekey -> {} value fetched from database with value as {}", cacheKey, cacheValue);
					} else if (!StringUtils.isEmpty(env.getProperty(cacheKey))) {

						cacheValue = env.getProperty(cacheKey);
						log.debug("Cachekey -> {} value fetched from property with value as {}", cacheKey, cacheValue);
					}

				}

			} else if (!StringUtils.isEmpty(env.getProperty(cacheKey))) {

				cacheValue = env.getProperty(cacheKey);
				log.info(
						"CacheLogInformation is null so Cachekey -> {} value fetched from property with value as {} for cacheReference -> {}",
						cacheKey, cacheValue, cacheReference);
			}
		} catch (ResourceNotFoundException exp) {

			log.error(
					"ResourceNotFoundException -> {} thrown, No cache present for cacheReference -> {} in database, please check the cache table for cacheKey -> {}",
					exp.getMessage(), cacheReference, cacheKey);

			if (!StringUtils.isEmpty(env.getProperty(cacheKey))) {

				cacheValue = env.getProperty(cacheKey);
				log.info(
						"Cachekey -> {} value fetched from property in ResourceNotFoundException block with value as {} for cacheReference -> {}",
						cacheKey, cacheValue, cacheReference);
			}
		} catch (Exception exp) {

			log.error(
					"Exception ->  {} thrown, No cache present for cacheReference -> {} in database, please check the cache table for cacheKey -> {}",
					exp.getMessage(), cacheReference, cacheKey);

			if (!StringUtils.isEmpty(env.getProperty(cacheKey))) {

				cacheValue = env.getProperty(cacheKey);
				log.info(
						"Cachekey -> {} value fetched from property in Exception block with value as {} for cacheReference -> {}",
						cacheKey, cacheValue, cacheReference);
			}
		}

		if (StringUtils.isEmpty(cacheValue)) {

			log.warn("Cachekey -> {} not available in database nor in property file for cacheReference -> {}", cacheKey,
					cacheReference);
		}

		return cacheValue;
	}

	@Override
	public ResponseEntity<String> evictCache() {

		String cacheReference = env.getProperty(SPRING_APP_NAME_CONST);
		log.info("Request to Refresh Cache for cacheReference -> {}", cacheReference);

		dsCacheService.evictCache(cacheReference);

		return new ResponseEntity<String>(ValidationResult.SUCCESS.toString(), HttpStatus.OK);
	}

}