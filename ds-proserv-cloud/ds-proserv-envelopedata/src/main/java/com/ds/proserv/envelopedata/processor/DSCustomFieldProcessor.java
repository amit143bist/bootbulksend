package com.ds.proserv.envelopedata.processor;

import static com.ds.proserv.common.lambda.LambdaExceptionWrappers.throwingConsumerWrapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.DataProcessorType;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.envelopedata.domain.CustomFieldSPRequest;
import com.ds.proserv.envelopedata.domain.DSEnvelopeData;
import com.ds.proserv.envelopedata.domain.DSEnvelopeSavedData;
import com.ds.proserv.envelopedata.model.DSCustomField;
import com.ds.proserv.envelopedata.repository.DSCustomFieldPagingAndSortingRepository;
import com.ds.proserv.envelopedata.service.DSDataHelperService;
import com.ds.proserv.envelopedata.transformer.DSCustomFieldTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DSCustomFieldProcessor extends AbstractDSProcessor {

	@Autowired
	private ObjectMapper objectMapper;

	/*
	 * @Autowired private DSCacheManager dsCacheManager;
	 */

	@Autowired
	private DSDataHelperService dsDataHelperService;

	@Autowired
	private DSCustomFieldTransformer dsCustomFieldTransformer;

	@Autowired
	private DSCustomFieldPagingAndSortingRepository dsCustomFieldPagingAndSortingRepository;

	@Override
	public long callSequence() {

		return 1;
	}

	@Override
	public DataProcessorType identifyProcessor() {

		return DataProcessorType.CUSTOMFIELD;
	}

	@Override
	public boolean canProcessRequest(List<String> allowedProcessors) {

		return allowedProcessors.contains(DataProcessorType.CUSTOMFIELD.toString().toUpperCase());
	}

	@Override
	public boolean isDataAvailableForProcessing(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeData.getCustomFieldToBeSavedList()
				&& !dsEnvelopeData.getCustomFieldToBeSavedList().isEmpty()) {

			return true;
		} else {

			log.warn("No customFields identified in bulkUpdateSaveEnvelopeData for processId -> {}",
					dsEnvelopeData.getProcessId());
			return false;
		}
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public CompletableFuture<String> compareAndPrepareData(DSEnvelopeData dsEnvelopeData) {

		return CompletableFuture.supplyAsync((Supplier<String>) () -> {

			String asyncStatus = AppConstants.SUCCESS_VALUE;
			String processId = dsEnvelopeData.getProcessId();

			log.info("Preparing all to be saved/updated customFields for processId -> {} and envelopeIds -> {}",
					processId, dsEnvelopeData.getEnvelopeIdsToSave());

			try {

				List<DSCustomField> savedDSCustomFieldList = null;
				if (isReadCustomFieldsByEnvIdsSPEnabled()) {

					savedDSCustomFieldList = dsDataHelperService
							.findAllDSCustomFieldsForAllEnvelopeIdsAfterSentDateTime(processId,
									dsEnvelopeData.getEnvelopeIdsToSave(), dsEnvelopeData.getLeastSentDateTime());
				} else {

					savedDSCustomFieldList = dsDataHelperService.findAllDSCustomFieldsByEnvelopeIds(processId,
							dsEnvelopeData.getEnvelopeIdsToSave(), dsEnvelopeData.getLeastSentDateTime());
				}

				AtomicReference<List<DSCustomField>> savedDSCustomFieldListAtomicReference = new AtomicReference<List<DSCustomField>>();
				savedDSCustomFieldListAtomicReference.set(savedDSCustomFieldList);

				dsEnvelopeData.getCustomFieldToBeSavedList().forEach(
						throwingConsumerWrapper(toBeSavedCustomField -> processDSCustomFieldData(dsEnvelopeData,
								toBeSavedCustomField, savedDSCustomFieldListAtomicReference.get())));

				log.info("Total DSCustomField to be saved/updated for processId -> {} is {} for envelopeIds -> {}",
						processId, dsEnvelopeData.getPrepareToSaveDSCustomFieldList().size(),
						dsEnvelopeData.getEnvelopeIdsToSave());
			} catch (Throwable exp) {

				log.error("Exception occurred in fetching customFields for processId -> {} and envelopeIds -> {}",
						processId, dsEnvelopeData.getEnvelopeIdsToSave());

				exp.printStackTrace();
				throw exp;
			}
			return asyncStatus;
		}, processorAsyncExecutor);
	}

	private boolean isReadCustomFieldsByEnvIdsSPEnabled() {

		/*
		 * String enableCustomFieldEnvIdsBySP = dsCacheManager
		 * .prepareAndRequestCacheDataByKey(PropertyCacheConstants.
		 * ENVDATA_CUSTFIELDS_SELECTBYENVIDS_STOREDPROC);
		 * 
		 * if (!StringUtils.isEmpty(enableCustomFieldEnvIdsBySP)) {
		 * 
		 * return Boolean.parseBoolean(enableCustomFieldEnvIdsBySP); }
		 */

		return true;
	}

	private void processDSCustomFieldData(DSEnvelopeData dsEnvelopeData, DSCustomFieldDefinition toBeSavedCustomField,
			List<DSCustomField> savedDSCustomFieldList) {

		String processId = dsEnvelopeData.getProcessId();
		DSCustomField filterSavedDSCustomField = savedDSCustomFieldList.stream().filter(savedDSCustomField -> {

			if (null != savedDSCustomField && null != toBeSavedCustomField
					&& savedDSCustomField.getEnvelopeId().equalsIgnoreCase(toBeSavedCustomField.getEnvelopeId())
					&& savedDSCustomField.getFieldName().equalsIgnoreCase(toBeSavedCustomField.getFieldName())
					&& savedDSCustomField.getFieldType().equals(toBeSavedCustomField.getFieldType())) {

				return true;
			} else {

				return false;
			}
		}).findFirst().orElse(null);

		if (null != filterSavedDSCustomField && !StringUtils.isEmpty(filterSavedDSCustomField.getId())) {

			if (log.isDebugEnabled()) {

				log.debug(
						"DSCustomField with fieldName -> {} and fieldType -> {} for EnvelopeId -> {} will be updated for processId -> {}",
						toBeSavedCustomField.getFieldName(), toBeSavedCustomField.getFieldType(),
						toBeSavedCustomField.getEnvelopeId(), processId);
			}

			if (isCustomFieldUpdated(filterSavedDSCustomField, toBeSavedCustomField)) {

				dsEnvelopeData.getPrepareToSaveDSCustomFieldList().add(dsCustomFieldTransformer
						.transformToDSCustomField(toBeSavedCustomField, filterSavedDSCustomField));
			}
		} else {

			if (log.isDebugEnabled()) {

				log.debug(
						"DSCustomField with fieldName -> {} and fieldType -> {}  for EnvelopeId -> {} will be saved for processId -> {}",
						toBeSavedCustomField.getFieldName(), toBeSavedCustomField.getFieldType(),
						toBeSavedCustomField.getEnvelopeId(), processId);
			}

			dsEnvelopeData.getPrepareToSaveDSCustomFieldList()
					.add(dsCustomFieldTransformer.transformToDSCustomField(toBeSavedCustomField));
		}
	}

	private boolean isCustomFieldUpdated(DSCustomField filterSavedDSCustomField,
			DSCustomFieldDefinition toBeSavedCustomField) {

		if (null != filterSavedDSCustomField && null != toBeSavedCustomField
				&& filterSavedDSCustomField.getEnvelopeId().equalsIgnoreCase(toBeSavedCustomField.getEnvelopeId())
				&& filterSavedDSCustomField.getFieldType().equals(toBeSavedCustomField.getFieldType())
				&& filterSavedDSCustomField.getFieldName().equalsIgnoreCase(toBeSavedCustomField.getFieldName())
				&& filterSavedDSCustomField.getFieldValue().equals(toBeSavedCustomField.getFieldValue())) {

			if (log.isDebugEnabled()) {

				log.debug("NOT doing DB updates for DSCustomField -> {} and envelopeId -> {}",
						filterSavedDSCustomField.getFieldName(), filterSavedDSCustomField.getEnvelopeId());
			}

			return false;
		}

		if (log.isDebugEnabled() && null != filterSavedDSCustomField) {

			log.debug("Doing DB updates for DSCustomField -> {} and envelopeId -> {}",
					filterSavedDSCustomField.getFieldName(), filterSavedDSCustomField.getEnvelopeId());
		}
		return true;

	}

	@Override
	public boolean isDataAvailableForSave(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeData.getPrepareToSaveDSCustomFieldList()
				&& !dsEnvelopeData.getPrepareToSaveDSCustomFieldList().isEmpty()) {

			return true;
		} else {

			log.error("No customFields identified in isDataAvailableForSave for saving in processId -> {}",
					dsEnvelopeData.getProcessId());

			return false;
		}
	}

	@Override
	public CompletableFuture<Void> callRepositorySaveOperations(DSEnvelopeData dsEnvelopeData) {

		log.info("SaveDSCustomFieldList triggered for processId -> {} and envelopeIds -> {}",
				dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

		if (isSaveDSCustomFieldBySPEnabled()) {

			return CompletableFuture.runAsync(() -> {

				try {

					CustomFieldSPRequest customFieldSPRequest = new CustomFieldSPRequest();
					customFieldSPRequest.setDsCustomFields(dsEnvelopeData.getPrepareToSaveDSCustomFieldList());

					try {

						String result = dsCustomFieldPagingAndSortingRepository
								.insertUpdate(objectMapper.writeValueAsString(customFieldSPRequest));

						if (!AppConstants.SUCCESS_VALUE.equalsIgnoreCase(result)) {

							log.error("CustomField data not inserted/updated in processId -> {} and envelopeIds -> {}",
									dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

							throw new ResourceNotSavedException("CustomField data not inserted/updated in processId -> "
									+ dsEnvelopeData.getProcessId());
						}

					} catch (JsonProcessingException e) {

						e.printStackTrace();

						log.error(
								"Exception {} happened in calling sproc_dscustomfield_insert_update in processId -> {} and envelopeIds -> {}",
								e, dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());

						throw new ResourceNotSavedException("Exception " + e, e);
					}
				} catch (Exception exp) {

					log.error("Exception {} occurred in saving customFields for processId -> {} and envelopeIds -> {}",
							exp, dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());
					exp.printStackTrace();
					throw exp;
				}
			}, processorAsyncExecutor);

		} else {

			return saveData(dsEnvelopeData);
		}

	}

	private CompletableFuture<Void> saveData(DSEnvelopeData dsEnvelopeData) {

		return CompletableFuture.runAsync(() -> {

			dsCustomFieldPagingAndSortingRepository.saveAll(dsEnvelopeData.getPrepareToSaveDSCustomFieldList());
		}, processorAsyncExecutor);
	}

	private boolean isSaveDSCustomFieldBySPEnabled() {

		/*
		 * String enableCustomFieldSaveBySP = dsCacheManager
		 * .prepareAndRequestCacheDataByKey(PropertyCacheConstants.
		 * ENVDATA_DSCUSTOMFIELD_SAVE_STOREDPROC);
		 * 
		 * if (!StringUtils.isEmpty(enableCustomFieldSaveBySP)) {
		 * 
		 * return Boolean.parseBoolean(enableCustomFieldSaveBySP); }
		 */

		return true;
	}

	@Override
	public void extractUniqueData(DSEnvelopeSavedData dsEnvelopeSavedData) {

		List<DSCustomField> dsCustomFields = dsEnvelopeSavedData.getDsCustomFields();
		if (null != dsCustomFields && !dsCustomFields.isEmpty()) {

			List<DSCustomField> uniqueDSCustomFields = dsCustomFields.stream().filter(value -> value != null)
					.collect(Collectors.collectingAndThen(
							Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DSCustomField::getId))),
							ArrayList::new));
			dsEnvelopeSavedData.setUniqueDSCustomFields(uniqueDSCustomFields);
		}
	}

}