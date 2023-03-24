package com.ds.proserv.cache.consumer.listener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.MQMessageProperties;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.cachedata.domain.CacheEvictRequest;
import com.ds.proserv.feign.listener.AbstractMigrationListener;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CacheEvictListener extends AbstractMigrationListener<CacheEvictRequest> {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@RabbitListener(queues = "#{@getQueueName}")
	public void processMessage(CacheEvictRequest cacheEvictRequest,
			@Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

		log.info("CacheEvictRequest received in processMessage() -> {} and xDeath value is {}", cacheEvictRequest,
				xDeath);

		super.processMessage(xDeath,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_CACHEEVICT_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_CACHEEVICT_QUEUE_RETRYLIMIT),
				cacheEvictRequest);

	}

	@Override
	protected void callService(CacheEvictRequest cacheEvictRequest) {

		log.info("Call Service called for cacheReference -> {}", cacheEvictRequest.getCacheReference());
		List<ServiceInstance> serviceInstances = this.discoveryClient
				.getInstances(cacheEvictRequest.getCacheReference());

		serviceInstances.forEach(serviceInstance -> {

			log.info("URI is {}", serviceInstance.getUri().toString());
			restTemplate.exchange(serviceInstance.getUri().toString() + "/docusign/cachelog/evict", HttpMethod.PUT,
					new HttpEntity<String>(createHeaders(
							dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_APP_USERNAME),
							dsCacheManager
									.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_APP_PASSWORD))),
					String.class);

		});

	}

	@Override
	protected void sendToDeadQueue(CacheEvictRequest cacheEvictRequest, String httpStatus, String errorHeaderMessage) {

		log.error("message in sendToDeadQueue() is -> {}, and errorHeaderMessage is {}", cacheEvictRequest,
				errorHeaderMessage);

		rabbitTemplate.convertAndSend(
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME),
				cacheEvictRequest, m -> {
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
			CacheEvictRequest cacheEvictRequest) {

		log.error(
				"{} is thrown and exception message is {} in processing cacheEvictRequest for cacheKey -> {} and cacheReference -> {}, retryCount is {}, retryLimit is {}, and errorStatusCode is {}",
				exp, exp.getMessage(), cacheEvictRequest.getCacheKey(),
				cacheEvictRequest.getCacheReference(), retryCount,
				getRetryLimit(
						dsCacheManager.prepareAndRequestCacheDataByKey(
								PropertyCacheConstants.PROCESS_CACHEEVICT_QUEUE_RETRYLIMIT),
						PropertyCacheConstants.PROCESS_CACHEEVICT_QUEUE_RETRYLIMIT),
				httpStatus);

	}

}