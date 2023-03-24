package com.ds.proserv.envelopedata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.proserv.envelopedata.model.DSException;
import com.ds.proserv.envelopedata.projection.DSExceptionIdProjection;

@Repository(value = "dsExceptionRepository")
public interface DSExceptionRepository extends CrudRepository<DSException, String> {

	Iterable<DSException> findAllByEnvelopeId(String envelopeId);
	
	Iterable<DSException> findAllByIdIn(List<String> exceptionIds);

	Iterable<DSExceptionIdProjection> findIdByRetryStatusIn(List<String> retryStatuses);

	Iterable<DSExceptionIdProjection> findIdByRetryStatusInOrRetryStatusIsNull(List<String> retryStatuses);

	@Modifying
	@Query("update DSException dse set dse.retryStatus = :retryStatus where dse.id in (:ids)")
	void updateDSExceptionStatus(@Param(value = "retryStatus") String retryStatus,
			@Param(value = "ids") List<String> ids);

}