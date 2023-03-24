package com.ds.proserv.envelopedata.transformer;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ds.proserv.envelopedata.model.DSEnvelopeDocLog;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDocLogDefinition;

@Component
public class DSEnvelopeDocLogTransformer {

	public DSEnvelopeDocLog transformToDSEnvelopeDocLog(DSEnvelopeDocLogDefinition dsEnvelopeDocLogDefinition) {

		DSEnvelopeDocLog dsEnvelopeDocLog = new DSEnvelopeDocLog();

		dsEnvelopeDocLog.setId(dsEnvelopeDocLogDefinition.getId());
		dsEnvelopeDocLog.setEnvelopeId(dsEnvelopeDocLogDefinition.getEnvelopeId());
		dsEnvelopeDocLog.setTimeGenerated(LocalDateTime.parse(dsEnvelopeDocLogDefinition.getTimeGenerated()));
		dsEnvelopeDocLog.setDocDownloaded(dsEnvelopeDocLogDefinition.getDocDownloaded());
		dsEnvelopeDocLog.setDocDownloadStatus(dsEnvelopeDocLogDefinition.getDocDownloadStatus());

		if (!StringUtils.isEmpty(dsEnvelopeDocLogDefinition.getDocDownloadDateTime())) {

			dsEnvelopeDocLog
					.setDocDownloadDateTime(LocalDateTime.parse(dsEnvelopeDocLogDefinition.getDocDownloadDateTime()));
		}
		dsEnvelopeDocLog.setDocDownloadFailureReason(dsEnvelopeDocLogDefinition.getDocDownloadFailureReason());

		return dsEnvelopeDocLog;
	}

	public DSEnvelopeDocLog transformToDSTabUpdate(DSEnvelopeDocLogDefinition dsEnvelopeDocLogDefinition,
			DSEnvelopeDocLog dsEnvelopeDocLog) {

		if (!StringUtils.isEmpty(dsEnvelopeDocLogDefinition.getEnvelopeId())) {

			dsEnvelopeDocLog.setEnvelopeId(dsEnvelopeDocLogDefinition.getEnvelopeId());
		}

		if (!StringUtils.isEmpty(dsEnvelopeDocLogDefinition.getTimeGenerated())) {

			dsEnvelopeDocLog.setTimeGenerated(LocalDateTime.parse(dsEnvelopeDocLogDefinition.getTimeGenerated()));
		}

		if (null != dsEnvelopeDocLogDefinition.getDocDownloaded()) {

			dsEnvelopeDocLog.setDocDownloaded(dsEnvelopeDocLogDefinition.getDocDownloaded());
		}

		if (!StringUtils.isEmpty(dsEnvelopeDocLogDefinition.getDocDownloadStatus())) {

			dsEnvelopeDocLog.setDocDownloadStatus(dsEnvelopeDocLogDefinition.getDocDownloadStatus());
		}

		if (!StringUtils.isEmpty(dsEnvelopeDocLogDefinition.getDocDownloadDateTime())) {

			dsEnvelopeDocLog
					.setDocDownloadDateTime(LocalDateTime.parse(dsEnvelopeDocLogDefinition.getDocDownloadDateTime()));
		}

		if (!StringUtils.isEmpty(dsEnvelopeDocLogDefinition.getDocDownloadFailureReason())) {

			dsEnvelopeDocLog.setDocDownloadFailureReason(dsEnvelopeDocLogDefinition.getDocDownloadFailureReason());
		}

		return dsEnvelopeDocLog;

	}

	public DSEnvelopeDocLogDefinition transformToDSTabDefinition(DSEnvelopeDocLog dsEnvelopeDocLog) {

		DSEnvelopeDocLogDefinition dsEnvelopeDocLogDefinition = new DSEnvelopeDocLogDefinition();

		dsEnvelopeDocLogDefinition.setId(dsEnvelopeDocLog.getId());
		dsEnvelopeDocLogDefinition.setEnvelopeId(dsEnvelopeDocLog.getEnvelopeId());
		dsEnvelopeDocLogDefinition.setTimeGenerated(dsEnvelopeDocLog.getTimeGenerated().toString());
		dsEnvelopeDocLogDefinition.setDocDownloaded(dsEnvelopeDocLog.getDocDownloaded());
		dsEnvelopeDocLogDefinition.setDocDownloadStatus(dsEnvelopeDocLog.getDocDownloadStatus());

		if (null != dsEnvelopeDocLog.getDocDownloadDateTime()) {

			dsEnvelopeDocLogDefinition.setDocDownloadDateTime(dsEnvelopeDocLog.getDocDownloadDateTime().toString());
		}
		dsEnvelopeDocLogDefinition.setDocDownloadFailureReason(dsEnvelopeDocLog.getDocDownloadFailureReason());

		return dsEnvelopeDocLogDefinition;
	}
}