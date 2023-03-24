package com.ds.proserv.bulksendmonitor.batch.transformer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ds.proserv.bulksendmonitor.batch.domain.BulkSendBatchMonitorResponse;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BulkSendMonitorTransformer {

	@Autowired
	private ObjectMapper objectMapper;

	public BulkSendProcessLogDefinition transformToBulkSendProcessLogDefinition(
			BulkSendBatchMonitorResponse bulkSendBatchMonitorResponse, String status) {

		BulkSendProcessLogDefinition bulkSendProcessLogDefinition = new BulkSendProcessLogDefinition();
		bulkSendProcessLogDefinition.setBatchId(bulkSendBatchMonitorResponse.getBatchId());
		bulkSendProcessLogDefinition.setBatchName(bulkSendBatchMonitorResponse.getBatchName());

		if (!StringUtils.isEmpty(bulkSendBatchMonitorResponse.getBatchSize())) {

			bulkSendProcessLogDefinition.setBatchSize(Long.valueOf(bulkSendBatchMonitorResponse.getBatchSize()));
		}
		bulkSendProcessLogDefinition.setBatchStatus(status);
		bulkSendProcessLogDefinition.setBatchSubmittedDateTime(bulkSendBatchMonitorResponse.getSubmittedDate());
		try {

			if (null != bulkSendBatchMonitorResponse.getBulkErrors()
					&& !bulkSendBatchMonitorResponse.getBulkErrors().isEmpty()) {

				bulkSendProcessLogDefinition
						.setBulkErrors(objectMapper.writeValueAsString(bulkSendBatchMonitorResponse.getBulkErrors()));
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		if (!StringUtils.isEmpty(bulkSendBatchMonitorResponse.getFailed())) {

			bulkSendProcessLogDefinition.setFailedSize(Long.valueOf(bulkSendBatchMonitorResponse.getFailed()));
		}

		bulkSendProcessLogDefinition.setMailingListId(bulkSendBatchMonitorResponse.getMailingListId());

		if (!StringUtils.isEmpty(bulkSendBatchMonitorResponse.getQueued())) {

			bulkSendProcessLogDefinition.setQueueSize(Long.valueOf(bulkSendBatchMonitorResponse.getQueued()));
		}

		if (!StringUtils.isEmpty(bulkSendBatchMonitorResponse.getSent())) {

			bulkSendProcessLogDefinition.setSuccessSize(Long.valueOf(bulkSendBatchMonitorResponse.getSent()));
		}

		return bulkSendProcessLogDefinition;
	}

}