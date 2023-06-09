package com.ds.proserv.common.constant;

public enum FailureStep {

	UNMARSHALL_CONNECT_XML,
	ASYNC_CONNECT_PROCESSING,
	PREPARING_CONNECT_DATA,
	SAVING_CONNECT_DATA,
	QUEUE_SAVING_CONNECT_DATA,
	BULK_CONNECT_PROCESS,
	BULK_CONNECT_SAVE,
	BULK_EXCEPTION_SAVE,
	BULK_SEND_RECORDS,
	TRIGGER_BATCH_JOB,
	PREPARE_ROW_DATA,
	PREPARE_API_URI,
	FETCH_TABS,
	FETCH_DATA_BY_ENVELOPEIDS,
	RETRY_LIMIT_EXCEEDED,
	NEW_ENVELOPE_VERSION_EXIST,
	BULK_REVIEWER_REASSIGNMENT,
	MESSAGE_QUEUED,
	CALLING_DS_API,
	COPYING_DB_DATA_TO_CSV,
	SPLITTING_WRITING_TO_CSV,
	SAVING_REPORT_DATA_IN_DB,
	JOINING_ALL_ACCOUNT_FUTURE,
	JOINING_ALL_PAGES_ACCOUNT_FUTURE,
	EVALUATE_JS_FUNCTION_EXPRESSION,
	OUTER_JSON_FILTER_PROCESSING,
	PROCESSROWDATAASYNC,
	PROCESSROWDATAFORASYNC,
	PROCESSENVDATAFORWRITINGARCHIVE,
	PROCESSENVDATAFORWRITINGNONARCHIVE,
	OUTER_JSON_FILTER_PROCESSING_SUPPLYSYNC,
	PROCESSDSAPICALLERROREXCEPTION,
	BULKSEND_MONITOR,
	ASYNC_BULKSEND_PROCESSING,
	CREATESENVELOPE,
	CREATEROLETABWHILEPREPARINGENVELOPE,
	ASYNC_BULKSEND_PULLRECORDDATA,
	ASYNC_BULKSEND_PREPAREPATHLIST,
	ASYNC_BULKSEND_DSAPI,
    UPDATEENVELOPEEXCEPTION,
    THROTTLESERVICE;
}