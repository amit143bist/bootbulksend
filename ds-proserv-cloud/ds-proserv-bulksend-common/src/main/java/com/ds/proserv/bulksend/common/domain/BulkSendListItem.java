package com.ds.proserv.bulksend.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BulkSendListItem extends AbstractEnvelopeItem {

	private String name;
	private String listId;

}