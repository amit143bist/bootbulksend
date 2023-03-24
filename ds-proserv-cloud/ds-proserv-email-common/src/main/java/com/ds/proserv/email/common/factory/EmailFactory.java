package com.ds.proserv.email.common.factory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ds.proserv.common.constant.MailProcessorType;
import com.ds.proserv.email.common.processor.IEmailProcessor;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmailFactory {

	@Autowired
	private List<IEmailProcessor> emailProcessors;

	public IEmailProcessor findAllowedProcessor(MailProcessorType mailProcessorType) {

		log.debug("Inside findAllowedProcessor for processorType -> {}", mailProcessorType);
		return emailProcessors.stream().filter(processor -> processor.canProcessRequest(mailProcessorType)).findFirst()
				.orElse(null);

	}
}