package com.ds.proserv.envelopedata.domain;

import static com.ds.proserv.common.lambda.LambdaExceptionWrappers.throwingConsumerWrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.ds.proserv.envelopedata.model.DSCustomField;
import com.ds.proserv.envelopedata.model.DSEnvelope;
import com.ds.proserv.envelopedata.model.DSEnvelopeDocLog;
import com.ds.proserv.envelopedata.model.DSRecipient;
import com.ds.proserv.envelopedata.model.DSRecipientAuth;
import com.ds.proserv.envelopedata.model.DSTab;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDocLogDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeInformation;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientAuthDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSTabDefinition;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class DSEnvelopeData {

	private String processId;
	private LocalDateTime leastSentDateTime;
	private List<String> envelopeIdsToSave;
	private List<DSEnvelopeDefinition> envelopeToBeSavedList;
	private List<DSTabDefinition> tabToBeSavedList;
	private List<DSCustomFieldDefinition> customFieldToBeSavedList;
	private List<DSRecipientAuthDefinition> recipientAuthToBeSavedList;
	private List<DSRecipientDefinition> recipientToBeSavedList;
	private List<DSEnvelopeDocLogDefinition> envelopeDocLogToBeSavedList;

	private List<DSTab> prepareToSaveDSTabList;
	private List<DSRecipient> prepareToSaveDSRecipientList;
	private List<DSEnvelope> prepareToSaveDSEnvelopeList;
	private List<DSRecipientAuth> prepareToSaveDSRecipientAuthList;
	private List<DSCustomField> prepareToSaveDSCustomFieldList;
	private List<DSEnvelopeDocLog> prepareToSaveDSEnvelopeDocLogList;

	private LocalDateTime singleEnvelopeSavedTimeGenerated;

	public DSEnvelopeData(DSEnvelopeInformation dsEnvelopeInformation) {

		this.processId = dsEnvelopeInformation.getProcessId();
		this.envelopeToBeSavedList = Collections.synchronizedList(dsEnvelopeInformation.getDsEnvelopeDefinitions());

		this.envelopeIdsToSave = Collections.synchronizedList(new ArrayList<String>(this.envelopeToBeSavedList.stream()
				.filter(dsEnvelopeDefinition -> dsEnvelopeDefinition.getEnvelopeId() != null)
				.map(DSEnvelopeDefinition::getEnvelopeId).collect(Collectors.toList())));

		List<LocalDateTime> sentDateTimeList = this.envelopeToBeSavedList.stream()
				.filter(dsEnvelopeDefinition -> dsEnvelopeDefinition.getSentDateTime() != null)
				.map(dsEnvelopeDefinition -> LocalDateTime.parse(dsEnvelopeDefinition.getSentDateTime()))
				.collect(Collectors.toList());

		if (null != sentDateTimeList && !sentDateTimeList.isEmpty()) {

			Collections.sort(sentDateTimeList);
			this.leastSentDateTime = sentDateTimeList.get(0);
		} else {

			log.warn("sentDateTimeList cannot be empty or null");
		}

		log.info(
				"Total EnvelopeIds -> {} are ready to be saved/updated for processId -> {} and least SentDateTime is {}",
				this.envelopeIdsToSave, this.processId, this.leastSentDateTime);

		this.tabToBeSavedList = Collections.synchronizedList(new ArrayList<DSTabDefinition>());
		this.customFieldToBeSavedList = Collections.synchronizedList(new ArrayList<DSCustomFieldDefinition>());
		this.recipientAuthToBeSavedList = Collections.synchronizedList(new ArrayList<DSRecipientAuthDefinition>());
		this.recipientToBeSavedList = Collections.synchronizedList(new ArrayList<DSRecipientDefinition>());
		this.envelopeDocLogToBeSavedList = Collections.synchronizedList(new ArrayList<DSEnvelopeDocLogDefinition>());

		this.envelopeToBeSavedList
				.forEach(throwingConsumerWrapper(envelopeToBeSaved -> prepareDataChildList(this.tabToBeSavedList,
						this.customFieldToBeSavedList, this.recipientAuthToBeSavedList, this.recipientToBeSavedList,
						this.envelopeDocLogToBeSavedList, envelopeToBeSaved)));

		this.prepareToSaveDSTabList = Collections.synchronizedList(new ArrayList<DSTab>());
		this.prepareToSaveDSRecipientList = Collections.synchronizedList(new ArrayList<DSRecipient>());
		this.prepareToSaveDSEnvelopeList = Collections.synchronizedList(new ArrayList<DSEnvelope>());
		this.prepareToSaveDSRecipientAuthList = Collections.synchronizedList(new ArrayList<DSRecipientAuth>());
		this.prepareToSaveDSCustomFieldList = Collections.synchronizedList(new ArrayList<DSCustomField>());
		this.prepareToSaveDSEnvelopeDocLogList = Collections.synchronizedList(new ArrayList<DSEnvelopeDocLog>());

	}

	private void prepareDataChildList(List<DSTabDefinition> tabToBeSavedList,
			List<DSCustomFieldDefinition> customFieldToBeSavedList,
			List<DSRecipientAuthDefinition> recipientAuthToBeSavedList,
			List<DSRecipientDefinition> recipientToBeSavedList,
			List<DSEnvelopeDocLogDefinition> envelopeDocLogToBeSavedList, DSEnvelopeDefinition envelopeToBeSaved) {

		if (null != envelopeToBeSaved.getDsCustomFieldDefinitions()
				&& !envelopeToBeSaved.getDsCustomFieldDefinitions().isEmpty()) {

			customFieldToBeSavedList.addAll(envelopeToBeSaved.getDsCustomFieldDefinitions());
		}

		if (null != envelopeToBeSaved.getDsEnvelopeDocLogDefinition()) {

			envelopeDocLogToBeSavedList.add(envelopeToBeSaved.getDsEnvelopeDocLogDefinition());
		}

		List<DSRecipientDefinition> recipientList = envelopeToBeSaved.getDsRecipientDefinitions();

		if (null != recipientList && !recipientList.isEmpty()) {

			recipientList.forEach(recipient -> {

				if (null != recipient.getDsTabDefinitions() && !recipient.getDsTabDefinitions().isEmpty()) {

					tabToBeSavedList.addAll(recipient.getDsTabDefinitions());
				}

				if (null != recipient.getDsRecipientAuthDefinitions()
						&& !recipient.getDsRecipientAuthDefinitions().isEmpty()) {

					recipientAuthToBeSavedList.addAll(recipient.getDsRecipientAuthDefinitions());
				}
			});

			recipientToBeSavedList.addAll(recipientList);
		}
	}

}