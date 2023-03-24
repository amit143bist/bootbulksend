package com.ds.proserv.bulksenddata.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.bulksenddata.model.BulkSendFailureLog;

@Repository(value = "bulkSendFailureLogPagingAndSortingRepository")
public interface BulkSendFailureLogPagingAndSortingRepository
		extends PagingAndSortingRepository<BulkSendFailureLog, String> {

	Long countByBatchFailureDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

	Slice<BulkSendFailureLog> findAllByBatchFailureDateTimeBetween(LocalDateTime startDateTime,
			LocalDateTime endDateTime, Pageable pageable);

}