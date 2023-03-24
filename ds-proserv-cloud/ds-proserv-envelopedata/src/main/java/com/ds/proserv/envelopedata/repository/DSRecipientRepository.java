package com.ds.proserv.envelopedata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.proserv.envelopedata.model.DSRecipient;

@Repository(value = "dsRecipientRepository")
public interface DSRecipientRepository extends CrudRepository<DSRecipient, String> {

	Iterable<DSRecipient> findAllByEnvelopeId(String envelopeId);

	Iterable<DSRecipient> findAllByEnvelopeIdIn(List<String> envelopeIds);

	List<DSRecipient> findAllByEnvelopeIdInAndCreatedDateTimeAfter(List<String> envelopeIds,
			LocalDateTime sentDateTime);

	@Query(value = "sproc_getrecipientforenvid :recordId, :sentdate", nativeQuery = true)
	List<DSRecipient> getAllRecipientsByEnvelopeIdsAfterSentDateTime(@Param("recordId") String recordIdsAsJSON,
			@Param("sentdate") LocalDateTime sentdate);

	@Procedure(procedureName = "sproc_dsrecipient_insert_update", outputParameterName = "result")
	String insertUpdate(String json);
}