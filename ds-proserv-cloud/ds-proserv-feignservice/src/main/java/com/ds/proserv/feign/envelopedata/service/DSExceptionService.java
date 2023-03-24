package com.ds.proserv.feign.envelopedata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionIdResult;
import com.ds.proserv.feign.envelopedata.domain.DSExceptionInformation;

public interface DSExceptionService {

	@PostMapping("/docusign/envelopedata/exception")
	ResponseEntity<DSExceptionDefinition> saveExceptionData(@RequestBody DSExceptionDefinition dsExceptionDefinition);

	@PostMapping("/docusign/envelopedata/exception/bulk")
	ResponseEntity<String> bulkSaveExceptionData(@RequestBody DSExceptionInformation dsExceptionInformation);

	@PutMapping("/docusign/envelopedata/exception/{id}")
	ResponseEntity<DSExceptionDefinition> updateExceptionData(@RequestBody DSExceptionDefinition dsExceptionDefinition,
			@PathVariable String id);

	@PutMapping("/docusign/envelopedata/exception/bulk/retrystatus")
	ResponseEntity<String> updateExceptionRetryStatus(@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/envelopedata/exception/{exceptionId}")
	ResponseEntity<DSExceptionDefinition> findExceptionById(String exceptionId);

	@PutMapping("/docusign/envelopedata/exception/ids")
	ResponseEntity<DSExceptionInformation> findExceptionByIds(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/envelopedata/exceptions/envelopeids/count")
	ResponseEntity<Long> countAllExceptionsByEnvelopeIds(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/envelopedata/exceptions/envelopeids")
	ResponseEntity<DSExceptionInformation> findAllExceptionsByEnvelopeIds(@RequestBody PageInformation pageInformation);

	@GetMapping("/docusign/envelopedata/exceptions/{envelopeId}")
	ResponseEntity<DSExceptionInformation> findExceptionsByEnvelopeId(@PathVariable String envelopeId);

	@PutMapping("/docusign/envelopedata/exceptions/daterange/count")
	ResponseEntity<Long> countAllExceptionsByDateRange(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/envelopedata/exceptions/daterange")
	ResponseEntity<DSExceptionInformation> findAllExceptionsByDateRange(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/envelopedata/exceptions/retrystatuses/count")
	ResponseEntity<Long> countByRetryStatusIn(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/envelopedata/exceptions/retrystatuses")
	ResponseEntity<DSExceptionInformation> findAllExceptionsByRetryStatuses(
			@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/envelopedata/exceptions/retrystatusesallvalues/count")
	ResponseEntity<Long> countByRetryStatusOrNullRetryStatus(@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/envelopedata/exceptions/retrystatusesallvalues")
	ResponseEntity<DSExceptionInformation> findAllExceptionsByRetryStatusesOrNullRetryStatus(
			@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/envelopedata/exceptions/exceptionids/byretrystatusesallvalues") // List<String> retrystatuses
	ResponseEntity<DSExceptionIdResult> findAllExceptionIdsByRetryStatuses(
			@RequestBody PageInformation pageInformation);

	@PutMapping("/docusign/envelopedata/exceptions/exceptionids/byretrystatusesincludingnull") // List<String>
																								// retrystatuses
	ResponseEntity<DSExceptionIdResult> findAllExceptionIdsByRetryStatusesOrNullRetryStatus(
			@RequestBody PageInformation pageInformation);

}