package com.ds.proserv.report.data.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.feign.report.domain.ConcurrentReportDataMessageDefinition;
import com.ds.proserv.feign.report.service.CoreReportDataService;
import com.ds.proserv.report.db.service.ReportJDBCService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CoreReportDataController implements CoreReportDataService {

	@Lazy
	@Autowired
	private ReportJDBCService reportJDBCService;

	@Override
	public ResponseEntity<String> saveReportData(
			ConcurrentReportDataMessageDefinition concurrentReportDataMessageDefinition) {

		log.info("Calling saveReportData for batchId -> {} and processId -> {}",
				concurrentReportDataMessageDefinition.getBatchId(),
				concurrentReportDataMessageDefinition.getProcessId());

		return new ResponseEntity<String>(
				reportJDBCService.saveReportData(concurrentReportDataMessageDefinition.getReportRowsList(),
						concurrentReportDataMessageDefinition.getTableColumnMetaData(),
						concurrentReportDataMessageDefinition.getAccountId(),
						concurrentReportDataMessageDefinition.getBatchId(),
						concurrentReportDataMessageDefinition.getProcessId(),
						concurrentReportDataMessageDefinition.getNextUri(),
						concurrentReportDataMessageDefinition.getPrimaryId()),
				HttpStatus.OK);

	}

}