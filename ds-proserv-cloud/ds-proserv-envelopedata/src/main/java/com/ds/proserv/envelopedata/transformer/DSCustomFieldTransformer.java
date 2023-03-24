package com.ds.proserv.envelopedata.transformer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.envelopedata.model.DSCustomField;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldDefinition;

@Component
public class DSCustomFieldTransformer {

	public DSCustomField transformToDSCustomField(DSCustomFieldDefinition dsCustomFieldDefinition) {

		DSCustomField dsCustomField = new DSCustomField();

		dsCustomField.setId(dsCustomFieldDefinition.getId());
		dsCustomField.setEnvelopeId(dsCustomFieldDefinition.getEnvelopeId());
		dsCustomField.setRecipientId(dsCustomFieldDefinition.getRecipientId());
		dsCustomField.setDocumentId(dsCustomFieldDefinition.getDocumentId());
		dsCustomField.setDocumentName(dsCustomFieldDefinition.getDocumentName());
		dsCustomField.setDocumentSequence(dsCustomFieldDefinition.getDocumentSequence());
		dsCustomField.setFieldName(dsCustomFieldDefinition.getFieldName());
		dsCustomField.setFieldValue(dsCustomFieldDefinition.getFieldValue());
		dsCustomField.setFieldType(dsCustomFieldDefinition.getFieldType());

		return dsCustomField;
	}

	public DSCustomField transformToDSCustomField(DSCustomFieldDefinition dsCustomFieldDefinition,
			DSCustomField dsCustomField) {

		if (!StringUtils.isEmpty(dsCustomFieldDefinition.getEnvelopeId())) {

			dsCustomField.setEnvelopeId(dsCustomFieldDefinition.getEnvelopeId());
		}

		if (!StringUtils.isEmpty(dsCustomFieldDefinition.getRecipientId())) {

			dsCustomField.setRecipientId(dsCustomFieldDefinition.getRecipientId());
		}

		if (null != dsCustomFieldDefinition.getDocumentId()) {

			dsCustomField.setDocumentId(dsCustomFieldDefinition.getDocumentId());
		}

		if (!StringUtils.isEmpty(dsCustomFieldDefinition.getDocumentName())) {

			dsCustomField.setDocumentName(dsCustomFieldDefinition.getDocumentName());
		}

		if (null != dsCustomFieldDefinition.getDocumentSequence()) {

			dsCustomField.setDocumentSequence(dsCustomFieldDefinition.getDocumentSequence());
		}

		if (!StringUtils.isEmpty(dsCustomFieldDefinition.getFieldName())) {

			dsCustomField.setFieldName(dsCustomFieldDefinition.getFieldName());
		}

		if (!StringUtils.isEmpty(dsCustomFieldDefinition.getFieldValue())) {

			dsCustomField.setFieldValue(dsCustomFieldDefinition.getFieldValue());
		}

		if (!StringUtils.isEmpty(dsCustomFieldDefinition.getFieldType())) {

			dsCustomField.setFieldType(dsCustomFieldDefinition.getFieldType());
		}

		return dsCustomField;
	}

	public DSCustomFieldDefinition transformToDSCustomFieldDefinition(DSCustomField dsCustomField) {

		DSCustomFieldDefinition dsCustomFieldDefinition = new DSCustomFieldDefinition();

		dsCustomFieldDefinition.setId(dsCustomField.getId());
		dsCustomFieldDefinition.setEnvelopeId(dsCustomField.getEnvelopeId());
		dsCustomFieldDefinition.setRecipientId(dsCustomField.getRecipientId());
		dsCustomFieldDefinition.setDocumentId(dsCustomField.getDocumentId());
		dsCustomFieldDefinition.setDocumentName(dsCustomField.getDocumentName());
		dsCustomFieldDefinition.setDocumentSequence(dsCustomField.getDocumentSequence());
		dsCustomFieldDefinition.setFieldName(dsCustomField.getFieldName());
		dsCustomFieldDefinition.setFieldValue(dsCustomField.getFieldValue());
		dsCustomFieldDefinition.setFieldType(dsCustomField.getFieldType());

		return dsCustomFieldDefinition;
	}
}