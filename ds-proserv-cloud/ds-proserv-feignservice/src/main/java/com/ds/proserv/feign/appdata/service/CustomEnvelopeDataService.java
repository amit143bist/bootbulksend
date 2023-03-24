package com.ds.proserv.feign.appdata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataCountBucketNameInformation;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataCountDateInformation;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataDefinition;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataIdRequest;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataIdResponse;
import com.ds.proserv.feign.appdata.domain.CustomEnvelopeDataInformation;

public interface CustomEnvelopeDataService {

	@PutMapping("/docusign/customdata/envelope/pendingdocdownloadids")
	ResponseEntity<CustomEnvelopeDataIdResponse> findPendingDocDownloadEnvelopesByEnvelopeIds(
			@RequestBody CustomEnvelopeDataIdRequest customEnvelopeDataIdRequest);

	@PostMapping("/docusign/customdata/envelope")
	ResponseEntity<CustomEnvelopeDataDefinition> saveEnvelopeData(
			@RequestBody CustomEnvelopeDataDefinition customEnvelopeDataDefinition);

	@PostMapping("/docusign/customdata/envelope/bulksave")
	ResponseEntity<CustomEnvelopeDataInformation> bulkSaveEnvelopeData(
			@RequestBody CustomEnvelopeDataInformation customEnvelopeDataInformation);

	@PutMapping("/docusign/customdata/envelope")
	ResponseEntity<CustomEnvelopeDataDefinition> updateEnvelopeData(
			@RequestBody CustomEnvelopeDataDefinition customEnvelopeDataDefinition);

	@PutMapping("/docusign/customdata/envelope/processtart")
	ResponseEntity<String> updateCustomEnvelopeDataProcessStatusStartTime(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/customdata/envelope/processend")
	ResponseEntity<String> updateCustomEnvelopeDataProcessStatusEndTime(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/customdata/envelope/docdownload")
	ResponseEntity<String> updateCustomEnvelopeDataDocDownloadStatusEndTime(
			@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/customdata/fromdate/{fromDate}/todate/{toDate}/status/{status}/count/{count}/pagenumber/{pageNumber}")
	ResponseEntity<CustomEnvelopeDataInformation> findEnvelopesByDateRange(@PathVariable String fromDate,
			@PathVariable String toDate, @PathVariable String status, @PathVariable Integer count,
			@PathVariable Integer pageNumber);

	@PutMapping("/docusign/customdata/fromdate/{fromDate}/todate/{toDate}/status/{status}/count/{count}/pagenumber/{pageNumber}")
	ResponseEntity<CustomEnvelopeDataInformation> findAndUpdateEnvelopesByDateRange(@PathVariable String fromDate,
			@PathVariable String toDate, @PathVariable String status, @PathVariable Integer count,
			@PathVariable Integer pageNumber);

	@GetMapping("/docusign/customdata/senderidentifier/{senderIdentifier}/fromdate/{fromDate}/todate/{toDate}/status/{status}/count/{count}/pagenumber/{pageNumber}")
	ResponseEntity<CustomEnvelopeDataInformation> findEnvelopesBySenderIdentifierAndDateRange(
			@PathVariable String senderIdentifier, @PathVariable String fromDate, @PathVariable String toDate,
			@PathVariable String status, @PathVariable Integer count, @PathVariable Integer pageNumber);

	@PutMapping("/docusign/customdata/senderidentifier/{senderIdentifier}/fromdate/{fromDate}/todate/{toDate}/status/{status}/count/{count}/pagenumber/{pageNumber}")
	ResponseEntity<CustomEnvelopeDataInformation> findAndUpdateEnvelopesBySenderIdentifierAndDateRange(
			@PathVariable String senderIdentifier, @PathVariable String fromDate, @PathVariable String toDate,
			@PathVariable String status, @PathVariable Integer count, @PathVariable Integer pageNumber);

	@GetMapping("/docusign/customdata/envelope/docdownload/count/bydate")
	ResponseEntity<CustomEnvelopeDataCountDateInformation> findAllDownloadedEnvelopesCountByDate();

	@PutMapping("/docusign/customdata/envelope/docdownload/bybucketname")
	ResponseEntity<String> updateCustomEnvelopeDataDocDownloadStatusEndTimeWithBucketName(
			@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/customdata/envelope/docdownload/count/bybucketname/{bucketName}")
	ResponseEntity<CustomEnvelopeDataCountBucketNameInformation> findAllDownloadedEnvelopesCountByBucketName(
			@PathVariable String bucketName);

	@PutMapping("/docusign/customdata/envelope/docdownload/findall/bybucketname")
	ResponseEntity<CustomEnvelopeDataIdResponse> findAllByDownloadBucketName(
			@RequestBody PageInformation pageInformation);
}