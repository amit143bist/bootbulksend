package com.ds.proserv.report.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.ds.proserv.common.constant.AccountFetchAPITypes;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.ReportRunArgs;
import com.ds.proserv.feign.util.ReportAppUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReportRunParamService {

	public void validateReportRunArgs(ReportRunArgs apiRunArgs) {

		log.info(" #################### Validating the ReportRunArgs for loadEnvelopeData #################### ");

		PathParam accountFetchAPITypeParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
				AppConstants.ACCOUNTS_FETCH_API_TO_USE_TYPE);

		if (null != accountFetchAPITypeParam) {

			String accountFetchAPIType = accountFetchAPITypeParam.getParamValue();
			if (StringUtils.isEmpty(accountFetchAPIType)
					|| !(AccountFetchAPITypes.ORGADMIN.toString().equalsIgnoreCase(accountFetchAPIType)
							|| AccountFetchAPITypes.USERINFO.toString().equalsIgnoreCase(accountFetchAPIType))) {

				throw new InvalidInputException("Wrong accountFetchAPIType " + accountFetchAPIType
						+ " sent, it should be ORGADMIN or USERINFO");
			}

			PathParam inputOrgId = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(), AppConstants.INPUT_ORG_ID);

			if (AccountFetchAPITypes.ORGADMIN.toString().equalsIgnoreCase(accountFetchAPIType) && (null == inputOrgId
					|| (null != inputOrgId && StringUtils.isEmpty(inputOrgId.getParamValue())))) {

				throw new InvalidInputException(
						"InputOrgId is missing or cannot be null for ORGADMIN as accountsFetchAPIToUse");
			}

		} else {

			throw new InvalidInputException("accountFetchAPIType param is missing, it should be ORGADMIN or USERINFO");
		}

		PathParam refreshDataBaseParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
				AppConstants.REFRESH_DATA_BASE_FLAG);

		if (null != refreshDataBaseParam) {

			String refreshDataBaseValue = refreshDataBaseParam.getParamValue();

			if (!StringUtils.isEmpty(refreshDataBaseValue) && !("true".equalsIgnoreCase(refreshDataBaseValue)
					|| "false".equalsIgnoreCase(refreshDataBaseValue))) {

				throw new InvalidInputException("Wrong value sent for refreshDataBase -> " + refreshDataBaseValue
						+ ", it should be true or false");
			}
		}

	}

	public Map<String, Object> prepareInputParams(ReportRunArgs apiRunArgs) {

		Map<String, Object> inputParams = new HashMap<String, Object>();

		List<PathParam> pathParamRemainingList = apiRunArgs.getPathParams();

		pathParamRemainingList.forEach(pathParam -> {

			// For Online mode these values will come in the JSON Body
			// For Batch mode these values will be passed from BatchTriggerInformation
			inputParams.put(pathParam.getParamName(), pathParam.getParamValue());
		});

		PathParam accountsFetchAPIToUsePathParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
				AppConstants.ACCOUNTS_FETCH_API_TO_USE_TYPE);

		if (null != accountsFetchAPIToUsePathParam) {

			String accountsFetchAPIToUse = accountsFetchAPIToUsePathParam.getParamValue();

			if (!StringUtils.isEmpty(accountsFetchAPIToUse)
					&& AccountFetchAPITypes.ORGADMIN.toString().equalsIgnoreCase(accountsFetchAPIToUse)) {

				inputParams.put(AppConstants.INPUT_ORG_ID, ReportAppUtil
						.findPathParam(apiRunArgs.getPathParams(), AppConstants.INPUT_ORG_ID).getParamValue());
			}
		}

		PathParam refreshDataBaseParam = ReportAppUtil.findPathParam(apiRunArgs.getPathParams(),
				AppConstants.REFRESH_DATA_BASE_FLAG);

		if (null != refreshDataBaseParam) {

			String refreshDataBaseValue = refreshDataBaseParam.getParamValue();

			if (!StringUtils.isEmpty(refreshDataBaseValue)) {

				inputParams.put(AppConstants.REFRESH_DATA_BASE_FLAG, refreshDataBaseValue);
			}
		}

		log.info("inputParams from reportRunArgs is {}", inputParams);

		return inputParams;

	}

}