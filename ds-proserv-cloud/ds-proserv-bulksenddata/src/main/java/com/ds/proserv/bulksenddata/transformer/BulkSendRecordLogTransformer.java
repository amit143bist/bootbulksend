package com.ds.proserv.bulksenddata.transformer;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.bulksenddata.model.BulkSendRecordLog;
import com.ds.proserv.bulksenddata.model.BulkSendRecordLogId;
import com.ds.proserv.feign.bulksenddata.domain.BulkSendRecordLogDefinition;

@Component
public class BulkSendRecordLogTransformer {

	public BulkSendRecordLog transformToBulkSendRecordLog(BulkSendRecordLogDefinition bulkSendRecordLogDefinition) {

		BulkSendRecordLog bulkSendRecordLog = new BulkSendRecordLog();

		bulkSendRecordLog.setBulkBatchId(bulkSendRecordLogDefinition.getBulkBatchId());

		BulkSendRecordLogId bulkSendRecordLogId = new BulkSendRecordLogId(bulkSendRecordLogDefinition.getRecordId(),
				bulkSendRecordLogDefinition.getRecordType());
		bulkSendRecordLog.setBulkSendRecordLogId(bulkSendRecordLogId);

		if (!StringUtils.isEmpty(bulkSendRecordLogDefinition.getStartDateTime())) {

			bulkSendRecordLog.setStartDateTime(LocalDateTime.parse(bulkSendRecordLogDefinition.getStartDateTime()));
		}

		if (!StringUtils.isEmpty(bulkSendRecordLogDefinition.getEndDateTime())) {

			bulkSendRecordLog.setEndDateTime(LocalDateTime.parse(bulkSendRecordLogDefinition.getEndDateTime()));
		}

		return bulkSendRecordLog;
	}

	public BulkSendRecordLogDefinition transformToBulkSendRecordLogDefinition(BulkSendRecordLog bulkSendRecordLog) {

		BulkSendRecordLogDefinition bulkSendRecordLogDefinition = new BulkSendRecordLogDefinition();

		bulkSendRecordLogDefinition.setBulkBatchId(bulkSendRecordLog.getBulkBatchId());

		bulkSendRecordLogDefinition.setRecordId(bulkSendRecordLog.getBulkSendRecordLogId().getRecordId());
		bulkSendRecordLogDefinition.setRecordType(bulkSendRecordLog.getBulkSendRecordLogId().getRecordType());

		if (null != bulkSendRecordLog.getStartDateTime()) {

			bulkSendRecordLogDefinition.setStartDateTime(bulkSendRecordLog.getStartDateTime().toString());
		}

		if (null != bulkSendRecordLog.getEndDateTime()) {

			bulkSendRecordLogDefinition.setEndDateTime(bulkSendRecordLog.getEndDateTime().toString());
		}

		return bulkSendRecordLogDefinition;
	}
}