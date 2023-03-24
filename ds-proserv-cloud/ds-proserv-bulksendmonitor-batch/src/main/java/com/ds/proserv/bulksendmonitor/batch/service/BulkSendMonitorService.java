package com.ds.proserv.bulksendmonitor.batch.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.batch.common.service.CoreBatchDataService;
import com.ds.proserv.bulksendmonitor.batch.domain.BulkSendBatchMonitorResponse;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.BatchStatus;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.dsapi.common.service.AccountService;
import com.ds.proserv.feign.account.domain.AccountDefinition;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.domain.ApiHourlyLimitData;
import com.ds.proserv.feign.util.ApiLimitUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BulkSendMonitorService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private AccountService accountService;

	@Autowired
	private CoreBatchDataService coreBatchDataService;

	@Autowired
	private DSCacheManager dsCacheManager;

	public Map<String, List<BulkSendBatchMonitorResponse>> fetchBatchStatusesByBatchIds(List<String> batchIds,
			String coreBatchId) {

		List<BulkSendBatchMonitorResponse> completedList = new ArrayList<BulkSendBatchMonitorResponse>();
		List<BulkSendBatchMonitorResponse> completedWithErrorList = new ArrayList<BulkSendBatchMonitorResponse>();
		List<BulkSendBatchMonitorResponse> inProgressList = new ArrayList<BulkSendBatchMonitorResponse>();

		AccountDefinition accountDefinition = accountService.getAccount(
				dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSAPI_ACCOUNT_ID,
						PropertyCacheConstants.DS_API_REFERENCE_NAME));

		AuthenticationResponse authenticationResponse = accountService.getTokenForSystemUser();

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION,
				authenticationResponse.getTokenType() + " " + authenticationResponse.getAccessToken());
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		Integer apiThresholdLimitPercent = Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
				PropertyCacheConstants.DS_API_REFERENCE_NAME));

		for (String batchId : batchIds) {

			HttpEntity<String> httpEntity = new HttpEntity<String>(headers);

			try {

				String batchUrl = MessageFormat.format(accountDefinition.getBaseUri() + fetchBulkStatusEndpoint(),
						accountDefinition.getAccountId(), batchId);

				ResponseEntity<BulkSendBatchMonitorResponse> bulkSendBatchMonitorResponseRespEntity = restTemplate
						.exchange(batchUrl, HttpMethod.GET, httpEntity, BulkSendBatchMonitorResponse.class);

				if (null != bulkSendBatchMonitorResponseRespEntity.getBody()) {

					BulkSendBatchMonitorResponse bulkSendBatchMonitorResponse = bulkSendBatchMonitorResponseRespEntity
							.getBody();

					Integer batchSize = Integer.parseInt(bulkSendBatchMonitorResponse.getBatchSize());
					Integer sentSize = Integer.parseInt(bulkSendBatchMonitorResponse.getSent());
					Integer failedSize = Integer.parseInt(bulkSendBatchMonitorResponse.getFailed());

					if (batchSize == (sentSize + failedSize)) {

						if (failedSize > 0) {

							completedWithErrorList.add(bulkSendBatchMonitorResponse);
						} else {

							completedList.add(bulkSendBatchMonitorResponse);
						}
					} else {

						inProgressList.add(bulkSendBatchMonitorResponse);
					}

				}

				ApiLimitUtil.readApiHourlyLimitData(bulkSendBatchMonitorResponseRespEntity.getHeaders(),
						apiThresholdLimitPercent);

			} catch (HttpClientErrorException exp) {

				ApiHourlyLimitData apiHourlyLimitData = ApiLimitUtil.readApiHourlyLimitData(exp.getResponseHeaders(),
						apiThresholdLimitPercent);

				if (null != apiHourlyLimitData && !StringUtils.isEmpty(apiHourlyLimitData.getDocuSignTraceToken())) {

					log.error(
							"Failure happened for CoreBatchId -> {} and BulkSendBatch -> {}.For more analysis, you can check with DS Support and provide DocuSignTraceToken -> {}",
							coreBatchId, batchId, apiHourlyLimitData.getDocuSignTraceToken());
				}

				log.error(
						"Calling BulkSendMonitorService.fetchBatchStatusesByBatchId: Receive HttpClientErrorException {}, responseBody -> {}",
						exp.getStatusCode(), exp.getResponseBodyAsString());

				coreBatchDataService.createFailureProcess(
						batchId + AppConstants.RESTRICTED_CHARACTER_REPLACEMENT
								+ apiHourlyLimitData.getDocuSignTraceToken(),
						coreBatchId, null, exp, FailureCode.ERROR_106, FailureStep.BULKSEND_MONITOR);
			}
		}

		Map<String, List<BulkSendBatchMonitorResponse>> batchStatusMap = new HashMap<String, List<BulkSendBatchMonitorResponse>>();
		batchStatusMap.put(BatchStatus.COMPLETED.toString(), completedList);
		batchStatusMap.put(BatchStatus.COMPLETED_WITH_ERROR.toString(), completedWithErrorList);
		batchStatusMap.put(BatchStatus.INPROGRESS.toString(), inProgressList);

		return batchStatusMap;
	}

	private String fetchBulkStatusEndpoint() {

		String bulkStatusEndpoint = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.BULKSEND_MONITOR_DS_API_ENDPOINT, PropertyCacheConstants.DS_API_REFERENCE_NAME);

		if (StringUtils.isEmpty(bulkStatusEndpoint)) {

			return "/restapi/v2.1/accounts/{0}/bulk_send_lists/{1}?include=all";
		} else {

			return bulkStatusEndpoint;
		}
	}
}