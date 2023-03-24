package com.ds.proserv.feign.report.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.feign.report.domain.ConcurrentReportDataMessageDefinition;

public interface CoreReportDataService {

	@PostMapping("docusign/report/savereportdata")
	ResponseEntity<String> saveReportData(
			@RequestBody ConcurrentReportDataMessageDefinition concurrentReportDataMessageDefinition);
}