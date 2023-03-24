package com.ds.proserv.envelopedata.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.envelopedata.model.DSTab;

@Repository(value = "dsTabPagingAndSortingRepository")
public interface DSTabPagingAndSortingRepository extends PagingAndSortingRepository<DSTab, String> {

	Slice<DSTab> findAllByEnvelopeIdIn(List<String> envelopeIds, Pageable pageable);

	Slice<DSTab> findAllByEnvelopeIdInAndTabLabelIn(List<String> envelopeIds, List<String> tabLabels,
			Pageable pageable);
}