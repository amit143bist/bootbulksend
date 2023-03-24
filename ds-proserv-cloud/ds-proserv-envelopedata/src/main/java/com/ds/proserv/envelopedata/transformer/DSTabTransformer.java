package com.ds.proserv.envelopedata.transformer;

import org.springframework.stereotype.Component;

import com.ds.proserv.envelopedata.model.DSTab;
import com.ds.proserv.feign.envelopedata.domain.DSTabDefinition;

@Component
public class DSTabTransformer {

	public DSTab transformToDSTab(DSTabDefinition dsTabDefinition) {

		DSTab newDsTab = new DSTab();

		newDsTab.setId(dsTabDefinition.getId());
		newDsTab.setEnvelopeId(dsTabDefinition.getEnvelopeId());
		newDsTab.setRecipientId(dsTabDefinition.getRecipientId());
		newDsTab.setTabName(dsTabDefinition.getTabName());
		newDsTab.setTabLabel(dsTabDefinition.getTabLabel());
		newDsTab.setTabValue(dsTabDefinition.getTabValue());
		newDsTab.setTabOriginalValue(dsTabDefinition.getTabOriginalValue());
		newDsTab.setTabStatus(dsTabDefinition.getTabStatus());

		return newDsTab;
	}

	public DSTab transformToDSTabUpdate(DSTabDefinition dsTabDefinition, DSTab dsTab) {

		dsTab.setEnvelopeId(dsTabDefinition.getEnvelopeId());
		dsTab.setRecipientId(dsTabDefinition.getRecipientId());
		dsTab.setTabName(dsTabDefinition.getTabName());
		dsTab.setTabLabel(dsTabDefinition.getTabLabel());
		dsTab.setTabValue(dsTabDefinition.getTabValue());
		dsTab.setTabOriginalValue(dsTabDefinition.getTabOriginalValue());
		dsTab.setTabStatus(dsTabDefinition.getTabStatus());

		return dsTab;
	}

	public DSTabDefinition transformToDSTabDefinition(DSTab dsTab) {

		DSTabDefinition dsTabDefinition = new DSTabDefinition();

		dsTabDefinition.setId(dsTab.getId());
		dsTabDefinition.setEnvelopeId(dsTab.getEnvelopeId());
		dsTabDefinition.setRecipientId(dsTab.getRecipientId());
		dsTabDefinition.setTabName(dsTab.getTabName());
		dsTabDefinition.setTabLabel(dsTab.getTabLabel());
		dsTabDefinition.setTabValue(dsTab.getTabValue());
		dsTabDefinition.setTabOriginalValue(dsTab.getTabOriginalValue());
		dsTabDefinition.setTabStatus(dsTab.getTabStatus());

		return dsTabDefinition;
	}

}