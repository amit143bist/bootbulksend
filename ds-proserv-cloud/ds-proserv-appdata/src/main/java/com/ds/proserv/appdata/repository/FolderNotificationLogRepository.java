package com.ds.proserv.appdata.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.ds.proserv.appdata.model.FolderNotificationLog;

public interface FolderNotificationLogRepository extends CrudRepository<FolderNotificationLog, String> {

	List<FolderNotificationLog> findAllByEventTypeAndFolderNameIn(String eventType, List<String> folderNames);

	Optional<FolderNotificationLog> findTopByEventTypeOrderByEventTimestampDesc(String eventType);

	@Modifying
	@Query("update FolderNotificationLog fnl set fnl.eventTimestamp = :eventTimestamp, fnl.updatedBy = 'DSAPP', fnl.updatedDateTime = :eventTimestamp, fnl.folderName = :folderName where fnl.id = :id")
	void updateFolderNotificationLog(@Param(value = "eventTimestamp") LocalDateTime eventTimestamp,
			@Param(value = "folderName") String folderName, @Param(value = "id") String id);
	
	@Modifying
	@Query("update FolderNotificationLog fnl set fnl.eventTimestamp = :eventTimestamp, fnl.fileCount = :fileCount, fnl.updatedBy = 'DSAPP', fnl.updatedDateTime = :eventTimestamp, fnl.folderName = :folderName where fnl.id = :id")
	void updateFolderNotificationLog(@Param(value = "eventTimestamp") LocalDateTime eventTimestamp,
			@Param(value = "folderName") String folderName, @Param(value = "fileCount") Long fileCount, @Param(value = "id") String id);
}