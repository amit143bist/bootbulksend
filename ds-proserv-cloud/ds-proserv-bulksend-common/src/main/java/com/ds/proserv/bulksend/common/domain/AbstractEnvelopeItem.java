package com.ds.proserv.bulksend.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractEnvelopeItem {

	private String envelopeId;

	private Boolean success;

	private String transMessage;

	private String rateLimitReset;

	private String rateLimitLimit;

	private String rateLimitRemaining;

	private String burstLimitRemaining;

	private String burstLimitLimit;

	private String docuSignTraceToken;

	private Integer httpStatusCode;

}