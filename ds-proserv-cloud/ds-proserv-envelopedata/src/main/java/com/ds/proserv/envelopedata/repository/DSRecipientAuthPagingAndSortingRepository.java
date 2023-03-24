package com.ds.proserv.envelopedata.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.envelopedata.model.DSRecipientAuth;

@Repository(value = "dsRecipientAuthPagingAndSortingRepository")
public interface DSRecipientAuthPagingAndSortingRepository extends PagingAndSortingRepository<DSRecipientAuth, String> {

	Iterable<DSRecipientAuth> findAllByEnvelopeIdIn(List<String> envelopeIds);

}