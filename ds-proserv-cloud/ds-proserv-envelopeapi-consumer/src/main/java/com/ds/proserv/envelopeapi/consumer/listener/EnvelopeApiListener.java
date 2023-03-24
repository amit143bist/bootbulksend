package com.ds.proserv.envelopeapi.consumer.listener;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.envelopeapi.consumer.service.DocuSignEnvelopeService;
import com.ds.proserv.feign.envelopeapi.domain.GenericEnvelopeMessageDefinition;
import com.ds.proserv.feign.listener.AbstractMigrationListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EnvelopeApiListener extends AbstractMigrationListener<GenericEnvelopeMessageDefinition> {

    @Autowired
    private DSCacheManager dsCacheManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private DocuSignEnvelopeService docuSignEnvelopeService;


    @Override
    @RabbitListener(queues = "#{@getQueueName}")
    protected void processMessage(GenericEnvelopeMessageDefinition genericEnvelopeMessageDefinition,
                                  @Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

        log.info("EnvelopeApiRequest received in processMessage() and xDeath value is {}", xDeath);

        super.processMessage(xDeath,
                getRetryLimit(
                        dsCacheManager.prepareAndRequestCacheDataByKey(
                                PropertyCacheConstants.PROCESS_ENVELOPEAPI_QUEUE_RETRYLIMIT),
                        PropertyCacheConstants.PROCESS_ENVELOPEAPI_QUEUE_RETRYLIMIT),
                genericEnvelopeMessageDefinition);
    }

    @Override
    protected void callService(GenericEnvelopeMessageDefinition genericEnvelopeMessageDefinition) {
        docuSignEnvelopeService.createEnvelope(genericEnvelopeMessageDefinition);
    }


    @Override
    protected void sendToDeadQueue(GenericEnvelopeMessageDefinition genericEnvelopeMessageDefinition, String httpStatus,
                                   String errorHeaderMessage) {

        log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}",
                genericEnvelopeMessageDefinition, errorHeaderMessage);

        rabbitTemplate.convertAndSend(
                "DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
                genericEnvelopeMessageDefinition, m -> {
                    m.getMessageProperties().getHeaders().put(MQMessageProperties.ERRORSTATUSCODE.toString(),
                            httpStatus);
                    m.getMessageProperties().getHeaders().put(MQMessageProperties.ERRORREASON.toString(),
                            errorHeaderMessage);
                    m.getMessageProperties().getHeaders().put(MQMessageProperties.ERRORTIMESTAMP.toString(),
                            DateTimeUtil.convertToString(LocalDateTime.now()));
                    return m;
                });
    }

    @Override
    protected void logErrorMessage(long retryCount, Exception exp, String expReason, String httpStatus,
                                   GenericEnvelopeMessageDefinition genericEnvelopeMessageDefinition) {

        log.error(
                "{} is thrown and exception message is {} in processing envelopapirequest and retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
                exp, exp.getMessage(), retryCount,
                getRetryLimit(
                        dsCacheManager.prepareAndRequestCacheDataByKey(
                                PropertyCacheConstants.PROCESS_ENVELOPEAPI_QUEUE_RETRYLIMIT),
                        PropertyCacheConstants.PROCESS_ENVELOPEAPI_QUEUE_RETRYLIMIT),
                httpStatus);
    }

}