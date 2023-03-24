package com.ds.proserv.appdata.transformer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.appdata.model.DrawApplication;
import com.ds.proserv.feign.appdata.domain.DrawApplicationDefinition;

@Component
public class DrawApplicationTransformer {

	public DrawApplication transformToDrawApplication(DrawApplicationDefinition drawApplicationDefinition) {

		DrawApplication drawApplication = new DrawApplication();
		drawApplication.setApplicationId(drawApplicationDefinition.getApplicationId());
		drawApplication.setAgentCode(drawApplicationDefinition.getAgentCode());
		drawApplication.setApplicationStatus(drawApplicationDefinition.getApplicationStatus());
		drawApplication.setBridgeEnvelopeId(drawApplicationDefinition.getBridgeEnvelopeId());
		drawApplication.setBulkBatchId(drawApplicationDefinition.getBulkBatchId());
		drawApplication.setDrawReference(drawApplicationDefinition.getDrawReference());
		drawApplication.setDuplicateRecord(drawApplicationDefinition.getDuplicateRecord());
		drawApplication.setLanguageCode(drawApplicationDefinition.getLanguageCode());
		drawApplication.setProgramType(drawApplicationDefinition.getProgramType());
		drawApplication.setTriggerEnvelopeId(drawApplicationDefinition.getTriggerEnvelopeId());

		return drawApplication;
	}

	public DrawApplication transformToDrawApplicationAsUpdate(DrawApplicationDefinition drawApplicationDefinition,
			DrawApplication drawApplication) {

		if (!StringUtils.isEmpty(drawApplicationDefinition.getAgentCode())) {

			drawApplication.setAgentCode(drawApplicationDefinition.getAgentCode());
		}

		if (!StringUtils.isEmpty(drawApplicationDefinition.getApplicationStatus())) {

			drawApplication.setApplicationStatus(drawApplicationDefinition.getApplicationStatus());
		}

		if (!StringUtils.isEmpty(drawApplicationDefinition.getBridgeEnvelopeId())) {

			drawApplication.setBridgeEnvelopeId(drawApplicationDefinition.getBridgeEnvelopeId());
		}

		if (!StringUtils.isEmpty(drawApplicationDefinition.getBulkBatchId())) {

			drawApplication.setBulkBatchId(drawApplicationDefinition.getBulkBatchId());
		}

		if (!StringUtils.isEmpty(drawApplicationDefinition.getDrawReference())) {

			drawApplication.setDrawReference(drawApplicationDefinition.getDrawReference());
		}

		if (null != drawApplicationDefinition.getDuplicateRecord()) {

			drawApplication.setDuplicateRecord(drawApplicationDefinition.getDuplicateRecord());
		}

		if (!StringUtils.isEmpty(drawApplicationDefinition.getLanguageCode())) {

			drawApplication.setLanguageCode(drawApplicationDefinition.getLanguageCode());
		}

		if (!StringUtils.isEmpty(drawApplicationDefinition.getProgramType())) {

			drawApplication.setProgramType(drawApplicationDefinition.getProgramType());
		}

		if (!StringUtils.isEmpty(drawApplicationDefinition.getTriggerEnvelopeId())) {

			drawApplication.setTriggerEnvelopeId(drawApplicationDefinition.getTriggerEnvelopeId());
		}

		return drawApplication;
	}

	public DrawApplicationDefinition transformToDrawApplicationDefinition(DrawApplication drawApplication) {

		DrawApplicationDefinition drawApplicationDefinition = new DrawApplicationDefinition();

		drawApplicationDefinition.setApplicationId(drawApplication.getApplicationId());
		drawApplicationDefinition.setAgentCode(drawApplication.getAgentCode());
		drawApplicationDefinition.setApplicationStatus(drawApplication.getApplicationStatus());
		drawApplicationDefinition.setBridgeEnvelopeId(drawApplication.getBridgeEnvelopeId());
		drawApplicationDefinition.setBulkBatchId(drawApplication.getBulkBatchId());
		drawApplicationDefinition.setDrawReference(drawApplication.getDrawReference());
		drawApplicationDefinition.setDuplicateRecord(drawApplication.getDuplicateRecord());
		drawApplicationDefinition.setLanguageCode(drawApplication.getLanguageCode());
		drawApplicationDefinition.setProgramType(drawApplication.getProgramType());
		drawApplicationDefinition.setTriggerEnvelopeId(drawApplication.getTriggerEnvelopeId());

		return drawApplicationDefinition;
	}
}