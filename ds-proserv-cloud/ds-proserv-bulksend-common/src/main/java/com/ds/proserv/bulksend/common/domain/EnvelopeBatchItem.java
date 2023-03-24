package com.ds.proserv.bulksend.common.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class EnvelopeBatchItem extends AbstractEnvelopeItem {

	private String batchId;
	private String listId;
	private String batchName;
	private String totalSent;
	private String batchSize;
	private String queueLimit;
	private String totalQueued;
	private String totalFailed;
	private String submittedDateTime;
	private String envelopeOrTemplateId;
	private List<String[]> rowDataList;
	private List<String> errors = null;
	private List<String> errorDetails = null;
	private String commaSeparatedRecordIds = null;
	private Long totalRecordIdsProcessed = null;
}