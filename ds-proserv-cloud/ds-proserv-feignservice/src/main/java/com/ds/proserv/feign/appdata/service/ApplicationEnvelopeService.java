package com.ds.proserv.feign.appdata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.appdata.domain.ApplicationEnvelopeDefinition;
import com.ds.proserv.feign.appdata.domain.ApplicationEnvelopeInformation;

public interface ApplicationEnvelopeService {

	@PostMapping("/docusign/app")
	ResponseEntity<ApplicationEnvelopeDefinition> save(
			@RequestBody ApplicationEnvelopeDefinition applicationEnvelopeDefinition);

	@PutMapping("/docusign/app/bulksave/applications")
	ResponseEntity<ApplicationEnvelopeInformation> bulkSave(
			@RequestBody ApplicationEnvelopeInformation applicationEnvelopeInformation);

	@GetMapping("/docusign/app/count/applicationid/{applicationId}")
	ResponseEntity<Long> countByApplicationId(@PathVariable String applicationId);

	@GetMapping("/docusign/app/count/applicationtype/{applicationType}")
	ResponseEntity<Long> countByApplicationType(@PathVariable String applicationType);

	@GetMapping("/docusign/app/count/failed/applicationtype/{applicationType}")
	ResponseEntity<Long> countByApplicationTypeAndEnvelopeIdIsNull(@PathVariable String applicationType);

	@PutMapping("/docusign/app/failed/applications/applicationtype/{applicationType}")
	ResponseEntity<ApplicationEnvelopeInformation> findAllByApplicationTypeAndEnvelopeIdIsNull(
			@PathVariable String applicationType, @RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/app/applications/applicationids")
	ResponseEntity<ApplicationEnvelopeInformation> findByApplicationIdIn(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/app/applications/envelopeids")
	ResponseEntity<ApplicationEnvelopeInformation> findByEnvelopeIdIn(@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/app/applications/recipientemails/{recipientEmail}")
	ResponseEntity<ApplicationEnvelopeInformation> findByRecipientEmailsContainingIgnoreCase(
			@PathVariable String recipientEmail);

	@GetMapping("/docusign/app/count/envelopes/fromdate/{fromDate}/todate/{toDate}/applicationtype/{applicationType}")
	ResponseEntity<Long> countByEnvelopeSentTimestampBetweenAndApplicationType(@PathVariable String fromDate,
			@PathVariable String toDate, @PathVariable String applicationType);

	@PutMapping("/docusign/app/applications/envelopeids/daterange/applicationtype")
	ResponseEntity<ApplicationEnvelopeInformation> findAllByEnvelopeSentTimestampBetweenAndApplicationType(
			@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/app/count/failed/fromdate/{fromDate}/todate/{toDate}/applicationtype/{applicationType}")
	ResponseEntity<Long> countByFailureTimestampBetweenAndApplicationType(@PathVariable String fromDate,
			@PathVariable String toDate, @PathVariable String applicationType);

	@PutMapping("/docusign/app/failed/applications/daterange/applicationtype")
	ResponseEntity<ApplicationEnvelopeInformation> findAllByFailureTimestampBetweenAndApplicationType(
			@RequestBody PageInformation pageInformation);
}