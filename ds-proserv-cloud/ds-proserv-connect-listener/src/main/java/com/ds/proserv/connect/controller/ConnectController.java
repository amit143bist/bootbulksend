package com.ds.proserv.connect.controller;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.annotation.security.RolesAllowed;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.batch.common.service.BatchQueueService;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.exception.InvalidMessageException;
import com.ds.proserv.common.exception.NewVersionExistException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.connect.domain.DocuSignEnvelopeInformation;
import com.ds.proserv.connect.service.ConnectAsyncService;
import com.ds.proserv.connect.service.ConnectProcessorService;
import com.ds.proserv.feign.connect.domain.ConnectMessageDefinition;
import com.ds.proserv.feign.connect.service.ConnectService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Slf4j
public class ConnectController implements ConnectService {

	@Autowired
	private JAXBContext jaxbContext;

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private TaskExecutor recordTaskExecutor;

	@Autowired
	private BatchQueueService batchQueueService;

	@Autowired
	private ConnectAsyncService connectAsyncService;
	
	@Autowired
	private ConnectProcessorService connectProcessorService;

	@Override
	public ResponseEntity<String> postConnect(String connectXML) {

		boolean timeZoneAvailable = DSUtil.isTimezoneAvailable(connectXML);
		String envelopeId = DSUtil.getEnvelopeId(connectXML);

		log.info("postConnect startTime is {} for envelopeId -> {}", LocalDateTime.now(), envelopeId);

		if (!timeZoneAvailable || StringUtils.isEmpty(envelopeId)) {

			log.error(" ^^^^^^^^^^^^^^^^^^^^^^^^ Failure Response returned to DocuSign ^^^^^^^^^^^^^^^^^^^^^^^^ ");
			log.error("Either timeZone node not available or envelopeId node not available");

			log.info("postConnect endTime with issue is {}", LocalDateTime.now());
			return new ResponseEntity<String>("XML not acceptable", HttpStatus.BAD_REQUEST);
		} else {

			if (processRouteCheck(PropertyCacheConstants.CONNECT_PROCESS_NOW_SYNC)) {

				String processResult = processMessagePerRoute(envelopeId, connectXML);

				log.info("processResult sent for envelopeId -> {}", envelopeId);
				return new ResponseEntity<String>(processResult, HttpStatus.OK);
			} else {

				processConnectDataAsync(envelopeId, connectXML);
			}
		}

		log.info("Success Response returned from postConnect to DocuSign for envelopeId -> {} at {}", envelopeId,
				LocalDateTime.now());
		return new ResponseEntity<String>("received", HttpStatus.OK);
	}

	private Boolean processRouteCheck(String processRoute) {

		String processQueueStr = dsCacheManager.prepareAndRequestCacheDataByKeyAndReference(processRoute,
				PropertyCacheConstants.CONNECT_REFERENCE_NAME);

		if (StringUtils.isEmpty(processQueueStr)) {

			return false;
		} else {

			return Boolean.parseBoolean(processQueueStr);
		}
	}

	private CompletableFuture<String> processConnectDataAsync(String envelopeId, String connectXML) {

		return CompletableFuture.supplyAsync((Supplier<String>) () -> {

			return processMessagePerRoute(envelopeId, connectXML);

		}, recordTaskExecutor).handleAsync((result, exp) -> {

			if (null != exp) {

				logErrorMessages("Async processing got exception in handleAsync", envelopeId);

				exp.printStackTrace();
				connectProcessorService.saveConnectAsException(envelopeId, connectXML, FailureCode.ERROR_107.toString(),
						FailureCode.ERROR_107.getFailureCodeDescription() + "_" + exp.toString(),
						FailureStep.ASYNC_CONNECT_PROCESSING.toString(), exp);

			} else {

				if (AppConstants.SUCCESS_VALUE.equalsIgnoreCase(result)) {

					log.info(
							" $$$$$$$$$$$$$$$$$$$$$$$$$ Async processing completed for EnvelopeId -> {} $$$$$$$$$$$$$$$$$$$$$$$$$ ",
							envelopeId);
				} else if (AppConstants.MB_QUEUED_VALUE.equalsIgnoreCase(result)) {

					log.info(
							" $$$$$$$$$$$$$$$$$$$$$$$$$ Async processing queued in MB for EnvelopeId -> {} $$$$$$$$$$$$$$$$$$$$$$$$$ ",
							envelopeId);
				} else if (AppConstants.DB_QUEUED_VALUE.equalsIgnoreCase(result)) {

					log.info(
							" $$$$$$$$$$$$$$$$$$$$$$$$$ Async processing queued in DB for EnvelopeId -> {} $$$$$$$$$$$$$$$$$$$$$$$$$ ",
							envelopeId);
				} else {

					logErrorMessages("Result is NOT success, check logs for error", envelopeId);
				}

			}

			return result;
		}, recordTaskExecutor);
	}

