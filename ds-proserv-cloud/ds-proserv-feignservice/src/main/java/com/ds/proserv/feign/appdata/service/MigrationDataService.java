package com.ds.proserv.feign.appdata.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.appdata.domain.MigrationDataDefinition;
import com.ds.proserv.feign.appdata.domain.MigrationReportDataDefinition;
import com.ds.proserv.feign.appdata.domain.MigrationReportDataInformation;
import com.ds.proserv.feign.appdata.domain.MigrationReportDataRequest;
import com.ds.proserv.feign.appdata.domain.MigrationReportDataResponse;

public interface MigrationDataService {

	@PostMapping("/docusign/migration/bulkupdate/data")
	ResponseEntity<String> bulkUpdateSaveMigrationData(@RequestBody MigrationDataDefinition migrationDataDefinition);

	@GetMapping("/docusign/migration/count/data/tablename/{tableName}")
	ResponseEntity<Long> countTotalRecords(@PathVariable String tableName);

	@PutMapping("/docusign/migration/dynamic/query/tablename/{tableName}")
	ResponseEntity<List<Map<String, Object>>> runOnDemandQuery(@PathVariable String tableName,
			@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/migration/dynamic/headerlist/tablename/{tableName}")
	ResponseEntity<MigrationReportDataResponse> createHeaderList(@PathVariable String tableName,
			@RequestBody MigrationReportDataRequest migrationReportDataRequest);

	@PutMapping("/docusign/migration/dynamic/reportdata")
	ResponseEntity<MigrationReportDataInformation> readReportData(
			@RequestBody MigrationReportDataDefinition migrationReportDataDefinition);
}