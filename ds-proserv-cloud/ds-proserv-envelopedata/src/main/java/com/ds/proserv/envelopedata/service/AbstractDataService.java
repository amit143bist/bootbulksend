package com.ds.proserv.envelopedata.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ds.proserv.common.util.DSUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDataService {

	/*
	 * @Autowired private DSCacheManager dsCacheManager;
	 */

	public List<String> findAllowedProcessorTypes() {

		/*
		 * String allowedProcessorTypesStr = dsCacheManager
		 * .prepareAndRequestCacheDataByKey(PropertyCacheConstants.
		 * CONNECT_ALLOWED_PROCESSOR_TYPES);
		 */

		String allowedProcessorTypesStr = null;
		log.debug("allowedProcessorTypesStr val is {}", allowedProcessorTypesStr);

		if (StringUtils.isEmpty(allowedProcessorTypesStr)) {

			return Arrays.asList("TAB", "CUSTOMFIELD", "RECIPIENT");
		} else {

			return DSUtil.getFieldsAsList(allowedProcessorTypesStr);
		}
	}
}