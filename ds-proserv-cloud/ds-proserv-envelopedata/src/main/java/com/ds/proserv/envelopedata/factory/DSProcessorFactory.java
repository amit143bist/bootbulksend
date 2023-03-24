package com.ds.proserv.envelopedata.factory;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ds.proserv.envelopedata.processor.IDSProcessor;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DSProcessorFactory {

	@Autowired
	private List<IDSProcessor> dsProcessors;

	public List<IDSProcessor> findAllowedProcessors(List<String> allowedProcessorTypes) {

		log.debug("Inside findAllowedProcessors for processorTypes -> {}", allowedProcessorTypes);
		return dsProcessors.stream().filter(processor -> processor.canProcessRequest(allowedProcessorTypes))
				.collect(Collectors.toList());

	}

}