package com.ds.proserv.connect.processor;

import java.util.List;

import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;

public interface IConnectProcessor {

	boolean canHandleRequest(List<String> allowedProcessorTypes);

	void processConnectData(String queueName, List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList);
}