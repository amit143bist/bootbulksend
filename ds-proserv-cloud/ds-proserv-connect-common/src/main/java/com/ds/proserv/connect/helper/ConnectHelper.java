package com.ds.proserv.connect.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConnectHelper {

	@Autowired
	private DSCacheManager dsCacheManager;

	public boolean isConnectByQueue() {

		String connectByQueue = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.PROCESS_CONNECT_BYQUEUE, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(connectByQueue)) {

			return true;
		}

		return Boolean.parseBoolean(connectByQueue);
	}

	public void handleAsyncStatus(String asyncStatus, Throwable exp, String callingMethodName, String processEnvId) {

		if (null != exp) {

			log.info("Async processing got exception in {} for processId/envelopeId -> {}", callingMethodName,
					processEnvId);

			exp.printStackTrace();

		} else {

			if (AppConstants.SUCCESS_VALUE.equalsIgnoreCase(asyncStatus)) {

				log.info(
						" $$$$$$$$$$$$$$$$$$$$$$$$$ Async processing completed in {} for processId -> {} $$$$$$$$$$$$$$$$$$$$$$$$$ ",
						callingMethodName, processEnvId);
			} else {

				log.warn("Result is NOT success, it is {}, check logs for more information for {} and processId -> {}",
						asyncStatus, callingMethodName, processEnvId);
			}

		}
	}

	public void handleAsyncStatus(Throwable exp, String callingMethodName, String processEnvId) {

		if (null != exp) {

			log.info("Async processing got exception in {} for processId/envelopeId -> {}", callingMethodName,
					processEnvId);

			exp.printStackTrace();

		} else {

			log.info(
					" $$$$$$$$$$$$$$$$$$$$$$$$$ Async processing completed in {} for processId -> {} $$$$$$$$$$$$$$$$$$$$$$$$$ ",
					callingMethodName, processEnvId);

		}
	}

}