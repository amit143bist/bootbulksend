package com.ds.proserv.bulksenddata.transformer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.bulksenddata.model.BulkSendEnvelopeLog;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendEnvelopeLogDefinition;

@Component
public class BulkSendEnvelopeLogTransformer {

	public BulkSendEnvelopeLog transformToBulkSendEnvelopeLog(
			BulkSendEnvelopeLogDefinition bulkSendEnvelopeLogDefinition) {

		BulkSendEnvelopeLog bulkSendEnvelopeLog = new BulkSendEnvelopeLog();
		bulkSendEnvelopeLog.setBulkBatchId(bulkSendEnvelopeLogDefinition.getBulkBatchId());
		bulkSendEnvelopeLog.setEnvelopeId(bulkSendEnvelopeLogDefinition.getEnvelopeId());

		return bulkSendEnvelopeLog;
	}

	public BulkSendEnvelopeLog transformToBulkSendEnvelopeLogUpdate(
			BulkSendEnvelopeLogDefinition bulkSendEnvelopeLogDefinition, BulkSendEnvelopeLog bulkSendEnvelopeLog) {

		if (!StringUtils.isEmpty(bulkSendEnvelopeLogDefinition.getBulkBatchId())) {

			bulkSendEnvelopeLog.setBulkBatchId(bulkSendEnvelopeLogDefinition.getBulkBatchId());
		}

		if (!StringUtils.isEmpty(bulkSendEnvelopeLogDefinition.getEnvelopeId())) {

			bulkSendEnvelopeLog.setEnvelopeId(bulkSendEnvelopeLogDefinition.getEnvelopeId());
		}

		return bulkSendEnvelopeLog;
	}

	public BulkSendEnvelopeLogDefinition transformToBulkSendEnvelopeLogDefinition(
			BulkSendEnvelopeLog bulkSendEnvelopeLog) {

		BulkSendEnvelopeLogDefinition bulkSendEnvelopeLogDefinition = new BulkSendEnvelopeLogDefinition();

		bulkSendEnvelopeLogDefinition.setId(bulkSendEnvelopeLog.getId());
		bulkSendEnvelopeLogDefinition.setBulkBatchId(bulkSendEnvelopeLog.getBulkBatchId());
		bulkSendEnvelopeLogDefinition.setEnvelopeId(bulkSendEnvelopeLog.getEnvelopeId());

		return bulkSendEnvelopeLogDefinition;
	}
}