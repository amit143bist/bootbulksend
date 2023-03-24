package com.ds.proserv.bulksend.common.helper;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.dsapi.common.service.AccountService;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DSEnvelopeFieldExtractor extends AbstractDSFieldExtractor {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private AccountService accountService;

	private static ConcurrentHashMap<String, List<Map<String, String>>> envelopeIdDataMap = new ConcurrentHashMap<String, List<Map<String, String>>>();

	ReentrantLock lock = new ReentrantLock();
	int counter = 0;

	public Map<String, List<Map<String, String>>> readEnvelopeAndPopulateMap(String baseUri, String accountGuid,
			String draftEnvelopeIdOrTemplateId, boolean isTemplate, String userId) {

		log.debug(" ******************** Calling readEnvelopeAndPopulateMap ******************** ");

		lock.lock();
		try {

			if (null == envelopeIdDataMap || envelopeIdDataMap.isEmpty()
					|| (null != envelopeIdDataMap && null == envelopeIdDataMap.get(draftEnvelopeIdOrTemplateId))) {

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

				HttpEntity<String> uri = new HttpEntity<String>(headers);

				String url = null;
				if (isTemplate) {

					log.info("Preparing readEnvelopeAndPopulateMap for templateId -> {}", draftEnvelopeIdOrTemplateId);
					String getEnvelopeEndpoint = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
							PropertyCacheConstants.DSAPI_GETTEMPLATE_ENDPOINT,
							PropertyCacheConstants.DS_API_REFERENCE_NAME);
					url = MessageFormat.format(baseUri + getEnvelopeEndpoint, accountGuid, draftEnvelopeIdOrTemplateId);
				} else {

					log.info("Preparing readEnvelopeAndPopulateMap for draftEnvelopeId -> {}",
							draftEnvelopeIdOrTemplateId);
					String getEnvelopeEndpoint = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
							PropertyCacheConstants.DSAPI_GETENVELOPE_ENDPOINT,
							PropertyCacheConstants.DS_API_REFERENCE_NAME);
					url = MessageFormat.format(baseUri + getEnvelopeEndpoint, accountGuid, draftEnvelopeIdOrTemplateId);
				}

				log.info(" Calling readEnvelopeAndPopulateMap with url >>>>> " + url);

				String envelopeJSON = restTemplate.exchange(url, HttpMethod.GET, uri, String.class).getBody();

				createMaps(draftEnvelopeIdOrTemplateId, envelopeIdDataMap, envelopeJSON);

			} else {

				log.info("draftEnvelopeIdOrTemplateId -> {} exist in envelopeIdDataMap", draftEnvelopeIdOrTemplateId);
			}

			if (log.isDebugEnabled()) {

				log.debug("EnvelopeIdDataMap>>> {} available and counter is {}", envelopeIdDataMap, counter);
			}
			counter++;
		} finally {
			lock.unlock();
		}

		return envelopeIdDataMap;
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

			throw new InvalidInputException("userId cannot be null in DSEnvelopeFieldExtractor");
		}

	}
}