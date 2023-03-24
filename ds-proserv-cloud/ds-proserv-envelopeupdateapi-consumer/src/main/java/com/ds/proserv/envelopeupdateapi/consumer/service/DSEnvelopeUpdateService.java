package com.ds.proserv.envelopeupdateapi.consumer.service;

import com.ds.proserv.broker.config.service.QueueService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.dsapi.common.service.AccountService;
import com.ds.proserv.feign.account.domain.AccountDefinition;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.domain.ApiHourlyLimitData;
import com.ds.proserv.feign.envelopeupdateapi.domain.EnvelopeUpdateResponse;
import com.ds.proserv.feign.envelopeupdateapi.domain.UpdateEnvelopeRequestMessageDefinition;
import com.ds.proserv.feign.util.ApiLimitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class DSEnvelopeUpdateService {

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

    public EnvelopeUpdateResponse updateEnvelope(UpdateEnvelopeRequestMessageDefinition updateEnvelopeRequestMessageDefinition) {
        Assert.notNull(updateEnvelopeRequestMessageDefinition, "docuSignInformation was null");
        Assert.notNull(updateEnvelopeRequestMessageDefinition.getPayload(), "updateEnvelopeRequestMessageDefinition payload was null");
        Assert.notNull(updateEnvelopeRequestMessageDefinition.getEnvelopeId(), "updateEnvelopeRequestMessageDefinition.getEnvelopeID was null");
        log.info("Updating envelope -> {}", updateEnvelopeRequestMessageDefinition.getEnvelopeId());

        AccountDefinition accountDefinition = accountService.getAccount(
                dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSAPI_ACCOUNT_ID,
                        PropertyCacheConstants.DS_API_REFERENCE_NAME));
        Assert.notNull(accountDefinition.getBaseUri(), "BaseUrl was null");

        //https://{{EnvironmentVal}}/restapi/v2.1/accounts/{{AccountIdVal}}/envelopes/{{EnvelopeId}}?advanced_update=true&resend_envelope=true
        String uri = accountDefinition.getBaseUri() + "/restapi/v2.1/accounts/"
                + dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(PropertyCacheConstants.DSAPI_ACCOUNT_ID,
                PropertyCacheConstants.DS_API_REFERENCE_NAME)
                + "/envelopes/{envelope}?advanced_update=true&resend_envelope=true";
        log.info(" Calling updateEnvelope with  URI -> {}", uri);

        HttpEntity<String> requestEntity = new HttpEntity<>(updateEnvelopeRequestMessageDefinition.getPayload(), setHeaders());

        Integer apiThresholdLimitPercent = Integer.parseInt(dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
                PropertyCacheConstants.REPORT_API_THRESHOLD_LIMIT_PERCENT,
                PropertyCacheConstants.DS_API_REFERENCE_NAME));

        Map<String, String> params = new HashMap<String, String>();
        params.put("envelope", updateEnvelopeRequestMessageDefinition.getEnvelopeId());

        try {
            return Optional.of(restTemplate.exchange(uri, HttpMethod.PUT, requestEntity,
                    EnvelopeUpdateResponse.class, params))
                    .map(envelopeUpdateResponse -> {

                        ApiLimitUtil.readApiHourlyLimitData(envelopeUpdateResponse.getHeaders(),
                                apiThresholdLimitPercent);
                        Assert.notNull( envelopeUpdateResponse.getBody(), "Update response was null");
                        Assert.notNull( envelopeUpdateResponse.getBody().getEnvelopeId(), "Envelope was not updated successfully");
                        log.info("updated Envelope: Envelope ID -> {} ",
                                envelopeUpdateResponse.getBody().getEnvelopeId());
                        return envelopeUpdateResponse.getBody();

                    }).orElseThrow(() -> new ResourceNotFoundException(
                            "Unable to update envelope ->  " + updateEnvelopeRequestMessageDefinition.getEnvelopeId()));
        } catch (HttpClientErrorException exp) {

            log.error(
                    "UpdateEnvelope: Receive HttpClientErrorException {}, responseBody -> {}",
                    exp.getStatusCode(), exp.getResponseBodyAsString());
            ApiHourlyLimitData apiHourlyLimitData = ApiLimitUtil.readApiHourlyLimitData(exp.getResponseHeaders(),
                    apiThresholdLimitPercent);
            if (exp.getResponseBodyAsString()
                    .contains(AppConstants.DSAPI_HOURLY_LIMIT_EXCEEDED_ERROR) && apiHourlyLimitData.isSleepThread()){
                log.error(
                        "Retrying update Envelope for envelopeId -> {}", updateEnvelopeRequestMessageDefinition.getEnvelopeId());
                return updateEnvelope(updateEnvelopeRequestMessageDefinition);
            } else {
                log.error("We have received another exception and thread was not sleep therefore we sending it to  {} quueue.",
                        dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
                                PropertyCacheConstants.PROCESS_FAILURE_QUEUE_NAME,
                                PropertyCacheConstants.QUEUE_REFERENCE_NAME));
                queueService.createFailureMessageAndSend(updateEnvelopeRequestMessageDefinition.getEnvelopeId(),
                        AppConstants.BATCHNOTCREATED, AppConstants.PROCESSNOTCREATED, exp, FailureCode.ERROR_223,
                        FailureStep.UPDATEENVELOPEEXCEPTION);
                throw exp;
            }
        }
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
}
