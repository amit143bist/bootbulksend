package com.ds.proserv.envelopedata.processor;

import static com.ds.proserv.common.lambda.LambdaExceptionWrappers.throwingConsumerWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.ds.proserv.envelopedata.domain.DSEnvelopeData;
import com.ds.proserv.envelopedata.domain.DSEnvelopeSavedData;
import com.ds.proserv.envelopedata.domain.TabSPRequest;
import com.ds.proserv.envelopedata.model.DSTab;
import com.ds.proserv.envelopedata.repository.DSTabRepository;
import com.ds.proserv.envelopedata.service.DSDataHelperService;
import com.ds.proserv.envelopedata.service.DSTabHelperService;
import com.ds.proserv.envelopedata.transformer.DSTabTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSTabDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DSTabProcessor extends AbstractDSProcessor {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DSTabHelperService dsTabHelperService;

	@Autowired
	private DSDataHelperService dsDataHelperService;

	@Autowired
	private DSTabTransformer dsTabTransformer;

	@Autowired
	private DSTabRepository dsTabRepository;

	@Override
	public long callSequence() {

		return 4;
	}

	@Override
	public DataProcessorType identifyProcessor() {

		return DataProcessorType.TAB;
	}

	@Override
	public boolean canProcessRequest(List<String> allowedProcessors) {

		return allowedProcessors.contains(DataProcessorType.TAB.toString().toUpperCase());
	}

	@Override
	public boolean isDataAvailableForProcessing(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeData.getTabToBeSavedList() && !dsEnvelopeData.getTabToBeSavedList().isEmpty()) {

			return true;
		} else {

			log.warn("No tabs identified in bulkUpdateSaveEnvelopeData for processId -> {}",
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

			try {
				log.info("Deduping of all tabs size -> {} for processId -> {} and envelopeIds -> {}",
						dsEnvelopeData.getTabToBeSavedList().size(), processId, dsEnvelopeData.getEnvelopeIdsToSave());
				Collection<DSTabDefinition> nonDuplicatedToBeSavedTabs = dsEnvelopeData.getTabToBeSavedList().stream()
						.<Map<String, DSTabDefinition>>collect(HashMap::new, (m, e) -> m.put(
								e.getEnvelopeId() + "_" + e.getRecipientId() + "_" + e.getTabLabel().toLowerCase(), e),
								Map::putAll)
						.values();

				log.info("All tabs size after deduping is {} for processId -> {} and envelopeIds -> {}",
						nonDuplicatedToBeSavedTabs.size(), processId, dsEnvelopeData.getEnvelopeIdsToSave());

				List<DSTab> savedDSTabList = null;
				if (isReadTabByEnvIdsSPEnabled()) {

					savedDSTabList = dsDataHelperService.getAllTabsByEnvelopeIdsAfterSentDateTime(processId,
							dsEnvelopeData.getEnvelopeIdsToSave(), dsEnvelopeData.getLeastSentDateTime());

				} else {

					savedDSTabList = dsDataHelperService.findAllTabsForAllEnvelopeIds(processId,
							dsEnvelopeData.getEnvelopeIdsToSave(), dsEnvelopeData.getLeastSentDateTime());
				}

				AtomicReference<List<DSTab>> savedDSTabListAtomicReference = new AtomicReference<List<DSTab>>();
				savedDSTabListAtomicReference.set(savedDSTabList);
				nonDuplicatedToBeSavedTabs
						.forEach(throwingConsumerWrapper(nonDupToBeSavedtab -> processDSTabData(dsEnvelopeData,
								nonDupToBeSavedtab, savedDSTabListAtomicReference.get())));

				log.info("Total Tabs to be saved/updated for processId -> {} and envelopeIds -> {} is {}", processId,
						dsEnvelopeData.getEnvelopeIdsToSave(), dsEnvelopeData.getPrepareToSaveDSTabList().size());
			} catch (Exception exp) {

				log.error("Some exception {} occurred in preparing tabdata for processId -> {} and envelopeIds -> {}",
						exp, dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());
				exp.printStackTrace();
				throw exp;
			}
			return asyncStatus;
		}, processorAsyncExecutor);
	}

	private boolean isReadTabByEnvIdsSPEnabled() {

		/*
		 * String enableTabEnvIdsBySP = dsCacheManager
		 * .prepareAndRequestCacheDataByKey(PropertyCacheConstants.
		 * DSTAB_SELECTBYENVIDS_STOREDPROC);
		 * 
		 * if (!StringUtils.isEmpty(enableTabEnvIdsBySP)) {
		 * 
		 * return Boolean.parseBoolean(enableTabEnvIdsBySP); }
		 */

		return true;
	}

	private void processDSTabData(DSEnvelopeData dsEnvelopeData, DSTabDefinition nonDupToBeSavedtab,
			List<DSTab> savedDSTabList) {

		String processId = dsEnvelopeData.getProcessId();
		DSTab filterSavedDSTab = savedDSTabList.stream().filter(tab -> {

			String nonDupTabKey = nonDupToBeSavedtab.getEnvelopeId() + "_" + nonDupToBeSavedtab.getRecipientId() + "_"
					+ nonDupToBeSavedtab.getTabLabel().toLowerCase();
			String savedTabKey = tab.getEnvelopeId() + "_" + tab.getRecipientId() + "_"
					+ tab.getTabLabel().toLowerCase();

			if (nonDupTabKey.equalsIgnoreCase(savedTabKey)) {

				return true;
			} else {

				return false;
			}
		}).findFirst().orElse(null);

		if (null != filterSavedDSTab && !StringUtils.isEmpty(filterSavedDSTab.getId())) {

			if (log.isDebugEnabled()) {

				log.debug("TabLabel -> {} for TabId -> {} will be updated for processId -> {} and envelopeId -> {}",
						nonDupToBeSavedtab.getTabLabel(), nonDupToBeSavedtab.getId(), processId,
						nonDupToBeSavedtab.getEnvelopeId());
			}

			if (isTabUpdated(filterSavedDSTab, nonDupToBeSavedtab)) {

				DSTab toUpdateDSTab = dsTabTransformer.transformToDSTabUpdate(nonDupToBeSavedtab, filterSavedDSTab);
				dsEnvelopeData.getPrepareToSaveDSTabList().add(toUpdateDSTab);
			}

		} else {

			if (log.isDebugEnabled()) {

				log.debug("TabLabel -> {} will be saved for processId -> {} and envelopeId -> {}",
						nonDupToBeSavedtab.getTabLabel(), processId, nonDupToBeSavedtab.getEnvelopeId());
			}

			DSTab toAddDSTab = dsTabTransformer.transformToDSTab(nonDupToBeSavedtab);
			dsEnvelopeData.getPrepareToSaveDSTabList().add(toAddDSTab);
		}
	}

	private boolean isTabUpdated(DSTab filterSavedDSTab, DSTabDefinition nonDupToBeSavedtab) {

		String nonDupTabKey = nonDupToBeSavedtab.getEnvelopeId() + "_" + nonDupToBeSavedtab.getRecipientId() + "_"
				+ nonDupToBeSavedtab.getTabStatus().toLowerCase() + "_" + nonDupToBeSavedtab.getTabLabel().toLowerCase()
				+ "_" + nonDupToBeSavedtab.getTabValue();

		String savedTabKey = filterSavedDSTab.getEnvelopeId() + "_" + filterSavedDSTab.getRecipientId() + "_"
				+ filterSavedDSTab.getTabStatus().toLowerCase() + "_" + filterSavedDSTab.getTabLabel().toLowerCase()
				+ "_" + filterSavedDSTab.getTabValue();

		if (nonDupTabKey.equalsIgnoreCase(savedTabKey)) {

			if (log.isDebugEnabled()) {

				log.debug("NOT doing DB updates as nonDupTabKey {} is same as savedTabKey -> {}", nonDupTabKey,
						savedTabKey);
			}

			return false;
		}

		if (log.isDebugEnabled()) {

			log.debug("Doing DB updates as nonDupTabKey {} is same as savedTabKey -> {}", nonDupTabKey, savedTabKey);
		}
		return true;
	}

	@Override
	public boolean isDataAvailableForSave(DSEnvelopeData dsEnvelopeData) {

		if (null != dsEnvelopeData.getPrepareToSaveDSTabList()
				&& !dsEnvelopeData.getPrepareToSaveDSTabList().isEmpty()) {

			return true;
		} else {

			log.error("No tabs identified in isDataAvailableForSave for saving in processId -> {}",
					dsEnvelopeData.getProcessId());
			return false;
		}
	}

	@Override
	public CompletableFuture<Void> callRepositorySaveOperations(DSEnvelopeData dsEnvelopeData) {

		log.info("SaveDSTabList triggered for processId -> {}", dsEnvelopeData.getProcessId());

		return CompletableFuture.runAsync(() -> {
			List<DSTab> dsTabList = dsEnvelopeData.getPrepareToSaveDSTabList();

			if (isSaveTabBySPEnabled()) {

				List<DSTab> insertDSTabList = dsTabList.stream().filter(dsTab -> dsTab.isNew())
						.collect(Collectors.toList());

				if (null != insertDSTabList && !insertDSTabList.isEmpty()) {

					log.info("Total insert size for processId -> {} is {}", dsEnvelopeData.getProcessId(),
							insertDSTabList.size());
					insertUpdate(dsEnvelopeData, insertDSTabList, true);

				}

				List<DSTab> updateDSTabList = dsTabList.stream().filter(dsTab -> !dsTab.isNew())
						.collect(Collectors.toList());

				if (null != updateDSTabList && !updateDSTabList.isEmpty()) {

					log.info("Total update size for processId -> {} is {}", dsEnvelopeData.getProcessId(),
							insertDSTabList.size());
					insertUpdate(dsEnvelopeData, updateDSTabList, false);
				}

			} else {

				if (null != dsTabList && !dsTabList.isEmpty()) {

					List<DSTab> updateDSTabList = dsTabList.stream().filter(dsTab -> !dsTab.isNew())
							.collect(Collectors.toList());

					if (null != updateDSTabList && !updateDSTabList.isEmpty()) {

						dsTabHelperService.updatedDSTabs(updateDSTabList, dsEnvelopeData.getProcessId());
					}

					List<DSTab> insertDSTabList = dsTabList.stream().filter(dsTab -> dsTab.isNew())
							.collect(Collectors.toList());

					if (null != insertDSTabList && !insertDSTabList.isEmpty()) {

						dsTabHelperService.insertDSTabs(insertDSTabList, dsEnvelopeData.getProcessId());
					}

				}
			}

		}, processorAsyncExecutor);

	}

	public void insertUpdate(DSEnvelopeData dsEnvelopeData, List<DSTab> dsTabList, boolean insert) {

		try {

			TabSPRequest tabSPRequest = new TabSPRequest();
			tabSPRequest.setDsTabs(dsTabList);

			String json = objectMapper.writeValueAsString(tabSPRequest);
			if (insert) {

				String result = dsTabRepository.insert(json);

				if (!AppConstants.SUCCESS_VALUE.equalsIgnoreCase(result)) {

					throw new ResourceNotSavedException("TabData not inserted with insertflag " + insert
							+ " envelopeId " + dsEnvelopeData.getEnvelopeIdsToSave());
				}
			} else {

				String result = dsTabRepository.update(json);

				if (!AppConstants.SUCCESS_VALUE.equalsIgnoreCase(result)) {

					throw new ResourceNotSavedException("TabData not updated with insertflag " + insert + " envelopeId "
							+ dsEnvelopeData.getEnvelopeIdsToSave());
				}
			}

		} catch (JsonProcessingException exp) {

			exp.printStackTrace();
			throw new ResourceNotSavedException("TabData not inserted/updated with insertflag " + insert
					+ " envelopeId " + dsEnvelopeData.getEnvelopeIdsToSave());
		} catch (Exception exp) {

			log.error("Some exception {} occurred in saving dstabdata for processId -> {} and envelopeIds -> {}",
					dsEnvelopeData.getProcessId(), dsEnvelopeData.getEnvelopeIdsToSave());
			exp.printStackTrace();
			throw exp;
		}

	}

	private boolean isSaveTabBySPEnabled() {

		/*
		 * String enableTabSaveBySP = dsCacheManager
		 * .prepareAndRequestCacheDataByKey(PropertyCacheConstants.DSTAB_SAVE_STOREDPROC
		 * );
		 * 
		 * if (!StringUtils.isEmpty(enableTabSaveBySP)) {
		 * 
		 * return Boolean.parseBoolean(enableTabSaveBySP); }
		 */

		return true;
	}

	@Override
	public void extractUniqueData(DSEnvelopeSavedData dsEnvelopeSavedData) {

		List<DSTab> dsTabs = dsEnvelopeSavedData.getDsTabs();
		if (null != dsTabs && !dsTabs.isEmpty()) {

			dsEnvelopeSavedData.setUniqueDSTabs(dsTabs.parallelStream().filter(value -> value != null)
					.collect(Collectors.collectingAndThen(
							Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DSTab::getId))),
							ArrayList::new)));
		}
	}

}