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
public class EnvelopeBatchTestItem extends AbstractEnvelopeItem {

	private Boolean canBeSent;
	private List<String> validationErrors = null;
	private List<String> validationErrorDetails = null;

}