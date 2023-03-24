package com.ds.proserv.envelopedata.transformer;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.envelopedata.model.DSEnvelope;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;

@Component
public class DSEnvelopeTransformer {

	public DSEnvelope transformToDSEnvelope(DSEnvelopeDefinition dsEnvelopeDefinition) {

		DSEnvelope newDsEnvelope = new DSEnvelope();
		// set envelopeId
		newDsEnvelope.setEnvelopeId(dsEnvelopeDefinition.getEnvelopeId());
		newDsEnvelope.setEnvelopeSubject(dsEnvelopeDefinition.getEnvelopeSubject());
		newDsEnvelope.setStatus(dsEnvelopeDefinition.getStatus());
		// set sender info
		newDsEnvelope.setSenderName(dsEnvelopeDefinition.getSenderName());
		newDsEnvelope.setSenderEmail(dsEnvelopeDefinition.getSenderEmail());

		// set completed date
		if (!StringUtils.isEmpty(dsEnvelopeDefinition.getCompletedDateTime())) {

			newDsEnvelope.setCompletedDateTime(LocalDateTime.parse(dsEnvelopeDefinition.getCompletedDateTime()));
		}

		// set delivered date
		if (!StringUtils.isEmpty(dsEnvelopeDefinition.getDeliveredDateTime())) {

			newDsEnvelope.setDeliveredDateTime(LocalDateTime.parse(dsEnvelopeDefinition.getDeliveredDateTime()));
		}

		// set sent date
		if (!StringUtils.isEmpty(dsEnvelopeDefinition.getSentDateTime())) {

			newDsEnvelope.setSentDateTime(LocalDateTime.parse(dsEnvelopeDefinition.getSentDateTime()));
		}

		// set declined date
		if (!StringUtils.isEmpty(dsEnvelopeDefinition.getDeclinedDateTime())) {

			newDsEnvelope.setDeclinedDateTime(LocalDateTime.parse(dsEnvelopeDefinition.getDeclinedDateTime()));
		}

		if (!StringUtils.isEmpty(dsEnvelopeDefinition.getTimeGenerated())) {

			newDsEnvelope.setTimeGenerated(LocalDateTime.parse(dsEnvelopeDefinition.getTimeGenerated()));
		}

		newDsEnvelope.setTerminalReason(dsEnvelopeDefinition.getTerminalReason());
		newDsEnvelope.setTimeZone(dsEnvelopeDefinition.getTimeZone());
		newDsEnvelope.setTimeZoneoffset(dsEnvelopeDefinition.getTimeZoneoffset());
		newDsEnvelope.setFileNames(dsEnvelopeDefinition.getFileNames());
		return newDsEnvelope;
	}

	public DSEnvelope transformToDSEnvelopeUpdate(DSEnvelopeDefinition dsEnvelopeDefinition, DSEnvelope dsEnvelope) {

		// set envelopeId
		dsEnvelope.setEnvelopeSubject(dsEnvelopeDefinition.getEnvelopeSubject());
		dsEnvelope.setStatus(dsEnvelopeDefinition.getStatus());
		// set sender info
		dsEnvelope.setSenderName(dsEnvelopeDefinition.getSenderName());
		dsEnvelope.setSenderEmail(dsEnvelopeDefinition.getSenderEmail());

		// set completed date
		if (!StringUtils.isEmpty(dsEnvelopeDefinition.getCompletedDateTime())) {

			dsEnvelope.setCompletedDateTime(LocalDateTime.parse(dsEnvelopeDefinition.getCompletedDateTime()));
		}

		// set delivered date
		if (!StringUtils.isEmpty(dsEnvelopeDefinition.getDeliveredDateTime())) {

			dsEnvelope.setDeliveredDateTime(LocalDateTime.parse(dsEnvelopeDefinition.getDeliveredDateTime()));
		}

		// set sent date
		if (!StringUtils.isEmpty(dsEnvelopeDefinition.getSentDateTime())) {

			dsEnvelope.setSentDateTime(LocalDateTime.parse(dsEnvelopeDefinition.getSentDateTime()));
		}

		// set declined date
		if (!StringUtils.isEmpty(dsEnvelopeDefinition.getDeclinedDateTime())) {

			dsEnvelope.setDeclinedDateTime(LocalDateTime.parse(dsEnvelopeDefinition.getDeclinedDateTime()));
		}

		if (!StringUtils.isEmpty(dsEnvelopeDefinition.getTimeGenerated())) {

			dsEnvelope.setTimeGenerated(LocalDateTime.parse(dsEnvelopeDefinition.getTimeGenerated()));
		}

		if (!StringUtils.isEmpty(dsEnvelopeDefinition.getTerminalReason())) {

			dsEnvelope.setTerminalReason(dsEnvelopeDefinition.getTerminalReason());
		}

		dsEnvelope.setTimeZone(dsEnvelopeDefinition.getTimeZone());
		dsEnvelope.setTimeZoneoffset(dsEnvelopeDefinition.getTimeZoneoffset());
		dsEnvelope.setFileNames(dsEnvelopeDefinition.getFileNames());
		return dsEnvelope;
	}

	public DSEnvelopeDefinition transformToDSEnvelopeDefinition(DSEnvelope dsEnvelope) {

		DSEnvelopeDefinition dsEnvelopeDefinition = new DSEnvelopeDefinition();

		dsEnvelopeDefinition.setEnvelopeId(dsEnvelope.getEnvelopeId());
		dsEnvelopeDefinition.setEnvelopeSubject(dsEnvelope.getEnvelopeSubject());
		dsEnvelopeDefinition.setStatus(dsEnvelope.getStatus());
		// set sender info
		dsEnvelopeDefinition.setSenderName(dsEnvelope.getSenderName());
		dsEnvelopeDefinition.setSenderEmail(dsEnvelope.getSenderEmail());

		// set completed date

		if (null != dsEnvelope.getCompletedDateTime()) {

			dsEnvelopeDefinition.setCompletedDateTime(dsEnvelope.getCompletedDateTime().toString());
		}

		// set delivered date
		if (null != dsEnvelope.getDeliveredDateTime()) {

			dsEnvelopeDefinition.setDeliveredDateTime(dsEnvelope.getDeliveredDateTime().toString());
		}

		// set sent date
		if (null != dsEnvelope.getSentDateTime()) {

			dsEnvelopeDefinition.setSentDateTime(dsEnvelope.getSentDateTime().toString());
		}

		// set declined date
		if (null != dsEnvelope.getDeclinedDateTime()) {

			dsEnvelopeDefinition.setDeclinedDateTime(dsEnvelope.getDeclinedDateTime().toString());
		}

		if (null != dsEnvelope.getTimeGenerated()) {

			dsEnvelopeDefinition.setTimeGenerated(dsEnvelope.getTimeGenerated().toString());
		}

		dsEnvelopeDefinition.setTerminalReason(dsEnvelope.getTerminalReason());

		dsEnvelopeDefinition.setTimeZone(dsEnvelope.getTimeZone());
		dsEnvelopeDefinition.setTimeZoneoffset(dsEnvelope.getTimeZoneoffset());
		dsEnvelopeDefinition.setFileNames(dsEnvelope.getFileNames());

		return dsEnvelopeDefinition;
	}
}