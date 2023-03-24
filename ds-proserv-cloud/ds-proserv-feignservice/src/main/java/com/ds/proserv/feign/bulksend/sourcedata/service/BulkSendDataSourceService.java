package com.ds.proserv.feign.bulksend.sourcedata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.bulksend.sourcedata.domain.BulkSendPrepareDefinition;

public interface BulkSendDataSourceService {

	@PutMapping("/docusign/bulksend/recordids")
	ResponseEntity<BulkSendPrepareDefinition> findBulkSendRecordIds(@RequestBody PageInformation pageInformation);
	
	@PutMapping("/docusign/bulksend/selectedrows")
	ResponseEntity<BulkSendPrepareDefinition> findBulkSendSelectedRows(@RequestBody PageInformation pageInformation);
	
	@PutMapping("/docusign/bulksend/update/selectedrows")
	ResponseEntity<String> updateBulkSendSelectedRows(@RequestBody PageInformation pageInformation);
}