package com.ds.proserv.bulksenddata.transformer;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.bulksenddata.model.BulkSendFailureLog;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendFailureLogDefinition;

@Component
public class BulkSendFailureLogTransformer {

	public BulkSendFailureLog transformToBulkSendFailureLog(BulkSendFailureLogDefinition bulkSendFailureLogDefinition) {

		BulkSendFailureLog bulkSendFailureLog = new BulkSendFailureLog();

		bulkSendFailureLog.setApplicationIds(bulkSendFailureLogDefinition.getApplicationIds());

		if (!StringUtils.isEmpty(bulkSendFailureLogDefinition.getBatchFailureDateTime())) {

			bulkSendFailureLog.setBatchFailureDateTime(
					LocalDateTime.parse(bulkSendFailureLogDefinition.getBatchFailureDateTime()));
		}

		bulkSendFailureLog.setBatchSize(bulkSendFailureLogDefinition.getBatchSize());
		bulkSendFailureLog.setErrorMessage(bulkSendFailureLogDefinition.getErrorMessage());
		bulkSendFailureLog.setId(bulkSendFailureLogDefinition.getId());

		return bulkSendFailureLog;
	}

	public BulkSendFailureLog transformToBulkSendFailureLogUpdate(
			BulkSendFailureLogDefinition bulkSendFailureLogDefinition, BulkSendFailureLog bulkSendFailureLog) {

		if (!StringUtils.isEmpty(bulkSendFailureLogDefinition.getApplicationIds())) {

			bulkSendFailureLog.setApplicationIds(bulkSendFailureLogDefinition.getApplicationIds());
		}

		if (!StringUtils.isEmpty(bulkSendFailureLogDefinition.getBatchFailureDateTime())) {

			bulkSendFailureLog.setBatchFailureDateTime(
					LocalDateTime.parse(bulkSendFailureLogDefinition.getBatchFailureDateTime()));
		}

		if (null != bulkSendFailureLogDefinition.getBatchSize()) {

			bulkSendFailureLog.setBatchSize(bulkSendFailureLogDefinition.getBatchSize());
		}

		if (null != bulkSendFailureLogDefinition.getErrorMessage()) {

			bulkSendFailureLog.setErrorMessage(bulkSendFailureLogDefinition.getErrorMessage());
		}

		return bulkSendFailureLog;
	}

	public BulkSendFailureLogDefinition transformToBulkSendFailureLogDefinition(BulkSendFailureLog bulkSendFailureLog) {

		BulkSendFailureLogDefinition bulkSendFailureLogDefinition = new BulkSendFailureLogDefinition();

		bulkSendFailureLogDefinition.setApplicationIds(bulkSendFailureLog.getApplicationIds());

		if (null != bulkSendFailureLog.getBatchFailureDateTime()) {

			bulkSendFailureLogDefinition
					.setBatchFailureDateTime(bulkSendFailureLog.getBatchFailureDateTime().toString());
		}

		bulkSendFailureLogDefinition.setBatchSize(bulkSendFailureLog.getBatchSize());
		bulkSendFailureLogDefinition.setErrorMessage(bulkSendFailureLog.getErrorMessage());
		bulkSendFailureLogDefinition.setId(bulkSendFailureLog.getId());

		return bulkSendFailureLogDefinition;
	}
}