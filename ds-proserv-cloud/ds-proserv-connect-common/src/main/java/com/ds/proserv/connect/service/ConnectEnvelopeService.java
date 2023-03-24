package com.ds.proserv.connect.service;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.CustomFieldType;
import com.ds.proserv.connect.domain.AuthenticationStatus;
import com.ds.proserv.connect.domain.ConnectSaveCacheData;
import com.ds.proserv.connect.domain.CustomTabType;
import com.ds.proserv.connect.domain.DocuSignEnvelopeInformation;
import com.ds.proserv.connect.domain.DocumentStatus;
import com.ds.proserv.connect.domain.EnvelopeStatus;
import com.ds.proserv.connect.domain.EnvelopeStatusCode;
import com.ds.proserv.connect.domain.EventResult;
import com.ds.proserv.connect.domain.FormDataXfdfField;
import com.ds.proserv.connect.domain.RecipientStatus;
import com.ds.proserv.connect.domain.RecipientStatusCode;
import com.ds.proserv.connect.domain.TabStatus;
import com.ds.proserv.connect.domain.TabTypeCode;
import com.ds.proserv.connect.helper.ConnectHelper;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDocLogDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientAuthDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSTabDefinition;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConnectEnvelopeService {

	@Autowired
	private TaskExecutor xmlTaskExecutor;

	@Autowired
	private ConnectHelper connectHelper;

	@Autowired
	private DSCacheManager dsCacheManager;

	public DSEnvelopeDefinition prepareEnvelopeDefinition(DocuSignEnvelopeInformation docuSignEnvelopeInformation) {

		EnvelopeStatus envelopeStatus = docuSignEnvelopeInformation.getEnvelopeStatus();
		String envelopeId = envelopeStatus.getEnvelopeID();

		try {

			log.info("Started preparing EnvelopeDefinition tree asynchronously for envelopeId -> {}", envelopeId);

			ConnectSaveCacheData connectSaveCacheData = new ConnectSaveCacheData(dsCacheManager);

			List<CompletableFuture<DSEnvelopeDefinition>> futureProcessAsyncList = new ArrayList<CompletableFuture<DSEnvelopeDefinition>>();

			futureProcessAsyncList.add(prepareEnvelopeLevelFieldDetailsAsync(docuSignEnvelopeInformation,
					envelopeStatus, envelopeId, connectSaveCacheData));
			futureProcessAsyncList
					.add(prepareCustomFieldAsync(docuSignEnvelopeInformation, envelopeId, connectSaveCacheData));
			futureProcessAsyncList
					.add(processRecipientDetailsAsync(docuSignEnvelopeInformation, envelopeId, connectSaveCacheData));

			return CompletableFuture
					.allOf(futureProcessAsyncList.toArray(new CompletableFuture[futureProcessAsyncList.size()]))
					.thenApply(processData -> {

						DSEnvelopeDefinition dsEnvelopeDefinition = futureProcessAsyncList.get(0).join();
						dsEnvelopeDefinition.setDsCustomFieldDefinitions(
								futureProcessAsyncList.get(1).join().getDsCustomFieldDefinitions());
						dsEnvelopeDefinition.setDsRecipientDefinitions(
								futureProcessAsyncList.get(2).join().getDsRecipientDefinitions());

						log.info("PrepareEnvelopeDetails processed for envelopeId asynchronously -> {}", envelopeId);

						dsEnvelopeDefinition.setPrepareResult(AppConstants.SUCCESS_VALUE);
						return dsEnvelopeDefinition;
					}).join();

		} catch (Throwable exp) {

			log.error(" ------------------------- ERROR: {} for EnvelopeId -> {} ------------------------- ",
					exp.getMessage(), envelopeId);

			exp.printStackTrace();
			throw exp;

		}

	}

	private CompletableFuture<DSEnvelopeDefinition> prepareCustomFieldAsync(
			DocuSignEnvelopeInformation docuSignEnvelopeInformation, String envelopeId,
			ConnectSaveCacheData connectSaveCacheData) {

		return CompletableFuture.supplyAsync((Supplier<DSEnvelopeDefinition>) () -> {

			try {
				DSEnvelopeDefinition newDsEnvelopeDefinition = new DSEnvelopeDefinition();
				Boolean saveCustomFieldData = connectSaveCacheData.isSaveCustomFieldData();

				if (null != saveCustomFieldData && saveCustomFieldData) {

					List<DSCustomFieldDefinition> dsCustomFieldDefinitionList = new ArrayList<DSCustomFieldDefinition>();

					prepareEnvelopeCustomFields(docuSignEnvelopeInformation, envelopeId, dsCustomFieldDefinitionList);

					prepareDocumentCustomFields(docuSignEnvelopeInformation, envelopeId, dsCustomFieldDefinitionList);

					docuSignEnvelopeInformation.getEnvelopeStatus().getRecipientStatuses().getRecipientStatus()
							.forEach(recipientStatus -> {

								if (null != recipientStatus.getCustomFields()
										&& null != recipientStatus.getCustomFields().getCustomField()
										&& !recipientStatus.getCustomFields().getCustomField().isEmpty()) {

									prepareRecipientCustomFields(envelopeId, recipientStatus,
											dsCustomFieldDefinitionList);
								}
							});

					if (null != dsCustomFieldDefinitionList && !dsCustomFieldDefinitionList.isEmpty()) {

						newDsEnvelopeDefinition.setDsCustomFieldDefinitions(dsCustomFieldDefinitionList);
					}
				} else {

					log.info("No need to save custom field for envelopeId -> {} in DB", envelopeId);
				}

				return newDsEnvelopeDefinition;
			} catch (Exception exp) {

				exp.printStackTrace();
				throw exp;
			}
		}, xmlTaskExecutor).handle((newDsEnvelopeDefinition, exp) -> {

			connectHelper.handleAsyncStatus(exp, "prepareCustomFieldAsync", envelopeId);
			return newDsEnvelopeDefinition;
		});
	}

	private CompletableFuture<DSEnvelopeDefinition> processRecipientDetailsAsync(
			DocuSignEnvelopeInformation docuSignEnvelopeInformation, String envelopeId,
			ConnectSaveCacheData connectSaveCacheData) {

		return CompletableFuture.supplyAsync((Supplier<DSEnvelopeDefinition>) () -> {

			try {
				DSEnvelopeDefinition newDsEnvelopeDefinition = new DSEnvelopeDefinition();
				Boolean saveRecipientData = connectSaveCacheData.isSaveRecipientData();

				if (null != saveRecipientData && saveRecipientData) {

					newDsEnvelopeDefinition.setDsRecipientDefinitions(prepareDSRecipientAndTabDefinitionList(
							docuSignEnvelopeInformation, envelopeId, connectSaveCacheData));
				}

				return newDsEnvelopeDefinition;
			} catch (Exception exp) {

				exp.printStackTrace();
				throw exp;
			}
		}, xmlTaskExecutor).handle((newDsEnvelopeDefinition, exp) -> {

			connectHelper.handleAsyncStatus(exp, "processRecipientDetailsAsync", envelopeId);
			return newDsEnvelopeDefinition;
		});
	}

	private CompletableFuture<DSEnvelopeDefinition> prepareEnvelopeLevelFieldDetailsAsync(
			DocuSignEnvelopeInformation docuSignEnvelopeInformation, EnvelopeStatus envelopeStatus, String envelopeId,
			ConnectSaveCacheData connectSaveCacheData) {

		return CompletableFuture.supplyAsync((Supplier<DSEnvelopeDefinition>) () -> {

			try {
				DSEnvelopeDefinition newDsEnvelopeDefinition = new DSEnvelopeDefinition();
				// set envelopeId
				newDsEnvelopeDefinition.setEnvelopeId(envelopeId);
				newDsEnvelopeDefinition.setEnvelopeSubject(envelopeStatus.getSubject());

				EnvelopeStatusCode envelopeStatusCode = envelopeStatus.getStatus();
				newDsEnvelopeDefinition.setStatus(envelopeStatusCode.value());

				log.info("EnvelopeId -> {} status from Connect Message is {}", envelopeId, envelopeStatusCode);

				if (EnvelopeStatusCode.VOIDED == envelopeStatusCode) {

					log.info("EnvelopeId -> {} is in Voided status", envelopeId);
					newDsEnvelopeDefinition.setTerminalReason(envelopeStatus.getVoidReason());

					if (!StringUtils.isEmpty(newDsEnvelopeDefinition.getTerminalReason())
							&& newDsEnvelopeDefinition.getTerminalReason().contains("Expired")) {

						newDsEnvelopeDefinition.setStatus(AppConstants.ENVELOPE_EXPIRED_STATUS);
					} else {
						log.warn("Either void reason is null or it is not expired void reason for envelopeId -> {}",
								envelopeId);
					}

				} else if (EnvelopeStatusCode.DECLINED == envelopeStatusCode) {

					// set declined date
					XMLGregorianCalendar utcDeclined = envelopeStatus.getDeclined();
					utcDeclined.setTimezone(docuSignEnvelopeInformation.getTimeZoneOffset());
					newDsEnvelopeDefinition.setDeclinedDateTime(
							utcDeclined.toGregorianCalendar().toZonedDateTime().toLocalDateTime().toString());

				}

				if (EnvelopeStatusCode.VOIDED != envelopeStatusCode
						&& EnvelopeStatusCode.COMPLETED != envelopeStatusCode
						&& null != envelopeStatus.getRecipientStatuses()
						&& null != envelopeStatus.getRecipientStatuses().getRecipientStatus()
						&& !envelopeStatus.getRecipientStatuses().getRecipientStatus().isEmpty()) {

					log.info("EnvelopeId -> {} status inside recipientStatus check is {}", envelopeId,
							envelopeStatusCode);

					envelopeStatus.getRecipientStatuses().getRecipientStatus().forEach(recipientStatus -> {

						if (RecipientStatusCode.DECLINED == recipientStatus.getStatus()) {

							log.info("EnvelopeId -> {} is in Declined status", envelopeId);

							newDsEnvelopeDefinition.setTerminalReason(recipientStatus.getDeclineReason());
							return;

						}

						if (RecipientStatusCode.AUTO_RESPONDED == recipientStatus.getStatus()) {

							log.info("EnvelopeId -> {} is in AutoResponded status", envelopeId);

							newDsEnvelopeDefinition.setStatus(RecipientStatusCode.AUTO_RESPONDED.value());
							return;
						}
					});
				}

				// set sender info
				newDsEnvelopeDefinition.setSenderName(envelopeStatus.getUserName());
				newDsEnvelopeDefinition.setSenderEmail(envelopeStatus.getEmail());
				newDsEnvelopeDefinition.setTimeZone(docuSignEnvelopeInformation.getTimeZone());
				newDsEnvelopeDefinition
						.setTimeZoneoffset(Long.valueOf(docuSignEnvelopeInformation.getTimeZoneOffset()));
				prepareFileNames(docuSignEnvelopeInformation, newDsEnvelopeDefinition);
				prepareEnvelopeEventTimeline(docuSignEnvelopeInformation, newDsEnvelopeDefinition);
				prepareDocDownloadInfo(docuSignEnvelopeInformation, newDsEnvelopeDefinition, connectSaveCacheData);

				return newDsEnvelopeDefinition;
			} catch (Exception exp) {

				exp.printStackTrace();
				throw exp;
			}
		}, xmlTaskExecutor).handle((newDsEnvelopeDefinition, exp) -> {

			connectHelper.handleAsyncStatus(exp, "prepareEnvelopeLevelFieldDetailsAsync", envelopeId);
			return newDsEnvelopeDefinition;
		});
	}

	private void prepareFileNames(DocuSignEnvelopeInformation docuSignEnvelopeInformation,
			DSEnvelopeDefinition newDsEnvelopeDefinition) {

		if (null != docuSignEnvelopeInformation.getEnvelopeStatus()
				&& null != docuSignEnvelopeInformation.getEnvelopeStatus().getDocumentStatuses()
				&& null != docuSignEnvelopeInformation.getEnvelopeStatus().getDocumentStatuses().getDocumentStatus()
				&& !docuSignEnvelopeInformation.getEnvelopeStatus().getDocumentStatuses().getDocumentStatus()
						.isEmpty()) {

			List<DocumentStatus> documentStatuses = docuSignEnvelopeInformation.getEnvelopeStatus()
					.getDocumentStatuses().getDocumentStatus();

			String fileNames = documentStatuses.stream().map(DocumentStatus::getName).map(name -> {

				if (!name.toLowerCase().contains(AppConstants.DOCUMENT_FILE_NAME_SUFFIX)) {
					return name + AppConstants.DOCUMENT_FILE_NAME_SUFFIX;
				} else {
					return name;
				}
			}).collect(Collectors.joining(AppConstants.COMMA_DELIMITER));

			newDsEnvelopeDefinition.setFileNames(fileNames);
		}
	}

	private void prepareDocDownloadInfo(DocuSignEnvelopeInformation docuSignEnvelopeInformation,
			DSEnvelopeDefinition newDsEnvelopeDefinition, ConnectSaveCacheData connectSaveCacheData) {

		List<String> envStatusesAvailableForDownload = connectSaveCacheData.getEnvStatusesAvailableForDownload();
		if (null != envStatusesAvailableForDownload && !envStatusesAvailableForDownload.isEmpty()) {

			if (envStatusesAvailableForDownload
					.contains(docuSignEnvelopeInformation.getEnvelopeStatus().getStatus().value())) {

				DSEnvelopeDocLogDefinition dsEnvelopeDocLogDefinition = new DSEnvelopeDocLogDefinition();
				dsEnvelopeDocLogDefinition
						.setEnvelopeId(docuSignEnvelopeInformation.getEnvelopeStatus().getEnvelopeID());
				dsEnvelopeDocLogDefinition.setTimeGenerated(newDsEnvelopeDefinition.getTimeGenerated());

				newDsEnvelopeDefinition.setDsEnvelopeDocLogDefinition(dsEnvelopeDocLogDefinition);
			}
		}
	}

	private void prepareDocumentCustomFields(DocuSignEnvelopeInformation docuSignEnvelopeInformation, String envelopeId,
			List<DSCustomFieldDefinition> dsCustomFieldDefinitionList) {

		log.info("prepareDocumentCustomFields processing for envelopeId -> {}", envelopeId);

		if (null != docuSignEnvelopeInformation.getEnvelopeStatus().getDocumentStatuses()
				&& null != docuSignEnvelopeInformation.getEnvelopeStatus().getDocumentStatuses().getDocumentStatus()
				&& !docuSignEnvelopeInformation.getEnvelopeStatus().getDocumentStatuses().getDocumentStatus()
						.isEmpty()) {

			docuSignEnvelopeInformation.getEnvelopeStatus().getDocumentStatuses().getDocumentStatus()
					.forEach(documentStatus -> {

						if (null != documentStatus.getDocumentFields()
								&& null != documentStatus.getDocumentFields().getDocumentField()
								&& !documentStatus.getDocumentFields().getDocumentField().isEmpty()) {

							documentStatus.getDocumentFields().getDocumentField().forEach(docField -> {

								DSCustomFieldDefinition dsDocumentCustomFieldDefinition = new DSCustomFieldDefinition();

								dsDocumentCustomFieldDefinition.setEnvelopeId(envelopeId);
								dsDocumentCustomFieldDefinition.setDocumentId(documentStatus.getID().longValue());
								dsDocumentCustomFieldDefinition.setDocumentName(documentStatus.getName());
								dsDocumentCustomFieldDefinition
										.setDocumentSequence(documentStatus.getSequence().longValue());
								dsDocumentCustomFieldDefinition.setFieldName(docField.getName());
								dsDocumentCustomFieldDefinition.setFieldValue(docField.getValue());
								dsDocumentCustomFieldDefinition.setFieldType(CustomFieldType.DCF.toString());
								dsCustomFieldDefinitionList.add(dsDocumentCustomFieldDefinition);

							});
						}

					});
		}
	}

	private void prepareEnvelopeCustomFields(DocuSignEnvelopeInformation docuSignEnvelopeInformation, String envelopeId,
			List<DSCustomFieldDefinition> dsCustomFieldDefinitionList) {

		log.info("prepareEnvelopeCustomFields processing for envelopeId -> {}", envelopeId);

		if (null != docuSignEnvelopeInformation.getEnvelopeStatus().getCustomFields()
				&& null != docuSignEnvelopeInformation.getEnvelopeStatus().getCustomFields().getCustomField()
				&& !docuSignEnvelopeInformation.getEnvelopeStatus().getCustomFields().getCustomField().isEmpty()) {

			docuSignEnvelopeInformation.getEnvelopeStatus().getCustomFields().getCustomField().forEach(customField -> {

				DSCustomFieldDefinition dsEnvelopeCustomFieldDefinition = new DSCustomFieldDefinition();

				dsEnvelopeCustomFieldDefinition.setEnvelopeId(envelopeId);
				dsEnvelopeCustomFieldDefinition.setFieldName(customField.getName());
				dsEnvelopeCustomFieldDefinition.setFieldValue(customField.getValue());
				dsEnvelopeCustomFieldDefinition.setFieldType(CustomFieldType.ECF.toString());

				dsCustomFieldDefinitionList.add(dsEnvelopeCustomFieldDefinition);
			});

		}
	}

	private void prepareEnvelopeEventTimeline(DocuSignEnvelopeInformation docuSignEnvelopeInformation,
			DSEnvelopeDefinition newDsEnvelopeDefinition) {

		log.info("prepareEnvelopeEventTimeline processing for envelopeId -> {}",
				docuSignEnvelopeInformation.getEnvelopeStatus().getEnvelopeID());

		// set completed date
		XMLGregorianCalendar utcCompleted = docuSignEnvelopeInformation.getEnvelopeStatus().getCompleted();

		if (null != utcCompleted) {

			utcCompleted.setTimezone(docuSignEnvelopeInformation.getTimeZoneOffset());
			newDsEnvelopeDefinition.setCompletedDateTime(
					utcCompleted.toGregorianCalendar().toZonedDateTime().toLocalDateTime().toString());
		}

		// set delivered date
		XMLGregorianCalendar utcDelivered = docuSignEnvelopeInformation.getEnvelopeStatus().getDelivered();

		if (null != utcDelivered) {

			utcDelivered.setTimezone(docuSignEnvelopeInformation.getTimeZoneOffset());
			newDsEnvelopeDefinition.setDeliveredDateTime(
					utcDelivered.toGregorianCalendar().toZonedDateTime().toLocalDateTime().toString());
		}

		// set sent date
		XMLGregorianCalendar utcSent = docuSignEnvelopeInformation.getEnvelopeStatus().getSent();

		if (null != utcSent) {

			utcSent.setTimezone(docuSignEnvelopeInformation.getTimeZoneOffset());
			newDsEnvelopeDefinition
					.setSentDateTime(utcSent.toGregorianCalendar().toZonedDateTime().toLocalDateTime().toString());
		}

		XMLGregorianCalendar utcTimeGenerated = docuSignEnvelopeInformation.getEnvelopeStatus().getTimeGenerated();
		utcTimeGenerated.setTimezone(docuSignEnvelopeInformation.getTimeZoneOffset());
		newDsEnvelopeDefinition.setTimeGenerated(
				utcTimeGenerated.toGregorianCalendar().toZonedDateTime().toLocalDateTime().toString());

	}

	private List<DSRecipientDefinition> prepareDSRecipientAndTabDefinitionList(
			DocuSignEnvelopeInformation docuSignEnvelopeInformation, String envelopeId,
			ConnectSaveCacheData connectSaveCacheData) {

		log.info("prepareDSRecipientAndTabDefinitionList started for envelopeId -> {}", envelopeId);

		List<DSRecipientDefinition> dsRecipientDefinitionList = new ArrayList<DSRecipientDefinition>();

		Boolean saveTabData = connectSaveCacheData.isSaveTabData();
		Boolean saveRecipientAuthData = connectSaveCacheData.isSaveRecipientAuthData();
		Boolean saveTabDataUsingFormData = connectSaveCacheData.isSaveTabDataUsingFormData();
		Boolean saveTabDataIgnoringNonFormTabTypes = connectSaveCacheData.isSaveTabDataIgnoreNonFormTabTypes();

		docuSignEnvelopeInformation.getEnvelopeStatus().getRecipientStatuses().getRecipientStatus()
				.forEach(recipientStatus -> {

					List<DSTabDefinition> dsTabDefinitionList = new ArrayList<DSTabDefinition>();

					if (null != saveTabData && saveTabData) {

						if (null != recipientStatus.getTabStatuses()
								&& null != recipientStatus.getTabStatuses().getTabStatus()
								&& !recipientStatus.getTabStatuses().getTabStatus().isEmpty()) {

							List<TabStatus> tabStatusList = recipientStatus.getTabStatuses().getTabStatus();

							if (null != saveTabDataUsingFormData && saveTabDataUsingFormData) {

								if (null != recipientStatus.getFormData()
										&& null != recipientStatus.getFormData().getXfdf()
										&& null != recipientStatus.getFormData().getXfdf().getFields()
										&& !recipientStatus.getFormData().getXfdf().getFields().getField().isEmpty()) {

									recipientStatus.getFormData().getXfdf().getFields().getField().forEach(formData -> {

										prepareTabListPerRecipient(envelopeId, recipientStatus, dsTabDefinitionList,
												tabStatusList, formData);
									});
								}
							} else {

								prepareTabListPerRecipient(envelopeId, recipientStatus, dsTabDefinitionList,
										tabStatusList, saveTabDataIgnoringNonFormTabTypes);
							}

						}
					}

					prepareRecipientList(envelopeId, dsRecipientDefinitionList, recipientStatus, dsTabDefinitionList,
							docuSignEnvelopeInformation.getTimeZoneOffset(), saveRecipientAuthData);
				});

		return dsRecipientDefinitionList;
	}

	private void prepareRecipientList(String envelopeId, List<DSRecipientDefinition> dsRecipientDefinitionList,
			RecipientStatus recipientStatus, List<DSTabDefinition> dsTabDefinitionList, Integer timeZoneOffset,
			Boolean saveRecipientAuthData) {

		log.info("PrepareRecipientList for envelopeId -> {}", envelopeId);
		DSRecipientDefinition dsRecipientDefinition = new DSRecipientDefinition();

		dsRecipientDefinition.setEnvelopeId(envelopeId);
		dsRecipientDefinition.setRecipientId(recipientStatus.getRecipientId());
		dsRecipientDefinition.setRecipientIPAddress(recipientStatus.getRecipientIPAddress());
		dsRecipientDefinition.setRoutingOrder(Long.valueOf(recipientStatus.getRoutingOrder()));
		dsRecipientDefinition.setRecipientEmail(recipientStatus.getEmail());
		dsRecipientDefinition.setRecipientName(recipientStatus.getUserName());
		dsRecipientDefinition.setStatus(recipientStatus.getStatus().toString());
		dsRecipientDefinition.setDsTabDefinitions(dsTabDefinitionList);
		dsRecipientDefinition.setDeclineReason(recipientStatus.getDeclineReason());

		dsRecipientDefinition.setClientUserId(recipientStatus.getClientUserId());

		prepareRecipientEventTimeline(envelopeId, recipientStatus, timeZoneOffset, dsRecipientDefinition);

		if (null != recipientStatus.getRecipientAuthenticationStatus() && null != saveRecipientAuthData
				&& saveRecipientAuthData) {

			List<DSRecipientAuthDefinition> dsRecipientAuthDefinitionList = new ArrayList<DSRecipientAuthDefinition>();
			prepareRecipientAuthDetails(envelopeId, timeZoneOffset, recipientStatus.getRecipientId(),
					recipientStatus.getRecipientAuthenticationStatus(), dsRecipientAuthDefinitionList);

			dsRecipientDefinition.setDsRecipientAuthDefinitions(dsRecipientAuthDefinitionList);
		}

		dsRecipientDefinitionList.add(dsRecipientDefinition);
	}

	private void prepareRecipientCustomFields(String envelopeId, RecipientStatus recipientStatus,
			List<DSCustomFieldDefinition> dsCustomFieldDefinitionList) {

		log.info("prepareRecipientCustomFields processing for envelopeId -> {} and recipientId -> {}", envelopeId,
				recipientStatus.getRecipientId());

		recipientStatus.getCustomFields().getCustomField().forEach(customField -> {

			if (!StringUtils.isEmpty(customField)) {

				DSCustomFieldDefinition dsRecipientCustomFieldDefinition = new DSCustomFieldDefinition();

				dsRecipientCustomFieldDefinition.setEnvelopeId(envelopeId);
				dsRecipientCustomFieldDefinition.setRecipientId(recipientStatus.getRecipientId());
				dsRecipientCustomFieldDefinition.setFieldName(customField);
				dsRecipientCustomFieldDefinition.setFieldValue(customField);
				dsRecipientCustomFieldDefinition.setFieldType(CustomFieldType.RCF.toString());

				dsCustomFieldDefinitionList.add(dsRecipientCustomFieldDefinition);
			}
		});
	}

	private void prepareRecipientEventTimeline(String envelopeId, RecipientStatus recipientStatus,
			Integer timeZoneOffset, DSRecipientDefinition dsRecipientDefinition) {

		log.info("prepareRecipientEventTimeline processing for envelopeId -> {} and recipientId -> {}", envelopeId,
				recipientStatus.getRecipientId());

		XMLGregorianCalendar utcSentDateTime = recipientStatus.getSent();
		if (null != utcSentDateTime) {

			utcSentDateTime.setTimezone(timeZoneOffset);
			dsRecipientDefinition.setSentDateTime(
					utcSentDateTime.toGregorianCalendar().toZonedDateTime().toLocalDateTime().toString());
		}

		XMLGregorianCalendar utcDeliveredDateTime = recipientStatus.getDelivered();

		if (null != utcDeliveredDateTime) {

			utcDeliveredDateTime.setTimezone(timeZoneOffset);
			dsRecipientDefinition.setDeliveredDateTime(
					utcDeliveredDateTime.toGregorianCalendar().toZonedDateTime().toLocalDateTime().toString());
		}

		XMLGregorianCalendar utcDeclinedDateTime = recipientStatus.getDeclined();
		if (null != utcDeclinedDateTime) {

			utcDeclinedDateTime.setTimezone(timeZoneOffset);
			dsRecipientDefinition.setDeclinedDateTime(
					utcDeclinedDateTime.toGregorianCalendar().toZonedDateTime().toLocalDateTime().toString());
		}

		XMLGregorianCalendar utcSignedDateTime = recipientStatus.getSigned();
		if (null != utcSignedDateTime) {

			utcSignedDateTime.setTimezone(timeZoneOffset);
			dsRecipientDefinition.setSignedDateTime(
					utcSignedDateTime.toGregorianCalendar().toZonedDateTime().toLocalDateTime().toString());
		}
	}

	private void prepareRecipientAuthDetails(String envelopeId, Integer timeZoneOffset, String recipientId,
			AuthenticationStatus authenticationStatus, List<DSRecipientAuthDefinition> dsRecipientAuthDefinitionList) {

		log.debug("prepareRecipientAuthDetails processing for envelopeId -> {} and recipientId -> {}", envelopeId,
				recipientId);

		try {

			final PropertyDescriptor[] propertyDescriptors = Introspector
					.getBeanInfo(authenticationStatus.getClass(), Object.class).getPropertyDescriptors();

			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {

				final Method readMethod = propertyDescriptor.getReadMethod();
				if (readMethod != null) {

					final String typeName = propertyDescriptor.getDisplayName();
					EventResult eventResult = (EventResult) readMethod.invoke(authenticationStatus, (Object[]) null);
					if (null != eventResult && null != eventResult.getEventTimestamp()
							&& null != eventResult.getStatus()) {

						DSRecipientAuthDefinition dsRecipientAuthDefinition = new DSRecipientAuthDefinition();
						dsRecipientAuthDefinition.setEnvelopeId(envelopeId);
						dsRecipientAuthDefinition.setRecipientId(recipientId);
						dsRecipientAuthDefinition.setType(typeName);
						dsRecipientAuthDefinition.setStatus(eventResult.getStatus().value());

						// set event date
						XMLGregorianCalendar utcEventDateTime = eventResult.getEventTimestamp();

						if (null != utcEventDateTime) {

							utcEventDateTime.setTimezone(timeZoneOffset);
							dsRecipientAuthDefinition.setEventDateTime(utcEventDateTime.toGregorianCalendar()
									.toZonedDateTime().toLocalDateTime().toString());
						}

						dsRecipientAuthDefinitionList.add(dsRecipientAuthDefinition);
					}
				}
			}

		} catch (SecurityException e) {

			log.error(
					" ------------------------- SecurityException ERROR: {} for EnvelopeId -> {} ------------------------- ",
					e.getMessage(), envelopeId);

			e.printStackTrace();
		} catch (IllegalAccessException e) {

			log.error(
					" ------------------------- IllegalAccessException ERROR: {} for EnvelopeId -> {} ------------------------- ",
					e.getMessage(), envelopeId);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {

			log.error(
					" ------------------------- IllegalArgumentException ERROR: {} for EnvelopeId -> {} ------------------------- ",
					e.getMessage(), envelopeId);
			e.printStackTrace();
		} catch (InvocationTargetException e) {

			log.error(
					" ------------------------- InvocationTargetException ERROR: {} for EnvelopeId -> {} ------------------------- ",
					e.getMessage(), envelopeId);
			e.printStackTrace();
		} catch (IntrospectionException e) {

			log.error(
					" ------------------------- IntrospectionException ERROR: {} for EnvelopeId -> {} ------------------------- ",
					e.getMessage(), envelopeId);
			e.printStackTrace();
		}

		log.info("prepareRecipientAuthDetails processed for envelopeId -> {} and recipientId -> {}", envelopeId,
				recipientId);
	}

	private void prepareTabListPerRecipient(String envelopeId, RecipientStatus recipientStatus,
			List<DSTabDefinition> dsTabDefinitionList, List<TabStatus> tabStatusList, FormDataXfdfField formData) {

		DSTabDefinition dsTabDefinition = new DSTabDefinition();

		TabStatus filterTabStatus = tabStatusList.stream().filter(tabStatus -> {

			log.debug(
					"TabLabel -> {}, tabValue -> {}, formName -> {} and formValue -> {} for envelopeId -> {} and recipientId -> {}",
					tabStatus.getTabLabel(), tabStatus.getTabValue(), formData.getName(), formData.getValue(),
					envelopeId, recipientStatus.getRecipientId());

			if ((!StringUtils.isEmpty(tabStatus.getTabLabel())
					&& formData.getName().equalsIgnoreCase(tabStatus.getTabLabel()))
					|| (!StringUtils.isEmpty(tabStatus.getTabName())
							&& formData.getName().equalsIgnoreCase(tabStatus.getTabName()))) {

				return true;
			} else {

				return false;
			}
		}).findFirst().orElse(null);

		if (null != filterTabStatus) {

			dsTabDefinition.setEnvelopeId(envelopeId);
			dsTabDefinition.setRecipientId(recipientStatus.getRecipientId());
			dsTabDefinition.setTabLabel(formData.getName());
			dsTabDefinition.setTabName(filterTabStatus.getTabName());
			dsTabDefinition.setTabOriginalValue(filterTabStatus.getOriginalValue());

			if (AppConstants.TABVALUE_X.equalsIgnoreCase(formData.getValue())) {

				dsTabDefinition.setTabValue(filterTabStatus.getTabName());

			} else {

				dsTabDefinition.setTabValue(formData.getValue());
			}

			dsTabDefinitionList.add(dsTabDefinition);
		}

	}

	private void prepareTabListPerRecipient(String envelopeId, RecipientStatus recipientStatus,
			List<DSTabDefinition> dsTabDefinitionList, List<TabStatus> tabStatusList,
			Boolean saveTabDataIgnoringNonFormTabTypes) {

		List<TabStatus> radioTabStatusList = new ArrayList<TabStatus>();

		if (saveTabDataIgnoringNonFormTabTypes) {

			tabStatusList.stream().forEach(tabStatus -> {

				if (!((null != tabStatus.getTabLabel()
						&& tabStatus.getTabLabel().toUpperCase().contains(AppConstants.TABLABEL_HREF))
						|| (tabStatus.getTabType() == TabTypeCode.INITIAL_HERE)
						|| (tabStatus.getTabType() == TabTypeCode.INITIAL_HERE_OPTIONAL)
						|| (tabStatus.getTabType() == TabTypeCode.SIGN_HERE)
						|| (tabStatus.getTabType() == TabTypeCode.SIGN_HERE_OPTIONAL))) {

					prepareTabDefinition(envelopeId, recipientStatus, dsTabDefinitionList, radioTabStatusList,
							tabStatus);

				}
			});

		} else {

			tabStatusList.stream().forEach(tabStatus -> {

				prepareTabDefinition(envelopeId, recipientStatus, dsTabDefinitionList, radioTabStatusList, tabStatus);

			});
		}

		if (null != radioTabStatusList && !radioTabStatusList.isEmpty()) {

			Collection<List<TabStatus>> groupByTabLabelCollection = radioTabStatusList.stream()
					.collect(Collectors.groupingBy(tabStatus -> tabStatus.getTabLabel())).values();

			groupByTabLabelCollection.stream().forEach(tabLabelGroup -> {

				TabStatus filteredTabStatus = tabLabelGroup.stream()
						.filter(tabStatus -> AppConstants.TABVALUE_X.equalsIgnoreCase(tabStatus.getTabValue()))
						.findAny().orElse(tabLabelGroup.get(0));

				DSTabDefinition dsTabDefinition = new DSTabDefinition();
				setSomeTabProperties(envelopeId, recipientStatus, dsTabDefinition, filteredTabStatus);

				if (!StringUtils.isEmpty(filteredTabStatus.getTabValue())) {

					dsTabDefinition.setTabValue(filteredTabStatus.getTabName());
				}

				dsTabDefinitionList.add(dsTabDefinition);
			});
		}
	}

	private void prepareTabDefinition(String envelopeId, RecipientStatus recipientStatus,
			List<DSTabDefinition> dsTabDefinitionList, List<TabStatus> radioTabStatusList, TabStatus tabStatus) {

		if (null != tabStatus.getCustomTabType() && CustomTabType.RADIO == tabStatus.getCustomTabType()) {

			radioTabStatusList.add(tabStatus);
		} else {

			DSTabDefinition dsTabDefinition = new DSTabDefinition();
			setSomeTabProperties(envelopeId, recipientStatus, dsTabDefinition, tabStatus);
			dsTabDefinition.setTabValue(tabStatus.getTabValue());

			if (null != tabStatus.getCustomTabType() && CustomTabType.CHECKBOX == tabStatus.getCustomTabType()) {

				if (AppConstants.TABVALUE_X.equalsIgnoreCase(tabStatus.getTabValue())) {

					dsTabDefinition.setTabValue(tabStatus.getTabName());
				}
			}

			dsTabDefinitionList.add(dsTabDefinition);
		}
	}

	private void setSomeTabProperties(String envelopeId, RecipientStatus recipientStatus,
			DSTabDefinition dsTabDefinition, TabStatus filteredTabStatus) {

		dsTabDefinition.setEnvelopeId(envelopeId);
		dsTabDefinition.setRecipientId(recipientStatus.getRecipientId());
		dsTabDefinition.setTabLabel(filteredTabStatus.getTabLabel());
		dsTabDefinition.setTabOriginalValue(filteredTabStatus.getOriginalValue());
		dsTabDefinition.setTabName(filteredTabStatus.getTabName());
		dsTabDefinition.setTabStatus(filteredTabStatus.getStatus());
	}
}