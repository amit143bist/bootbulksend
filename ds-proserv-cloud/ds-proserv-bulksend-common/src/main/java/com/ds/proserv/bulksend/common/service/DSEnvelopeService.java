package com.ds.proserv.bulksend.common.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

import com.ds.proserv.bulksend.common.domain.BulkSendListItem;
import com.ds.proserv.bulksend.common.domain.EnvelopeBatchItem;
import com.ds.proserv.bulksend.common.domain.EnvelopeBatchTestItem;
import com.ds.proserv.bulksend.common.domain.EnvelopeItem;
import com.ds.proserv.bulksend.common.helper.DSEnvelopeCreateAsyncHelper;
import com.ds.proserv.bulksend.common.helper.DSEnvelopeFieldExtractor;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.dsapi.common.service.AccountService;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.domain.ApiHourlyLimitData;
import com.ds.proserv.feign.util.ApiLimitUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DSEnvelopeService {

	@Autowired
	protected DSCacheManager dsCacheManager;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DSEnvelopeFieldExtractor dsEnvelopeFieldExtractor;

	@Autowired
	private DSEnvelopeCreateAsyncHelper dsEnvelopeCreateAsyncHelper;

	@Autowired
	private AccountService accountService;

	private static Configuration pathConfiguration = Configuration.builder()
			.options(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL).build();

	public String createEnvelopeTemplate(String baseUri, String draftEnvelopeFilePath, String accountGuid,
			String userId) throws FileNotFoundException {

		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines(Paths.get(draftEnvelopeFilePath), StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String jsonReqBody = contentBuilder.toString();

		HttpEntity<String> uri = new HttpEntity<String>(jsonReqBody, prepareHttpHeaders(userId));

		String envCreateApi = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSAPI_CREATEENVELOPE_ENDPOINT, PropertyCacheConstants.DS_API_REFERENCE_NAME);

		String createEnvelopeUrl = MessageFormat.format(baseUri + envCreateApi, accountGuid);

		ResponseEntity<EnvelopeItem> envelopeItemResponseEntity = restTemplate.exchange(createEnvelopeUrl,
				HttpMethod.POST, uri, EnvelopeItem.class);

		log.info("Draft EnvelopeId created is -> {}", envelopeItemResponseEntity.getBody().getEnvelopeId());

		return envelopeItemResponseEntity.getBody().getEnvelopeId();

	}

	private HttpHeaders prepareHttpHeaders(String userId) {

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
		return headers;
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
		} else {

			throw new InvalidInputException("userId cannot be null");
		}

	}

	public String createBulkSendListJSON(EnvelopeBatchItem envelopeBatchItemToProcess, String batchName, String baseUri,
			String draftEnvelopeIdOrTemplateId, boolean isTemplate, String bulkSendHeaderLine, String accountGuid,
			String userId) {

		log.info("Processing starting for {}", batchName);
		List<ObjectNode> inlineTemplateNodeList = new ArrayList<>(envelopeBatchItemToProcess.getRowDataList().size());

		Map<String, List<Map<String, String>>> envelopeIdDataMap = dsEnvelopeFieldExtractor
				.readEnvelopeAndPopulateMap(baseUri, accountGuid, draftEnvelopeIdOrTemplateId, isTemplate, userId);

		List<Map<String, String>> envelopeIdDataList = envelopeIdDataMap.get(draftEnvelopeIdOrTemplateId);
		if (null == envelopeIdDataList) {

			log.warn(
					"Something went wrong in fetching template or draftenvelope data for draftEnvelopeIdOrTemplateId -> {}",
					draftEnvelopeIdOrTemplateId);
			throw new InvalidInputException(
					"Something went wrong in fetching template or draftenvelope data for draftEnvelopeIdOrTemplateId "
							+ draftEnvelopeIdOrTemplateId);
		}

		for (String[] item : envelopeBatchItemToProcess.getRowDataList()) {

			ObjectNode createInlineTemplateNode = dsEnvelopeCreateAsyncHelper.createInlineTemplateRequestNode(item,
					"created", batchName, bulkSendHeaderLine, envelopeIdDataList);

			log.debug("envelopeReqBodyJSON {} for batchName is {}", createInlineTemplateNode, batchName);

			if (null == createInlineTemplateNode) {

				log.warn("Something went wrong in converting row data to an element of BulkCopies for rowData -> {}",
						item.toString());
				throw new InvalidInputException(
						"Something went wrong in converting row data to an element of BulkCopies for rowData " + item);
			}

			inlineTemplateNodeList.add(createInlineTemplateNode);
		}

		String bulkSendListJSON = dsEnvelopeCreateAsyncHelper.createBulkSendJSON(inlineTemplateNodeList, batchName);
		log.info("bulkSendListJSON {} for batchName -> {}", bulkSendListJSON, batchName);

		HttpEntity<String> uri = new HttpEntity<String>(bulkSendListJSON, prepareHttpHeaders(userId));

		String bulkSendMailingListEndpoint = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSAPI_BULKSEND_CREATE_MAILINGLIST_ENDPOINT,
				PropertyCacheConstants.DS_API_REFERENCE_NAME);

		String url = MessageFormat.format(baseUri + bulkSendMailingListEndpoint, accountGuid);

		ResponseEntity<BulkSendListItem> responseEntity = restTemplate.exchange(url, HttpMethod.POST, uri,
				BulkSendListItem.class);

		BulkSendListItem bulkSendListItem = responseEntity.getBody();
		String listId = bulkSendListItem.getListId();

		String apiThresholdLimitPercentAsStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
				PropertyCacheConstants.DS_API_REFERENCE_NAME);
		ApiLimitUtil.readApiHourlyLimitData(responseEntity.getHeaders(),
				Integer.valueOf(apiThresholdLimitPercentAsStr));

		return listId;
	}

	public EnvelopeBatchTestItem testBulkSendListWithEnvelopeOrTemplateId(String listId,
			String draftEnvelopeIdOrTemplateId, String baseUri, String accountGuid, String userId) {

		String envelopeOrTemplateMsgBody = "{envelopeOrTemplateId:" + "\"" + draftEnvelopeIdOrTemplateId + "\"" + "}";
		HttpEntity<String> uri = new HttpEntity<String>(envelopeOrTemplateMsgBody, prepareHttpHeaders(userId));

		log.info("Inside testBulkSendListWithEnvelopeOrTemplateId for listId -> {}", listId);

		String bulkSendTestEndpoint = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSAPI_BULKSEND_TEST_ENDPOINT, PropertyCacheConstants.DS_API_REFERENCE_NAME);

		String testUrl = MessageFormat.format(baseUri + bulkSendTestEndpoint, accountGuid, listId);

		ResponseEntity<EnvelopeBatchTestItem> envelopeBatchTestItemResponseEntity = restTemplate.exchange(testUrl,
				HttpMethod.POST, uri, EnvelopeBatchTestItem.class);

		String apiThresholdLimitPercentAsStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
				PropertyCacheConstants.DS_API_REFERENCE_NAME);

		ApiLimitUtil.readApiHourlyLimitData(envelopeBatchTestItemResponseEntity.getHeaders(),
				Integer.valueOf(apiThresholdLimitPercentAsStr));

		return envelopeBatchTestItemResponseEntity.getBody();
	}

	public EnvelopeBatchItem sendBulkSendListWithEnvelopeOrTemplateId(String listId, HttpEntity<String> uri,
			Boolean canBeSent, int counter, String baseUri, String batchName, String accountGuid) {

		EnvelopeBatchItem envelopeBatchItem = null;

		String bulkSendSendEndpoint = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DSAPI_BULKSEND_SEND_ENDPOINT, PropertyCacheConstants.DS_API_REFERENCE_NAME);

		String sendUrl = MessageFormat.format(baseUri + bulkSendSendEndpoint, accountGuid, listId);
		try {

			log.info(
					"Inside sendBulkSendListWithEnvelopeOrTemplateId for listId -> {} canBeSent value is {} sendUrl -> {} and counter value is -> {}",
					listId, canBeSent, sendUrl, counter);
			if (null != canBeSent && canBeSent) {

				ResponseEntity<EnvelopeBatchItem> envelopeBatchItemResponseEntity = restTemplate.exchange(sendUrl,
						HttpMethod.POST, uri, EnvelopeBatchItem.class);

				if (null != envelopeBatchItemResponseEntity) {

					envelopeBatchItem = envelopeBatchItemResponseEntity.getBody();

					HttpHeaders responseHeaders = envelopeBatchItemResponseEntity.getHeaders();

					String apiThresholdLimitPercentAsStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
							PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
							PropertyCacheConstants.DS_API_REFERENCE_NAME);

					ApiLimitUtil.readApiHourlyLimitData(responseHeaders,
							Integer.valueOf(apiThresholdLimitPercentAsStr));

					envelopeBatchItem.setSuccess(true);
					envelopeBatchItem.setTransMessage("Success");
					envelopeBatchItem.setBatchName(batchName);
				}
			}
		} catch (Exception exp) {

			log.debug("Exception caused by {} with error message {}", exp, exp.getMessage());
			if (exp instanceof HttpClientErrorException) {

				HttpClientErrorException clientExp = (HttpClientErrorException) exp;

				log.debug("Exception ResponseBody " + clientExp.getResponseBodyAsString());

				//
				if ((clientExp.getResponseBodyAsString().contains("errorDetails")
						&& clientExp.getResponseBodyAsString().contains("envelopes waiting"))) {

					ReadContext ctx = JsonPath.using(pathConfiguration).parse(clientExp.getResponseBodyAsString());

					List<String> errorDetails = ctx.read("$.errorDetails");

					String queueError = null;

					for (String error : errorDetails) {

						if (error.contains("envelopes waiting")) {

							queueError = error;
						}
					}

					List<String> queueDetails = new ArrayList<>();
					Pattern p = Pattern.compile("\\d+");
					Matcher m = p.matcher(queueError);
					while (m.find()) {

						queueDetails.add(m.group());
					}

					Long sleepValue = Long.parseLong(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
							PropertyCacheConstants.DSBULKSEND_BACKSOFF_INTERVAL,
							PropertyCacheConstants.BULKSEND_REFERENCE_NAME));
					if (null != queueDetails && !queueDetails.isEmpty() && queueDetails.size() >= 2) {

						log.info("Maximum Queue size is " + queueDetails.get(0)
								+ " current envelope pending to be processed is " + queueDetails.get(1)
								+ " so sending thread to sleep for " + (sleepValue * counter) + " milliseconds");
					}

					try {

						Thread.sleep(sleepValue * counter);
						return sendBulkSendListWithEnvelopeOrTemplateId(listId, uri, canBeSent, ++counter, baseUri,
								batchName, accountGuid);

					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} else if (clientExp.getResponseBodyAsString()
						.contains(AppConstants.DSAPI_HOURLY_LIMIT_EXCEEDED_ERROR)) {

					log.warn("DS API Hourly limit exceeded, so checking if threads needs to put to sleep or not");
					String apiThresholdLimitPercentAsStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
							PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
							PropertyCacheConstants.DS_API_REFERENCE_NAME);

					ApiHourlyLimitData apiHourlyLimitData = ApiLimitUtil.readApiHourlyLimitData(
							clientExp.getResponseHeaders(), Integer.valueOf(apiThresholdLimitPercentAsStr));
					if (apiHourlyLimitData.isSleepThread()) {

						log.warn(
								"DS API Hourly limit exceeded, thread just workup and reprocessing the BulkSend message");
						return sendBulkSendListWithEnvelopeOrTemplateId(listId, uri, canBeSent, ++counter, baseUri,
								batchName, accountGuid);
					}

				} else
					throw exp;

			}
		}

		return envelopeBatchItem;
	}

}