package com.ds.proserv.envelopedata.projection;

import com.ds.proserv.envelopedata.model.DSCustomField;
import com.ds.proserv.envelopedata.model.DSEnvelope;
import com.ds.proserv.envelopedata.model.DSRecipient;
import com.ds.proserv.envelopedata.model.DSRecipientAuth;
import com.ds.proserv.envelopedata.model.DSTab;

public interface DSEnvelopeProjection {

	public DSTab getTab();

	public DSEnvelope getEnvelope();

	public DSRecipient getRecipient();

	public DSCustomField getCustomField();

	public DSRecipientAuth getRecipientAuth();
}