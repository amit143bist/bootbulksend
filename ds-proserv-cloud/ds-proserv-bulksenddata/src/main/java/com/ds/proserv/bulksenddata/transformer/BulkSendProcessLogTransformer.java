package com.ds.proserv.bulksenddata.transformer;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.bulksenddata.model.BulkSendProcessLog;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendProcessLogDefinition;

@Component
public class BulkSendProcessLogTransformer {

	public BulkSendProcessLog transformToBulkSendProcessLog(BulkSendProcessLogDefinition bulkSendProcessLogDefinition) {

		BulkSendProcessLog bulkSendProcessLog = new BulkSendProcessLog();

		bulkSendProcessLog.setBatchId(bulkSendProcessLogDefinition.getBatchId());
		bulkSendProcessLog.setBatchName(bulkSendProcessLogDefinition.getBatchName());
		bulkSendProcessLog.setBatchSize(bulkSendProcessLogDefinition.getBatchSize());
		bulkSendProcessLog.setBatchStatus(bulkSendProcessLogDefinition.getBatchStatus());

		if (!StringUtils.isEmpty(bulkSendProcessLogDefinition.getBatchSubmittedDateTime())) {

			bulkSendProcessLog.setBatchSubmittedDateTime(
					LocalDateTime.parse(bulkSendProcessLogDefinition.getBatchSubmittedDateTime()));
		}

		bulkSendProcessLog.setFailedSize(bulkSendProcessLogDefinition.getFailedSize());
		bulkSendProcessLog.setMailingListId(bulkSendProcessLogDefinition.getMailingListId());
		bulkSendProcessLog.setQueueSize(bulkSendProcessLogDefinition.getQueueSize());
		bulkSendProcessLog.setSuccessSize(bulkSendProcessLogDefinition.getSuccessSize());
		bulkSendProcessLog.setBulkErrors(bulkSendProcessLogDefinition.getBulkErrors());

		return bulkSendProcessLog;
	}

	public BulkSendProcessLog transformToBulkSendProcessLogUpdate(
			BulkSendProcessLogDefinition bulkSendProcessLogDefinition, BulkSendProcessLog bulkSendProcessLog) {

		if (!StringUtils.isEmpty(bulkSendProcessLogDefinition.getBatchId())) {

			bulkSendProcessLog.setBatchId(bulkSendProcessLogDefinition.getBatchId());
		}

		if (!StringUtils.isEmpty(bulkSendProcessLogDefinition.getBatchName())) {

			bulkSendProcessLog.setBatchName(bulkSendProcessLogDefinition.getBatchName());
		}

		if (null != bulkSendProcessLogDefinition.getBatchSize()) {

			bulkSendProcessLog.setBatchSize(bulkSendProcessLogDefinition.getBatchSize());

		}

		if (!StringUtils.isEmpty(bulkSendProcessLogDefinition.getBatchStatus())) {

			bulkSendProcessLog.setBatchStatus(bulkSendProcessLogDefinition.getBatchStatus());
		}

		if (!StringUtils.isEmpty(bulkSendProcessLogDefinition.getBatchSubmittedDateTime())) {

			bulkSendProcessLog.setBatchSubmittedDateTime(DateTimeUtil
					.checkAndconvertToLocalDateTime(bulkSendProcessLogDefinition.getBatchSubmittedDateTime()));
		}

		if (null != bulkSendProcessLogDefinition.getFailedSize()) {

			bulkSendProcessLog.setFailedSize(bulkSendProcessLogDefinition.getFailedSize());
		}

		if (!StringUtils.isEmpty(bulkSendProcessLogDefinition.getMailingListId())) {

			bulkSendProcessLog.setMailingListId(bulkSendProcessLogDefinition.getMailingListId());
		}

		if (null != bulkSendProcessLogDefinition.getQueueSize()) {

			bulkSendProcessLog.setQueueSize(bulkSendProcessLogDefinition.getQueueSize());
		}

		if (null != bulkSendProcessLogDefinition.getSuccessSize()) {

			bulkSendProcessLog.setSuccessSize(bulkSendProcessLogDefinition.getSuccessSize());
		}

		if (!StringUtils.isEmpty(bulkSendProcessLogDefinition.getBulkErrors())) {

			bulkSendProcessLog.setBulkErrors(bulkSendProcessLogDefinition.getBulkErrors());
		}

		return bulkSendProcessLog;
	}

	public BulkSendProcessLogDefinition transformToBulkSendProcessLogDefinition(BulkSendProcessLog bulkSendProcessLog) {

		BulkSendProcessLogDefinition bulkSendProcessLogDefinition = new BulkSendProcessLogDefinition();

		bulkSendProcessLogDefinition.setBatchId(bulkSendProcessLog.getBatchId());
		bulkSendProcessLogDefinition.setBatchName(bulkSendProcessLog.getBatchName());
		bulkSendProcessLogDefinition.setBatchSize(bulkSendProcessLog.getBatchSize());
		bulkSendProcessLogDefinition.setBatchStatus(bulkSendProcessLog.getBatchStatus());

		if (null != bulkSendProcessLog.getBatchSubmittedDateTime()) {

			bulkSendProcessLogDefinition
					.setBatchSubmittedDateTime(bulkSendProcessLog.getBatchSubmittedDateTime().toString());
		}

		bulkSendProcessLogDefinition.setFailedSize(bulkSendProcessLog.getFailedSize());
		bulkSendProcessLogDefinition.setMailingListId(bulkSendProcessLog.getMailingListId());
		bulkSendProcessLogDefinition.setQueueSize(bulkSendProcessLog.getQueueSize());
		bulkSendProcessLogDefinition.setSuccessSize(bulkSendProcessLog.getSuccessSize());
		bulkSendProcessLogDefinition.setBulkErrors(bulkSendProcessLog.getBulkErrors());

		return bulkSendProcessLogDefinition;
	}

}