package com.ds.proserv.bulksend.common.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.ds.proserv.bulksend.common.domain.EnvelopeBatchItem;
import com.ds.proserv.bulksend.common.domain.EnvelopeBatchTestItem;
import com.ds.proserv.bulksend.common.service.DSEnvelopeService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.dsapi.common.service.AccountService;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DSEnvCreateAsyncAPIProcessor {

	@Autowired
	protected DSCacheManager dsCacheManager;

	@Autowired
	private AccountService accountService;

	@Autowired
	private DSEnvelopeService dsEnvelopeService;

	public EnvelopeBatchItem process(EnvelopeBatchItem envelopeBatchItemToProcess, String baseUri,
			String draftEnvelopeIdOrTemplateId, String batchName, String batchId, String processId, boolean isTemplate,
			String bulkSendHeaderLine, String accountGuid, String userId) {

		log.info("Preparing Data for BulkSend for processId -> {} and batchId -> {}", processId, batchId);

		String listId = null;
		HttpEntity<String> uri = null;
		EnvelopeBatchItem envelopeBatchItem = null;
		try {

			AuthenticationResponse authenticationResponse = null;
			AuthenticationRequest authenticationRequest = createAuthenticationRequest(userId);

			if (null != authenticationRequest) {
				authenticationResponse = accountService.getTokenForUser(authenticationRequest);
			} else {

				authenticationResponse = accountService.getTokenForSystemUser();
			}

			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.AUTHORIZATION,
					authenticationResponse.getTokenType() + " " + authenticationResponse.getAccessToken());
			headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

			listId = dsEnvelopeService.createBulkSendListJSON(envelopeBatchItemToProcess, batchName, baseUri,
					draftEnvelopeIdOrTemplateId, isTemplate, bulkSendHeaderLine, accountGuid, userId);

			log.info("BulkSendListId is {}", listId);
			log.info("EnvelopeOrTemplateId is {}", draftEnvelopeIdOrTemplateId);

			String envelopeOrTemplateMsgBody = "{envelopeOrTemplateId:" + "\"" + draftEnvelopeIdOrTemplateId + "\""
					+ "}";
			uri = new HttpEntity<String>(envelopeOrTemplateMsgBody, headers);

			EnvelopeBatchTestItem envelopeBatchTestItem = dsEnvelopeService.testBulkSendListWithEnvelopeOrTemplateId(
					listId, draftEnvelopeIdOrTemplateId, baseUri, accountGuid, userId);

			if (null != envelopeBatchTestItem && envelopeBatchTestItem.getCanBeSent()) {

				envelopeBatchItem = dsEnvelopeService.sendBulkSendListWithEnvelopeOrTemplateId(listId, uri,
						envelopeBatchTestItem.getCanBeSent(), 1, baseUri, batchName, accountGuid);
				envelopeBatchItem.setListId(listId);
			} else {

				envelopeBatchItem = new EnvelopeBatchItem();
				envelopeBatchItem.setBatchName(batchName);
				envelopeBatchItem.setSuccess(false);
				envelopeBatchItem.setErrorDetails(envelopeBatchTestItem.getValidationErrorDetails());
			}

			log.info("Preparing completed for BulkSend for processId -> {} and batchId -> {}", processId, batchId);
		} catch (Exception exp) {

			log.info("Error happened in BulkSend for processId -> {} and batchId -> {}", processId, batchId);
			exp.printStackTrace();

			envelopeBatchItem = new EnvelopeBatchItem();
			envelopeBatchItem.setBatchName(batchName);
			envelopeBatchItem.setSuccess(false);

			List<String> errorDetails = new ArrayList<String>();
			errorDetails.add(exp.getMessage());
			envelopeBatchItem.setErrorDetails(errorDetails);
		}
		return envelopeBatchItem;
	}

	private AuthenticationRequest createAuthenticationRequest(String userId) {

		String dsBulkSendScopes = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSBULKSEND_SCOPES, PropertyCacheConstants.BULKSEND_REFERENCE_NAME);

		if (!StringUtils.isEmpty(userId)) {

			AuthenticationRequest authenticationRequest = new AuthenticationRequest();
			authenticationRequest.setUser(userId);

			if (!StringUtils.isEmpty(dsBulkSendScopes)) {

				authenticationRequest.setScopes(dsBulkSendScopes);
			} else {

				authenticationRequest.setScopes(AppConstants.ESIGN_SCOPES);
			}

			return authenticationRequest;
		}

		return null;
	}

}