package com.ds.proserv.coredata.transformer;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.coredata.model.CoreConcurrentProcessLog;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;

@Component
public class ConcurrentProcessLogTransformer {

	public CoreConcurrentProcessLog tranformToCoreConcurrentProcessLog(
			ConcurrentProcessLogDefinition concurrentProcessLogRequest) {

		CoreConcurrentProcessLog coreConcurrentProcessLog = new CoreConcurrentProcessLog();
		coreConcurrentProcessLog.setBatchId(concurrentProcessLogRequest.getBatchId());
		coreConcurrentProcessLog.setProcessStatus(concurrentProcessLogRequest.getProcessStatus());
		coreConcurrentProcessLog.setTotalRecordsInProcess(concurrentProcessLogRequest.getTotalRecordsInProcess());
		coreConcurrentProcessLog.setProcessStartDateTime(LocalDateTime.now());
		coreConcurrentProcessLog.setAccountId(concurrentProcessLogRequest.getAccountId());
		coreConcurrentProcessLog.setGroupId(concurrentProcessLogRequest.getGroupId());
		coreConcurrentProcessLog.setUserId(concurrentProcessLogRequest.getUserId());

		return coreConcurrentProcessLog;

	}

	public ConcurrentProcessLogDefinition tranformToConcurrentProcessLogResponse(
			CoreConcurrentProcessLog coreConcurrentProcessLog) {

		ConcurrentProcessLogDefinition concurrentProcessLogResponse = new ConcurrentProcessLogDefinition();
		concurrentProcessLogResponse.setBatchId(coreConcurrentProcessLog.getBatchId().toString());
		concurrentProcessLogResponse.setProcessId(coreConcurrentProcessLog.getProcessId().toString());
		concurrentProcessLogResponse.setProcessStartDateTime(
				DateTimeUtil.convertToString(coreConcurrentProcessLog.getProcessStartDateTime()));
		concurrentProcessLogResponse
				.setProcessEndDateTime(DateTimeUtil.convertToString(coreConcurrentProcessLog.getProcessEndDateTime()));
		concurrentProcessLogResponse.setProcessStatus(coreConcurrentProcessLog.getProcessStatus());
		concurrentProcessLogResponse.setTotalRecordsInProcess(coreConcurrentProcessLog.getTotalRecordsInProcess());
		concurrentProcessLogResponse.setAccountId(coreConcurrentProcessLog.getAccountId());
		concurrentProcessLogResponse.setGroupId(coreConcurrentProcessLog.getGroupId());
		concurrentProcessLogResponse.setUserId(coreConcurrentProcessLog.getUserId());

		return concurrentProcessLogResponse;
	}

}
