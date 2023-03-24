package com.ds.proserv.envelopedata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.proserv.envelopedata.model.DSCustomField;

@Repository(value = "dsCustomFieldPagingAndSortingRepository")
public interface DSCustomFieldPagingAndSortingRepository extends PagingAndSortingRepository<DSCustomField, String> {

	Iterable<DSCustomField> findAllByEnvelopeIdIn(List<String> envelopeIds);

	List<DSCustomField> findAllByEnvelopeIdInAndFieldName(List<String> envelopeIds, String fieldName);

	List<DSCustomField> findAllByEnvelopeIdInAndCreatedDateTimeAfter(List<String> envelopeIds,
			LocalDateTime sentDateTime);

	@Query(value = "sproc_getdscustomfieldforenvid :recordId, :sentdate", nativeQuery = true)
	List<DSCustomField> getAllCustomFieldsByEnvelopeIdsAfterSentDateTime(@Param("recordId") String recordIdsAsJSON,
			@Param("sentdate") LocalDateTime sentdate);

	@Procedure(procedureName = "sproc_dscustomfield_insert_update", outputParameterName = "result")
	String insertUpdate(String json);
}