package com.ds.proserv.common.exception;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetails {

	@JsonIgnoreProperties(ignoreUnknown = true)
	private String timestamp;

	@JsonIgnoreProperties(ignoreUnknown = true)
	private String message;

	@JsonIgnoreProperties(ignoreUnknown = true)
	private List<String> details;
}