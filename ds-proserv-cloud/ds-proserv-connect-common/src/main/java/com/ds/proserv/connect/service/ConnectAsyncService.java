package com.ds.proserv.connect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.feign.connect.domain.ConnectMessageDefinition;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConnectAsyncService {

	@Autowired
	private ConnectProcessorService connectProcessorService;

	public void saveEnvelopeData(ConnectMessageDefinition connectMessageDefinition) {

		String connectXML = connectMessageDefinition.getConnectXML();

		if (!StringUtils.isEmpty(connectXML)) {

			String envelopeId = DSUtil.getEnvelopeId(connectXML);

			log.info("Calling validateAndSaveEnvelopeData for envelopeId -> {}", envelopeId);
			connectProcessorService.validateAndSaveEnvelopeData(envelopeId, connectXML,
					connectMessageDefinition.getProcessId(), connectMessageDefinition.getBatchId());
		} else {

			log.info("Calling bulkSaveByIds for processId -> {}", connectMessageDefinition.getProcessId());
			connectProcessorService.bulkSaveByIds(connectMessageDefinition);
		}

	}
}