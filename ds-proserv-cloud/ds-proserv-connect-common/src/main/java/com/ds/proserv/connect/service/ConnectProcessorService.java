package com.ds.proserv.connect.service;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.RetryStatus;
import com.ds.proserv.common.exception.NewVersionExistException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.connect.client.DSEnvelopeClient;
import com.ds.proserv.connect.client.DSExceptionClient;
import com.ds.proserv.connect.domain.ConnectCacheData;
import com.ds.proserv.connect.domain.DocuSignEnvelopeInformation;
import com.ds.proserv.feign.connect.domain.ConnectMessageDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeInformation;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionDefinition;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConnectProcessorService {

	@Autowired
	private JAXBContext jaxbContext;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private DSEnvelopeClient dsEnvelopeClient;

	@Autowired
	private DSExceptionClient dsExceptionClient;

	@Autowired
	private ConnectEnvelopeService connectEnvelopeService;

	@Autowired
	private ConnectProcessorBulkService connectProcessorBulkService;

	@Autowired
	private ConnectDownstreamQueueService connectDownstreamQueueService;

	public void validateAndSaveEnvelopeData(String envelopeId, String connectXML, String processId, String batchId) {

		DocuSignEnvelopeInformation docuSignEnvelopeInformation = marshalXMLToDSEnvelopeInformation(envelopeId,
				connectXML);

		if (null != docuSignEnvelopeInformation) {

			DSEnvelopeDefinition readyToSaveDSEnvelopeDefinition = connectEnvelopeService
					.prepareEnvelopeDefinition(docuSignEnvelopeInformation);
			String savedResult = saveProcessedEnvelopeDefinition(processId, batchId, readyToSaveDSEnvelopeDefinition);

			if (null != savedResult && AppConstants.STALE_MESSAGE.equalsIgnoreCase(savedResult)) {

				log.info("Envelope already exist for envelopeId -> {} and is a stale Message", envelopeId);
				parkStaleMessage(envelopeId, connectXML, docuSignEnvelopeInformation);
			}

			if (null != savedResult && AppConstants.FAILURE_VALUE.equalsIgnoreCase(savedResult)) {

				throw new ResourceNotSavedException(envelopeId + " not saved");
			}
		} else {

			log.error("DocuSignEnvelopeInformation is null for envelopeId -> {} in validateAndSaveEnvelopeData",
					envelopeId);
		}
	}

	public void validateAndSaveEnvelopeDataV2(String envelopeId, String connectXML, String processId, String batchId) {

		DocuSignEnvelopeInformation docuSignEnvelopeInformation = marshalXMLToDSEnvelopeInformation(envelopeId,
				connectXML);

		if (null != docuSignEnvelopeInformation) {

			DSEnvelopeDefinition alreadySavedDSEnvelopeDefinition = dsEnvelopeClient
					.findEnvelopeByEnvelopeId(envelopeId).getBody();

			DSEnvelopeDefinition readyToSaveDSEnvelopeDefinition = null;
			if (null != alreadySavedDSEnvelopeDefinition
					&& !AppConstants.SUCCESS_VALUE.equalsIgnoreCase(alreadySavedDSEnvelopeDefinition.getResult())) {

				log.info(
						"Envelope does not exist for envelopeId -> {} in validateAndSaveEnvelopeData, sending for preparation",
						envelopeId);
				readyToSaveDSEnvelopeDefinition = connectEnvelopeService
						.prepareEnvelopeDefinition(docuSignEnvelopeInformation);

			} else {

				log.info("Envelope already exist in for envelopeId -> {} in validateAndSaveEnvelopeData", envelopeId);

				if (null != alreadySavedDSEnvelopeDefinition
						&& !StringUtils.isEmpty(alreadySavedDSEnvelopeDefinition.getEnvelopeId())
						&& connectProcessorBulkService.canProcessConnectMessage(docuSignEnvelopeInformation, envelopeId,
								alreadySavedDSEnvelopeDefinition)) {

					log.info(
							"Envelope already exist for envelopeId -> {} but can be processed for newer version in validateAndSaveEnvelopeData, sending for preparation",
							envelopeId);

					readyToSaveDSEnvelopeDefinition = connectEnvelopeService
							.prepareEnvelopeDefinition(docuSignEnvelopeInformation);

				} else {

					parkStaleMessage(envelopeId, connectXML, docuSignEnvelopeInformation);

				}

			}

			String savedResult = null;
			if (null != readyToSaveDSEnvelopeDefinition && AppConstants.SUCCESS_VALUE
					.equalsIgnoreCase(readyToSaveDSEnvelopeDefinition.getPrepareResult())) {

				savedResult = saveProcessedEnvelopeDefinition(processId, batchId, readyToSaveDSEnvelopeDefinition);
			} else {

				log.info("Nothing to save for envelopeId -> {} in validateAndSaveEnvelopeData", envelopeId);
			}

			if (null != savedResult && AppConstants.FAILURE_VALUE.equalsIgnoreCase(savedResult)) {

				throw new ResourceNotSavedException(envelopeId + " not saved");
			}

		} else {

			log.error("DocuSignEnvelopeInformation is null for envelopeId -> {} in validateAndSaveEnvelopeData",
					envelopeId);
		}

	}

	private void parkStaleMessage(String envelopeId, String connectXML,
			DocuSignEnvelopeInformation docuSignEnvelopeInformation) {
		LocalDateTime timeGeneratedInCurrentXML = getCurrentXMLTimeGenerated(docuSignEnvelopeInformation);
		log.warn("EnvelopeId -> {} data is latest in the DB and will not process current XML with timegenerated -> {}",
				envelopeId, timeGeneratedInCurrentXML);

		NewVersionExistException exp = new NewVersionExistException(envelopeId + " already moved to new version");
		saveConnectAsException(envelopeId, connectXML, FailureCode.ERROR_215.toString(),
				FailureCode.ERROR_215.getFailureCodeDescription(), FailureStep.QUEUE_SAVING_CONNECT_DATA.toString(),
				exp);
	}

	private DocuSignEnvelopeInformation marshalXMLToDSEnvelopeInformation(String envelopeId, String connectXML) {

		DocuSignEnvelopeInformation docuSignEnvelopeInformation = null;
		try {

			StreamSource streamSource = new StreamSource(new StringReader(connectXML));
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			docuSignEnvelopeInformation = jaxbUnmarshaller.unmarshal(streamSource, DocuSignEnvelopeInformation.class)
					.getValue();

		} catch (JAXBException exp) {

			log.error(" ------------------------- ERROR: {} for EnvelopeId -> {} ------------------------- ",
					"JAXBException", envelopeId);

			exp.printStackTrace();
			saveConnectAsException(envelopeId, connectXML, FailureCode.ERROR_202.toString(),
					FailureCode.ERROR_202.getFailureCodeDescription(), FailureStep.UNMARSHALL_CONNECT_XML.toString(),
					exp);

		}
		return docuSignEnvelopeInformation;
	}

	private String saveProcessedEnvelopeDefinition(String processId, String batchId,
			DSEnvelopeDefinition newDsEnvelopeDefinition) {

		log.info(
				"Envelope prepared successfully for envelopeId -> {} in processAndSaveMessage, now calling bulkUpdateSaveEnvelopeDataV2",
				newDsEnvelopeDefinition.getEnvelopeId());

		try {

			List<DSEnvelopeDefinition> dsEnvelopeDefinitionList = new ArrayList<DSEnvelopeDefinition>();
			dsEnvelopeDefinitionList.add(newDsEnvelopeDefinition);

			DSEnvelopeInformation dsEnvelopeInformation = new DSEnvelopeInformation();
			dsEnvelopeInformation.setDsEnvelopeDefinitions(dsEnvelopeDefinitionList);

			DSEnvelopeInformation savedDSEnvelopeDefinition = dsEnvelopeClient
					.bulkUpdateSaveEnvelopeDataV2(dsEnvelopeInformation).getBody();

			if (null != savedDSEnvelopeDefinition
					&& AppConstants.SUCCESS_VALUE.equalsIgnoreCase(savedDSEnvelopeDefinition.getResult())) {

				List<DSEnvelopeDefinition> toProcessDSEnvelopeDefinitionList = new ArrayList<DSEnvelopeDefinition>();
				toProcessDSEnvelopeDefinitionList.add(newDsEnvelopeDefinition);

				log.info(
						"Successfully saved envelopeId -> {}, now sending to downstreamQueues in saveProcessedEnvelopeDefinition",
						newDsEnvelopeDefinition.getEnvelopeId());
				connectDownstreamQueueService.sendToDownstreamQueues(toProcessDSEnvelopeDefinitionList, processId,
						batchId);
			}

			return savedDSEnvelopeDefinition.getResult();
			/*
			 * else if (null != savedDSEnvelopeDefinition &&
			 * AppConstants.STALE_MESSAGE.equalsIgnoreCase(savedDSEnvelopeDefinition.
			 * getResult())) {
			 * 
			 * log.warn(
			 * "savedDSEnvelopeDefinition status was STALE_MESSAGE for envelopeId -> {} in saveProcessedEnvelopeDefinition"
			 * , newDsEnvelopeDefinition.getEnvelopeId()); return
			 * AppConstants.STALE_MESSAGE; } else {
			 * 
			 * log.info(
			 * "savedDSEnvelopeDefinition status was NOT success for envelopeId -> {} in saveProcessedEnvelopeDefinition"
			 * , newDsEnvelopeDefinition.getEnvelopeId()); return
			 * AppConstants.FAILURE_VALUE; }
			 */
		} catch (Exception exp) {

			log.error("Some exception {} occurred in saving envelope details for envelopeId -> {}",
					newDsEnvelopeDefinition.getEnvelopeId());
			exp.printStackTrace();

			throw exp;
		}

	}

	private LocalDateTime getCurrentXMLTimeGenerated(DocuSignEnvelopeInformation docuSignEnvelopeInformation) {

		XMLGregorianCalendar utcTimeGenerated = docuSignEnvelopeInformation.getEnvelopeStatus().getTimeGenerated();
		utcTimeGenerated.setTimezone(docuSignEnvelopeInformation.getTimeZoneOffset());

		return utcTimeGenerated.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
	}

	public void saveConnectAsException(String envelopeId, String connectXML, String exceptionCode,
			String exceptionReason, String exceptionStep, Throwable exp) {

		if (null != exp) {

			log.error("exp -> {} thrown with reason {} for envelopeId -> {} with errorCode -> {} at errorStep -> {}",
					exp, exceptionReason, envelopeId, exceptionCode, exceptionStep);
		} else {
			log.error("{} for envelopeId -> {} with errorCode -> {} at errorStep -> {}", exceptionReason, envelopeId,
					exceptionCode, exceptionStep);
		}

		DSExceptionDefinition dsExceptionDefinition = new DSExceptionDefinition();

		if (FailureCode.ERROR_202.toString().equalsIgnoreCase(exceptionCode)) {

			dsExceptionDefinition.setRetryStatus(RetryStatus.M.toString());
		}

		if (FailureCode.ERROR_203.toString().equalsIgnoreCase(exceptionCode)) {

			dsExceptionDefinition.setRetryStatus(RetryStatus.J.toString());
		}

		if (FailureCode.ERROR_215.toString().equalsIgnoreCase(exceptionCode)) {

			dsExceptionDefinition.setRetryStatus(RetryStatus.I.toString());
		}

		dsExceptionDefinition.setEnvelopeId(envelopeId);
		dsExceptionDefinition.setEnvelopeXml(connectXML);
		dsExceptionDefinition.setExceptionCode(exceptionCode);
		dsExceptionDefinition.setExceptionDateTime(LocalDateTime.now().toString());
		dsExceptionDefinition.setExceptionReason(exceptionReason);
		dsExceptionDefinition.setExceptionStep(exceptionStep);

		log.warn("Saving Connect message in DSException table for envelopeId -> {} and reason -> {}", envelopeId,
				dsExceptionDefinition.getRetryStatus());
		dsExceptionClient.saveExceptionData(dsExceptionDefinition);
	}

	public void bulkSaveByIds(ConnectMessageDefinition connectMessageDefinition) {

		CompletableFuture.supplyAsync((Supplier<List<DSEnvelopeDefinition>>) () -> {

			List<String> recordIds = connectMessageDefinition.getRecordIds();

			List<DSExceptionDefinition> dsExceptionDefinitionList = connectProcessorBulkService
					.fetchAndPrepareDSExceptionDefinitionList(recordIds, connectMessageDefinition.getBatchId(),
							connectMessageDefinition.getPageNumber());

			ConnectCacheData connectCacheData = new ConnectCacheData(dsCacheManager);
			List<DSEnvelopeDefinition> processedEnvelopeDefinitionList = connectProcessorBulkService
					.processBulkSaveWithCounter(1, dsExceptionDefinitionList, connectMessageDefinition.getBatchId(),
							connectMessageDefinition.getProcessId(), connectCacheData);

			connectProcessorBulkService.bulkSaveExceptionAfterProcessing(1, dsExceptionDefinitionList,
					connectMessageDefinition.getBatchId(), connectMessageDefinition.getProcessId(), connectCacheData);

			return processedEnvelopeDefinitionList;
		}).handle((processedEnvelopeDefinitionList, exp) -> {

			if (null == exp) {

				connectDownstreamQueueService.sendToDownstreamQueues(processedEnvelopeDefinitionList,
						connectMessageDefinition.getProcessId(), connectMessageDefinition.getBatchId());

				return AppConstants.SUCCESS_VALUE;
			} else {
				exp.printStackTrace();

				log.error("Some exception -> {} occurred in bulkSaveByIds", exp);
				throw new ResourceNotSavedException(exp.getMessage());
			}
		});

	}

}