package com.ds.proserv.report.data.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.feign.report.domain.BatchResultResponse;
import com.ds.proserv.feign.report.domain.PrepareReportDefinition;
import com.ds.proserv.feign.report.service.CorePrepareReportDataService;
import com.ds.proserv.report.processor.PrepareReportDataProcessor;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CorePrepareReportDataController implements CorePrepareReportDataService{

	@Autowired
	private PrepareReportDataProcessor prepareReportDataProcessor;

	@PostMapping("docusign/report/preparereportdata")
	public ResponseEntity<BatchResultResponse> prepareReportData(
			@RequestBody PrepareReportDefinition prepareReportDefinition, HttpServletResponse response,
			@RequestHeader Map<String, String> headers) {

		log.info("Calling prepareReportData");
		return prepareReportDataProcessor.callAPIWithInputParams(prepareReportDefinition, response, headers);

	}

}