package com.ds.proserv.report.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ds.proserv.feign.report.domain.BatchResultInformation;
import com.ds.proserv.feign.report.domain.BatchResultResponse;
import com.ds.proserv.feign.report.domain.PrepareDataAPI;
import com.ds.proserv.feign.report.domain.PrepareReportDefinition;
import com.ds.proserv.feign.util.ReportAppUtil;
import com.ds.proserv.report.prepare.factory.PrepareDataFactory;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PrepareReportDataProcessor {

	@Autowired
	PrepareDataFactory prepareDataFactory;

	public ResponseEntity<BatchResultResponse> callAPIWithInputParams(PrepareReportDefinition prepareReportDefinition,
			HttpServletResponse response, Map<String, String> headers) {

		List<BatchResultInformation> batchResultInformationList = new ArrayList<BatchResultInformation>();
		for (PrepareDataAPI prepareAPI : prepareReportDefinition.getPrepareDataAPIs()) {

			printRunModeByBatchType(headers, prepareAPI.getApiRunArgs().getBatchType());

			prepareDataFactory.prepareData(ReportAppUtil.getAPICategoryType(prepareAPI.getApiCategory()))
					.map(prepareDataService -> {

						return batchResultInformationList.add(prepareDataService.startPrepareDataProcess(prepareAPI));
					});

		}

		BatchResultResponse batchResultResponse = new BatchResultResponse();
		batchResultResponse.setBatchResultInformations(batchResultInformationList);

		return new ResponseEntity<BatchResultResponse>(batchResultResponse, HttpStatus.CREATED);

	}

	public void printRunModeByBatchType(Map<String, String> headers, String batchType) {

		if (null != headers && null != headers.get("accept")) {

			log.info(
					" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
			log.info(" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ RUNNING " + batchType
					+ " IN ONLINE MODE $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
			log.info(
					" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
		} else {

			log.info(
					" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
			log.info(" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ RUNNING " + batchType
					+ " IN BATCH MODE $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
			log.info(
					" $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ $@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@$@ ");
		}
	}

}