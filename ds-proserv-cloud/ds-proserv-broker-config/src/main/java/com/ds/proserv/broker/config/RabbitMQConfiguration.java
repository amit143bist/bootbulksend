package com.ds.proserv.broker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PropertyCacheConstants;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RabbitMQConfiguration implements RabbitListenerConfigurer {

	public static final String DEAD_LETTER_EXCHANGE_CONSTANT = "x-dead-letter-exchange";

	public static final String DEAD_LETTER_ROUTING_KEY_CONSTANT = "x-dead-letter-routing-key";

	@Autowired
	private DSCacheManager dsCacheManager;

	@Bean
	public String getQueueName() {

		return dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME);
	}

	@Bean
	Queue workerQueue() {
		return QueueBuilder
				.durable(dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME))
				.withArgument(DEAD_LETTER_EXCHANGE_CONSTANT,
						"RETRY_" + dsCacheManager
								.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_EXCHANGE_NAME))
				.withArgument(DEAD_LETTER_ROUTING_KEY_CONSTANT, "RETRY_"
						+ dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_ROUTING_KEY))
				.build();
	}

	@Bean
	Exchange workerExchange() {
		return ExchangeBuilder
				.directExchange(
						dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_EXCHANGE_NAME))
				.build();
	}

	@Bean
	Binding bindingToWorkBinding() {

		log.debug("workerQueue -> {}, workerExchange -> {}, workerRoutingKey -> {}", workerQueue().getName(),
				workerExchange().getName(),
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_ROUTING_KEY));

		return BindingBuilder.bind(workerQueue()).to(workerExchange())
				.with(dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_ROUTING_KEY))
				.noargs();
	}

	@Bean
	Queue retryQueue() {

		return QueueBuilder
				.durable("RETRY_"
						+ dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME))
				.withArgument(DEAD_LETTER_EXCHANGE_CONSTANT,
						dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_EXCHANGE_NAME))
				.withArgument(DEAD_LETTER_ROUTING_KEY_CONSTANT,
						dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_ROUTING_KEY))
				.withArgument("x-message-ttl", Integer.valueOf(
						dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_TTL)))
				.build();
	}

	@Bean
	Exchange retryExchange() {

		return ExchangeBuilder
				.directExchange("RETRY_"
						+ dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_EXCHANGE_NAME))
				.build();
	}

	@Bean
	Binding bindingToRetryBinding() {

		log.debug("retryLetterQueue -> {}, retryExchange -> {}, retryRoutingKey -> {}", retryQueue().getName(),
				retryExchange().getName(),
				"RETRY_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_ROUTING_KEY));
		return BindingBuilder.bind(retryQueue()).to(retryExchange())
				.with("RETRY_"
						+ dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_ROUTING_KEY))
				.noargs();
	}

	@Bean
	Queue deadQueue() {

		return QueueBuilder
				.durable("DEAD_"
						+ dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_QUEUE_NAME))
				.build();
	}

	@Bean
	Exchange deadExchange() {

		return ExchangeBuilder
				.directExchange("DEAD_"
						+ dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_EXCHANGE_NAME))
				.build();
	}

	@Bean
	Binding bindingToDeadBinding() {

		log.debug("deadQueue -> {}, deadExchange -> {}, deadRoutingKey -> {}", deadQueue().getName(),
				deadExchange().getName(),
				"DEAD_" + dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_ROUTING_KEY));
		return BindingBuilder.bind(deadQueue()).to(deadExchange())
				.with("DEAD_"
						+ dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_ROUTING_KEY))
				.noargs();
	}

	@Bean
	@ConditionalOnProperty(value = PropertyCacheConstants.PROSERV_QUEUE_NAME, havingValue = "CORE_PARALLEL_PROCESS_START_QUEUE")
	Queue holdQueue() {

		return QueueBuilder.durable("HOLD_CORE_PARALLEL_PROCESS_START_QUEUE").build();
	}

	@Bean
	@ConditionalOnProperty(value = PropertyCacheConstants.PROSERV_QUEUE_NAME, havingValue = "CORE_PARALLEL_PROCESS_START_QUEUE")
	Binding bindingToHoldBinding() {

		log.debug("holdQueue -> {}, holdExchange -> {}, holdRoutingKey -> {}", holdQueue().getName(),
				workerExchange().getName(),
				dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.PROSERV_ROUTING_KEY));
		return BindingBuilder.bind(holdQueue()).to(workerExchange()).with("HOLD_CORE_PARALLEL_PROCESS_START_QUEUE")
				.noargs();
	}

	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter() {

		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {

		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
		return rabbitTemplate;
	}

	@Bean
	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {

		final RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
		return rabbitAdmin;
	}

	@Override
	public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {

		registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
	}

	@Bean
	MessageHandlerMethodFactory messageHandlerMethodFactory() {

		DefaultMessageHandlerMethodFactory messageHandlerMethodFactory = new DefaultMessageHandlerMethodFactory();
		messageHandlerMethodFactory.setMessageConverter(consumerJackson2MessageConverter());
		return messageHandlerMethodFactory;
	}

	@Bean
	public MappingJackson2MessageConverter consumerJackson2MessageConverter() {

		return new MappingJackson2MessageConverter();
	}
}