package com.ds.proserv.envelopedata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.envelopedata.model.DSEnvelopeDocLog;

@Repository(value = "dsEnvelopeDocLogPagingAndSortingRepository")
public interface DSEnvelopeDocLogPagingAndSortingRepository
		extends PagingAndSortingRepository<DSEnvelopeDocLog, String> {

	Long countByEnvelopeIdIn(Iterable<String> envelopeIds);

	Slice<DSEnvelopeDocLog> findAllByEnvelopeIdIn(Iterable<String> envelopeIds, Pageable pageable);

	Long countByTimeGeneratedBetweenAndDocDownloadStatusOrTimeGeneratedBetweenAndDocDownloadStatusIsNull(
			LocalDateTime startDateTime, LocalDateTime endDateTime, String docDownloadStatus,
			LocalDateTime OrStartDateTime, LocalDateTime OrEndDateTime);

	Slice<DSEnvelopeDocLog> findAllByTimeGeneratedBetweenAndDocDownloadStatusOrTimeGeneratedBetweenAndDocDownloadStatusIsNull(
			LocalDateTime startDateTime, LocalDateTime endDateTime, String docDownloadStatus,
			LocalDateTime OrStartDateTime, LocalDateTime OrEndDateTime, Pageable pageable);

	Long countByDocDownloadStatusInOrDocDownloadStatusIsNull(List<String> docDownloadStatuses);

	Slice<DSEnvelopeDocLog> findAllByDocDownloadStatusInOrDocDownloadStatusIsNull(List<String> docDownloadStatuses,
			Pageable pageable);

	Long countByDocDownloadStatusIn(List<String> docDownloadStatuses);

	Slice<DSEnvelopeDocLog> findAllByDocDownloadStatusIn(List<String> docDownloadStatus, Pageable pageable);
}