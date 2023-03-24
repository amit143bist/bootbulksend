package com.ds.proserv.docdownload.consumer.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.EventType;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.commondata.client.CoreScheduledBatchLogClient;
import com.ds.proserv.docdownload.consumer.client.FolderNotificationClient;
import com.ds.proserv.feign.appdata.domain.FolderNotificationDefinition;
import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BucketService {

	private static final String BUCKET_NAME = "bucketName";
	private static final String RESET_TIME = "resetTime";

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private FolderNotificationClient folderNotificationClient;

	@Autowired
	private CoreScheduledBatchLogClient coreScheduledBatchLogClient;

	private static ConcurrentHashMap<String, String> bucketMap = new ConcurrentHashMap<String, String>();

	private Integer checkTimeDiffDuration() {

		String timeDurationDiff = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.CHECK_TIME_DURATION);

		if (!StringUtils.isEmpty(timeDurationDiff)) {

			return Integer.parseInt(timeDurationDiff);
		}

		return 600;
	}

	public String findBucketName(int counter) {

		if (null != bucketMap && !bucketMap.isEmpty()) {

			String lastResetTime = bucketMap.get(RESET_TIME);

			if (!StringUtils.isEmpty(lastResetTime)) {

				log.info("RESETTIME {} is not null or empty", lastResetTime);

				LocalDateTime resetTime = LocalDateTime.parse(lastResetTime);
				LocalDateTime currentTime = LocalDateTime.now();

				if (resetTime.isAfter(currentTime)) {

					long timeDiff = ChronoUnit.SECONDS.between(currentTime, resetTime);

					// If time difference is less than 10 mins then pause and get updated bucket
					// name
					Integer compareTimeDiff = checkTimeDiffDuration();
					if (timeDiff < compareTimeDiff) {

						log.warn("DO NOT START NEW DOWNLOAD if less than {} seconds left to start Notification Job",
								compareTimeDiff);

						int sleepInterval = ((compareTimeDiff + 20) * 1000) * counter;
						try {

							log.info("Thread put to sleep for {} milliseconds as timediff is below threshold",
									sleepInterval);
							Thread.sleep(sleepInterval);

						} catch (InterruptedException e) {

							log.info("InterruptedException occurred in findBucketName for counter {}", counter);
							e.printStackTrace();
						}

						return findBucketName((counter + 1));
					} else {

						log.info("Reset Time {} is after currentTime {} by more than {} seconds", resetTime,
								currentTime, compareTimeDiff);
					}

					// refresh this map
					// findBucketAndPauseIfBatchRunning(1);
				} else {

					log.info("RESET time is NOT After current Time");
					// refresh this map
					findBucketAndPauseIfBatchRunning(1);
				}

			}
		} else {

			if (null == bucketMap) {

				bucketMap = new ConcurrentHashMap<String, String>();
			}
			findBucketAndPauseIfBatchRunning(1);
		}

		return bucketMap.get(BUCKET_NAME);
	}

	private void findBucketAndPauseIfBatchRunning(int counter) {

		String batchType = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.DS_JOB_BATCHTYPE, AppConstants.DS_NOTIFICATION_BATCH_NAME);

		ScheduledBatchLogResponse scheduledBatchLogResponse = null;
		try {

			scheduledBatchLogResponse = coreScheduledBatchLogClient.findLatestBatchByBatchType(batchType).getBody();
		} catch (Exception exp) {

			// Below code should run only once, when batch will be triggered first time
			log.info("No Batch running of batchType -> {}", batchType);
		}

		if (null != scheduledBatchLogResponse) {

			if (StringUtils.isEmpty(scheduledBatchLogResponse.getBatchId())) {

				log.info("No Batch exist of type {} so finding firstBucketName", batchType);
				findFirstBucketName(scheduledBatchLogResponse, 1);
			} else {

				if (StringUtils.isEmpty(scheduledBatchLogResponse.getBatchEndDateTime())) {

					log.info("{} batch is still running so pause doc download till this batch completion", batchType);

					try {

						int sleepInterval = 100000 * counter;
						log.info("Putting thread to sleep for {} milliseconds, to wait for {} batch to finish",
								sleepInterval, batchType);

						Thread.sleep(sleepInterval);
					} catch (InterruptedException e) {

						log.info("InterruptedException occurred in findBucketAndPauseIfBatchRunning for counter {}",
								counter);
						e.printStackTrace();
					}

					findBucketAndPauseIfBatchRunning((counter + 1));
				} else {

					log.info("{} batch is not running so find the current bucketname to be used", batchType);

					FolderNotificationDefinition folderNotificationDefinition = null;
					try {

						folderNotificationDefinition = folderNotificationClient
								.findLatestFolderNameByEventType(EventType.MIGRATIONTOCLM.toString()).getBody();
					} catch (Exception exp) {

						// Below code should run only once, when batch will be triggered first time
						log.info("No folder exist for eventtype in else -> {}", EventType.MIGRATIONTOCLM.toString());
					}

					if (null == folderNotificationDefinition
							|| StringUtils.isEmpty(folderNotificationDefinition.getFolderName())) {

						log.info("No folderExist in db for eventType {} so creating first bucket",
								EventType.MIGRATIONTOCLM.toString());
						findFirstBucketName(scheduledBatchLogResponse, 1);
					} else {

						LocalDateTime resetTime = findResetTime(scheduledBatchLogResponse);
						String bucketName = folderNotificationDefinition.getFolderName();

						bucketMap.put(RESET_TIME, resetTime.toString());
						bucketMap.put(BUCKET_NAME, bucketName);
					}

				}
			}
		} else {

			findFirstBucketName(scheduledBatchLogResponse, 1);
		}

	}

	private String findFirstBucketName(ScheduledBatchLogResponse scheduledBatchLogResponse, int backOffCounter) {

		scheduledBatchLogResponse = checkIfBatchAvailable(scheduledBatchLogResponse);

		FolderNotificationDefinition folderNotificationDefinition = null;
		try {

			folderNotificationDefinition = folderNotificationClient
					.findLatestFolderNameByEventType(EventType.MIGRATIONTOCLM.toString()).getBody();
		} catch (Exception exp) {

			// Below code should run only once, when batch will be triggered first time
			log.info("No folder exist for eventtype in findFirstBucketName -> {}", EventType.MIGRATIONTOCLM.toString());
		}
		log.info("Manifest file not created till now, return first default bucketName");

		String bucketName = null;
		if (null == folderNotificationDefinition || StringUtils.isEmpty(folderNotificationDefinition.getId())) {

			int sleepInterval = 100000 * backOffCounter;
			log.info(
					"No Bucket ever created so putting thread to sleep for {} milliseconds, to wait for {} batch to finish",
					sleepInterval, backOffCounter);
			try {

				Thread.sleep(sleepInterval);
			} catch (InterruptedException e) {

				log.info("InterruptedException occurred in findFirstBucketName for counter {}", backOffCounter);
				e.printStackTrace();
			}

			return findFirstBucketName(scheduledBatchLogResponse, (backOffCounter + 1));
		} else {

			log.info("folderNotificationDefinition is not null in findFirstBucketName");
			bucketName = folderNotificationDefinition.getFolderName();
		}

		LocalDateTime resetTime = findResetTime(scheduledBatchLogResponse);

		log.info("Setting bucketName -> {} and resetTime -> {} in bucketMap", bucketName, resetTime.toString());
		bucketMap.put(RESET_TIME, resetTime.toString());
		bucketMap.put(BUCKET_NAME, bucketName);
		return bucketName;
	}

	private ScheduledBatchLogResponse checkIfBatchAvailable(ScheduledBatchLogResponse scheduledBatchLogResponse) {

		if (null == scheduledBatchLogResponse) {

			String batchType = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
					PropertyCacheConstants.DS_JOB_BATCHTYPE, AppConstants.DS_NOTIFICATION_BATCH_NAME);
			try {

				scheduledBatchLogResponse = coreScheduledBatchLogClient.findLatestBatchByBatchType(batchType).getBody();
			} catch (Exception exp) {

				// Below code should run only once, when batch will be triggered first time
				log.info("No Batch running of batchType -> {}", batchType);
			}
		}
		return scheduledBatchLogResponse;
	}

	private LocalDateTime findResetTime(ScheduledBatchLogResponse scheduledBatchLogResponse) {

		String notificationJobFrequency = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.NOTIFICATION_MIGRATIONREADY_RATE_IN_SECS,
				AppConstants.DS_NOTIFICATION_BATCH_NAME);
		String lastBatchStartDateTimeAsStr = scheduledBatchLogResponse.getBatchStartDateTime();

		LocalDateTime lastBatchStartDateTime = LocalDateTime.parse(lastBatchStartDateTimeAsStr);
		LocalDateTime resetTime = lastBatchStartDateTime.plusSeconds(Integer.parseInt(notificationJobFrequency));
		return resetTime;
	}

	public boolean useBucketLogic() {

		// USE_BUCKET_LOGIC
		String useBucketLogicStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(
				PropertyCacheConstants.USE_BUCKET_LOGIC, AppConstants.DS_NOTIFICATION_BATCH_NAME);

		if (!StringUtils.isEmpty(useBucketLogicStr)) {

			return Boolean.parseBoolean(useBucketLogicStr);
		}

		return true;
	}
}