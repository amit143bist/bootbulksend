package com.ds.proserv.appdata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;

@Component
public class EnvelopeDataHelperService {

	@Autowired
	private DSCacheManager dsCacheManager;

	public boolean isFetchAppEnvDataBySPEnabled() {

		String enableFetchAppEnvDataBySP = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.APPENVDATA_SELECTBYENVIDS_STOREDPROC);

		if (!StringUtils.isEmpty(enableFetchAppEnvDataBySP)) {

			return Boolean.parseBoolean(enableFetchAppEnvDataBySP);
		}

		return true;
	}

	public boolean isUpdateProcessStatusStartDateTimeBySPEnabled() {

		String updateProcessStatusDataBySP = dsCacheManager.prepareAndRequestCacheDataByKey(
				PropertyCacheConstants.CUSTENVDATA_UPDATEPROCESSSTATUS_STARTDATETIME_BYENVIDS_STOREDPROC);

		if (!StringUtils.isEmpty(updateProcessStatusDataBySP)) {

			return Boolean.parseBoolean(updateProcessStatusDataBySP);
		}

		return true;
	}

	public boolean isUpdateProcessStatusEndDateTimeBySPEnabled() {

		String updateProcessStatusDataBySP = dsCacheManager.prepareAndRequestCacheDataByKey(
				PropertyCacheConstants.CUSTENVDATA_UPDATEPROCESSSTATUS_ENDDATETIME_BYENVIDS_STOREDPROC);

		if (!StringUtils.isEmpty(updateProcessStatusDataBySP)) {

			return Boolean.parseBoolean(updateProcessStatusDataBySP);
		}

		return true;
	}

	public boolean isUpdateDocDownloadEndDateTimeBySPEnabled() {

		String updateDocDownloadStatusDataBySP = dsCacheManager.prepareAndRequestCacheDataByKey(
				PropertyCacheConstants.CUSTENVDATA_UPDATEDOCDOWNLOAD_ENDDATETIME_BYENVIDS_STOREDPROC);

		if (!StringUtils.isEmpty(updateDocDownloadStatusDataBySP)) {

			return Boolean.parseBoolean(updateDocDownloadStatusDataBySP);
		}

		return true;
	}

	public boolean isUpdateDocDownloadEndDateTimeAndBucketNameBySPEnabled() {

		String updateDocDownloadStatusDataBySP = dsCacheManager.prepareAndRequestCacheDataByKey(
				PropertyCacheConstants.CUSTENVDATA_UPDATEDOCDOWNLOAD_ENDDATETIME_BUCKETNAME_BYENVIDS_STOREDPROC);

		if (!StringUtils.isEmpty(updateDocDownloadStatusDataBySP)) {

			return Boolean.parseBoolean(updateDocDownloadStatusDataBySP);
		}

		return true;
	}

	public boolean isCustomEnvDataFetchByIdSPEnabled() {

		String enableCustomEnvDataFetchByIdSP = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.CUSTENVDATA_SELECTBYENVID_STOREDPROC);

		if (!StringUtils.isEmpty(enableCustomEnvDataFetchByIdSP)) {

			return Boolean.parseBoolean(enableCustomEnvDataFetchByIdSP);
		}

		return true;
	}

	public boolean isCustomEnvDataFetchBySPEnabled() {

		String enableCustomEnvDataFetchBySP = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.CUSTOMENVDATA_SELECTBYBUCKETNAME_STOREDPROC);

		if (!StringUtils.isEmpty(enableCustomEnvDataFetchBySP)) {

			return Boolean.parseBoolean(enableCustomEnvDataFetchBySP);
		}

		return true;
	}
}