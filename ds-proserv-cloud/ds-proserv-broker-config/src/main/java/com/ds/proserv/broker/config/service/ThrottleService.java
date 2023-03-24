package com.ds.proserv.broker.config.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ThrottleService {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private RabbitListenerEndpointRegistry registry;

	@Autowired
	private QueueService queueService;

	public void throttleConsumers() {

		AtomicReference<String> containerName = new AtomicReference<String>();

		try {

			LocalDateTime localDateTime = LocalDateTime.now();
			int currentHour = localDateTime.getHour();

			int startHour = offStartHour();
			int endHour = offEndHour();

			// OFF HOURS Settings
			if ((DayOfWeek.SATURDAY == localDateTime.getDayOfWeek() || DayOfWeek.SUNDAY == localDateTime.getDayOfWeek())
					|| (currentHour > startHour || currentHour < endHour)) {

				registry.getListenerContainers().forEach(c -> {

					SimpleMessageListenerContainer simpleMessageListenerContainer = ((SimpleMessageListenerContainer) c);

					int activeConsumerCount = simpleMessageListenerContainer.getActiveConsumerCount();
					log.info("ActiveConsumerCount in offhours -> {} for containerName -> {}", activeConsumerCount,
							simpleMessageListenerContainer.getClass().toString());
					containerName.set(simpleMessageListenerContainer.getClass().toString() + "_offhours");
					setMaxConcurrency(simpleMessageListenerContainer, maxOffHourConcurrentConnection(),
							activeConsumerCount, maxOffHourPrefetch());
				});

			} else {

				registry.getListenerContainers().forEach(c -> {

					SimpleMessageListenerContainer simpleMessageListenerContainer = ((SimpleMessageListenerContainer) c);

					int activeConsumerCount = simpleMessageListenerContainer.getActiveConsumerCount();
					log.info("ActiveConsumerCount in peak hours -> {} for containerName -> {}", activeConsumerCount,
							simpleMessageListenerContainer.getClass().toString());

					containerName.set(simpleMessageListenerContainer.getClass().toString() + "_peakhours");
					setMaxConcurrency(simpleMessageListenerContainer, maxPeakHourConcurrentConnection(),
							activeConsumerCount, maxPeakHourPrefetch());
				});
			}

		} catch (Throwable exp) {

			exp.printStackTrace();
			log.error("exp -> {} caused with message -> {}", exp, exp.getMessage());

			queueService.createFailureMessageAndSend(containerName.get(), null, null, exp, FailureCode.ERROR_225,
					FailureStep.THROTTLESERVICE);
		}

	}

	private void setMaxConcurrency(SimpleMessageListenerContainer simpleMessageListenerContainer,
			int maxConcurrentConnection, int activeConsumerCount, int prefetchCount) {

		if (maxConcurrentConnection > 2 && maxConcurrentConnection != activeConsumerCount) {

			simpleMessageListenerContainer.setMaxConcurrentConsumers(maxConcurrentConnection);

			if (prefetchCount > 1) {

				simpleMessageListenerContainer.setPrefetchCount(prefetchCount);
			}
		}
	}

	private int offStartHour() {

		String startHourStr = dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.OFF_START_HOUR);

		if (!StringUtils.isEmpty(startHourStr)) {

			return Integer.parseInt(startHourStr);
		}

		return 18;
	}

	private int offEndHour() {

		String endHourStr = dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.OFF_END_HOUR);

		if (!StringUtils.isEmpty(endHourStr)) {

			return Integer.parseInt(endHourStr);
		}

		return 8;

	}

	private int maxOffHourConcurrentConnection() {

		String maxConcurrentConnectionStr = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.MAX_OFFHOUR_CONCURRENT_CONNECTIONS);

		if (!StringUtils.isEmpty(maxConcurrentConnectionStr)) {

			return Integer.parseInt(maxConcurrentConnectionStr);
		}

		return 30;

	}

	private int maxPeakHourConcurrentConnection() {

		String maxConcurrentConnectionStr = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.MAX_PEAKHOUR_CONCURRENT_CONNECTIONS);

		if (!StringUtils.isEmpty(maxConcurrentConnectionStr)) {

			return Integer.parseInt(maxConcurrentConnectionStr);
		}

		return 10;

	}

	private int maxOffHourPrefetch() {

		String maxPrefetchCountStr = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.MAX_OFFHOUR_PREFETCH_COUNT);

		if (!StringUtils.isEmpty(maxPrefetchCountStr)) {

			return Integer.parseInt(maxPrefetchCountStr);
		}

		return 30;

	}

	private int maxPeakHourPrefetch() {

		String maxPrefetchCountStr = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.MAX_PEAKHOUR_PREFETCH_COUNT);

		if (!StringUtils.isEmpty(maxPrefetchCountStr)) {

			return Integer.parseInt(maxPrefetchCountStr);
		}

		return 30;

	}

}