package com.ds.proserv.envelopedata.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.envelopedata.model.DSRecipient;

@Repository(value = "dsRecipientPagingAndSortingRepository")
public interface DSRecipientPagingAndSortingRepository extends PagingAndSortingRepository<DSRecipient, String> {

	Slice<DSRecipient> findAllByEnvelopeIdIn(List<String> envelopeIds, Pageable pageable);
}