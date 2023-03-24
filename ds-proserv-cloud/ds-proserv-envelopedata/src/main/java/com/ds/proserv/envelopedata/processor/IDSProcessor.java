package com.ds.proserv.envelopedata.processor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ds.proserv.common.constant.DataProcessorType;
import com.ds.proserv.envelopedata.domain.DSEnvelopeData;
import com.ds.proserv.envelopedata.domain.DSEnvelopeSavedData;

public interface IDSProcessor {

	long callSequence();

	DataProcessorType identifyProcessor();

	boolean canProcessRequest(List<String> allowedProcessorTypes);

	boolean isDataAvailableForProcessing(DSEnvelopeData dsEnvelopeData);

	CompletableFuture<String> compareAndPrepareData(DSEnvelopeData dsEnvelopeData);

	boolean isDataAvailableForSave(DSEnvelopeData dsEnvelopeData);

	CompletableFuture<Void> callRepositorySaveOperations(DSEnvelopeData dsEnvelopeData);

	void extractUniqueData(DSEnvelopeSavedData dsEnvelopeSavedData);
}