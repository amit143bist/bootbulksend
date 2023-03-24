package com.ds.proserv.coredata.repository;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.coredata.model.CoreProcessFailureLog;

@Lazy
@Repository(value = "coreProcessFailureLogRepository")
public interface CoreProcessFailureLogRepository extends CrudRepository<CoreProcessFailureLog, String> {

	Iterable<CoreProcessFailureLog> findAllByProcessIdAndRetryStatusOrProcessIdAndRetryStatusIsNull(String processId,
			String retryStatus, String orProcessId);

	Iterable<CoreProcessFailureLog> findAllByFailureRecordIdAndRetryStatusOrFailureRecordIdAndRetryStatusIsNull(
			String failureRecordId, String retryStatus, String orfailureRecordId);

	Iterable<CoreProcessFailureLog> findAllByProcessIdInAndRetryStatusOrProcessIdInAndRetryStatusIsNull(
			List<String> processIds, String retryStatus, List<String> orProcessIds);

	Long countByProcessIdInAndRetryStatusOrProcessIdInAndRetryStatusIsNull(List<String> processIds, String retryStatus,
			List<String> orProcessIds);

	Iterable<CoreProcessFailureLog> findAllByRetryStatusOrRetryStatusIsNull(String retryStatus);

	Long countByRetryStatusOrRetryStatusIsNull(String retryStatus);
	
	Iterable<CoreProcessFailureLog> findAllByBatchIdAndRetryStatusOrBatchIdAndRetryStatusIsNull(String batchId,
			String retryStatus, String orBatchId);
	
	Iterable<CoreProcessFailureLog> findAllByBatchIdInAndRetryStatusOrBatchIdInAndRetryStatusIsNull(
			List<String> batchIds, String retryStatus, List<String> orbatchIds);
	
	Long countByBatchIdInAndRetryStatusOrBatchIdInAndRetryStatusIsNull(List<String> batchIds, String retryStatus,
			List<String> orBatchIds);
}