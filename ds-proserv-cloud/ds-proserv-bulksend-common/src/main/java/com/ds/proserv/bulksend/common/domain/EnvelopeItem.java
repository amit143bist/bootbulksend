package com.ds.proserv.bulksend.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class EnvelopeItem extends AbstractEnvelopeItem {

	private String uri;

	private String status;

	private String rowData;

	private String statusDateTime;

}