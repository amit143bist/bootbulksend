package com.ds.proserv.envelopedata.repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import com.ds.proserv.envelopedata.AsyncConfiguration;
import com.ds.proserv.envelopedata.model.DSEnvelope;
import com.ds.proserv.envelopedata.projection.DSEnvelopeProjection;

@Repository(value = "dsEnvelopeRepository")
public interface DSEnvelopeRepository extends CrudRepository<DSEnvelope, String> {

	@Query("select tab as tab, envelope as envelope, recipient as recipient, customField as customField, recipientAuth as recipientAuth from DSEnvelope envelope left outer join DSTab tab on tab.envelopeId = envelope.envelopeId left outer join DSRecipient recipient on recipient.envelopeId = envelope.envelopeId left outer join DSCustomField customField on customField.envelopeId = envelope.envelopeId left outer join DSRecipientAuth recipientAuth on recipientAuth.envelopeId = envelope.envelopeId where envelope.envelopeId in (:envelopeIds)")
	List<DSEnvelopeProjection> getDSEnvelopeTreeByProjection(@Param(value = "envelopeIds") List<String> envelopeIds);

	@Async(AsyncConfiguration.TASK_EXECUTOR_PROCESSOR)
	@Query(value = "sproc_getdsenvelopeforenvid :recordId", nativeQuery = true)
	CompletableFuture<List<DSEnvelope>> getAllEnvelopesByEnvelopeIds(@Param("recordId") String recordIdsAsJSON);

	@Procedure(procedureName = "sproc_dsenvelope_insert_update", outputParameterName = "result")
	String insertUpdate(String json);
}