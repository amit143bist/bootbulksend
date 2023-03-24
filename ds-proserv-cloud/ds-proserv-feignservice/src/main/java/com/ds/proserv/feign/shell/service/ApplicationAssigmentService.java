package com.ds.proserv.feign.shell.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface ApplicationAssigmentService {

	@PutMapping(value = "/docusign/applications/group")
	ResponseEntity<String> assignApplicationToReviewerGroup();

	@PutMapping(value = "/docusign/applications/group/{groupid}")
	ResponseEntity<String> assignApplicationToGroup(@PathVariable(required = false) String groupid);

	@PutMapping(value = "/docusign/applications/drawreference/{drawReference}")
	ResponseEntity<String> updateApplicationDrawStatus(@RequestParam("file") MultipartFile file,
			@PathVariable() String drawReference);

}
