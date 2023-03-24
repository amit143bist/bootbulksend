package com.ds.proserv.report.data.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.common.constant.ValidationResult;
import com.ds.proserv.feign.report.domain.PrepareReportDefinition;
import com.ds.proserv.feign.report.service.CoreManageReportDataService;
import com.ds.proserv.report.processor.ManageReportDataProcessor;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CoreManageReportDataController implements CoreManageReportDataService {

	@Autowired
	private ManageReportDataProcessor manageReportDataProcessor;

	@PostMapping("docusign/report/managereportdata/table")
	public ResponseEntity<String> manageReportData(@RequestBody PrepareReportDefinition prepareReportDefinition,
			HttpServletResponse response, @RequestHeader Map<String, String> headers) throws Exception {

		log.info("Calling manageReportData");
		manageReportDataProcessor.callAPIWithInputParams(prepareReportDefinition, response, headers);

		return new ResponseEntity<String>(ValidationResult.QUEUED.toString(), HttpStatus.ACCEPTED);
	}
}