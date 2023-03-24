package com.ds.proserv.common.constant;

public enum ApplicationStatus {

	APP_INITIATOR_FINISHED, // When PF envelope is completed
	APP_BULKSUBMITTED, // When BulkSend is done
	APP_BULKFAILED, // When BulkSend failed
	APP_BRIDGE_FINISHED, // When BulkSend envelope's first recipient completed
	APP_DRAW_SELECTED, // When 25K draw is selected
	APP_DRAW_REJECTED, // Draw Rejected - Might be manual
	APP_DRAW_DUPLICATE, // Duplicate Flag
	APP_REVIEWER_SUBMITTED, // When App sent to Reviewer by Shell code
	APP_REVIEWER_APPROVED, // When App rejected by Reviewer
	APP_REVIEWER_DENIED, // When App declined by Reviewer
	APP_INITIATOR_VOIDED, // When Envelope Voided
	APP_INITIATOR_EXPIRED, // When Envelope Expired
	APP_INITIATOR_AUTORESPONDED, // When Initiator Envelope AutoResponded
	APP_INITIATOR_DECLINED, // When Envelope declined by Initiator
	APP_BRIDGE_DECLINED, // When Bridge Envelope declined by Initiator
	APP_BRIDGE_VOIDED, // When Bridge Envelope Voided
	APP_BRIDGE_EXPIRED, // When Bridge Envelope Expired
	APP_BRIDGE_AUTORESPONDED, // When Bridge Envelope AutoResponded
	APP_INITIATOR_UNKNOWN, // When Initiator Envelope Unknown
	APP_BRIDGE_UNKNOWN, // When Bridge Envelope Unknown
	ENVELOPE_UNKNOWN;
}