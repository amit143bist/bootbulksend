package com.ds.proserv.connect.domain;

import org.apache.commons.lang3.StringUtils;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;

public class ConnectCacheData {

	private DSCacheManager dsCacheManager;

	public ConnectCacheData(DSCacheManager dsCacheManager) {
		super();
		this.dsCacheManager = dsCacheManager;
	}

	public Boolean isStepSleepEnabled() {

		String stepSleepEnabledStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_JOB_STEPSLEEP_ENABLED, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(stepSleepEnabledStr)) {

			return false;
		} else {

			return Boolean.parseBoolean(stepSleepEnabledStr);
		}
	}

	public Integer getThresholdCheck() {

		String thresholdCheckStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_JOB_STEPSLEEP_THRESHOLDCHECK,
				PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(thresholdCheckStr)) {

			return 10;
		} else {

			return Integer.parseInt(thresholdCheckStr);
		}
	}

	// in milliseconds
	public Integer getSleepInterval() {

		String sleepIntervalStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_JOB_STEPSLEEP_INTERVAL, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(sleepIntervalStr)) {

			return 60000;
		} else {

			return Integer.parseInt(sleepIntervalStr);
		}

	}

	public Integer getBacksOffLimit() {

		String backsOffLimitStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_JOB_BACKSOFF_LIMIT, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(backsOffLimitStr)) {

			return 5;
		} else {

			return Integer.parseInt(backsOffLimitStr);
		}

	}

	public Integer getBacksOffInterval() {

		String backsOffIntervalStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_JOB_BACKSOFF_INTERVAL, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(backsOffIntervalStr)) {

			return 60000;
		} else {

			return Integer.parseInt(backsOffIntervalStr);
		}
	}

	public Integer getRecordsPerPage() {

		String recordsPerPageStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_JOB_RECORDS_PERPAGE, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(recordsPerPageStr)) {

			return 50;
		} else {

			return Integer.parseInt(recordsPerPageStr);
		}
	}

}