	private String processMessagePerRoute(String envelopeId, String connectXML) {

		log.info("EnvelopeId -> {} will be queued to MB or DB", envelopeId);
		if (processRouteCheck(PropertyCacheConstants.CONNECT_QUEUE_TO_MB)) {

			return queueToMB(connectXML, envelopeId);

		} else if (processRouteCheck(PropertyCacheConstants.CONNECT_QUEUE_TO_DB)) {

			log.info("Write direct to dsexception for envelopeId -> {} for later processing", envelopeId);
			connectProcessorService.saveConnectAsException(envelopeId, connectXML, FailureCode.ERROR_213.toString(),
					FailureCode.ERROR_213.getFailureCodeDescription(), FailureStep.MESSAGE_QUEUED.toString(), null);

			log.info("EnvelopeId -> {} queued to DB", envelopeId);
			return AppConstants.DB_QUEUED_VALUE;
		}

		return unmarshallAndProcessConnectMessage(envelopeId, connectXML);
	}

	private String queueToMB(String connectXML, String envelopeId) {

		try {

			ConnectMessageDefinition connectMessageDefinition = new ConnectMessageDefinition();
			connectMessageDefinition.setConnectXML(connectXML);

			batchQueueService.findQueueNameAndSend(PropertyCacheConstants.PROCESS_CONNECT_QUEUE_NAME,
					connectMessageDefinition);

			log.info("EnvelopeId -> {} queued to MB", envelopeId);
			return AppConstants.MB_QUEUED_VALUE;
		} catch (Exception exp) {

			log.error("Some exception -> {} occurred while sending message via Queue", exp);
			log.info("Write direct to dsexception for envelopeId -> {}", envelopeId);
			connectProcessorService.saveConnectAsException(envelopeId, connectXML, FailureCode.ERROR_216.toString(),
					FailureCode.ERROR_216.getFailureCodeDescription(), FailureStep.MESSAGE_QUEUED.toString(), null);

			log.info("EnvelopeId -> {} queued to DB after sending to MB threw exception", envelopeId);
			return AppConstants.DB_QUEUED_AFTER_MB_ERROR_VALUE;
		}
	}

	private String unmarshallAndProcessConnectMessage(String envelopeId, String connectXML) {

		String result = AppConstants.SUCCESS_VALUE;
		try {

			StreamSource streamSource = new StreamSource(new StringReader(connectXML));

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			DocuSignEnvelopeInformation docuSignEnvelopeInformation = jaxbUnmarshaller
					.unmarshal(streamSource, DocuSignEnvelopeInformation.class).getValue();

			if (null != docuSignEnvelopeInformation && null != docuSignEnvelopeInformation.getEnvelopeStatus()) {

				log.info("Unmarhalled XML successfully for EnvelopeId -> {}", envelopeId);

				try {

					ConnectMessageDefinition connectMessageDefinition = new ConnectMessageDefinition();
					connectMessageDefinition.setConnectXML(connectXML);

					connectAsyncService.saveEnvelopeData(connectMessageDefinition);

				} catch (NewVersionExistException exp) {

					logErrorMessages(
							"NewVersionExistException in ConnectController.unmarshallAndProcessConnectMessage()",
							envelopeId);

					connectProcessorService.saveConnectAsException(envelopeId, connectXML,
							FailureCode.ERROR_215.toString(), FailureCode.ERROR_215.getFailureCodeDescription(),
							FailureStep.SAVING_CONNECT_DATA.toString(), exp);
				} catch (Throwable exp) {

					logErrorMessages("Unknown Exception in ConnectController.unmarshallAndProcessConnectMessage()",
							envelopeId);

					exp.printStackTrace();
					connectProcessorService.saveConnectAsException(envelopeId, connectXML,
							FailureCode.ERROR_201.toString(),
							FailureCode.ERROR_201.getFailureCodeDescription() + "_" + exp.toString(),
							FailureStep.SAVING_CONNECT_DATA.toString(), exp);

					result = AppConstants.FAILURE_VALUE;
				}

			} else {

				logErrorMessages("ConnectController.unmarshallAndProcessConnectMessage(): Invalid Connect Message",
						envelopeId);
				connectProcessorService.saveConnectAsException(envelopeId, connectXML,
						FailureCode.ERROR_203.getFailureCodeDescription(), "No EnvelopeStatus in the ConnectXML",
						FailureStep.UNMARSHALL_CONNECT_XML.toString(),
						new InvalidMessageException("No EnvelopeStatus in the ConnectXML"));

				result = AppConstants.FAILURE_VALUE;
			}

		} catch (JAXBException exp) {

			logErrorMessages("JAXBException", envelopeId);

			exp.printStackTrace();
			connectProcessorService.saveConnectAsException(envelopeId, connectXML,
					FailureCode.ERROR_202.getFailureCodeDescription(), FailureCode.ERROR_202.toString(),
					FailureStep.UNMARSHALL_CONNECT_XML.toString(), exp);

			result = AppConstants.FAILURE_VALUE;
		}
		return result;
	}

	private void logErrorMessages(String message, String envelopeId) {

		log.error(" ------------------------- ERROR: {} for EnvelopeId -> {} ------------------------- ", message,
				envelopeId);
	}

}