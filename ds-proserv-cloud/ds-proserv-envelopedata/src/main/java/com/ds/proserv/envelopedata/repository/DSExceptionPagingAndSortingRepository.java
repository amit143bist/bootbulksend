package com.ds.proserv.envelopedata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.envelopedata.model.DSException;

@Repository(value = "dsExceptionPagingAndSortingRepository")
public interface DSExceptionPagingAndSortingRepository extends PagingAndSortingRepository<DSException, String> {

	Long countByEnvelopeIdIn(Iterable<String> envelopeIds);

	Slice<DSException> findAllByEnvelopeIdIn(Iterable<String> envelopeIds, Pageable pageable);

	Long countByExceptionDateTimeBetweenAndRetryStatusOrExceptionDateTimeBetweenAndRetryStatusIsNull(
			LocalDateTime startDateTime, LocalDateTime endDateTime, String retryStatus, LocalDateTime OrStartDateTime,
			LocalDateTime OrEndDateTime);

	Slice<DSException> findAllByExceptionDateTimeBetweenAndRetryStatusOrExceptionDateTimeBetweenAndRetryStatusIsNull(
			LocalDateTime startDateTime, LocalDateTime endDateTime, String retryStatus, LocalDateTime OrStartDateTime,
			LocalDateTime OrEndDateTime, Pageable pageable);

	Long countByRetryStatusInOrRetryStatusIsNull(List<String> retryStatuses);

	Slice<DSException> findAllByRetryStatusInOrRetryStatusIsNull(List<String> retryStatuses, Pageable pageable);

	Long countByRetryStatusIn(List<String> retryStatuses);

	Slice<DSException> findAllByRetryStatusIn(List<String> retryStatuses, Pageable pageable);
}