package com.ds.proserv.feign.connect.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

public interface ConnectService {

	@PostMapping(value = "/connect/notification")
	public @ResponseBody ResponseEntity<String> postConnect(@RequestBody String connectXML);
}