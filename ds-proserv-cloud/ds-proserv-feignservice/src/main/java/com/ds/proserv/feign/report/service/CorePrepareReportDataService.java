package com.ds.proserv.feign.report.service;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.ds.proserv.feign.report.domain.BatchResultResponse;
import com.ds.proserv.feign.report.domain.PrepareReportDefinition;

public interface CorePrepareReportDataService {

	@PostMapping("docusign/report/preparereportdata")
	ResponseEntity<BatchResultResponse> prepareReportData(
			@RequestBody PrepareReportDefinition prepareReportDefinition, HttpServletResponse response,
			@RequestHeader Map<String, String> headers);
}