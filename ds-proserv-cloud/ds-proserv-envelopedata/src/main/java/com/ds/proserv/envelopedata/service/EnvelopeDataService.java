package com.ds.proserv.envelopedata.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ds.proserv.envelopedata.model.DSEnvelope;
import com.ds.proserv.envelopedata.model.DSRecipient;
import com.ds.proserv.envelopedata.model.DSTab;
import com.ds.proserv.envelopedata.repository.DSEnvelopeRepository;
import com.ds.proserv.envelopedata.repository.DSRecipientRepository;
import com.ds.proserv.envelopedata.repository.DSTabRepository;
import com.ds.proserv.envelopedata.transformer.DSEnvelopeTransformer;
import com.ds.proserv.envelopedata.transformer.DSRecipientTransformer;
import com.ds.proserv.envelopedata.transformer.DSTabTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSTabDefinition;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class EnvelopeDataService extends AbstractDataService {

	@Autowired
	private DSTabRepository dsTabRepository;

	@Autowired
	private DSEnvelopeRepository dsEnvelopeRepository;

	@Autowired
	private DSRecipientRepository dsRecipientRepository;

	@Autowired
	private DSTabTransformer dsTabTransformer;

	@Autowired
	private DSEnvelopeTransformer dsEnvelopeTransformer;

	@Autowired
	private DSRecipientTransformer dsRecipientTransformer;

	public DSEnvelope saveEnvelopeData(DSEnvelopeDefinition dsEnvelopeDefinition) {

		String envelopeId = dsEnvelopeDefinition.getEnvelopeId();
		log.info("Starting to save Connect Data for envelopeId -> {}", envelopeId);

		saveRecipientAndTabDetails(dsEnvelopeDefinition, envelopeId, false);
		return saveEnvelopeDetails(dsEnvelopeDefinition, envelopeId);
	}

	private void saveRecipientAndTabDetails(DSEnvelopeDefinition dsEnvelopeDefinition, String envelopeId,
			boolean isUpdate) {

		// setup ds_recipients
		List<DSRecipientDefinition> recipientList = dsEnvelopeDefinition.getDsRecipientDefinitions();
		ArrayList<DSRecipient> dsRecipientList = new ArrayList<DSRecipient>();

		recipientList.forEach(recipient -> {

			String recipientId = recipient.getRecipientId();
			saveTabDetails(envelopeId, recipient, recipientId, isUpdate);

			DSRecipient dsRecipientDB = dsRecipientRepository.findById(recipientId).orElse(null);

			if (null != dsRecipientDB && !StringUtils.isEmpty(dsRecipientDB.getRecipientId())) {

				DSRecipient exitingDsRecipient = dsRecipientTransformer.transformToDSRecipientUpdate(recipient,
						dsRecipientDB);
				dsRecipientList.add(exitingDsRecipient);
			} else {

				DSRecipient newDsRecipient = dsRecipientTransformer.transformToDSRecipient(recipient);
				dsRecipientList.add(newDsRecipient);
			}

		});

		dsRecipientRepository.saveAll(dsRecipientList);
		log.info("EnvelopeDataService.saveRecipientAndTabDetails(): recipients added for envelopeId -> {}", envelopeId);
	}

	private void saveTabDetails(String envelopeId, DSRecipientDefinition recipient, String recipientId,
			boolean isUpdate) {

		log.info("EnvelopeDataService.saveTabDetails called for recipientId -> {} and envelopeId -> {}", recipientId,
				envelopeId);
		// setup ds_tabs
		List<DSTabDefinition> tabList = recipient.getDsTabDefinitions();

		if (null != tabList && !tabList.isEmpty()) {

			ArrayList<DSTab> dsTabList = new ArrayList<DSTab>();

			Collection<DSTabDefinition> nonDuplicatedTabs = tabList.stream().<Map<String, DSTabDefinition>>collect(
					HashMap::new, (m, e) -> m.put(e.getTabLabel().toLowerCase(), e), Map::putAll).values();

			if (isUpdate) {

				Iterable<DSTab> savedDSTabIterator = dsTabRepository.findAllByEnvelopeId(envelopeId);

				List<DSTab> savedDSTabList = StreamSupport.stream(savedDSTabIterator.spliterator(), false)
						.collect(Collectors.toList());

				nonDuplicatedTabs.forEach(nonDuptab -> {

					DSTab filterSavedDSTab = savedDSTabList.stream().filter(tab -> {

						String nonDupTabKey = nonDuptab.getTabLabel().toLowerCase();
						String savedTabKey = tab.getTabLabel().toLowerCase();

						if (nonDupTabKey.equalsIgnoreCase(savedTabKey)) {

							return true;
						} else {

							return false;
						}
					}).findFirst().orElse(null);

					if (null != filterSavedDSTab && !StringUtils.isEmpty(filterSavedDSTab.getId())) {

						DSTab toUpdateDSTab = dsTabTransformer.transformToDSTabUpdate(nonDuptab, filterSavedDSTab);
						dsTabList.add(toUpdateDSTab);

					} else {

						DSTab toAddDSTab = dsTabTransformer.transformToDSTab(nonDuptab);
						dsTabList.add(toAddDSTab);
					}

				});

				log.info("Update call for tabList for recipientId -> {} and envelopeId -> {}", recipientId, envelopeId);

			} else {

				nonDuplicatedTabs.forEach(tab -> {

					DSTab newDsTab = dsTabTransformer.transformToDSTab(tab);
					dsTabList.add(newDsTab);

				});

			}

			log.info("Tab SaveAll called for recipient {} and envelopeId -> {}", recipientId, envelopeId);
			dsTabRepository.saveAll(dsTabList);

		} else {

			log.warn("no tabs for recipient {} and envelopeId -> {}", recipientId, envelopeId);
		}

		log.info("EnvelopeDataService.saveTabDetails(): tabs added for recipient {} and envelopeId -> {}", recipientId,
				envelopeId);
	}

	private DSEnvelope saveEnvelopeDetails(DSEnvelopeDefinition dsEnvelopeDefinition, String envelopeId) {

		DSEnvelope newDsEnvelope = dsEnvelopeTransformer.transformToDSEnvelope(dsEnvelopeDefinition);

		log.info("EnvelopeDataService.saveEnvelopeDetails(): envelope added for envelopeId -> {}", envelopeId);
		return dsEnvelopeRepository.save(newDsEnvelope);
	}

	public DSEnvelope updateEnvelopeData(DSEnvelopeDefinition dsEnvelopeDefinition) {

		String envelopeId = dsEnvelopeDefinition.getEnvelopeId();
		log.info("Starting to save Connect Data (updateEnvelopeData) for update envelopeId -> {}", envelopeId);

		saveRecipientAndTabDetails(dsEnvelopeDefinition, envelopeId, true);

		DSEnvelope dsEnvelope = dsEnvelopeRepository.findById(envelopeId).orElse(null);

		if (null != dsEnvelope && !StringUtils.isEmpty(dsEnvelope.getEnvelopeId())) {

			DSEnvelope updatedDSEnvelope = dsEnvelopeTransformer.transformToDSEnvelopeUpdate(dsEnvelopeDefinition,
					dsEnvelope);

			return dsEnvelopeRepository.save(updatedDSEnvelope);
		} else {

			DSEnvelope newDsEnvelope = dsEnvelopeTransformer.transformToDSEnvelope(dsEnvelopeDefinition);
			return dsEnvelopeRepository.save(newDsEnvelope);
		}

	}

	public DSEnvelopeDefinition findEnvelopeTreeByEnvelopeId(String envelopeId) {

		DSEnvelopeDefinition dsEnvelopeDefinition = null;
		Optional<DSEnvelope> dsEnvelopeOptional = dsEnvelopeRepository.findById(envelopeId);

		if (null != dsEnvelopeOptional && dsEnvelopeOptional.isPresent()) {

			Iterable<DSRecipient> dsRecipientIterable = dsRecipientRepository.findAllByEnvelopeId(envelopeId);

			List<DSRecipientDefinition> dsRecipientDefinitionList = new ArrayList<DSRecipientDefinition>();
			dsRecipientIterable.forEach(dsRecipient -> {

				DSRecipientDefinition dsRecipientDefinition = dsRecipientTransformer
						.transformToDSRecipientDefinition(dsRecipient);

				Iterable<DSTab> dsTabIterable = dsTabRepository.findAllByRecipientId(dsRecipient.getRecipientId());

				List<DSTabDefinition> dsTabDefinitionList = new ArrayList<DSTabDefinition>();
				dsTabIterable.forEach(dsTab -> {

					dsTabDefinitionList.add(dsTabTransformer.transformToDSTabDefinition(dsTab));
				});

				dsRecipientDefinition.setDsTabDefinitions(dsTabDefinitionList);
				dsRecipientDefinitionList.add(dsRecipientDefinition);
			});

			DSEnvelope dsEnvelope = dsEnvelopeOptional.get();
			dsEnvelopeDefinition = dsEnvelopeTransformer.transformToDSEnvelopeDefinition(dsEnvelope);

			dsEnvelopeDefinition.setDsRecipientDefinitions(dsRecipientDefinitionList);
		}

		return dsEnvelopeDefinition;
	}

}