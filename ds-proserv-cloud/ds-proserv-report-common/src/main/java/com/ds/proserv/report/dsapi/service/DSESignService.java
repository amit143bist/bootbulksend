package com.ds.proserv.report.dsapi.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.feign.authentication.domain.AuthenticationRequest;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.domain.ApiHourlyLimitData;
import com.ds.proserv.feign.dsapi.domain.DocumentResponse;
import com.ds.proserv.feign.dsapi.domain.EnvelopeDocument;
import com.ds.proserv.feign.util.ApiLimitUtil;
import com.ds.proserv.feign.util.ReportAppUtil;
import com.ds.proserv.report.auth.cache.DSAuthorizationCache;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DSESignService {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DSAuthorizationCache dsAuthorizationCache;

	public List<EnvelopeDocument> fetchAllDocuments(AuthenticationRequest authenticationRequest, String envelopeId) {

		log.debug("Retrieving documents from accountId -> {} for envelopeId -> {}",
				authenticationRequest.getAccountGuid(), envelopeId);

		AuthenticationResponse authenticationResponse = dsAuthorizationCache.requestToken(authenticationRequest);

		Assert.notNull(authenticationResponse, "AuthenticationResponse is empty");
		HttpEntity<String> httpEntity = ReportAppUtil.prepareHTTPEntity(authenticationResponse,
				MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE);

		try {
			return Optional.ofNullable(restTemplate.exchange(
					dsAuthorizationCache.requestBaseUrl(authenticationRequest) + "/accounts/"
							+ authenticationRequest.getAccountGuid() + "/envelopes/" + envelopeId + "/"
							+ dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
									PropertyCacheConstants.REPORT_ESIGN_API_DOC_ENDPOINT,
									PropertyCacheConstants.DS_API_REFERENCE_NAME),
					HttpMethod.GET, httpEntity, DocumentResponse.class)).map(documentResponse -> {

						Assert.isTrue(documentResponse.getStatusCode().is2xxSuccessful(),
								"Docusign userInfo data is not returned with 2xx status code");
						Assert.notNull(documentResponse.getBody(), "documentResponse is null");

						ApiLimitUtil.readApiHourlyLimitData(documentResponse.getHeaders(),
								Integer.valueOf(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
										PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
										PropertyCacheConstants.DS_API_REFERENCE_NAME)));

						return documentResponse.getBody().getEnvelopeDocuments();

					}).orElseThrow(() -> new ResourceNotFoundException(
							"documentResponse not retured for envelopeId " + envelopeId));
		} catch (HttpClientErrorException exp) {

			ApiHourlyLimitData apiHourlyLimitData = ApiLimitUtil.readApiHourlyLimitData(exp.getResponseHeaders(),
					Integer.valueOf(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
							PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
							PropertyCacheConstants.DS_API_REFERENCE_NAME)));

			if (null != apiHourlyLimitData && !StringUtils.isEmpty(apiHourlyLimitData.getDocuSignTraceToken())) {

				log.info("For more analysis, you can check with DS Support and provide DocuSignTraceToken -> {}",
						apiHourlyLimitData.getDocuSignTraceToken());
			}

			log.info(
					"Calling DSEnvelopeService.fetchAllDocuments: Receive HttpClientErrorException {}, responseBody -> {}",
					exp.getStatusCode(), exp.getResponseBodyAsString());
			throw new ResourceNotFoundException("Unable to call DSEnvelopeService.fetchAllDocuments", exp);

		}
	}

}