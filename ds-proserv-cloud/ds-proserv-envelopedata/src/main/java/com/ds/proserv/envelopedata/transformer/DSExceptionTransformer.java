package com.ds.proserv.envelopedata.transformer;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ds.proserv.envelopedata.model.DSException;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionDefinition;

@Component
public class DSExceptionTransformer {

	public DSException transformToDSException(DSExceptionDefinition dsExceptionDefinition) {

		DSException dsException = new DSException();

		dsException.setEnvelopeId(dsExceptionDefinition.getEnvelopeId());
		dsException.setEnvelopeXml(dsExceptionDefinition.getEnvelopeXml());
		dsException.setExceptionCode(dsExceptionDefinition.getExceptionCode());

		dsException.setExceptionDateTime(LocalDateTime.parse(dsExceptionDefinition.getExceptionDateTime()));
		dsException.setExceptionReason(dsExceptionDefinition.getExceptionReason());
		dsException.setExceptionStep(dsExceptionDefinition.getExceptionStep());
		dsException.setRetryCount(dsExceptionDefinition.getRetryCount());

		if (!StringUtils.isEmpty(dsExceptionDefinition.getRetryDateTime())) {

			dsException.setRetryDateTime(LocalDateTime.parse(dsExceptionDefinition.getRetryDateTime()));
		}

		dsException.setRetryStatus(dsExceptionDefinition.getRetryStatus());
		dsException.setId(dsExceptionDefinition.getId());

		return dsException;
	}

	public DSException transformToDSExceptionUpdate(DSExceptionDefinition dsExceptionDefinition,
			DSException dsException) {

		if (!StringUtils.isEmpty(dsExceptionDefinition.getEnvelopeId())) {

			dsException.setEnvelopeId(dsExceptionDefinition.getEnvelopeId());
		}

		if (!StringUtils.isEmpty(dsExceptionDefinition.getEnvelopeXml())) {

			dsException.setEnvelopeXml(dsExceptionDefinition.getEnvelopeXml());
		}

		if (!StringUtils.isEmpty(dsExceptionDefinition.getExceptionCode())) {

			dsException.setExceptionCode(dsExceptionDefinition.getExceptionCode());
		}

		if (!StringUtils.isEmpty(dsExceptionDefinition.getExceptionDateTime())) {

			dsException.setExceptionDateTime(LocalDateTime.parse(dsExceptionDefinition.getExceptionDateTime()));
		}

		if (!StringUtils.isEmpty(dsExceptionDefinition.getExceptionReason())) {

			dsException.setExceptionReason(dsExceptionDefinition.getExceptionReason());
		}

		if (!StringUtils.isEmpty(dsExceptionDefinition.getExceptionStep())) {

			dsException.setExceptionStep(dsExceptionDefinition.getExceptionStep());
		}

		if (null != dsExceptionDefinition.getRetryCount()) {

			dsException.setRetryCount(dsExceptionDefinition.getRetryCount());
		}

		if (!StringUtils.isEmpty(dsExceptionDefinition.getRetryDateTime())) {

			dsException.setRetryDateTime(LocalDateTime.parse(dsExceptionDefinition.getRetryDateTime()));
		}

		if (!StringUtils.isEmpty(dsExceptionDefinition.getRetryStatus())) {

			dsException.setRetryStatus(dsExceptionDefinition.getRetryStatus());
		}

		return dsException;
	}

	public DSExceptionDefinition transformToDSExceptionDefinition(DSException dsException) {

		DSExceptionDefinition dsExceptionDefinition = new DSExceptionDefinition();

		dsExceptionDefinition.setEnvelopeId(dsException.getEnvelopeId());
		dsExceptionDefinition.setEnvelopeXml(dsException.getEnvelopeXml());
		dsExceptionDefinition.setExceptionCode(dsException.getExceptionCode());
		dsExceptionDefinition.setExceptionDateTime(dsException.getExceptionDateTime().toString());
		dsExceptionDefinition.setExceptionReason(dsException.getExceptionReason());
		dsExceptionDefinition.setExceptionStep(dsException.getExceptionStep());
		dsExceptionDefinition.setRetryCount(dsException.getRetryCount());

		if (null != dsException.getRetryDateTime()) {

			dsExceptionDefinition.setRetryDateTime(dsException.getRetryDateTime().toString());
		}

		dsExceptionDefinition.setRetryStatus(dsException.getRetryStatus());
		dsExceptionDefinition.setId(dsException.getId());

		return dsExceptionDefinition;

	}
}