package com.ds.proserv.envelopeupdateapi.consumer.listener;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.ListenerProcessingException;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.envelopeupdateapi.consumer.service.DSEnvelopeUpdateService;
import com.ds.proserv.feign.envelopeupdateapi.domain.UpdateEnvelopeRequestMessageDefinition;
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
public class EnvelopeUpdateListener extends AbstractMigrationListener<UpdateEnvelopeRequestMessageDefinition> {

    @Autowired
    private DSCacheManager dsCacheManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private DSEnvelopeUpdateService dsEnvelopeUpdateService;

    @Override
    @RabbitListener(queues = "#{@getQueueName}")
    protected void processMessage(UpdateEnvelopeRequestMessageDefinition updateEnvelopeRequestMessageDefinition,
                                  @Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {
        log.info("EnvelopeApiRequest received in processMessage() and xDeath value is {}", xDeath);

        super.processMessage(xDeath,
                getRetryLimit(
                        dsCacheManager.prepareAndRequestCacheDataByKey(
                                PropertyCacheConstants.PROCESS_ENVELOPEUPDATEAPI_QUEUE_RETRYLIMIT),
                        PropertyCacheConstants.PROCESS_ENVELOPEUPDATEAPI_QUEUE_RETRYLIMIT),
                updateEnvelopeRequestMessageDefinition);
    }

    @Override
    protected void callService(UpdateEnvelopeRequestMessageDefinition updateEnvelopeRequestMessageDefinition) {
        try {

            dsEnvelopeUpdateService.updateEnvelope(updateEnvelopeRequestMessageDefinition);

        } catch (Throwable ex) {

            throw new ListenerProcessingException(ex.getMessage());
        }
    }


    @Override
    protected void sendToDeadQueue(UpdateEnvelopeRequestMessageDefinition updateEnvelopeRequestMessageDefinition, String httpStatus, String errorHeaderMessage) {
        log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}",
                updateEnvelopeRequestMessageDefinition, errorHeaderMessage);

        rabbitTemplate.convertAndSend(
                "DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
                updateEnvelopeRequestMessageDefinition, m -> {
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
                                   UpdateEnvelopeRequestMessageDefinition updateEnvelopeRequestMessageDefinition) {

        log.error(
                "{} is thrown and exception message is {} in processing envelopupdateapirequest and retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
                exp, exp.getMessage(), retryCount,
                getRetryLimit(
                        dsCacheManager.prepareAndRequestCacheDataByKey(
                                PropertyCacheConstants.PROCESS_ENVELOPEAPI_QUEUE_RETRYLIMIT),
                        PropertyCacheConstants.PROCESS_ENVELOPEAPI_QUEUE_RETRYLIMIT),
                httpStatus);
    }
}
