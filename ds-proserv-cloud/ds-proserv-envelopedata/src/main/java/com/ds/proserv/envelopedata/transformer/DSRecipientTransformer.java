package com.ds.proserv.envelopedata.transformer;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.envelopedata.model.DSRecipient;
import com.ds.proserv.feign.envelopedata.domain.DSRecipientDefinition;

@Component
public class DSRecipientTransformer {

	public DSRecipient transformToDSRecipient(DSRecipientDefinition dsRecipientDefinition) {

		DSRecipient newDsRecipient = new DSRecipient();

		newDsRecipient.setRecipientId(dsRecipientDefinition.getRecipientId());
		newDsRecipient.setEnvelopeId(dsRecipientDefinition.getEnvelopeId());
		newDsRecipient.setRecipientEmail(dsRecipientDefinition.getRecipientEmail());
		newDsRecipient.setRecipientName(dsRecipientDefinition.getRecipientName());
		newDsRecipient.setRoutingOrder(dsRecipientDefinition.getRoutingOrder());
		newDsRecipient.setStatus(dsRecipientDefinition.getStatus());
		newDsRecipient.setRecipientIPAddress(dsRecipientDefinition.getRecipientIPAddress());
		newDsRecipient.setDeclineReason(dsRecipientDefinition.getDeclineReason());
		newDsRecipient.setClientUserId(dsRecipientDefinition.getClientUserId());

		if (!StringUtils.isEmpty(dsRecipientDefinition.getSentDateTime())) {

			newDsRecipient.setSentDateTime(LocalDateTime.parse(dsRecipientDefinition.getSentDateTime()));
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getDeliveredDateTime())) {

			newDsRecipient.setDeliveredDateTime(LocalDateTime.parse(dsRecipientDefinition.getDeliveredDateTime()));
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getDeclinedDateTime())) {

			newDsRecipient.setDeclinedDateTime(LocalDateTime.parse(dsRecipientDefinition.getDeclinedDateTime()));
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getSignedDateTime())) {

			newDsRecipient.setSignedDateTime(LocalDateTime.parse(dsRecipientDefinition.getSignedDateTime()));
		}
		return newDsRecipient;
	}

	public DSRecipient transformToDSRecipientUpdate(DSRecipientDefinition dsRecipientDefinition,
			DSRecipient dsRecipient) {

		if (!StringUtils.isEmpty(dsRecipientDefinition.getRecipientId())) {

			dsRecipient.setRecipientId(dsRecipientDefinition.getRecipientId());
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getEnvelopeId())) {

			dsRecipient.setEnvelopeId(dsRecipientDefinition.getEnvelopeId());
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getRecipientEmail())) {

			dsRecipient.setRecipientEmail(dsRecipientDefinition.getRecipientEmail());
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getRecipientName())) {

			dsRecipient.setRecipientName(dsRecipientDefinition.getRecipientName());
		}

		if (null != dsRecipientDefinition.getRoutingOrder()) {

			dsRecipient.setRoutingOrder(dsRecipientDefinition.getRoutingOrder());
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getStatus())) {

			dsRecipient.setStatus(dsRecipientDefinition.getStatus());
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getRecipientIPAddress())) {

			dsRecipient.setRecipientIPAddress(dsRecipientDefinition.getRecipientIPAddress());
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getDeclineReason())) {

			dsRecipient.setDeclineReason(dsRecipientDefinition.getDeclineReason());
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getClientUserId())) {

			dsRecipient.setClientUserId(dsRecipientDefinition.getClientUserId());
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getSentDateTime())) {

			dsRecipient.setSentDateTime(LocalDateTime.parse(dsRecipientDefinition.getSentDateTime()));
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getDeliveredDateTime())) {

			dsRecipient.setDeliveredDateTime(LocalDateTime.parse(dsRecipientDefinition.getDeliveredDateTime()));
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getDeclinedDateTime())) {

			dsRecipient.setDeclinedDateTime(LocalDateTime.parse(dsRecipientDefinition.getDeclinedDateTime()));
		}

		if (!StringUtils.isEmpty(dsRecipientDefinition.getSignedDateTime())) {

			dsRecipient.setSignedDateTime(LocalDateTime.parse(dsRecipientDefinition.getSignedDateTime()));
		}

		return dsRecipient;

	}

	public DSRecipientDefinition transformToDSRecipientDefinition(DSRecipient dsRecipient) {

		DSRecipientDefinition dsRecipientDefinition = new DSRecipientDefinition();

		dsRecipientDefinition.setRecipientId(dsRecipient.getRecipientId());
		dsRecipientDefinition.setEnvelopeId(dsRecipient.getEnvelopeId());
		dsRecipientDefinition.setRecipientEmail(dsRecipient.getRecipientEmail());
		dsRecipientDefinition.setRecipientName(dsRecipient.getRecipientName());
		dsRecipientDefinition.setRoutingOrder(dsRecipient.getRoutingOrder());
		dsRecipientDefinition.setStatus(dsRecipient.getStatus());
		dsRecipientDefinition.setRecipientIPAddress(dsRecipient.getRecipientIPAddress());
		dsRecipientDefinition.setDeclineReason(dsRecipient.getDeclineReason());
		dsRecipientDefinition.setClientUserId(dsRecipient.getClientUserId());

		if (null != dsRecipient.getSentDateTime()) {

			dsRecipientDefinition.setSentDateTime(dsRecipient.getSentDateTime().toString());
		}

		if (null != dsRecipient.getDeliveredDateTime()) {

			dsRecipientDefinition.setDeliveredDateTime(dsRecipient.getDeliveredDateTime().toString());
		}

		if (null != dsRecipient.getDeclinedDateTime()) {

			dsRecipientDefinition.setDeclinedDateTime(dsRecipient.getDeclinedDateTime().toString());
		}

		if (null != dsRecipient.getSignedDateTime()) {

			dsRecipientDefinition.setSignedDateTime(dsRecipient.getSignedDateTime().toString());
		}

		return dsRecipientDefinition;
	}
}