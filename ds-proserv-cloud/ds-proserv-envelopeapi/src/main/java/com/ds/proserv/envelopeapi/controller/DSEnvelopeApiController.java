package com.ds.proserv.envelopeapi.controller;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.feign.envelopeapi.domain.GenericEnvelopeMessageDefinition;
import com.ds.proserv.feign.envelopeapi.service.DSEnvelopeApiService;
import com.ds.proserv.feign.envelopeupdateapi.domain.UpdateEnvelopeRequestMessageDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

@Slf4j
@RestController
@RolesAllowed("USER")
public class DSEnvelopeApiController implements DSEnvelopeApiService {

    @Autowired
    private DSCacheManager dsCacheManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public ResponseEntity<String> createEnvelope(
            GenericEnvelopeMessageDefinition genericEnvelopeMessageDefinition) {

        log.info("Received communityPartner application for applicationId -> {} and applicationType -> {}",
                genericEnvelopeMessageDefinition.getApplicationId(),
                genericEnvelopeMessageDefinition.getApplicationType());
        String queueName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
                PropertyCacheConstants.PROCESS_ENVELOPEAPI_QUEUE_NAME, PropertyCacheConstants.QUEUE_REFERENCE_NAME);

        rabbitTemplate.convertAndSend(queueName, genericEnvelopeMessageDefinition);

        return new ResponseEntity<>(AppConstants.QUEUED_VALUE, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> updateEnvelope(String envelopeId, String payload) {

        log.info("Received update envelope for envelopeId -> {}, request -> {} ", envelopeId,
                envelopeId);
        String queueName = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
                PropertyCacheConstants.PROCESS_ENVELOPEUPDATEAPI_QUEUE_NAME, PropertyCacheConstants.QUEUE_REFERENCE_NAME);
        log.info("Sending message to queue -> {}", queueName);
        UpdateEnvelopeRequestMessageDefinition updateEnvelopeRequestMessageDefinition = new UpdateEnvelopeRequestMessageDefinition(envelopeId, payload);
        rabbitTemplate.convertAndSend(queueName, updateEnvelopeRequestMessageDefinition);
        return new ResponseEntity<>(AppConstants.QUEUED_VALUE, HttpStatus.OK);
    }
}

