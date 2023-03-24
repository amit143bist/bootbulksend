package com.ds.proserv.coredata.transformer;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.coredata.model.CoreScheduledBatchLog;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogRequest;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogResponse;

@Component
public class CoreScheduledBatchLogTransformer {

	public CoreScheduledBatchLog transformToCoreScheduledBatchLog(ScheduledBatchLogRequest scheduledBatchLogRequest) {

		CoreScheduledBatchLog coreScheduledBatchLog = new CoreScheduledBatchLog();

		coreScheduledBatchLog.setBatchType(scheduledBatchLogRequest.getBatchType());
		coreScheduledBatchLog.setBatchStartParameters(scheduledBatchLogRequest.getBatchStartParameters());
		coreScheduledBatchLog.setBatchStartDateTime(LocalDateTime.now());
		coreScheduledBatchLog.setTotalRecords(scheduledBatchLogRequest.getTotalRecords());

		return coreScheduledBatchLog;
	}

	public ScheduledBatchLogResponse transformToScheduledBatchLogResponse(CoreScheduledBatchLog coreScheduledBatchLog) {

		ScheduledBatchLogResponse scheduledBatchLogResponse = new ScheduledBatchLogResponse();

		scheduledBatchLogResponse.setBatchId(coreScheduledBatchLog.getBatchId().toString());
		scheduledBatchLogResponse
				.setBatchStartDateTime(DateTimeUtil.convertToString(coreScheduledBatchLog.getBatchStartDateTime()));
		scheduledBatchLogResponse
				.setBatchEndDateTime(DateTimeUtil.convertToString(coreScheduledBatchLog.getBatchEndDateTime()));
		scheduledBatchLogResponse.setBatchStartParameters(coreScheduledBatchLog.getBatchStartParameters());
		scheduledBatchLogResponse.setBatchType(coreScheduledBatchLog.getBatchType());
		scheduledBatchLogResponse.setTotalRecords(coreScheduledBatchLog.getTotalRecords());

		return scheduledBatchLogResponse;
	}
}