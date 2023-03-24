package com.ds.proserv.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum FailureCode {

	ERROR_101("InterruptedException"),
	ERROR_102("ExecutionException"),
	ERROR_103("NoSuchMethodException"),
	ERROR_104("ScriptException"),
	ERROR_105("SQLException"),
	ERROR_106("HttpClientErrorException"),
	ERROR_107("UnknownException"),
	ERROR_108("UserDoesNotExistException"),
	ERROR_109("IOException"),
	ERROR_112("BulkSendException"),
	ERROR_201("ConnectSaveException"),
	ERROR_202("JAXBException"),
	ERROR_203("InvalidMessageException"),
	ERROR_204("InvalidProgramTypeException"),
	ERROR_205("AgentCodeMissingException"),
	ERROR_206("URLConnectionException"),
	ERROR_207("MissingSignatureDetailsException"),
	ERROR_208("FetchingTabException"),
	ERROR_209("EmptyTabException"),
	ERROR_210("PrepareRowDataException"),
	ERROR_211("BulkSendException"),
	ERROR_212("ReviewerAssigmentException"),
	ERROR_213("MessageQueued"),
	ERROR_214("ConnectPrepareException"),
	ERROR_215("NewVersionExistException"),
	ERROR_216("FailedToQueueinMB"),
	ERROR_220("EmptyBulkSendRecordData"),
	ERROR_221("EmptyBulkSendPathList"),
	ERROR_222("BulkSendDSAPIError"),
    ERROR_223("UpdateEnvelopeException"),
    ERROR_224("CreateEnvelopeException"),
    ERROR_225("ThrottleException");


    @Getter
	private String failureCodeDescription;
}