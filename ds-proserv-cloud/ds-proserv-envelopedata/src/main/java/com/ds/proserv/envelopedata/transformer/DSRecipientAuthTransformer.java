package com.ds.proserv.envelopedata.transformer;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ds.proserv.envelopedata.model.DSRecipientAuth;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientAuthDefinition;

@Component
public class DSRecipientAuthTransformer {

	public DSRecipientAuth transformToDSRecipientAuth(DSRecipientAuthDefinition dsRecipientAuthDefinition) {

		DSRecipientAuth dsRecipientAuth = new DSRecipientAuth();
		dsRecipientAuth.setEnvelopeId(dsRecipientAuthDefinition.getEnvelopeId());
		dsRecipientAuth.setRecipientId(dsRecipientAuthDefinition.getRecipientId());
		dsRecipientAuth.setType(dsRecipientAuthDefinition.getType());
		dsRecipientAuth.setStatus(dsRecipientAuthDefinition.getStatus());

		if (!StringUtils.isEmpty(dsRecipientAuthDefinition.getEventDateTime())) {

			dsRecipientAuth.setEventDateTime(LocalDateTime.parse(dsRecipientAuthDefinition.getEventDateTime()));
		}

		return dsRecipientAuth;
	}

	public DSRecipientAuth transformToDSRecipientAuthUpdate(DSRecipientAuthDefinition dsRecipientAuthDefinition,
			DSRecipientAuth dsRecipientAuth) {

		if (!StringUtils.isEmpty(dsRecipientAuthDefinition.getEnvelopeId())) {

			dsRecipientAuth.setEnvelopeId(dsRecipientAuthDefinition.getEnvelopeId());
		}

		if (!StringUtils.isEmpty(dsRecipientAuthDefinition.getRecipientId())) {

			dsRecipientAuth.setRecipientId(dsRecipientAuthDefinition.getRecipientId());
		}

		if (!StringUtils.isEmpty(dsRecipientAuthDefinition.getType())) {

			dsRecipientAuth.setType(dsRecipientAuthDefinition.getType());
		}

		if (!StringUtils.isEmpty(dsRecipientAuthDefinition.getStatus())) {

			dsRecipientAuth.setStatus(dsRecipientAuthDefinition.getStatus());
		}

		if (!StringUtils.isEmpty(dsRecipientAuthDefinition.getEventDateTime())) {

			dsRecipientAuth.setEventDateTime(LocalDateTime.parse(dsRecipientAuthDefinition.getEventDateTime()));
		}

		return dsRecipientAuth;
	}

	public DSRecipientAuthDefinition transformToDSRecipientAuthDefinition(DSRecipientAuth dsRecipientAuth) {

		DSRecipientAuthDefinition dsRecipientAuthDefinition = new DSRecipientAuthDefinition();

		dsRecipientAuthDefinition.setId(dsRecipientAuth.getId());
		dsRecipientAuthDefinition.setEnvelopeId(dsRecipientAuth.getEnvelopeId());
		dsRecipientAuthDefinition.setRecipientId(dsRecipientAuth.getRecipientId());
		dsRecipientAuthDefinition.setType(dsRecipientAuth.getType());
		dsRecipientAuthDefinition.setStatus(dsRecipientAuth.getStatus());

		if (null != dsRecipientAuth.getEventDateTime()) {

			dsRecipientAuthDefinition.setEventDateTime(dsRecipientAuth.getEventDateTime().toString());
		}

		return dsRecipientAuthDefinition;
	}
}