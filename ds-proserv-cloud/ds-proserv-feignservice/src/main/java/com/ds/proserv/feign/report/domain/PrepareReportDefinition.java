package com.ds.proserv.feign.report.domain;

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
@JsonPropertyOrder({ "prepareDataAPIs", "manageDataAPIs", "jobRunArgs" })
public class PrepareReportDefinition implements IDocuSignInformation{

	@JsonProperty("prepareDataAPIs")
	private List<PrepareDataAPI> prepareDataAPIs = null;// Used for PREPAREDATA

	@JsonProperty("manageDataAPIs")
	private List<ManageDataAPI> manageDataAPIs = null;// Used for MANAGEDATA

	@JsonProperty(value = "jobRunArgs")
	private ReportRunArgs jobRunArgs;// Used for both PREPAREDATA and MANAGEDATA

}