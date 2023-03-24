package com.ds.proserv.connect.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;

public class ConnectSaveCacheData {

	private DSCacheManager dsCacheManager;

	public ConnectSaveCacheData(DSCacheManager dsCacheManager) {
		super();
		this.dsCacheManager = dsCacheManager;
	}

	public Boolean isSaveTabData() {

		String saveTabDataStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_SAVE_TABDATA, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(saveTabDataStr)) {

			return true;
		} else {

			return Boolean.parseBoolean(saveTabDataStr);
		}
	}

	public Boolean isSaveTabDataUsingFormData() {

		String saveTabDataStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_SAVE_TABDATA_USING_FORMDATA,
				PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(saveTabDataStr)) {

			return false;
		} else {

			return Boolean.parseBoolean(saveTabDataStr);
		}
	}
	
	public Boolean isSaveTabDataIgnoreNonFormTabTypes() {

		String saveTabDataStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_SAVE_IGNORE_NONFORMTABTYPES,
				PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(saveTabDataStr)) {

			return true;
		} else {

			return Boolean.parseBoolean(saveTabDataStr);
		}
	}

	public Boolean isSaveRecipientData() {

		String saveRecipientDataStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_SAVE_RECIPIENTDATA, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(saveRecipientDataStr)) {

			return true;
		} else {

			return Boolean.parseBoolean(saveRecipientDataStr);
		}
	}

	public Boolean isSaveCustomFieldData() {

		String saveCustomFieldDataStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_SAVE_CUSTOMFIELDDATA, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(saveCustomFieldDataStr)) {

			return true;
		} else {

			return Boolean.parseBoolean(saveCustomFieldDataStr);
		}
	}

	public Boolean isSaveDocDownloadData() {

		String saveDocDownloadDataStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_SAVE_DOCDOWNLOADDATA, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(saveDocDownloadDataStr)) {

			return false;
		} else {

			return Boolean.parseBoolean(saveDocDownloadDataStr);
		}
	}

	public Boolean isSaveRecipientAuthData() {

		String saveRecipientAuthDataStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.CONNECT_SAVE_RECIPIENTAUTHDATA, PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(saveRecipientAuthDataStr)) {

			return false;
		} else {

			return Boolean.parseBoolean(saveRecipientAuthDataStr);
		}
	}

	public List<String> getEnvStatusesAvailableForDownload() {

		if (null != isSaveDocDownloadData() && isSaveDocDownloadData()) {

			String saveDocDownloadEnvStatusesStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.CONNECT_SAVE_DOCDOWNLOAD_ENVSTATUS,
					PropertyCacheConstants.CONNECT_REFERENCE_NAME);

			if (StringUtils.isEmpty(saveDocDownloadEnvStatusesStr)) {

				List<String> envStatusesAvailableToDownload = new ArrayList<String>(4);
				envStatusesAvailableToDownload.add(EnvelopeStatusCode.COMPLETED.value());
				envStatusesAvailableToDownload.add(EnvelopeStatusCode.DECLINED.value());
				envStatusesAvailableToDownload.add(EnvelopeStatusCode.VOIDED.value());
				envStatusesAvailableToDownload.add(AppConstants.ENVELOPE_EXPIRED_STATUS);
				return envStatusesAvailableToDownload;
			} else {

				List<String> envStatusesAvailableToDownload = Stream
						.of(saveDocDownloadEnvStatusesStr.split(AppConstants.COMMA_DELIMITER)).map(String::trim)
						.collect(Collectors.toList());

				return envStatusesAvailableToDownload;
			}
		} else {

			return null;
		}
	}

}