package com.ds.proserv.notificationdata.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ds.proserv.notificationdata.model.NotificationDetailLog;

@Repository(value = "notificationDetailLogRepository")
public interface NotificationDetailLogRepository extends CrudRepository<NotificationDetailLog, String> {

}