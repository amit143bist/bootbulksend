package com.ds.proserv.envelopedata.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ds.proserv.envelopedata.domain.DSEnvelopeSavedData;
import com.ds.proserv.envelopedata.factory.DSProcessorFactory;
import com.ds.proserv.envelopedata.model.DSCustomField;
import com.ds.proserv.envelopedata.model.DSRecipient;
import com.ds.proserv.envelopedata.model.DSRecipientAuth;
import com.ds.proserv.envelopedata.model.DSTab;
import com.ds.proserv.envelopedata.processor.IDSProcessor;
import com.ds.proserv.envelopedata.projection.DSEnvelopeProjection;
import com.ds.proserv.envelopedata.transformer.DSCustomFieldTransformer;
import com.ds.proserv.envelopedata.transformer.DSEnvelopeTransformer;
import com.ds.proserv.envelopedata.transformer.DSRecipientAuthTransformer;
import com.ds.proserv.envelopedata.transformer.DSRecipientTransformer;
import com.ds.proserv.envelopedata.transformer.DSTabTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeInformation;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientAuthDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSTabDefinition;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PrepareEnvelopeTreeService extends AbstractDataService {

	@Autowired
	private DSTabTransformer dsTabTransformer;

	@Autowired
	private DSRecipientTransformer dsRecipientTransformer;

	@Autowired
	private DSEnvelopeTransformer dsEnvelopeTransformer;

	@Autowired
	private DSCustomFieldTransformer dsCustomFieldTransformer;

	@Autowired
	private DSRecipientAuthTransformer dsRecipientAuthTransformer;

	@Autowired
	private DSProcessorFactory dsProcessorFactory;

	public DSEnvelopeInformation convertToDSEnvelopeInformation(List<DSEnvelopeProjection> dsEnvelopeProjectionList,
			int envelopesSize) {

		DSEnvelopeInformation dsEnvelopeInformation = new DSEnvelopeInformation();

		List<DSEnvelopeDefinition> dsEnvelopeDefinitions = new ArrayList<DSEnvelopeDefinition>(envelopesSize);

		Collection<List<DSEnvelopeProjection>> groupByEnvelopeIdColl = dsEnvelopeProjectionList.stream()
				.collect(Collectors
						.groupingBy(dsEnvelopeProjection -> dsEnvelopeProjection.getEnvelope().getEnvelopeId()))
				.values();

		List<IDSProcessor> dsProcessors = dsProcessorFactory.findAllowedProcessors(findAllowedProcessorTypes());

		groupByEnvelopeIdColl.forEach(groupByEnvelopeId -> {

			DSEnvelopeDefinition dsEnvelopeDefinition = dsEnvelopeTransformer
					.transformToDSEnvelopeDefinition(groupByEnvelopeId.get(0).getEnvelope());

			String envelopeId = groupByEnvelopeId.get(0).getEnvelope().getEnvelopeId();
			log.debug("Preparing tree for envelopeId -> {}", envelopeId);

			DSEnvelopeSavedData dsEnvelopeSavedData = new DSEnvelopeSavedData(groupByEnvelopeId);
			dsProcessors.forEach(dsProcessor -> {

				log.debug("Calling {} processor for envelopeId -> {}", dsProcessor.identifyProcessor(), envelopeId);

				dsProcessor.extractUniqueData(dsEnvelopeSavedData);

			});

			prepareAndSetDSCustomFieldDefinitions(dsEnvelopeDefinition, dsEnvelopeSavedData);

			prepareAndSetDSRecipientDefinitions(dsEnvelopeDefinition, dsEnvelopeSavedData);

			dsEnvelopeDefinitions.add(dsEnvelopeDefinition);

			log.debug("Tree completed for envelopeId -> {}", envelopeId);
		});

		dsEnvelopeInformation.setDsEnvelopeDefinitions(dsEnvelopeDefinitions);
		dsEnvelopeInformation.setTotalRecords(Long.valueOf(dsEnvelopeDefinitions.size()));

		return dsEnvelopeInformation;
	}

	private void prepareAndSetDSRecipientDefinitions(DSEnvelopeDefinition dsEnvelopeDefinition,
			DSEnvelopeSavedData dsEnvelopeSavedData) {

		List<DSRecipient> uniqueDSRecipients = dsEnvelopeSavedData.getUniqueDSRecipients();

		if (null != uniqueDSRecipients && !uniqueDSRecipients.isEmpty()) {

			List<DSRecipientDefinition> dsRecipientDefinitions = new ArrayList<DSRecipientDefinition>(
					uniqueDSRecipients.size());

			for (DSRecipient recipient : uniqueDSRecipients) {

				DSRecipientDefinition dsRecipientDefinition = dsRecipientTransformer
						.transformToDSRecipientDefinition(recipient);

				prepareAndSetRecipientAuth(dsRecipientDefinition, dsEnvelopeSavedData);
				prepareAndSetTab(dsRecipientDefinition, dsEnvelopeSavedData);

				dsRecipientDefinitions.add(dsRecipientDefinition);
			}

			dsEnvelopeDefinition.setDsRecipientDefinitions(dsRecipientDefinitions);
		}
	}

	private void prepareAndSetDSCustomFieldDefinitions(DSEnvelopeDefinition dsEnvelopeDefinition,
			DSEnvelopeSavedData dsEnvelopeSavedData) {

		List<DSCustomField> uniqueDSCustomFields = dsEnvelopeSavedData.getUniqueDSCustomFields();
		if (null != uniqueDSCustomFields && !uniqueDSCustomFields.isEmpty()) {

			List<DSCustomFieldDefinition> dsCustomFieldDefinitions = new ArrayList<DSCustomFieldDefinition>(
					uniqueDSCustomFields.size());

			uniqueDSCustomFields.stream().filter(value -> value != null).forEach(customField -> {

				dsCustomFieldDefinitions.add(dsCustomFieldTransformer.transformToDSCustomFieldDefinition(customField));
			});

			dsEnvelopeDefinition.setDsCustomFieldDefinitions(dsCustomFieldDefinitions);
		}
	}

	private void prepareAndSetTab(DSRecipientDefinition dsRecipientDefinition,
			DSEnvelopeSavedData dsEnvelopeSavedData) {

		List<DSTab> uniqueDSTabs = dsEnvelopeSavedData.getUniqueDSTabs();
		if (null != uniqueDSTabs && !uniqueDSTabs.isEmpty()) {

			List<DSTab> filteredDSRecipientTabs = uniqueDSTabs.stream()
					.filter(dsTab -> dsTab.getRecipientId().equalsIgnoreCase(dsRecipientDefinition.getRecipientId()))
					.collect(Collectors.toList());

			List<DSTabDefinition> dsTabDefinitions = new ArrayList<DSTabDefinition>(filteredDSRecipientTabs.size());
			filteredDSRecipientTabs.forEach(tab -> {

				dsTabDefinitions.add(dsTabTransformer.transformToDSTabDefinition(tab));
			});

			dsRecipientDefinition.setDsTabDefinitions(dsTabDefinitions);

		}
	}

	private void prepareAndSetRecipientAuth(DSRecipientDefinition dsRecipientDefinition,
			DSEnvelopeSavedData dsEnvelopeSavedData) {

		List<DSRecipientAuth> uniqueDSRecipientAuths = dsEnvelopeSavedData.getUniqueDSRecipientAuths();

		if (null != uniqueDSRecipientAuths && !uniqueDSRecipientAuths.isEmpty()) {

			List<DSRecipientAuth> filteredDSRecipientAuths = uniqueDSRecipientAuths.stream()
					.filter(recipientAuth -> recipientAuth.getRecipientId()
							.equalsIgnoreCase(dsRecipientDefinition.getRecipientId()))
					.collect(Collectors.toList());

			if (null != filteredDSRecipientAuths && !filteredDSRecipientAuths.isEmpty()) {

				List<DSRecipientAuthDefinition> dsRecipientAuthDefinitions = new ArrayList<DSRecipientAuthDefinition>(
						filteredDSRecipientAuths.size());

				filteredDSRecipientAuths.forEach(recipientAuth -> {

					dsRecipientAuthDefinitions
							.add(dsRecipientAuthTransformer.transformToDSRecipientAuthDefinition(recipientAuth));
				});

				dsRecipientDefinition.setDsRecipientAuthDefinitions(dsRecipientAuthDefinitions);
			}
		}
	}
}