package com.ds.proserv.bulksenddata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.bulksenddata.model.BulkSendProcessLog;
import com.ds.proserv.bulksenddata.projection.BulkSendProcessLogIdProjection;

@Repository(value = "bulkSendProcessLogPagingAndSortingRepository")
public interface BulkSendProcessLogPagingAndSortingRepository
		extends PagingAndSortingRepository<BulkSendProcessLog, String> {

	Long countByBatchStatus(String batchStatus);

	Slice<BulkSendProcessLog> findAllByBatchStatusIn(List<String> batchStatuses, Pageable pageable);

	Long countByBatchName(String batchName);

	Slice<BulkSendProcessLog> findAllByBatchNameIn(List<String> batchNames, Pageable pageable);

	Long countByBatchSubmittedDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

	Slice<BulkSendProcessLog> findAllByBatchSubmittedDateTimeBetween(LocalDateTime startDateTime,
			LocalDateTime endDateTime, Pageable pageable);

	Long countByBatchSubmittedDateTimeBetweenAndBatchStatusOrBatchSubmittedDateTimeBetweenAndBatchStatusIsNull(
			LocalDateTime startDateTime, LocalDateTime endDateTime, String batchStatus, LocalDateTime OrStartDateTime,
			LocalDateTime OrEndDateTime);

	Slice<BulkSendProcessLog> findAllByBatchSubmittedDateTimeBetweenAndBatchStatusOrBatchSubmittedDateTimeBetweenAndBatchStatusIsNull(
			LocalDateTime startDateTime, LocalDateTime endDateTime, String batchStatus, LocalDateTime OrStartDateTime,
			LocalDateTime OrEndDateTime, Pageable pageable);

	Iterable<BulkSendProcessLogIdProjection> findBatchIdByBatchStatusIn(List<String> batchStatuses);

}