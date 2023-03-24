package com.ds.proserv.bulksenddata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.proserv.bulksenddata.model.BulkSendRecordLog;
import com.ds.proserv.bulksenddata.model.BulkSendRecordLogId;

@Repository(value = "bulkSendRecordLogPagingAndSortingRepository")
public interface BulkSendRecordLogPagingAndSortingRepository
		extends PagingAndSortingRepository<BulkSendRecordLog, BulkSendRecordLogId> {

	Iterable<BulkSendRecordLog> findAllByStartDateTimeAndEndDateTime(LocalDateTime startDateTime,
			LocalDateTime endDateTime);

	Iterable<BulkSendRecordLog> findAllByBulkSendRecordLogIdRecordTypeAndBulkSendRecordLogIdRecordIdIn(
			String recordType, List<String> recordIds);

	@Query(value = "sproc_getbulksendrecordlog :recordtype, :recordId", nativeQuery = true)
	Iterable<BulkSendRecordLog> getAllRecordByRecordTypeAndRecordIds(@Param("recordtype") String recordtype,
			@Param("recordId") String recordId);
}