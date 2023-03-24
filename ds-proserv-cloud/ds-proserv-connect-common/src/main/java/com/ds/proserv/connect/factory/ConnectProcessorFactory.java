package com.ds.proserv.connect.factory;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ds.proserv.connect.processor.IConnectProcessor;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConnectProcessorFactory {

	@Autowired
	private List<IConnectProcessor> connectProcessors;

	public List<IConnectProcessor> processData(List<String> allowedProcessorTypes) {

		log.debug("Inside findAllowedProcessors for processorTypes -> {}", allowedProcessorTypes);
		return connectProcessors.stream().filter(processor -> processor.canHandleRequest(allowedProcessorTypes))
				.collect(Collectors.toList());

	}
}