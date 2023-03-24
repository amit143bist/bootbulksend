package com.ds.proserv.feign.appdata.domain;

import java.util.Map;

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
@JsonPropertyOrder({ "csvHeaderMap" })
public class MigrationReportDataResponse implements IDocuSignInformation {

	@JsonProperty("csvHeaderMap")
	private Map<String, String> csvHeaderMap;
}