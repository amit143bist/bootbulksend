package com.ds.proserv.feign.coredata.domain;

import java.util.List;

import com.ds.proserv.feign.domain.IDocuSignInformation;
import com.ds.proserv.feign.report.domain.ReportData;
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
@JsonPropertyOrder({ "processId", "batchId", "processStartDateTime", "processEndDateTime", "processStatus",
		"totalRecordsInProcess", "groupId", "accountId", "userId", "reportRowsList" })
public class ConcurrentProcessLogDefinition implements IDocuSignInformation {

	@JsonProperty("processId")
	private String processId;
	@JsonProperty("batchId")
	private String batchId;
	@JsonProperty("processStartDateTime")
	private String processStartDateTime;
	@JsonProperty("processEndDateTime")
	private String processEndDateTime;
	@JsonProperty("processStatus")
	private String processStatus;
	@JsonProperty("totalRecordsInProcess")
	private Long totalRecordsInProcess;
	@JsonProperty("groupId")
	private String groupId;
	@JsonProperty("accountId")
	private String accountId;
	@JsonProperty("userId")
	private String userId;
	@JsonProperty("reportRowsList")
	private List<List<ReportData>> reportRowsList;

}