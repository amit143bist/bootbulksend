package com.ds.proserv.appdata.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.proserv.appdata.model.DrawApplication;
import com.ds.proserv.appdata.projection.DrawApplicationIdProjection;

@Repository(value = "drawApplicationPagingAndSortingRepository")
public interface DrawApplicationPagingAndSortingRepository extends PagingAndSortingRepository<DrawApplication, String> {

	Optional<DrawApplication> findByTriggerEnvelopeId(String triggerEnvelopeId);

	Optional<DrawApplication> findByBridgeEnvelopeId(String bridgeEnvelopeId);

	Optional<DrawApplication> findByBulkBatchId(String bulkBatchId);

	Slice<DrawApplication> findAllByApplicationIdIn(List<String> applicationIds, Pageable pageable);

	Long countByApplicationStatus(String applicationStatus);

	Slice<DrawApplication> findAllByApplicationStatusIn(List<String> applicationStatuses, Pageable pageable);

	Long countByLanguageCode(String languageCode);

	Slice<DrawApplication> findAllByLanguageCodeIn(List<String> languageCodes, Pageable pageable);

	Long countByAgentCode(String agentCode);

	Slice<DrawApplication> findAllByAgentCodeIn(List<String> agentCodes, Pageable pageable);

	Long countByDrawReference(String drawReference);

	Slice<DrawApplication> findAllByDrawReferenceIn(List<String> drawReferences, Pageable pageable);

	Long countByProgramType(String programType);

	Slice<DrawApplication> findAllByProgramTypeIn(List<String> programTypes, Pageable pageable);

	Iterable<DrawApplicationIdProjection> findApplicationIdByApplicationStatusIn(List<String> applicationStatuses);

	List<DrawApplication> findAllByTriggerEnvelopeIdOrBridgeEnvelopeId(String envelopeId, String orEnvelopeId);

	@Modifying
	@Query("update DrawApplication da set da.applicationStatus = :applicationStatus where da.applicationId in (:applicationIds)")
	void updateDrawApplicationStatus(@Param(value = "applicationStatus") String applicationStatus,
			@Param(value = "applicationIds") List<String> applicationIds);

}