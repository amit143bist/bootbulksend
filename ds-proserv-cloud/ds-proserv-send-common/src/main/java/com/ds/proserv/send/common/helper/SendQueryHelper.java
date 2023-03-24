package com.ds.proserv.send.common.helper;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.util.DSUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SendQueryHelper {

	@Autowired
	private DSCacheManager dsCacheManager;

	public boolean isBulkSendByQueue(String cacheReference) {

		String bulkSendByQueue = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.PROCESS_BULKSEND_BYQUEUE, cacheReference);

		if (StringUtils.isEmpty(bulkSendByQueue)) {

			return true;
		}

		return Boolean.parseBoolean(bulkSendByQueue);
	}

	public boolean isBulkSendFailureByQueue(String cacheReference) {

		String bulkSendFailureByQueue = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.PROCESS_FAILURE_BYQUEUE, cacheReference);

		if (StringUtils.isEmpty(bulkSendFailureByQueue)) {

			return true;
		}

		return Boolean.parseBoolean(bulkSendFailureByQueue);
	}

	public String getProgramStartDateTime(String cacheReference) {

		String programStartDateTime = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSBULKSEND_PROGRAM_STARTDATETIME, cacheReference);
		if (StringUtils.isEmpty(programStartDateTime)) {

			return "2021-01-01T00:00:00.0000000Z";
		}

		return programStartDateTime;
	}

	public Integer getBatchSize(String cacheReference) {

		return Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSBULKSEND_BATCH_SIZE, cacheReference));
	}

	public List<String> getQueryIdentifiers(String cacheReference) {

		String queryIdentifiers = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSSEND_QUERY_IDENTIFIERS, cacheReference);

		if (StringUtils.isEmpty(queryIdentifiers)) {

			throw new InvalidInputException("queryIdentifiers cannot be null or empty");
		}

		return DSUtil.getFieldsAsList(queryIdentifiers);
	}

	public String getUserId(String queryIdentifier, String cacheReference) {

		String userId = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier + PropertyCacheConstants.DSSEND_USERID_SUFFIX,
				cacheReference);

		if (!StringUtils.isEmpty(userId)) {

			return userId;
		} else {

			userId = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.DSBULKSEND_USERID, cacheReference);
			if (!StringUtils.isEmpty(userId)) {

				return userId;
			} else {

				userId = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
						PropertyCacheConstants.DSAUTH_USERID, PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME);

				if (!StringUtils.isEmpty(userId)) {

					return userId;
				} else {

					throw new InvalidInputException("userId cannot be null or empty for " + queryIdentifier);
				}
			}
		}

	}

	public String getAccountId(String queryIdentifier, String cacheReference) {

		String accountId = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier + PropertyCacheConstants.DSSEND_ACCOUNTID_SUFFIX,
				cacheReference);

		if (!StringUtils.isEmpty(accountId)) {

			return accountId;
		} else {

			accountId = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.DSBULKSEND_ACCOUNTID, cacheReference);
			if (!StringUtils.isEmpty(accountId)) {

				return accountId;
			} else {

				accountId = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
						PropertyCacheConstants.DSAPI_ACCOUNT_ID, PropertyCacheConstants.DS_API_REFERENCE_NAME);

				if (!StringUtils.isEmpty(accountId)) {

					return accountId;
				} else {

					throw new InvalidInputException("accountId cannot be null or empty for " + queryIdentifier);
				}
			}
		}

	}

	public boolean useTemplate(String queryIdentifier, String cacheReference) {

		String useTemplateStr = dsCacheManager
				.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
						+ PropertyCacheConstants.DSSEND_USETEMPLATE_SUFFIX, cacheReference);

		log.info("TemplateFlag -> {} for queryIdentifier -> {}", useTemplateStr, queryIdentifier);
		if (!StringUtils.isEmpty(useTemplateStr)) {

			return Boolean.parseBoolean(useTemplateStr);
		}

		return true;
	}

	public String getSelectRecordIdQueryType(String queryIdentifier, String cacheReference) {

		String selectRecordIdQueryType = dsCacheManager
				.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
						+ PropertyCacheConstants.DSSEND_SELECTRECORDID_SQL_SUFFIX, cacheReference);

		if (StringUtils.isEmpty(selectRecordIdQueryType)) {

			throw new InvalidInputException(
					"selectRecordIdQueryType cannot be null or empty for " + PropertyCacheConstants.DSSEND_PREFIX
							+ queryIdentifier + PropertyCacheConstants.DSSEND_SELECTRECORDID_SQL_SUFFIX);
		}

		return selectRecordIdQueryType;
	}

	public boolean getSelectRecordIdByDateRange(String queryIdentifier, String cacheReference) {

		String selectRecordIdByDateRangeQueryType = dsCacheManager
				.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
						+ PropertyCacheConstants.DSSEND_SELECTRECORDID_SQL_BYDATERANGE_SUFFIX, cacheReference);

		if (StringUtils.isEmpty(selectRecordIdByDateRangeQueryType)) {

			return true;
		}

		return Boolean.parseBoolean(selectRecordIdByDateRangeQueryType);
	}

	public String getSelectRecordDataQueryType(String queryIdentifier, String cacheReference) {

		String selectRecordDataQueryType = dsCacheManager
				.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
						+ PropertyCacheConstants.DSSEND_SELECTRECORDDATA_SQL_SUFFIX, cacheReference);

		if (StringUtils.isEmpty(selectRecordDataQueryType)) {

			throw new InvalidInputException(
					"selectRecordDataQueryType cannot be null or empty for " + PropertyCacheConstants.DSSEND_PREFIX
							+ queryIdentifier + PropertyCacheConstants.DSSEND_SELECTRECORDDATA_SQL_SUFFIX);
		}

		return selectRecordDataQueryType;
	}

	public String getSelectRecordDataQueryTypePrimaryKeyParamName(String queryIdentifier, String cacheReference) {

		String selectRecordDataQueryTypePrimaryKeyParamName = dsCacheManager
				.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
						+ PropertyCacheConstants.DSSEND_SELECTRECORDDATA_SQL_PRIMARYKEY_SUFFIX, cacheReference);

		if (StringUtils.isEmpty(selectRecordDataQueryTypePrimaryKeyParamName)) {

			throw new InvalidInputException("selectRecordDataQueryTypePrimaryKeyParamName cannot be null or empty for "
					+ PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
					+ PropertyCacheConstants.DSSEND_SELECTRECORDDATA_SQL_PRIMARYKEY_SUFFIX);
		}

		return selectRecordDataQueryTypePrimaryKeyParamName;
	}

	public String getUpdateRecordDataQueryType(String queryIdentifier, String cacheReference) {

		String updateRecordDataQueryType = dsCacheManager
				.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
						+ PropertyCacheConstants.DSSEND_UPDATERECORDDATA_SUFFIX, cacheReference);

		return updateRecordDataQueryType;
	}

	public String getUpdateRecordDataQueryTypePrimaryKeyParamName(String queryIdentifier, String cacheReference) {

		String updateRecordDataQueryTypePrimaryKeyParamName = dsCacheManager
				.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
						+ PropertyCacheConstants.DSSEND_UPDATERECORDDATA_PRIMARYKEY_SUFFIX, cacheReference);

		if (!StringUtils.isEmpty(getUpdateRecordDataQueryType(queryIdentifier, cacheReference))
				&& StringUtils.isEmpty(updateRecordDataQueryTypePrimaryKeyParamName)) {

			throw new InvalidInputException("updateRecordDataQueryTypePrimaryKeyParamName cannot be null or empty for "
					+ PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
					+ PropertyCacheConstants.DSSEND_UPDATERECORDDATA_PRIMARYKEY_SUFFIX);
		}

		return updateRecordDataQueryTypePrimaryKeyParamName;
	}

	public String getTemplateId(String queryIdentifier, String cacheReference) {

		String templateId = dsCacheManager
				.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
						+ PropertyCacheConstants.DSSEND_TEMPLATE_ID_SUFFIX, cacheReference);

		if (StringUtils.isEmpty(templateId) && useTemplate(queryIdentifier, cacheReference)) {

			throw new InvalidInputException(
					"templateId cannot be null or empty for " + PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
							+ PropertyCacheConstants.DSSEND_TEMPLATE_ID_SUFFIX);
		}

		return templateId;
	}

	public String getDraftEnvelopeFilePath(String queryIdentifier, String cacheReference) {

		String draftEnvelopeFilePath = dsCacheManager
				.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
						+ PropertyCacheConstants.DSSEND_DRAFTENVELOPE_FILEPATH_SUFFIX, cacheReference);

		if (StringUtils.isEmpty(draftEnvelopeFilePath) && !useTemplate(queryIdentifier, cacheReference)) {

			throw new InvalidInputException(
					"draftEnvelopeFilePath cannot be null or empty for " + PropertyCacheConstants.DSSEND_PREFIX
							+ queryIdentifier + PropertyCacheConstants.DSSEND_DRAFTENVELOPE_FILEPATH_SUFFIX);
		}

		return draftEnvelopeFilePath;
	}

	public String getRuleEnginePath(String queryIdentifier, String cacheReference) {

		String ruleEnginePath = dsCacheManager
				.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSSEND_PREFIX + queryIdentifier
						+ PropertyCacheConstants.DSSEND_RULENGINGPATH_SUFFIX, cacheReference);

		if (StringUtils.isEmpty(ruleEnginePath)) {

			throw new InvalidInputException(
					"ruleEnginePath cannot be null or empty for " + PropertyCacheConstants.DSSEND_PREFIX
							+ queryIdentifier + PropertyCacheConstants.DSSEND_RULENGINGPATH_SUFFIX);
		}

		return ruleEnginePath;
	}

	public List<String> getIgnoreAppIds(String processId, String cacheReference) {

		String processIgnoreKeyName = PropertyCacheConstants.DSSEND_PREFIX + processId
				+ PropertyCacheConstants.DSSEND_IGNOREAPPIDS_SUFFIX;

		String ignoreCommaSeparatedAppIds = dsCacheManager
				.prepareAndRequestCacheDataByKeyAndReference(processIgnoreKeyName, cacheReference);

		if (StringUtils.isEmpty(ignoreCommaSeparatedAppIds)) {

			return null;
		}

		return DSUtil.getFieldsAsList(ignoreCommaSeparatedAppIds);
	}

	public boolean useTrackIdsFlow(String cacheReference) {

		String useTrackIdsStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSBULKSEND_TRACKIDS, cacheReference);

		if (!StringUtils.isEmpty(useTrackIdsStr)) {

			return Boolean.parseBoolean(useTrackIdsStr);
		}

		return true;
	}

	public boolean useTrackIdsWithToDateFlow(String cacheReference) {

		String useTrackIdsWithToDateStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSBULKSEND_TRACKIDS_WITH_TODATE, cacheReference);

		if (!StringUtils.isEmpty(useTrackIdsWithToDateStr)) {

			return Boolean.parseBoolean(useTrackIdsWithToDateStr);
		}

		return true;
	}
}