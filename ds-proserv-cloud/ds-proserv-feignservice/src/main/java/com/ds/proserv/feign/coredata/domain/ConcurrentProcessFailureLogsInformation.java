package com.ds.proserv.feign.coredata.domain;

import java.math.BigInteger;
import java.util.List;

import com.ds.proserv.feign.domain.IDocuSignInformation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "totalFailureCount", "concurrentProcessFailureLogDefinitions" })
public class ConcurrentProcessFailureLogsInformation implements IDocuSignInformation {

	@JsonProperty("totalFailureCount")
	private BigInteger totalFailureCount = null;
	@JsonProperty("concurrentProcessFailureLogDefinitions")
	private List<ConcurrentProcessFailureLogDefinition> concurrentProcessFailureLogDefinitions = null;

}