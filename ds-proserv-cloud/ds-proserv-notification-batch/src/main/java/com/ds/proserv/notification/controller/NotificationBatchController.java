package com.ds.proserv.notification.controller;

import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ds.proserv.batch.common.service.CoreBatchDataService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.EventType;
import com.ds.proserv.common.constant.MailProcessorType;
import com.ds.proserv.common.constant.NotificationType;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.email.common.service.EmailService;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataCountBucketNameInformation;
import com.ds.proserv.feign.appdata.domain.FolderNotificationDefinition;
import com.ds.proserv.notification.client.CustomEnvelopeDataClient;
import com.ds.proserv.notification.client.FolderNotificationClient;
import com.ds.proserv.notification.service.CreateCSVService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class NotificationBatchController {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private EmailService emailService;

	@Autowired
	private CreateCSVService createCSVService;

	@Autowired
	private CoreBatchDataService coreBatchDataService;

	@Autowired
	private CustomEnvelopeDataClient customEnvelopeDataClient;

	@Autowired
	private FolderNotificationClient folderNotificationClient;

	@Scheduled(fixedRateString = "#{@getScheduleMigrationReadyFixedRate}")
	public void triggerMigrationCheckJobV2() throws Exception {

		int fileCount = 0;
		String batchId = coreBatchDataService.checkOrCreateBatch();

		FolderNotificationDefinition existingFolderNotificationDefinition = findLatestFolderByEventType(batchId);

		if (null != existingFolderNotificationDefinition
				&& !StringUtils.isEmpty(existingFolderNotificationDefinition.getId())) {

			String bucketName = existingFolderNotificationDefinition.getFolderName();

			log.info("Bucket -> {} is read from folder table", bucketName);

			CustomEnvelopeDataCountBucketNameInformation customEnvelopeDataCountBucketNameInformation = customEnvelopeDataClient
					.findAllDownloadedEnvelopesCountByBucketName(bucketName).getBody();

			if (null != customEnvelopeDataCountBucketNameInformation
					&& null != customEnvelopeDataCountBucketNameInformation.getTotalRecords()
					&& customEnvelopeDataCountBucketNameInformation.getTotalRecords() > 0) {

				log.info("Bucket -> {} is ready for migration", bucketName);

				fileCount = createCSVService.createCSV(bucketName, batchId);

				existingFolderNotificationDefinition.setFileCount(Long.valueOf(fileCount));
				folderNotificationClient.updateFolderNotification(existingFolderNotificationDefinition);

				StringBuilder strBuilder = new StringBuilder();

				strBuilder.append(
						"<table><thead style='color:black;'><tr><th>FolderName&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th><th>FileCount</th></tr></thead>");

				strBuilder.append("<tbody>");

				strBuilder.append("<tr style='color:blue;'><td>" + bucketName + "</td><td>" + fileCount + "</td></tr>");

				strBuilder.append("</tbody></table>");

				String emailSubject = dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.NOTIFICATION_CLMMIGRATION_EMAILSUBJECT);
				String emailBody = strBuilder.toString();

				List<String> toRecipients = DSUtil.getFieldsAsList(
						dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.MIGRATION_TO_RECIPIENTS));

				log.info("emailBody in triggerMigrationCheckJob is {}", emailBody);

				log.info("Preparing to send Notification via GMAIL to {}", toRecipients);
				emailService.sentNotification(MailProcessorType.GMAIL, NotificationType.READYFORMIGRATION, toRecipients,
						null, null, emailSubject, emailBody, null);

				createNewBucket(bucketName);

			} else {

				log.info("Bucket -> {} is NOT ready for migration", bucketName);

			}
		}

		coreBatchDataService.finishNewBatch(batchId, Long.valueOf(fileCount));
	}

	private FolderNotificationDefinition findLatestFolderByEventType(String batchId) {

		FolderNotificationDefinition existingFolderNotificationDefinition = null;
		try {

			existingFolderNotificationDefinition = folderNotificationClient
					.findLatestFolderNameByEventType(EventType.MIGRATIONTOCLM.toString()).getBody();
		} catch (Exception exp) {

			log.info("exp -> {} is caught for batchId -> {}", exp, batchId);
			// ResponseStatusException exp
			// Below code should run only once, when batch will be triggered first time
			log.info("No folder exist for eventType -> {} in batchId -> {}", EventType.MIGRATIONTOCLM.toString(),
					batchId);
			if (exp instanceof ResponseStatusException
					&& ((ResponseStatusException) exp).getStatus() == HttpStatus.NOT_FOUND) {

				log.info("Folders are empty in db for batchId -> {}", batchId);
				FolderNotificationDefinition folderNotificationDefinition = new FolderNotificationDefinition();
				folderNotificationDefinition.setEventType(EventType.MIGRATIONTOCLM.toString());
				folderNotificationDefinition.setFolderName(AppConstants.DEFAULT_DOWNLOAD_BUCKETNAME + "_" + 1);

				folderNotificationClient.saveFolderNotification(folderNotificationDefinition);

			}
		}
		return existingFolderNotificationDefinition;
	}

	private void createNewBucket(String oldBucketName) {

		String[] bucketArr = oldBucketName.split("_");
		int oldBucketNumber = Integer.parseInt(bucketArr[1]);

		FolderNotificationDefinition folderNotificationDefinition = new FolderNotificationDefinition();
		folderNotificationDefinition.setEventType(EventType.MIGRATIONTOCLM.toString());
		folderNotificationDefinition.setFolderName(bucketArr[0] + "_" + (oldBucketNumber + 1));

		log.info("Creating new bucket -> {}", folderNotificationDefinition.getFolderName());
		folderNotificationClient.saveFolderNotification(folderNotificationDefinition);
	}

	@Scheduled(fixedRateString = "#{@getScheduleDeadQueueFixedRate}")
	public void triggerDeadQueueCheckJob() throws Exception {

		if (isDeadQueueStatusCheckEnabled()) {

			log.info("DeadQueueStatusCheck is enabled");

			try {

				String vmHostNamesAsStr = dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.VM_HOSTNAMES);
				List<String> hostNameList = DSUtil.getFieldsAsList(vmHostNamesAsStr);

				String deadQueueNamesAsStr = dsCacheManager
						.prepareAndRequestCacheDataByKey(PropertyCacheConstants.VM_DEAD_QUEUENAMES);
				List<String> deadQueueNames = DSUtil.getFieldsAsList(deadQueueNamesAsStr);

				StringBuilder strBuilder = new StringBuilder();

				strBuilder.append(
						"<table><thead style='color:black;'><tr><th>VMName&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th><th>QueueName&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th><th>Count</th></tr></thead>");

				strBuilder.append("<tbody>");

				boolean sendMessage = false;
				for (String hostName : hostNameList) {

					log.info("Checking DEADQueue count in hostName -> {}", hostName);
					CachingConnectionFactory factory = new CachingConnectionFactory(buildConnectionFactory(hostName,
							dsCacheManager.prepareAndRequestCacheDataByKey(hostName + ".username"),
							dsCacheManager.prepareAndRequestCacheDataByKey(hostName + ".password")).getObject());
					RabbitAdmin rabbitAdmin = new RabbitAdmin(factory);

					for (String queueName : deadQueueNames) {

						Properties queueProperties = rabbitAdmin.getQueueProperties(queueName);

						if (null != queueProperties) {

							Integer messageCount = Integer
									.parseInt(queueProperties.get("QUEUE_MESSAGE_COUNT").toString());

							if (messageCount > 0) {

								sendMessage = true;

								strBuilder.append("<tr style='color:blue;'><td>" + hostName + "</td><td>" + queueName
										+ "</td><td>" + messageCount + "</td></tr>");
							}
						}
					}

				}

				strBuilder.append("</tbody></table>");
				if (sendMessage) {

					String emailSubject = dsCacheManager
							.prepareAndRequestCacheDataByKey(PropertyCacheConstants.NOTIFICATION_DEADQUEUES_EMAILSUBJECT);
					String emailBody = strBuilder.toString();

					log.info("emailBody in triggerDeadQueueCheckJob is {}", emailBody);
					List<String> toRecipients = DSUtil.getFieldsAsList(dsCacheManager
							.prepareAndRequestCacheDataByKey(PropertyCacheConstants.DEADQUEUES_TO_RECIPIENTS));

					emailService.sentNotification(MailProcessorType.GMAIL, NotificationType.DEADQUEUE, toRecipients,
							null, null, emailSubject, emailBody, null);
				}

			} catch (Exception exp) {

				log.error("exp -> {} occurred in triggerDeadQueueCheckJob", exp);
				exp.printStackTrace();
			}
		} else {

			log.info("DeadQueueStatusCheck is disabled");
		}

	}

	private boolean isDeadQueueStatusCheckEnabled() {

		String checkStr = dsCacheManager
				.prepareAndRequestCacheDataByKey(PropertyCacheConstants.ENABLE_DEADQUEUE_STATUS_CHECK);

		if (!StringUtils.isEmpty(checkStr)) {

			return Boolean.parseBoolean(checkStr);
		}

		return false;
	}

	private static RabbitConnectionFactoryBean buildConnectionFactory(String hostName, String userName,
			String password) {

		RabbitConnectionFactoryBean factory = new RabbitConnectionFactoryBean();
		// Generic connection properties
		factory.setHost(hostName);
		factory.setPort(5672);
		factory.setUsername(userName);
		factory.setPassword(password);
		factory.setVirtualHost("/");

		factory.afterPropertiesSet();

		return factory;
	}

	// , 5672
	@RequestMapping(value = "/successcallback", method = RequestMethod.GET)
	public String successcallback(ModelMap model) {

		log.info("NotificationBatchController.successcallback() called");

		return "successcallback";
	}

	@RequestMapping(value = "/fetchgoogletoken", method = RequestMethod.GET)
	@ResponseBody
	public String fetchGoogleToken(@RequestParam("authCode") String authCode,
			@RequestParam("authScope") String authScope, HttpServletRequest request, ModelMap model) {

		log.info("NotificationBatchController.fetchgoogletoken() called for scope -> {}", authScope);

		emailService.doAuthFlowAndSaveToken(MailProcessorType.GMAIL, request, authCode);

		return AppConstants.SUCCESS_VALUE;
	}

	@RequestMapping(value = "/gmailauth", method = RequestMethod.GET)
	public String gmailauth(ModelMap model, HttpServletRequest request) {

		log.info("NotificationBatchController.gmailauth() called");

		model.put("googleOAuthUrl", emailService.getAuthUrl(MailProcessorType.GMAIL, request));

		return "gmailauth";
	}
}