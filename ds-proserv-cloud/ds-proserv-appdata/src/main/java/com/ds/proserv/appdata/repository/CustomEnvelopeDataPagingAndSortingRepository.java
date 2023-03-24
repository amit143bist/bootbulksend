package com.ds.proserv.appdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ds.proserv.appdata.model.CustomEnvelopeData;
import com.ds.proserv.appdata.projection.CustomEnvelopeDataBucketNameProjection;
import com.ds.proserv.appdata.projection.CustomEnvelopeDataCountDateProjection;
import com.ds.proserv.appdata.projection.CustomEnvelopeDataIdProjection;

@Repository(value = "customEnvelopeDataRepository")
public interface CustomEnvelopeDataPagingAndSortingRepository
		extends PagingAndSortingRepository<CustomEnvelopeData, String> {

	Iterable<CustomEnvelopeDataIdProjection> findAllByEnvelopeIdInAndDocDownloadStatusFlagIsNull(
			List<String> envelopeIds);

	Slice<CustomEnvelopeData> findAllByEnvTimeStampBetweenAndEnvProcessStatusFlag(LocalDateTime startDateTime,
			LocalDateTime endDateTime, String envProcessStatusFlag, Pageable pageable);

	Slice<CustomEnvelopeData> findAllByEnvTimeStampBetweenAndEnvProcessStatusFlagIsNull(LocalDateTime startDateTime,
			LocalDateTime endDateTime, Pageable pageable);

	Slice<CustomEnvelopeData> findAllByEnvTimeStampBetweenAndEnvProcessStatusFlagIn(LocalDateTime startDateTime,
			LocalDateTime endDateTime, List<String> envProcessStatusFlags, Pageable pageable);

	Slice<CustomEnvelopeData> findAllByEnvTimeStampBetweenAndEnvProcessStatusFlagInOrEnvTimeStampBetweenAndEnvProcessStatusFlagIsNull(
			LocalDateTime startDateTime, LocalDateTime endDateTime, List<String> envProcessStatusFlags,
			LocalDateTime OrStartDateTime, LocalDateTime OrEndDateTime, Pageable pageable);

	Slice<CustomEnvelopeData> findAllByEnvTimeStampBetweenAndEnvProcessStatusFlagOrEnvTimeStampBetweenAndEnvProcessStatusFlagIsNull(
			LocalDateTime startDateTime, LocalDateTime endDateTime, String envProcessStatusFlag,
			LocalDateTime OrStartDateTime, LocalDateTime OrEndDateTime, Pageable pageable);

	// Below with SenderIdentifier
	Slice<CustomEnvelopeData> findAllByEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlag(
			LocalDateTime startDateTime, LocalDateTime endDateTime, String senderIdentifier,
			String envProcessStatusFlag, Pageable pageable);

	Slice<CustomEnvelopeData> findAllByEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlagIsNull(
			LocalDateTime startDateTime, LocalDateTime endDateTime, String senderIdentifier, Pageable pageable);

	Slice<CustomEnvelopeData> findAllByEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlagIn(
			LocalDateTime startDateTime, LocalDateTime endDateTime, String senderIdentifier,
			List<String> envProcessStatusFlags, Pageable pageable);

	Slice<CustomEnvelopeData> findAllByEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlagInOrEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlagIsNull(
			LocalDateTime startDateTime, LocalDateTime endDateTime, String senderIdentifier,
			List<String> envProcessStatusFlags, LocalDateTime OrStartDateTime, LocalDateTime OrEndDateTime,
			String orSenderIdentifier, Pageable pageable);

	Slice<CustomEnvelopeData> findAllByEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlagOrEnvTimeStampBetweenAndSenderIdentifierAndEnvProcessStatusFlagIsNull(
			LocalDateTime startDateTime, LocalDateTime endDateTime, String senderIdentifier,
			String envProcessStatusFlag, LocalDateTime OrStartDateTime, LocalDateTime OrEndDateTime,
			String orSenderIdentifier, Pageable pageable);

	Slice<CustomEnvelopeDataIdProjection> findAllByDownloadBucketName(String downloadBucketName, Pageable pageable);

	@Modifying
	@Query(value = "sproc_updatecustomenvelopedata_envprocessstartdatetime :recordId", nativeQuery = true)
	void updateCustomEnvelopeDataProcessStatusStartTimeBySP(@Param("recordId") String recordIdsAsJSON);

	@Modifying
	@Query("update CustomEnvelopeData ced set ced.envProcessStatusFlag = :envProcessStatusFlag, ced.envProcessStartDateTime = :envProcessStartDateTime, ced.updatedBy = 'DSAPP', ced.updatedDateTime = :envProcessStartDateTime where ced.envelopeId in (:envelopeIds)")
	void updateCustomEnvelopeDataProcessStatusStartTime(
			@Param(value = "envProcessStatusFlag") String envProcessStatusFlag,
			@Param(value = "envProcessStartDateTime") LocalDateTime envProcessStartDateTime,
			@Param(value = "envelopeIds") List<String> envelopeIds);

	@Modifying
	@Query(value = "sproc_updatecustomenvelopedata_envprocessenddatetime :recordId", nativeQuery = true)
	void updateCustomEnvelopeDataProcessStatusEndTimeBySP(@Param("recordId") String recordIdsAsJSON);

	@Modifying
	@Query("update CustomEnvelopeData ced set ced.envProcessStatusFlag = :envProcessStatusFlag, ced.envProcessEndDateTime = :envProcessEndDateTime, ced.updatedBy = 'DSAPP', ced.updatedDateTime = :envProcessEndDateTime where ced.envelopeId in (:envelopeIds)")
	void updateCustomEnvelopeDataProcessStatusEndTime(
			@Param(value = "envProcessStatusFlag") String envProcessStatusFlag,
			@Param(value = "envProcessEndDateTime") LocalDateTime envProcessEndDateTime,
			@Param(value = "envelopeIds") List<String> envelopeIds);

	@Modifying
	@Query(value = "sproc_updatecustomenvelopedata_docdownloadtimestamp_without_bucketname :recordId", nativeQuery = true)
	void updateCustomEnvelopeDataDocDownloadStatusEndTimeBySP(@Param("recordId") String recordIdsAsJSON);

	@Modifying
	@Query("update CustomEnvelopeData ced set ced.docDownloadStatusFlag = :docDownloadStatusFlag, ced.docDownloadTimeStamp = :docDownloadTimeStamp, ced.updatedBy = 'DSAPP', ced.updatedDateTime = :docDownloadTimeStamp where ced.envelopeId in (:envelopeIds)")
	void updateCustomEnvelopeDataDocDownloadStatusEndTime(
			@Param(value = "docDownloadStatusFlag") String docDownloadStatusFlag,
			@Param(value = "docDownloadTimeStamp") LocalDateTime docDownloadTimeStamp,
			@Param(value = "envelopeIds") List<String> envelopeIds);

	@Modifying
	@Query(value = "sproc_updatecustomenvelopedata_docdownloadtimestamp :recordId, :downloadBucketName", nativeQuery = true)
	void updateCustomEnvelopeDataDocDownloadStatusEndTimeBucketNameBySP(@Param("recordId") String recordIdsAsJSON,
			@Param("downloadBucketName") String downloadBucketName);

	@Modifying
	@Query("update CustomEnvelopeData ced set ced.docDownloadStatusFlag = :docDownloadStatusFlag, ced.docDownloadTimeStamp = :docDownloadTimeStamp, ced.downloadBucketName = :downloadBucketName, ced.updatedBy = 'DSAPP', ced.updatedDateTime = :docDownloadTimeStamp where ced.envelopeId in (:envelopeIds)")
	void updateCustomEnvelopeDataDocDownloadStatusEndTimeByBucketName(
			@Param(value = "docDownloadStatusFlag") String docDownloadStatusFlag,
			@Param(value = "docDownloadTimeStamp") LocalDateTime docDownloadTimeStamp,
			@Param(value = "downloadBucketName") String downloadBucketName,
			@Param(value = "envelopeIds") List<String> envelopeIds);

	@Query("select count(1) as count, c1.envDate as date from CustomEnvelopeData c1 where c1.envDate in (select distinct envDate from CustomEnvelopeData c2 where c2.docDownloadStatusFlag = 'COMPLETED' and c2.envDate not in (select envDate from CustomEnvelopeData where (docDownloadStatusFlag != 'COMPLETED' or docDownloadStatusFlag is null))) group by c1.envDate")
	List<CustomEnvelopeDataCountDateProjection> findAllDownloadedEnvelopesCountByDate();

	@Query("select count(1) as count, c1.downloadBucketName as downloadBucketName from CustomEnvelopeData c1 where c1.downloadBucketName = :downloadBucketName and c1.downloadBucketName in (select distinct downloadBucketName from CustomEnvelopeData c2 where c2.downloadBucketName = :downloadBucketName and c2.docDownloadStatusFlag = 'COMPLETED' and c2.downloadBucketName not in (select downloadBucketName from CustomEnvelopeData where downloadBucketName = :downloadBucketName and (docDownloadStatusFlag != 'COMPLETED' or docDownloadStatusFlag is null))) group by c1.downloadBucketName")
	List<CustomEnvelopeDataBucketNameProjection> findAllDownloadedEnvelopesCountByBucketName(
			@Param(value = "downloadBucketName") String downloadBucketName);

	@Query(value = "sproc_getcustomenvelopedata :downloadBucketName", nativeQuery = true)
	List<CustomEnvelopeDataBucketNameProjection> getAllDownloadedEnvelopesCountByBucketName(
			@Param("downloadBucketName") String downloadBucketName);

	@Query(value = "sproc_getcustomenvelopedataforenvid :envId", nativeQuery = true)
	CustomEnvelopeData getCustomEnvelopeDataById(@Param("envId") String envId);
}