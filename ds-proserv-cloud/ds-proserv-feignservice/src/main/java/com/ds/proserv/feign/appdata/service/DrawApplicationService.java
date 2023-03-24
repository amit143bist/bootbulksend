package com.ds.proserv.feign.appdata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.appdata.domain.DrawApplicationDefinition;
import com.ds.proserv.feign.appdata.domain.DrawApplicationIdResult;
import com.ds.proserv.feign.appdata.domain.DrawApplicationInformation;

public interface DrawApplicationService {

	@PostMapping("/docusign/draw")
	ResponseEntity<DrawApplicationDefinition> saveApplication(
			@RequestBody DrawApplicationDefinition drawApplicationDefinition);

	@PutMapping("/docusign/draw/{applicationId}")
	ResponseEntity<DrawApplicationDefinition> updateApplication(
			@RequestBody DrawApplicationDefinition drawApplicationDefinition, @PathVariable String applicationId);

	@PutMapping("/docusign/draw/bulkupdate/applications")
	ResponseEntity<DrawApplicationInformation> bulkUpdateSaveApplication(
			@RequestBody DrawApplicationInformation drawApplicationInformation);

	@PutMapping("/docusign/draw/bulkupdate/applicationstatus")
	ResponseEntity<String> bulkUpdateSaveApplicationStatus(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/draw/bulkupdate")
	ResponseEntity<String> bulkUpdateApplications(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/draw/bulkupdate/optional")
	ResponseEntity<String> bulkUpdateApplicationsOptional(@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/draw/application/{applicationId}")
	ResponseEntity<DrawApplicationDefinition> findByApplicationId(@PathVariable String applicationId);

	@GetMapping("/docusign/draw/envelope/{envelopeId}")
	ResponseEntity<DrawApplicationDefinition> findByEnvelopeId(@PathVariable String envelopeId);

	@GetMapping("/docusign/draw/application/trigger/{triggerEnvelopeId}")
	ResponseEntity<DrawApplicationDefinition> findByTriggerEnvelopeId(@PathVariable String triggerEnvelopeId);

	@GetMapping("/docusign/draw/application/bridge/{bridgeEnvelopeId}")
	ResponseEntity<DrawApplicationDefinition> findByBridgeEnvelopeId(@PathVariable String bridgeEnvelopeId);

	@GetMapping("/docusign/draw/application/bulkbatch/{bulkBatchId}")
	ResponseEntity<DrawApplicationDefinition> findByBulkBatchId(@PathVariable String bulkBatchId);

	@PutMapping("/docusign/draw/application/applicationids") // List<String> applicationIds
	ResponseEntity<DrawApplicationInformation> findAllByApplicationIdIn(@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/draw/application/applicationstatus/{applicationStatus}/count")
	ResponseEntity<Long> countByApplicationStatus(@PathVariable String applicationStatus);

	@PutMapping("/docusign/draw/application/applicationstatus") // List<String> applicationStatus
	ResponseEntity<DrawApplicationInformation> findAllByApplicationStatuses(
			@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/draw/application/languagecode/{languageCode}/count")
	ResponseEntity<Long> countByLanguageCode(@PathVariable String languageCode);

	@PutMapping("/docusign/draw/application/languagecodes") // String languageCode
	ResponseEntity<DrawApplicationInformation> findAllByLanguageCodes(@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/draw/application/agentcode/{agentCode}/count")
	ResponseEntity<Long> countByAgentCode(@PathVariable String agentCode);

	@PutMapping("/docusign/draw/application/agentcodes") // String agentCode
	ResponseEntity<DrawApplicationInformation> findAllByAgentCodes(@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/draw/application/drawreference/{drawReference}/count")
	ResponseEntity<Long> countByDrawReference(@PathVariable String drawReference);

	@PutMapping("/docusign/draw/application/drawreferences") // String drawReference
	ResponseEntity<DrawApplicationInformation> findAllByDrawReferences(@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/draw/application/programtype/{programType}/count")
	ResponseEntity<Long> countByProgramType(@PathVariable String programType);

	@PutMapping("/docusign/draw/application/programtypes") // String programType
	ResponseEntity<DrawApplicationInformation> findAllByProgramTypes(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/draw/application/applicationids/bystatus") // List<String> applicationIds
	ResponseEntity<DrawApplicationIdResult> findAllDrawApplicationIdsByApplicationStatuses(
			@RequestBody PageInformation pageInformation);
}