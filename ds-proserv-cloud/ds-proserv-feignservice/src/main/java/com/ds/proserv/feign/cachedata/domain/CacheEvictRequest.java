package com.ds.proserv.feign.cachedata.domain;

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
@JsonPropertyOrder({ "cacheKey", "cacheReference" })
public class CacheEvictRequest implements IDocuSignInformation {

	@JsonProperty("cacheKey")
	private String cacheKey;

	@JsonProperty("cacheReference")
	private String cacheReference;
}