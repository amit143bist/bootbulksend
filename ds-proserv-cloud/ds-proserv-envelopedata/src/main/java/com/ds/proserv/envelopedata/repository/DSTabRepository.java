package com.ds.proserv.envelopedata.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.proserv.envelopedata.model.DSTab;

@Repository(value = "dsTabRepository")
public interface DSTabRepository extends CrudRepository<DSTab, String> {

	String INSERT_SQL = "insert into dstab (createdby, createddatetime, envelopeid, recipientid, tablabel, tabname, taboriginalvalue, tabstatus, tabvalue, id) values (:createdby, :createddatetime, :envelopeid, :recipientid, :tablabel, :tabname, :taboriginalvalue, :tabstatus, :tabvalue, :id)";

	String UPDATE_SQL = "update dstab set updatedby=:updatedby, updateddatetime=:updateddatetime, envelopeid=:envelopeid, recipientid=:recipientid, tablabel=:tablabel, tabname=:tabname, taboriginalvalue=:taboriginalvalue, tabstatus=:tabstatus, tabvalue=:tabvalue where id=:id";

	Iterable<DSTab> findAllByEnvelopeId(String envelopeId);

	List<DSTab> findAllByEnvelopeIdInAndCreatedDateTimeAfter(List<String> envelopeIds, LocalDateTime sentDateTime);

	Iterable<DSTab> findAllByRecipientId(String recipientId);

	Optional<DSTab> findByTabLabelAndEnvelopeId(String tabLabel, String envelopeId);

	@Procedure(procedureName = "sproc_dstab_insert_update", outputParameterName = "result")
	String insertUpdate(String json);
	
	@Procedure(procedureName = "sproc_dstab_insert", outputParameterName = "result")
	String insert(String json);
	
	@Procedure(procedureName = "sproc_dstab_update", outputParameterName = "result")
	String update(String json);

	@Query(value = "sproc_getdstabforenvid :recordId, :sentdate", nativeQuery = true)
	List<DSTab> getAllTabsByEnvelopeIdsAfterSentDateTime(@Param("recordId") String recordIdsAsJSON,
			@Param("sentdate") LocalDateTime sentdate);

}