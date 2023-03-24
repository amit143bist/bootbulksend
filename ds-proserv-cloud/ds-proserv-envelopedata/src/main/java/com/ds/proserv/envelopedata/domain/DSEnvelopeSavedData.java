package com.ds.proserv.envelopedata.domain;

import java.util.ArrayList;
import java.util.List;

import com.ds.proserv.envelopedata.model.DSCustomField;
import com.ds.proserv.envelopedata.model.DSRecipient;
import com.ds.proserv.envelopedata.model.DSRecipientAuth;
import com.ds.proserv.envelopedata.model.DSTab;
import com.ds.proserv.envelopedata.projection.DSEnvelopeProjection;

import lombok.Data;

@Data
public class DSEnvelopeSavedData {

	private List<DSCustomField> dsCustomFields = new ArrayList<DSCustomField>();
	private List<DSRecipient> dsRecipients = new ArrayList<DSRecipient>();
	private List<DSTab> dsTabs = new ArrayList<DSTab>();
	private List<DSRecipientAuth> dsRecipientAuths = new ArrayList<DSRecipientAuth>();

	private List<DSCustomField> uniqueDSCustomFields;
	private List<DSRecipient> uniqueDSRecipients;
	private List<DSTab> uniqueDSTabs;
	private List<DSRecipientAuth> uniqueDSRecipientAuths;

	public DSEnvelopeSavedData(List<DSEnvelopeProjection> groupByEnvelopeId) {

		groupByEnvelopeId.forEach(eachEnvelopeInColl -> {

			dsCustomFields.add(eachEnvelopeInColl.getCustomField());
			dsRecipients.add(eachEnvelopeInColl.getRecipient());
			dsTabs.add(eachEnvelopeInColl.getTab());
			dsRecipientAuths.add(eachEnvelopeInColl.getRecipientAuth());
		});

	}

}