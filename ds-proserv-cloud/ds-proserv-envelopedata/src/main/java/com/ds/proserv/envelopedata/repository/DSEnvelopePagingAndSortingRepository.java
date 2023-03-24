package com.ds.proserv.envelopedata.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.envelopedata.model.DSEnvelope;

@Repository(value = "dsEnvelopePagingAndSortingRepository")
public interface DSEnvelopePagingAndSortingRepository extends PagingAndSortingRepository<DSEnvelope, String> {

	Slice<DSEnvelope> findAllByEnvelopeIdIn(Iterable<String> envelopeIds, Pageable pageable);
}