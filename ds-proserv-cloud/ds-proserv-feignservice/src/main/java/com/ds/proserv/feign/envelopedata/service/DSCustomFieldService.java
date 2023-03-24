package com.ds.proserv.feign.envelopedata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldInformation;

public interface DSCustomFieldService {

	@PutMapping("/docusign/envelopedata/customfields/byenvelopeids")
	ResponseEntity<DSCustomFieldInformation> findCustomFieldsByEnvelopeIdsAndFieldName(@RequestBody PageInformation pageInformation);
}