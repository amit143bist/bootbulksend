package com.ds.proserv.envelopeapi.consumer.service;

import com.ds.proserv.broker.config.service.QueueService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.ListenerProcessingException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.dsapi.common.service.AccountService;
import com.ds.proserv.feign.account.domain.AccountDefinition;
import com.ds.proserv.feign.appdata.domain.ApplicationEnvelopeDefinition;
import com.ds.proserv.feign.appdata.domain.ApplicationEnvelopeMessageDefinition;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.domain.ApiHourlyLimitData;
import com.ds.proserv.feign.envelopeapi.domain.*;
import com.ds.proserv.feign.report.domain.ReportData;
import com.ds.proserv.feign.ruleengine.domain.RuleEngineDefinition;
import com.ds.proserv.feign.util.ApiLimitUtil;
import com.ds.proserv.feign.util.ReportDataRuleUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.script.ScriptEngine;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocuSignEnvelopeService {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private HttpHeaders headers;

	@Autowired
	private AccountService accountService;

	@Autowired
	private QueueService queueService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ScriptEngine scriptEngine;

	@Autowired()
	@Qualifier("landlordRuleEngineDefinition")
	RuleEngineDefinition landlordRuleEngineDefinition;

	@Autowired()
	@Qualifier("tenantRuleEngineDefinition")
	RuleEngineDefinition tenantRuleEngineDefinition;

	public CreateEnvelopeResponse createEnvelope(GenericEnvelopeMessageDefinition dsEnvelopeDefinition) {

		return createEnvelope(dsEnvelopeDefinition, "sent");
	}

	public CreateEnvelopeResponse createEnvelope(GenericEnvelopeMessageDefinition dsEnvelopeDefinition, String status) {

		log.info("DSEnvelopeApiService.createEnvelope() -> {}", dsEnvelopeDefinition.getApplicationId());

		List<String> missingInputParams = new ArrayList<>();

		if (StringUtils.isEmpty(dsEnvelopeDefinition.getApplicationType())) {
			missingInputParams.add("applicationType");
		}
		if (null == dsEnvelopeDefinition.getApplicationId()) {
			missingInputParams.add("applicationId");
		}
		if (!missingInputParams.isEmpty()) {

			String failureReason = "Missing Required params:" + missingInputParams.toString();
			log.error("createEnvelope() methods call, error: {} therefore we sending to queue ->  {}", failureReason,
					dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
							PropertyCacheConstants.PROCESS_APPLICATIONENVELOPE_QUEUE_NAME,
							PropertyCacheConstants.QUEUE_REFERENCE_NAME));

			queueService.createFailureMessageAndSend(AppConstants.REQUIRED_PARAM_MISSING, AppConstants.BATCHNOTCREATED,
					AppConstants.PROCESSNOTCREATED, failureReason, FailureCode.ERROR_224, FailureStep.CREATESENVELOPE);
			ListenerProcessingException exception = new ListenerProcessingException(failureReason);
			sendApplicationEnvelopeMessage(dsEnvelopeDefinition, exception, null);
			throw exception;
		}

		// From here we know that we have the required applicationId and applicationType
		AccountDefinition accountDefinition = accountService.getAccount(
				dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSAPI_ACCOUNT_ID,
						PropertyCacheConstants.DS_API_REFERENCE_NAME));
		Assert.notNull(accountDefinition.getBaseUri(), "BaseUrl was null");

		String uri = accountDefinition.getBaseUri() + "/restapi/v2.1/accounts/"
				+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSAPI_ACCOUNT_ID,
						PropertyCacheConstants.DS_API_REFERENCE_NAME)
				+ "/envelopes";
		log.info(" Calling createEnvelope with  URI -> {}", uri);

		HttpEntity<CreateEnvelopeRequest> requestEntity = new HttpEntity<>(
				buildEnvelopeRequest(dsEnvelopeDefinition, status), setHeaders());

		Integer apiThresholdLimitPercent = Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
				PropertyCacheConstants.DS_API_REFERENCE_NAME));

		try {

			return Optional.of(restTemplate.exchange(uri, HttpMethod.POST, requestEntity, CreateEnvelopeResponse.class))
					.map(createEnvelopeResponse -> {

						Assert.notNull(createEnvelopeResponse.getBody(),
								"No recipient return for envelope  " + dsEnvelopeDefinition.getApplicationId());
						Assert.isTrue(createEnvelopeResponse.getStatusCode().is2xxSuccessful(),
								"createEnvelope  is not returned with 200 status code");
						ApiLimitUtil.readApiHourlyLimitData(createEnvelopeResponse.getHeaders(),
								apiThresholdLimitPercent);
						log.info("createEnvelope: Envelope ID -> {} with status -> {}",
								createEnvelopeResponse.getBody().envelopeId, status);
						log.info("Posting Envelope ID -> {} with status -> {} to recorded in queue -> {}",
								createEnvelopeResponse.getBody().envelopeId, status,
								dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
										PropertyCacheConstants.PROCESS_APPLICATIONENVELOPE_QUEUE_NAME,
										PropertyCacheConstants.QUEUE_REFERENCE_NAME));
						// post it to a queue here
						sendApplicationEnvelopeMessage(dsEnvelopeDefinition, null, createEnvelopeResponse.getBody());
						return createEnvelopeResponse.getBody();

					}).orElseThrow(() -> new ResourceNotFoundException(
							"Unable to create envelope for application " + dsEnvelopeDefinition.getApplicationId()));
		} catch (HttpClientErrorException exp) {

			log.error("createEnvelope: Receive HttpClientErrorException {}, responseBody -> {}", exp.getStatusCode(),
					exp.getResponseBodyAsString());

			ApiHourlyLimitData apiHourlyLimitData = ApiLimitUtil.readApiHourlyLimitData(exp.getResponseHeaders(),
					apiThresholdLimitPercent);

			if (exp.getResponseBodyAsString().contains(AppConstants.DSAPI_HOURLY_LIMIT_EXCEEDED_ERROR)
					&& apiHourlyLimitData.isSleepThread()) {
				log.error("We will retry to create the Envelope  for application -> {} once the thread wake up.",
						dsEnvelopeDefinition.getApplicationId());
				return createEnvelope(dsEnvelopeDefinition, status);
			} else {
				log.error(
						"We have received another exception and thread was not sleep therefore we sending it to  {} quueue.",
						dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
								PropertyCacheConstants.PROCESS_APPLICATIONENVELOPE_QUEUE_NAME,
								PropertyCacheConstants.QUEUE_REFERENCE_NAME));
				sendApplicationEnvelopeMessage(dsEnvelopeDefinition, exp, null);
				queueService.createFailureMessageAndSend(
						dsEnvelopeDefinition.getApplicationType() + "_" + dsEnvelopeDefinition.getApplicationId(),
						AppConstants.BATCHNOTCREATED, AppConstants.PROCESSNOTCREATED, exp, FailureCode.ERROR_224,
						FailureStep.CREATESENVELOPE);
				throw new ListenerProcessingException(exp.getMessage());
			}
		}
	}

	private void sendApplicationEnvelopeMessage(GenericEnvelopeMessageDefinition genericEnvelopeMessageDefinition,
			Throwable ex, CreateEnvelopeResponse envelopeResponse) {

		ApplicationEnvelopeDefinition applicationEnvelopeDefinition = new ApplicationEnvelopeDefinition();
		applicationEnvelopeDefinition
				.setApplicationId(String.valueOf(genericEnvelopeMessageDefinition.getApplicationId()));
		applicationEnvelopeDefinition.setApplicationType(genericEnvelopeMessageDefinition.getApplicationType());

		Item communityPartnerCodeItem = genericEnvelopeMessageDefinition.getRecipients().stream().flatMap(
				recipient -> recipient.getItems().stream().filter(item -> StringUtils.isNotEmpty(item.getDataValue())
						&& AppConstants.APPLICATION_COMMUNITYPARTNER_CODE.equalsIgnoreCase(item.getDataLabel())))
				.findFirst().orElse(null);

		if (null != communityPartnerCodeItem) {

			log.info("Retrieved community partner code -> {}", communityPartnerCodeItem.getDataValue());
			applicationEnvelopeDefinition.setCommunityPartnerCode(communityPartnerCodeItem.getDataValue());
		}

		if (null != ex) {
			applicationEnvelopeDefinition.setFailureReason(ex.getMessage());
			applicationEnvelopeDefinition.setFailureTimestamp(LocalDateTime.now().toString());
		}
		if (null != envelopeResponse) {
			applicationEnvelopeDefinition.setEnvelopeSentTimestamp(
					DateTimeUtil.convertToSQLDateTimeFromDateTimeAsString(envelopeResponse.getStatusDateTime(), null));
			applicationEnvelopeDefinition.setEnvelopeId(envelopeResponse.getEnvelopeId());
		}

		List<String> recipientEmails = genericEnvelopeMessageDefinition.getRecipients().stream()
				.map(Recipient::getEmail).collect(Collectors.toList());
		applicationEnvelopeDefinition.setRecipientEmails(recipientEmails);

		List<ApplicationEnvelopeDefinition> applicationEnvelopeDefinitionList = new ArrayList<>();
		applicationEnvelopeDefinitionList.add(applicationEnvelopeDefinition);

		ApplicationEnvelopeMessageDefinition applicationEnvelopeMessageDefinition = new ApplicationEnvelopeMessageDefinition();
		applicationEnvelopeMessageDefinition.setApplicationEnvelopeDefinitions(applicationEnvelopeDefinitionList);
		applicationEnvelopeMessageDefinition.setProcessId(AppConstants.PROCESSNOTCREATED);
		log.info("Sending message to -> {} for application envelope -> {} created by community partner Code -> {}",
				PropertyCacheConstants.PROCESS_APPLICATIONENVELOPE_QUEUE_NAME,
				applicationEnvelopeDefinition.getEnvelopeId(), applicationEnvelopeDefinition.getCommunityPartnerCode());
		queueService.findQueueNameAndSend(PropertyCacheConstants.PROCESS_APPLICATIONENVELOPE_QUEUE_NAME,
				applicationEnvelopeMessageDefinition);
	}

	private HttpHeaders setHeaders() {

		log.debug("AuthenticationResponse is called for user -> {}",
				dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSAUTH_USERID,
						PropertyCacheConstants.TOKEN_PROP_REFERENCE_NAME));

		headers.setContentType(MediaType.APPLICATION_JSON);

		AuthenticationResponse authenticationResponse = accountService.getTokenForSystemUser();

		Assert.notNull(authenticationResponse.getAccessToken(), "AuthenticationResponse.token response was null");

		log.debug("Retrieve token for system user ->  ", authenticationResponse.getAccessToken());
		headers.set(HttpHeaders.AUTHORIZATION,
				authenticationResponse.getTokenType() + " " + authenticationResponse.getAccessToken());
		return headers;

	}

	private String getTemplateId(GenericEnvelopeMessageDefinition ihdaApplicationDefinition) {

		log.info("ApplicationType -> {}", ihdaApplicationDefinition.getApplicationType());

		if (StringUtils.equalsIgnoreCase(ihdaApplicationDefinition.getApplicationType(), AppConstants.ROLE_TENANT)) {
			String tenantTemplate = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.SEND_TENANT_TEMPLATE, PropertyCacheConstants.DS_API_REFERENCE_NAME);
			log.info("Creating envelope request using tenant template -> {}", tenantTemplate);

			return tenantTemplate;
		}
		if (StringUtils.equalsIgnoreCase(ihdaApplicationDefinition.getApplicationType(), AppConstants.ROLE_LANDLORD)) {
			String landlordTemplate = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.SEND_LANDLORD_TEMPLATE, PropertyCacheConstants.DS_API_REFERENCE_NAME);

			log.info("Creating envelope request using landlord template -> {}", landlordTemplate);

			return landlordTemplate;
		}
		return null;
	}

	private CreateEnvelopeRequest buildEnvelopeRequest(GenericEnvelopeMessageDefinition envelopeDefinition,
			String status) {

		log.info(
				"Application retrieved for application ID -> {}, type -> {}, Landlord name -> {} ({}), tenant name -> {} ({})",
				envelopeDefinition.getApplicationId(), envelopeDefinition.getApplicationType(),
				envelopeDefinition.getRecipients().get(0).getFullName(),
				envelopeDefinition.getRecipients().get(0).getEmail(),
				envelopeDefinition.getRecipients().get(1).getFullName(),
				envelopeDefinition.getRecipients().get(1).getEmail());
		try {
			String bulkSendPrepareDefinitionJson;
			bulkSendPrepareDefinitionJson = objectMapper.writeValueAsString(envelopeDefinition);

			List<List<ReportData>> reportRowsList = new ArrayList<>();
			if (envelopeDefinition.getApplicationType().equalsIgnoreCase(AppConstants.ROLE_LANDLORD)) {
				reportRowsList
						.add(ReportDataRuleUtil.prepareColumnDataMap(landlordRuleEngineDefinition.getOutputColumns(),
								bulkSendPrepareDefinitionJson, "$", null, null, scriptEngine));
			} else {
				reportRowsList
						.add(ReportDataRuleUtil.prepareColumnDataMap(tenantRuleEngineDefinition.getOutputColumns(),
								bulkSendPrepareDefinitionJson, "$", null, null, scriptEngine));
			}

			CreateEnvelopeRequest request = new CreateEnvelopeRequest();

			CompositeTemplate compositeTemplate = new CompositeTemplate();

			ServerTemplate serverTemplate = new ServerTemplate();
			serverTemplate.setTemplateId(getTemplateId(envelopeDefinition));
			serverTemplate.setSequence(1);

			compositeTemplate.getServerTemplates().add(serverTemplate);

			InlineTemplate inlineTemplate = new InlineTemplate();
			inlineTemplate.setSequence(2);

			AtomicInteger recipientId = new AtomicInteger(1);
			Recipients recipients = new Recipients();

			envelopeDefinition.getRecipients().parallelStream().forEach(recipient -> {

				Signer signer = new Signer();
				signer.setEmail(recipient.getEmail());
				signer.setName(recipient.getFullName());
				signer.setRecipientId(String.valueOf(recipientId.getAndIncrement()));
				signer.setRoleName(recipient.getRoleName());

				log.info("Creating Signer for rolename -> {}", recipient.getRoleName());

				CustomFields customFields = new CustomFields();

				if (!reportRowsList.isEmpty()) {
					for (ReportData reportData : reportRowsList.get(0)) {
						if (null != reportData.getReportColumnValue()) {

							if (!reportData.getReportColumnName().contains("::")) {

								TextCustomField textCustomField = prepareEnvelopeCustomField(reportData);

								customFields.getTextCustomFields().add(textCustomField);
								inlineTemplate.setCustomFields(customFields);
							} else {
								String roleName = DSUtil.getRoleName(reportData.getReportColumnName());
								if (roleName.equalsIgnoreCase(recipient.getRoleName())) {
									String dataLabel = DSUtil.getDataLabel(reportData.getReportColumnName());
									TextTab textTab = new TextTab();
									textTab.setTabLabel("\\*" + dataLabel);
									textTab.setValue(String.valueOf(reportData.getReportColumnValue()));
									signer.getTabs().getTextTabs().add(textTab);
									if (log.isDebugEnabled()) {
										log.debug("Creating TextField with label -> {}, and value -> {}", textTab.getTabLabel(), textTab.getValue());
									}
								}

							}
						}
					}
				}

				recipients.getSigners().add(signer);
			});

			inlineTemplate.setRecipients(recipients);

			compositeTemplate.getInlineTemplates().add(inlineTemplate);
			request.getCompositeTemplates().add(compositeTemplate);
			request.setStatus(status);
			return request;
		} catch (JsonParseException e) {
			log.error("JsonParseException -> {} caught for applicationId -> {} and applicationType -> {}", e,
					envelopeDefinition.getApplicationId(), envelopeDefinition.getApplicationType());
			e.printStackTrace();
			throw new ListenerProcessingException(e.getMessage());
		} catch (JsonMappingException e) {
			log.error("JsonMappingException -> {} caught for applicationId -> {} and applicationType -> {}", e,
					envelopeDefinition.getApplicationId(), envelopeDefinition.getApplicationType());
			e.printStackTrace();
			throw new ListenerProcessingException(e.getMessage());
		} catch (IOException e) {
			log.error("IOException -> {} caught for applicationId -> {} and applicationType -> {}", e,
					envelopeDefinition.getApplicationId(), envelopeDefinition.getApplicationType());
			e.printStackTrace();
			throw new ListenerProcessingException(e.getMessage());
		}
	}

	private TextCustomField prepareEnvelopeCustomField(ReportData reportData) {

		TextCustomField textCustomField = new TextCustomField();
		textCustomField.setName(reportData.getReportColumnName());
		textCustomField.setShow("true");
		textCustomField.setRequired("false");
		textCustomField.setValue(String.valueOf(reportData.getReportColumnValue()));
		return textCustomField;
	}

}