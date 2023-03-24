package com.ds.proserv.coredata.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.coredata.model.CoreScheduledBatchLog;

@Lazy
@Repository(value = "batchLogRepository")
public interface CoreScheduledBatchLogRepository extends CrudRepository<CoreScheduledBatchLog, String> {

	List<CoreScheduledBatchLog> findAllByBatchTypeAndBatchEndDateTimeIsNull(String batchType);

	Iterable<CoreScheduledBatchLog> findAllByBatchTypeAndBatchEndDateTimeIsNotNull(String batchType);

	Optional<CoreScheduledBatchLog> findTopByBatchTypeOrderByBatchStartDateTimeDesc(String batchType);

	Iterable<CoreScheduledBatchLog> findAllByBatchType(String batchType);

	Iterable<CoreScheduledBatchLog> findAllByBatchTypeAndBatchStartDateTimeBetween(String batchType,
			LocalDateTime fromDate, LocalDateTime toDate);
}