package com.ds.proserv.bulksenddata.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.bulksenddata.model.BulkSendEnvelopeLog;

@Repository(value = "bulkSendEnvelopeLogPagingAndSortingRepositoryRepository")
public interface BulkSendEnvelopeLogPagingAndSortingRepositoryRepository
		extends PagingAndSortingRepository<BulkSendEnvelopeLog, String> {

	Long countByBulkBatchId(String bulkBatchId);

	Optional<BulkSendEnvelopeLog> findByEnvelopeId(String envelopeId);

	List<BulkSendEnvelopeLog> findAllByBulkBatchIdIn(List<String> bulkBatchIds);

	Slice<BulkSendEnvelopeLog> findAllByBulkBatchId(String bulkBatchId, Pageable pageable);

	Long countByBulkBatchIdIn(List<String> bulkBatchIds);

	Slice<BulkSendEnvelopeLog> findAllByBulkBatchIdIn(List<String> bulkBatchIds, Pageable pageable);

	Slice<BulkSendEnvelopeLog> findAllByEnvelopeIdIn(List<String> envelopeIds, Pageable pageable);

	@Procedure(procedureName = "sproc_bulksendenvelopelog_insert", outputParameterName = "result")
	String insert(String json);
}