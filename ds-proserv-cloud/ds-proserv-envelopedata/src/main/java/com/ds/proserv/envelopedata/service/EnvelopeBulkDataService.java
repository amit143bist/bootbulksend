package com.ds.proserv.envelopedata.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.DataProcessorType;
import com.ds.proserv.common.exception.NewVersionExistException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.envelopedata.AsyncConfiguration;
import com.ds.proserv.envelopedata.domain.DSEnvelopeData;
import com.ds.proserv.envelopedata.factory.DSProcessorFactory;
import com.ds.proserv.envelopedata.processor.DSEnvelopeProcessor;
import com.ds.proserv.envelopedata.processor.IDSProcessor;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeInformation;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EnvelopeBulkDataService extends AbstractDataService {

	@Autowired
	private DSProcessorFactory dsProcessorFactory;

	@Autowired
	private TaskExecutor processorAsyncExecutor;

	@Autowired
	private DSEnvelopeProcessor dsEnvelopeProcessor;

	@Async(AsyncConfiguration.TASK_EXECUTOR_PROCESSOR)
	public CompletableFuture<String> bulkUpdateSaveEnvelopeData(DSEnvelopeInformation dsEnvelopeInformation) {

		try {
			DSEnvelopeData dsEnvelopeData = new DSEnvelopeData(dsEnvelopeInformation);

			boolean isSingleEnvelopeSave = false;
			if (null != dsEnvelopeData.getEnvelopeIdsToSave() && dsEnvelopeData.getEnvelopeIdsToSave().size() == 1) {

				dsEnvelopeProcessor.compareAndPrepareData(dsEnvelopeData).join();

				isSingleEnvelopeSave = true;

				if (null != dsEnvelopeData.getSingleEnvelopeSavedTimeGenerated()
						&& null != dsEnvelopeData.getEnvelopeToBeSavedList()
						&& !dsEnvelopeData.getEnvelopeToBeSavedList().isEmpty()) {

					LocalDateTime timeGeneratedFromSavedData = dsEnvelopeData.getSingleEnvelopeSavedTimeGenerated();
					LocalDateTime timeGeneratedFromRequest = LocalDateTime
							.parse(dsEnvelopeData.getEnvelopeToBeSavedList().get(0).getTimeGenerated());

					if (log.isDebugEnabled()) {

						log.debug(
								"timeGeneratedFromRequest -> {} and timeGeneratedFromSavedData -> {} for envelopeId -> {}",
								timeGeneratedFromRequest, timeGeneratedFromSavedData,
								dsEnvelopeData.getEnvelopeIdsToSave());
					}

					if (timeGeneratedFromSavedData.isBefore(timeGeneratedFromRequest)) {

						log.info("Continue saving of envelope related data for envelopeId -> {}",
								dsEnvelopeData.getEnvelopeIdsToSave());
					} else {

						log.warn("{} already moved to new version", dsEnvelopeData.getEnvelopeIdsToSave());
						throw new NewVersionExistException(
								dsEnvelopeData.getEnvelopeIdsToSave() + " already moved to new version");
					}
				} else {

					if (log.isDebugEnabled()) {

						log.debug(
								"getSingleEnvelopeSavedTimeGenerated {} and/or getEnvelopeToBeSavedList {} is empty for envelopeId -> {}",
								dsEnvelopeData.getSingleEnvelopeSavedTimeGenerated(),
								dsEnvelopeData.getEnvelopeToBeSavedList(), dsEnvelopeData.getEnvelopeIdsToSave());
					}
				}

			}

			List<IDSProcessor> dsProcessors = dsProcessorFactory.findAllowedProcessors(findAllowedProcessorTypes());
			dsProcessors.sort(Comparator.comparing(IDSProcessor::callSequence));

			log.info("Total processors to used in processId -> {} is {} and envelopeIds -> {}",
					dsEnvelopeData.getProcessId(), dsProcessors.size(), dsEnvelopeData.getEnvelopeIdsToSave());

			List<CompletableFuture<String>> processCompletableFutures = Collections
					.synchronizedList(new ArrayList<CompletableFuture<String>>());
			List<CompletableFuture<Void>> saveCompletableFutures = Collections
					.synchronizedList(new ArrayList<CompletableFuture<Void>>());

			dsProcessors.forEach(dsProcessor -> {

				prepareAndSaveForEachProcessor(dsEnvelopeData, processCompletableFutures, saveCompletableFutures,
						dsProcessor);

			});

			AtomicBoolean exceptionOccurred = new AtomicBoolean();
			List<String> resultProcessList = waitForAllPrepareFutures(dsEnvelopeData, exceptionOccurred,
					processCompletableFutures);

			waitForSaveFutures(dsEnvelopeData, exceptionOccurred, saveCompletableFutures);

			log.info("All wait completed in processId -> {} and envelopeIds -> {}", dsEnvelopeData.getProcessId(),
					dsEnvelopeData.getEnvelopeIdsToSave());

			for (String result : resultProcessList) {

				if ((!StringUtils.isEmpty(result) && !AppConstants.SUCCESS_VALUE.equalsIgnoreCase(result))) {

					log.error(
							"Envelope and related Data not saved or updated properly in processId -> {} and envelopeIds -> {}",
							dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());
					throw new ResourceNotSavedException(
							"Envelope and related Data not saved or updated properly for envelopeIds "
									+ dsEnvelopeData.getEnvelopeIdsToSave());
				}
			}

			if (exceptionOccurred.get()) {

				log.error(
						"Envelope and related Data not saved or updated properly for processId -> {} and envelopeIds -> {}",
						dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

				throw new ResourceNotSavedException("Envelope and related Data not saved or updated properly");
			} else {

				if (isSingleEnvelopeSave) {

					prepareAndSaveEnvelopeDataForOneEnvelope(dsEnvelopeData);
				} else {

					prepareAndSaveEnvelopeData(dsEnvelopeData);
				}
			}

			return CompletableFuture.supplyAsync((Supplier<String>) () -> {

				return AppConstants.SUCCESS_VALUE;

			}, processorAsyncExecutor);
		} catch (NewVersionExistException exp) {

			throw exp;
		} catch (Throwable exp) {

			exp.printStackTrace();

			throw exp;
		}

	}

	private void prepareAndSaveForEachProcessor(DSEnvelopeData dsEnvelopeData,
			List<CompletableFuture<String>> processCompletableFutures,
			List<CompletableFuture<Void>> saveCompletableFutures, IDSProcessor dsProcessor) {

		log.info("Calling {} processor for preparation in processId -> {} and envelopeIds -> {}",
				dsProcessor.identifyProcessor(), dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

		if (dsProcessor.isDataAvailableForProcessing(dsEnvelopeData)) {

			CompletableFuture<String> prepareFuture = dsProcessor.compareAndPrepareData(dsEnvelopeData);
			processCompletableFutures.add(prepareFuture);

			prepareFuture.thenRunAsync(() -> {

				if (dsProcessor.isDataAvailableForSave(dsEnvelopeData)) {

					log.info("Calling save for processor -> {} in processId -> {} and envelopeIds -> {}",
							dsProcessor.identifyProcessor(), dsEnvelopeData.getProcessId(),
							dsEnvelopeData.getEnvelopeIdsToSave());
					saveCompletableFutures.add(dsProcessor.callRepositorySaveOperations(dsEnvelopeData));
				}
			}, processorAsyncExecutor);
		}
	}

	private void prepareAndSaveEnvelopeDataForOneEnvelope(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeProcessor && dsEnvelopeProcessor.isDataAvailableForSave(dsEnvelopeData)) {

			log.info("Calling save for processor -> {} in processId -> {} and envelopeIds -> {}",
					DataProcessorType.ENVELOPE, dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

			try {

				dsEnvelopeProcessor.callRepositorySaveOperations(dsEnvelopeData).join();
			} catch (Exception exp) {

				log.error(
						"Some exception {} occurred in EnvelopeProcessor.prepareAndSaveEnvelopeDataForOneEnvelope for processId -> {} and envelopeIds -> {}",
						dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());
				exp.printStackTrace();
				throw exp;
			}
		} else {

			log.warn(
					"No EnvelopeData available in EnvelopeProcessor.prepareAndSaveEnvelopeDataForOneEnvelope to save for processId -> {} and envelopeIds -> {}",
					dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());
		}

	}

	private void prepareAndSaveEnvelopeData(DSEnvelopeData dsEnvelopeData) {

		log.error(
				"All other types saved properly, now preparing and saving envelopedata for processId -> {} and envelopeId -> {}",
				dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

		List<CompletableFuture<String>> processCompletableFutures = Collections
				.synchronizedList(new ArrayList<CompletableFuture<String>>());
		List<CompletableFuture<Void>> saveCompletableFutures = Collections
				.synchronizedList(new ArrayList<CompletableFuture<Void>>());

		if (null != dsEnvelopeProcessor && dsEnvelopeProcessor.isDataAvailableForProcessing(dsEnvelopeData)) {

			CompletableFuture<String> envelopeFuture = dsEnvelopeProcessor.compareAndPrepareData(dsEnvelopeData);
			processCompletableFutures.add(envelopeFuture);

			envelopeFuture.thenRun(() -> {

				if (null != dsEnvelopeProcessor && dsEnvelopeProcessor.isDataAvailableForSave(dsEnvelopeData)) {

					log.info("Calling save for processor -> {} in processId -> {} and envelopeIds -> {}",
							DataProcessorType.ENVELOPE, dsEnvelopeData.getProcessId(),
							dsEnvelopeData.getEnvelopeIdsToSave());

					try {

						saveCompletableFutures.add(dsEnvelopeProcessor.callRepositorySaveOperations(dsEnvelopeData));
					} catch (Exception exp) {

						log.error(
								"Some exception {} occurred in EnvelopeProcessor for processId -> {} and envelopeIds -> {}",
								dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());
						exp.printStackTrace();
						throw exp;
					}
				}
			});

		} else {

			log.warn(
					"No EnvelopeData available in EnvelopeProcessor.prepareAndSaveEnvelopeData to save for processId -> {} and envelopeIds -> {}",
					dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());
		}

		AtomicBoolean exceptionOccurred = new AtomicBoolean();
		List<String> resultProcessList = waitForAllPrepareFutures(dsEnvelopeData, exceptionOccurred,
				processCompletableFutures);
		waitForSaveFutures(dsEnvelopeData, exceptionOccurred, saveCompletableFutures);

		for (String result : resultProcessList) {

			if ((!StringUtils.isEmpty(result) && !AppConstants.SUCCESS_VALUE.equalsIgnoreCase(result))) {

				log.error("EnvelopeData not saved or updated properly in processId -> {} and envelopeIds -> {}",
						dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());
				throw new ResourceNotSavedException("EnvelopeData not saved or updated properly for envelopeIds "
						+ dsEnvelopeData.getEnvelopeIdsToSave());
			}
		}

		if (exceptionOccurred.get()) {

			log.error("EnvelopeData not saved or updated properly for processId -> {} and envelopeIds -> {}",
					dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

			throw new ResourceNotSavedException("EnvelopeData not saved or updated properly");
		}
	}

	private void waitForSaveFutures(DSEnvelopeData dsEnvelopeData, AtomicBoolean exceptionOccurred,
			List<CompletableFuture<Void>> saveCompletableFutures) {

		CompletableFuture.allOf(saveCompletableFutures.toArray(new CompletableFuture[0]))
				.thenApply((v) -> saveCompletableFutures.stream().map((cf) -> cf.join())).handle((result, exp) -> {

					if (null != exp) {

						exceptionOccurred.set(true);
						log.error(
								"Inside waitForSaveFutures, Exception -> {} occurred while saving for processId -> {} and envelopeIds -> {}",
								exp, dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

						exp.printStackTrace();
					}
					return result;
				}).join();
	}

	private List<String> waitForAllPrepareFutures(DSEnvelopeData dsEnvelopeData, AtomicBoolean exceptionOccurred,
			List<CompletableFuture<String>> processCompletableFutures) {

		List<String> resultProcessList = Collections.synchronizedList(new ArrayList<String>());

		CompletableFuture.allOf(processCompletableFutures.toArray(new CompletableFuture[0])).thenApply(v -> {

			processCompletableFutures.forEach(processCompletableFuture -> {

				String result = processCompletableFuture.join();

				if (log.isDebugEnabled()) {

					log.debug(
							"Result inside waitForAllPrepareFutures.forloop is {} for processId -> {} and envelopeIds -> {}",
							result, dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());
				}
				resultProcessList.add(result);
			});

			return resultProcessList;
		}).join();

		return resultProcessList;
	}

}