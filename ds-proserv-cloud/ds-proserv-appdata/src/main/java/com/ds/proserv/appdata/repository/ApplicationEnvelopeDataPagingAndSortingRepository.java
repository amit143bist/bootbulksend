package com.ds.proserv.appdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.proserv.appdata.model.ApplicationEnvelopeData;

@Repository(value = "applicationEnvelopeDataPagingAndSortingRepository")
public interface ApplicationEnvelopeDataPagingAndSortingRepository
		extends PagingAndSortingRepository<ApplicationEnvelopeData, String> {

	Long countByApplicationId(String applicationId);

	Long countByApplicationType(String applicationType);

	Long countByApplicationTypeAndEnvelopeIdIsNull(String applicationType);

	Slice<ApplicationEnvelopeData> findAllByApplicationTypeAndEnvelopeIdIsNull(String applicationType,
			Pageable pageable);

	List<ApplicationEnvelopeData> findAllByApplicationIdIn(List<String> applicationIds);

	List<ApplicationEnvelopeData> findAllByEnvelopeIdIn(List<String> envelopeIds);

	List<ApplicationEnvelopeData> findAllByRecipientEmailsContainingIgnoreCase(String recipientEmail);

	Long countByEnvelopeSentTimestampBetweenAndApplicationType(LocalDateTime startDateTime, LocalDateTime endDateTime,
			String applicationType);

	Slice<ApplicationEnvelopeData> findAllByEnvelopeSentTimestampBetweenAndApplicationType(LocalDateTime startDateTime,
			LocalDateTime endDateTime, String applicationType, Pageable pageable);

	Long countByFailureTimestampBetweenAndApplicationType(LocalDateTime startDateTime, LocalDateTime endDateTime,
			String applicationType);

	Slice<ApplicationEnvelopeData> findAllByFailureTimestampBetweenAndApplicationType(LocalDateTime startDateTime,
			LocalDateTime endDateTime, String applicationType, Pageable pageable);

	@Query(value = "sproc_getapplicationenvelopedata :recordId", nativeQuery = true)
	List<ApplicationEnvelopeData> getAllApplicationDataByEnvelopeIds(@Param("recordId") String recordIdsAsJSON);
	
	@Procedure(procedureName = "sproc_applicationenvelopedata_insert", outputParameterName = "result")
	String insert(String json);

}