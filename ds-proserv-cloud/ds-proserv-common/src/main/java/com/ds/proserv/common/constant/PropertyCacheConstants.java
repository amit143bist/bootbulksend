package com.ds.proserv.common.constant;

public interface PropertyCacheConstants {

	// This group name for common across microservices
	String PROPERTY_CACHE_NAME = "propertycache";
	String CSV_COLUMN_HEADER_REFERENCE = "csvcolumnheaders";
	String DBSERVICE_REFERENCE_NAME = "dbservicecreds";
	String QUEUE_REFERENCE_NAME = "queuenames";
	String TOKEN_PROP_REFERENCE_NAME = "tokenproperties";
	String CONNECT_REFERENCE_NAME = "connectproperties";
	String DS_API_REFERENCE_NAME = "dsapiproperties";
	String DS_ASYNC_JOB_REFERENCE_NAME = "dsasyncjobproperties";
	String BULKSEND_REFERENCE_NAME = "bulksendproperties";
	String SEND_REFERENCE_NAME = "directsendproperties";

	String KNOWN_ISSUE_DESC = "proserv.application.dupissuedesc";

	String PROCESS_START_BYQUEUE = "proserv.process.startbyqueue";
	String PROCESS_FAILURE_BYQUEUE = "proserv.process.failurebyqueue";
	String PROCESS_REPORTDATA_BYQUEUE = "proserv.process.reportdatabyqueue";
	String PROCESS_DOCDOWNLOAD_BYQUEUE = "proserv.process.docdownloadbyqueue";
	String PROCESS_CONNECT_BYQUEUE = "proserv.process.connectbyqueue";
	String PROCESS_BULKSEND_BYQUEUE = "proserv.process.bulksendbyqueue";
	String PROCESS_EXCEPTION_BYQUEUE = "proserv.process.exceptionbyqueue";

	String PROCESS_DOCDOWNLOAD_PERQUEUE_LIMIT = "proserv.docdownload.perqueue.limit";

	String PROCESS_START_QUEUE_NAME = "proserv.processstart.queuename";
	String PROCESS_FAILURE_QUEUE_NAME = "proserv.processfailure.queuename";
	String PROCESS_COMPLETE_QUEUE_NAME = "proserv.processcomplete.queuename";
	String PROCESS_REPORTDATA_QUEUE_NAME = "proserv.reportdata.queuename";
	String PROCESS_REPORTCOMPLETE_QUEUE_NAME = "proserv.reportcomplete.queuename";
	String PROCESS_CACHEEVICT_QUEUE_NAME = "proserv.cacheevict.queuename";
	String PROCESS_DOCDOWNLOAD_QUEUE_NAME = "proserv.docdownload.queuename";
	String PROCESS_CONNECT_QUEUE_NAME = "proserv.connect.queuename";
	String PROCESS_BULKSEND_QUEUE_NAME = "proserv.bulksend.queuename";
	String PROCESS_EXCEPTION_QUEUE_NAME = "proserv.exception.queuename";
	String PROCESS_DOCMIGRATION_QUEUE_NAME = "proserv.docmigration.queuename";
	String PROCESS_BULKSENDENVELOPELOG_QUEUE_NAME = "proserv.bulksendenvelopelog.queuename";
	String PROCESS_ENVELOPEAPI_QUEUE_NAME = "proserv.envelopeapi.queuename";
	String PROCESS_APPLICATIONENVELOPE_QUEUE_NAME = "proserv.applicationenvelope.queuename";
	String PROCESS_ENVELOPEUPDATEAPI_QUEUE_NAME = "proserv.envelopeupdateapi.queuename";
	String PROCESS_BULKSENDPROCESSFAILURE_QUEUE_NAME = "proserv.bulksendprocessfailure.queuename";
	String PROCESS_BULKSENDRECORDLOG_QUEUE_NAME = "proserv.bulksendrecordlog.queuename";

	String PROCESS_CONNECT_QUEUE_RETRYLIMIT = "proserv.connect.queue.failure.retrylimit";
	String PROCESS_BULKSEND_QUEUE_RETRYLIMIT = "proserv.bulksend.queue.failure.retrylimit";
	String PROCESS_EXCEPTION_QUEUE_RETRYLIMIT = "proserv.exception.queue.failure.retrylimit";

	String PROCESS_START_QUEUE_RETRYLIMIT = "proserv.processstart.queue.failure.retrylimit";
	String PROCESS_FAILURE_QUEUE_RETRYLIMIT = "proserv.processfailure.queue.failure.retrylimit";
	String PROCESS_COMPLETE_QUEUE_RETRYLIMIT = "proserv.processcomplete.queue.failure.retrylimit";
	String PROCESS_REPORTDATA_QUEUE_RETRYLIMIT = "proserv.reportdata.queue.failure.retrylimit";
	String PROCESS_CACHEEVICT_QUEUE_RETRYLIMIT = "proserv.cacheevict.queue.failure.retrylimit";
	String PROCESS_REPORTCOMPLETE_QUEUE_RETRYLIMIT = "proserv.reportcomplete.queue.failure.retrylimit";
	String PROCESS_DOCDOWNLOAD_QUEUE_RETRYLIMIT = "proserv.docdownload.queue.failure.retrylimit";
	String PROCESS_DOCMIGRATION_QUEUE_RETRYLIMIT = "proserv.docmigration.queue.failure.retrylimit";
	String PROCESS_BULKSENDENVELOPELOG_QUEUE_RETRYLIMIT = "proserv.bulksendenvelopelog.queue.failure.retrylimit";
	String PROCESS_ENVELOPEAPI_QUEUE_RETRYLIMIT = "proserv.envelopeapi.queue.failure.retrylimit";
	String PROCESS_ENVELOPEUPDATEAPI_QUEUE_RETRYLIMIT = "proserv.envelopeupdateapi.queue.failure.retrylimit";
	String PROCESS_APPLICATIONENVELOPE_QUEUE_RETRYLIMIT = "proserv.applicationenvelope.queue.failure.retrylimit";
	String PROCESS_BULKSENDPROCESSFAILURE_QUEUE_RETRYLIMIT = "proserv.bulksendprocessfailure.queue.failure.retrylimit";
	String PROCESS_BULKSENDRECORDLOG_QUEUE_RETRYLIMIT = "proserv.bulksendrecordlog.queue.failure.retrylimit";

	String PROCESS_REPORTDATA_QUEUE_PUSH_TO_COMPLETE_QUEUE = "proserv.reportdata.queue.push.completequeue";

	String CORE_POOL_SIZE_CONST = "app.async.corepoolsize";
	String MAX_POOL_SIZE_CONST = "app.async.maxpoolsize";
	String APP_QUEUE_CAPACITY_CONST = "app.async.queuecapacity";
	String APP_EXECUTOR_NAME_CONST = "app.async.executornameprefix";

	String APP_DB_AUDITOR_NAME = "app.db.auditorname";

	String CONNECT_APP_USERNAME = "connect.application.username";
	String CONNECT_APP_PASSWORD = "connect.application.password";

	String PROSERV_APP_USERNAME = "proserv.application.username";
	String PROSERV_APP_PASSWORD = "proserv.application.password";

	String PROSERV_QUEUE_NAME = "proserv.queue.name";
	String PROSERV_ROUTING_KEY = "proserv.routing.key";
	String PROSERV_EXCHANGE_NAME = "proserv.exchange.name";
	String PROSERV_QUEUE_TTL = "proserv.queue.ttl";

	String DS_COMMON_CACHE_EXPIRY_SECS = "ds.propertycache.cacheexpirationseconds";

	String DSAUTH_TESTTOKEN = "app.authorization.token.testtoken";
	String DSAUTH_AUD = "app.authorization.aud";
	String DSAUTH_USERID = "app.authorization.userId";
	String DSAUTH_SCOPES = "app.authorization.scopes";
	String DSAUTH_INTEGRATORKEY = "app.authorization.integratorKey";
	String DSAUTH_EXPIRY_SECONDS = "app.authorization.token.expirationSeconds";
	String DSAUTH_CACHE_EXPIRY_SECS = "app.authorization.token.cacheExpirationSeconds";
	String DSAUTH_RSA_PRIVATEKEY_PATH = "app.authorization.rsaPrivateKeyPath";
	String DSAUTH_RSA_PUBLICKEY_PATH = "app.authorization.rsaPublicKeyPath";

	String REPORT_RULEENGINE_FILEPATH = "app.ruleEngineJsonFilePath";
	String REPORT_CREATETABLE_FILEPATH = "app.createTablesJsonFilePath";

	String REPORT_CLICK_API_VERSION = "app.clickAPIVersion";
	String REPORT_CLICK_API_ENDPOINT = "app.clickAPIEndPoint";
	String REPORT_CLM_API_VERSION = "app.clmAPIVersion";
	String REPORT_CLM_AUTH_API_VERSION = "app.clmAuthAPIVersion";
	String REPORT_CLM_API_BASEURL = "app.clmAPIBaseUrl";
	String REPORT_CLM_AUTH_API_BASEURL = "app.clmAuthAPIBaseUrl";
	String REPORT_CLM_API_USER_ENDPOINT = "app.clmAPIUserEndPoint";
	String REPORT_ESIGN_API_VERSION = "app.esignAPIVersion";
	String REPORT_ESIGN_API_DOC_ENDPOINT = "app.esignAPIDocumentsEndpoint";
	String REPORT_ESIGN_API_USERS_ENDPOINT = "app.esignAPIAccountUsersEndpoint";
	String REPORT_API_THRESHOLD_LIMIT_PERCENT = "app.apiThresholdLimitPercent";
	String REPORT_ORG_ADMIN_API_BASEURL = "app.orgAdminAPIBaseUrl";
	String REPORT_ROOMS_API_BASEURL = "app.roomsAPIBaseUrl";
	String REPORT_ROOMS_API_VERSION = "app.roomsAPIVersion";
	String REPORT_TOTALROWSPERPROCESS = "app.totalRowsPerProcess";

	String BASICAUTH_APP_USERNAME = "proserv.application.username";
	String BASICAUTH_APP_PASSWORD = "proserv.application.password";
	String BASICAUTH_DSAPPDATA_USERNAME = "proserv.dsappdata.username";
	String BASICAUTH_DSAPPDATA_PASSWORD = "proserv.dsappdata.password";
	String BASICAUTH_DSAUTHENTICATION_USERNAME = "proserv.dsauthentication.username";
	String BASICAUTH_DSAUTHENTICATION_PASSWORD = "proserv.dsauthentication.password";
	String BASICAUTH_DSCACHEDATA_USERNAME = "proserv.dscachedata.username";
	String BASICAUTH_DSCACHEDATA_PASSWORD = "proserv.dscachedata.password";
	String BASICAUTH_DSCOREDATA_USERNAME = "proserv.dscoredata.username";
	String BASICAUTH_DSCOREDATA_PASSWORD = "proserv.dscoredata.password";
	String BASICAUTH_DSENVELOPEDATA_USERNAME = "proserv.dsenvelopedata.username";
	String BASICAUTH_DSENVELOPEDATA_PASSWORD = "proserv.dsenvelopedata.password";
	String BASICAUTH_DSIHDADATA_USERNAME = "proserv.dsihdadata.username";
	String BASICAUTH_DSIHDADATA_PASSWORD = "proserv.dsihdadata.password";
	String BASICAUTH_DSREPORTDATA_USERNAME = "proserv.dsreportdata.username";
	String BASICAUTH_DSREPORTDATA_PASSWORD = "proserv.dsreportdata.password";
	String BASICAUTH_DSBULKSENDDATA_USERNAME = "proserv.dsbulksenddata.username";
	String BASICAUTH_DSBULKSENDDATA_PASSWORD = "proserv.dsbulksenddata.password";

	String BASICAUTH_DSBULKSENDSOURCEDATA_USERNAME = "proserv.dsbulksendsourcedata.username";// 3/17/2021
	String BASICAUTH_DSBULKSENDSOURCEDATA_PASSWORD = "proserv.dsbulksendsourcedata.password";// 3/17/2021

	String BASICAUTH_DSNOTIFICATIONDATA_USERNAME = "proserv.dsnotificationdata.username";// 3/29/2021
	String BASICAUTH_DSNOTIFICATIONDATA_PASSWORD = "proserv.dsnotificationdata.password";// 3/29/2021

	String HAZELCAST_LOCKDURATION = "proserv.application.hazelcast.lockduration";

	String SHELL_INFO_COLOR = "shell.out.info";
	String SHELL_ERROR_COLOR = "shell.out.error";
	String SHELL_SUCCESS_COLOR = "shell.out.success";
	String SHELL_WARNING_COLOR = "shell.out.warning";

	String CONNECT_JOB_RETRY_LIMIT = "ds.job.retrylimit";
	String CONNECT_JOB_RECORDS_PERPAGE = "ds.job.recordsperpage";
	String CONNECT_JOB_BACKSOFF_INTERVAL = "ds.job.backsoffinterval";
	String CONNECT_JOB_BACKSOFF_LIMIT = "ds.job.backsofflimit";
	String CONNECT_JOB_STEPSLEEP_INTERVAL = "ds.job.stepsleep.interval";
	String CONNECT_JOB_STEPSLEEP_THRESHOLDCHECK = "ds.job.stepsleep.thresholdcheck";
	String CONNECT_JOB_STEPSLEEP_ENABLED = "ds.job.stepsleep.enabled";

	String CONNECT_SAVE_TABDATA = "connect.save.tabdata";
	String CONNECT_SAVE_RECIPIENTDATA = "connect.save.recipientdata";
	String CONNECT_SAVE_CUSTOMFIELDDATA = "connect.save.customfielddata";
	String CONNECT_SAVE_DOCDOWNLOADDATA = "connect.save.docdownloaddata";
	String CONNECT_SAVE_RECIPIENTAUTHDATA = "connect.save.recipientauthdata";
	String CONNECT_SAVE_DOCDOWNLOAD_ENVSTATUS = "connect.save.docdownload.envstatus";// COMMA SEPARATED
	String CONNECT_SAVE_TABDATA_USING_FORMDATA = "connect.save.tabdata.formdata";
	String CONNECT_DOWNSTREAM_QUEUE_NAMES = "connect.downstream.queuenames";// COMMA SEPARATED

	String CONNECT_ALLOWED_PROCESSOR_TYPES = "connect.allowed.processors";// COMMA SEPARATED "TAB", "ENVELOPE",
																			// "CUSTOMFIELD", "RECIPIENT",
																			// "RECIPIENTAUTH", "ENVELOPEDOCLOG"
	String CONNECT_ALLOWED_CUSTOMER_PROCESSOR_TYPES = "connect.allowed.customer.processors";
	String CONNECT_EXCLUSION_FIELDNAMES = "connect.exclusion.fieldnames";
	String CONNECT_SAVE_DOCMIGRATION_ENVSTATUS = "connect.save.docmigration.envstatus";// COMMA SEPARATED
	String CONNECT_QUEUE_TO_DB = "ds.connectqueuedasync.db";
	String CONNECT_QUEUE_TO_MB = "ds.connectqueuedasync.mb";
	String CONNECT_PROCESS_NOW_SYNC = "ds.connectprocessnowsync";

	// 4/30/2021
	String CONNECT_SAVE_IGNORE_NONFORMTABTYPES = "connect.save.ignore.nonformtabtypes";

	String DOCMIGRATION_RULE_ENGINE_PATH = "proserv.docmigration.ruleengine.path";
	String API_LANDLORD_RULE_ENGINE = "send.landlord.ruleengine.path";
	String API_TENANT_RULE_ENGINE = "send.tenant.ruleengine.path";

	String DSAPI_BASE_URL = "docusign.api.baseuri";
	String DSAPI_ACCOUNT_ID = "app.authorization.accountguid";
	String DSAPI_USER_INFO_ENDPOINT = "app.authorization.userinfoendpoint";

	String SEND_LANDLORD_TEMPLATE = "send.landlord.template";
	String SEND_TENANT_TEMPLATE = "send.tenant.template";

	String BULKSEND_SCHEDULE_TRIGGER_SECS = "bulksend.trigger.expirationseconds";
	String DS_JOB_BATCHTYPE = "ds.job.batchtype";

	String BULKSEND_MONITOR_DS_API_ENDPOINT = "app.esignAPIBulkSendMonitorEndpoint";

	String DS_ASYNC_QUEUE_CAPACITY = "ds.async.queuecapacity";
	String DS_ASYNC_MAX_POOLSIZE = "ds.async.maxpoolsize";
	String DS_ASYNC_CORE_POOLSIZE = "ds.async.corepoolsize";
	String DS_ASYNC_EXECUTORNAME_PREFIX = "ds.async.executornameprefix";

	String DS_ASYNC_XML_QUEUE_CAPACITY = "ds.async.xml.queuecapacity";
	String DS_ASYNC_XML_MAX_POOLSIZE = "ds.async.xml.maxpoolsize";
	String DS_ASYNC_XML_CORE_POOLSIZE = "ds.async.xml.corepoolsize";
	String DS_ASYNC_XML_EXECUTORNAME_PREFIX = "ds.async.xml.executornameprefix";

	String DS_TRIGGER_RATE_IN_SECS = "ds.triggerintervalinseconds";

	// 3/17/2021

	String DSBULKSEND_SQL_DEFINITION_PATH = "proserv.bulksend.sqldefinition.path";
	String DSBULKSEND_USERID = "proserv.bulksend.userid";
	String DSBULKSEND_ACCOUNTID = "proserv.bulksend.accountid";
	String DSBULKSEND_SCOPES = "proserv.bulksend.scopes";
	String DSAPI_GETENVELOPE_ENDPOINT = "app.esignAPIGetEnvelope";
	String DSAPI_GETTEMPLATE_ENDPOINT = "app.esignAPIGetTemplate";
	String DSAPI_CREATEENVELOPE_ENDPOINT = "app.esignAPICreateEnvelope";

	String DSAPI_BULKSEND_CREATE_MAILINGLIST_ENDPOINT = "app.esignAPIBulkSendMailingList";
	String DSAPI_BULKSEND_TEST_ENDPOINT = "app.esignAPIBulkSendTestApi";
	String DSAPI_BULKSEND_SEND_ENDPOINT = "app.esignAPIBulkSendSendApi";

	String DSAPI_TEMPLATE_RECIPIENTTYPES = "proserv.bulksend.template.recipienttypes";
	String DSAPI_TEMPLATE_LABELTABTYPES = "proserv.bulksend.template.labeltabtypes";
	String DSAPI_TEMPLATE_GROUPTABTYPES = "proserv.bulksend.template.grouptabtypes";
	String DSBULKSEND_ENVELOPE_FIELDS = "proserv.bulksend.envelope.fields";
	String DSBULKSEND_RECIPIENT_FIELDS = "proserv.bulksend.recipient.fields";
	String DSBULKSEND_SIGNATUREPROVIDER_PREFIX = "proserv.bulksend.signatureprovider.";
	String DSBULKSEND_AUTH_PREFIX = "proserv.bulksend.auth.";
	String DSBULKSEND_BACKSOFF_INTERVAL = "proserv.bulksend.backsoffinterval";
	String DSBULKSEND_BATCH_SIZE = "proserv.bulksend.batchsize";
	String DSBULKSEND_PROGRAM_STARTDATETIME = "proserv.bulksend.program.startdatetime";
	String DSBULKSEND_PAGINATION_LIMIT = "proserv.bulksend.pagination.limit";

	String DSSEND_QUERY_IDENTIFIERS = "proserv.send.queryidentifiers";
	String DSSEND_PREFIX = "proserv.send.";
	String DSSEND_USERID_SUFFIX = ".userid";
	String DSSEND_ACCOUNTID_SUFFIX = ".accountid";
	String DSSEND_USETEMPLATE_SUFFIX = ".usetemplate";
	String DSSEND_RULENGINGPATH_SUFFIX = ".ruleenginepath";
	String DSSEND_SELECTRECORDID_SQL_SUFFIX = ".selectrecordidquerytype.name";
	String DSSEND_SELECTRECORDID_SQL_BYDATERANGE_SUFFIX = ".selectrecordidquerytype.bydaterange";
	String DSSEND_SELECTRECORDDATA_SQL_PRIMARYKEY_SUFFIX = ".selectrecorddataquerytype.primarykeyparametername";
	String DSSEND_SELECTRECORDDATA_SQL_SUFFIX = ".selectrecorddataquerytype.name";
	String DSSEND_UPDATERECORDDATA_SUFFIX = ".updaterecorddataquerytype.name";
	String DSSEND_UPDATERECORDDATA_PRIMARYKEY_SUFFIX = ".updaterecorddataquerytype.primarykeyparametername";
	String DSSEND_TEMPLATE_ID_SUFFIX = ".templateid";
	String DSSEND_DRAFTENVELOPE_FILEPATH_SUFFIX = ".draftenveloperequest.filepath";

	// 3/26/2021
	String DSBULKSEND_SQLDATE_SOURCE_FORMAT = "proserv.bulksend.sqldate.sourceformat";
	String DSBULKSEND_SQLDATE_TARGET_FORMAT = "proserv.bulksend.sqldate.targetformat";

	// 3/28/2021
	String GMAIL_CLIENTID = "proserv.notification.gmail.clientid";
	String GMAIL_CLIENTSECRET = "proserv.notification.gmail.clientsecret";

	String GMAIL_APP_IDENTIFIER = "proserv.notification.gmail.appidentifier";
	String GMAIL_APP_NAME = "proserv.notification.gmail.appname";

	String MIGRATION_TO_RECIPIENTS = "proserv.notification.migration.email.torecipients";// COMMA SEPARATED
	String DEADQUEUES_TO_RECIPIENTS = "proserv.notification.deadqueues.email.torecipients";// COMMA SEPARATED

	String VM_HOSTNAMES = "proserv.notification.server.hostnames";
	String VM_DEAD_QUEUENAMES = "proserv.notification.server.queuenames";

	String NOTIFICATION_DEADQUEUE_RATE_IN_SECS = "proserv.notification.deadqueue.job.triggerintervalinseconds";
	String NOTIFICATION_MIGRATIONREADY_RATE_IN_SECS = "proserv.notification.migrationready.job.triggerintervalinseconds";

	// 3/29/2021
	String DOCDOWNLOAD_RULE_ENGINE_PATH = "proserv.docdownload.ruleengine.path";

	// 3/30/2021
	String CSVDOWNLOAD_RULE_ENGINE_PATH = "proserv.csvdownload.ruleengine.path";

	String ENABLE_DEADQUEUE_STATUS_CHECK = "proserv.enable.deadqueue.status.check";

	// 4/2/2021
	String REPROCESSING_PAST_RECORDS = "proserv.enable.processing.past.records";

	// 4/8/2021
	String USE_BUCKET_LOGIC = "proserv.enable.bucket.log";
	String CHECK_TIME_DURATION = "proserv.check.time.duration";

	String OFF_START_HOUR = "proserv.off.start.hour";
	String OFF_END_HOUR = "proserv.off.end.hour";
	String MAX_OFFHOUR_CONCURRENT_CONNECTIONS = "max.offhour.concurrent.connection";
	String MAX_PEAKHOUR_CONCURRENT_CONNECTIONS = "max.peakhour.concurrent.connection";

	String ENVELOPES_PAGINATION_LIMIT = "proserv.envelopes.pagination.limit";

	// 4/12/2021
	String DSTAB_SAVE_STOREDPROC = "proserv.enable.dstab.save.storedproc";
	String DSTAB_SELECTBYENVIDS_STOREDPROC = "proserv.enable.dstab.select.byenvids.storedproc";
	String MIGDATA_SAVE_STOREDPROC = "proserv.enable.migrationdata.save.storedproc";
	String MIGDATA_SELECTBYENVIDS_STOREDPROC = "proserv.enable.migrationdata.select.byenvids.storedproc";
	String CUSTOMENVDATA_SELECTBYBUCKETNAME_STOREDPROC = "proserv.enable.customenvdata.select.bybucketname.storedproc";
	String BULKSENDRECORD_SELECTBYRECORDIDS_STOREDPROC = "proserv.enable.bulksendrecord.select.byrecordids.storedproc";

	// 4/19/2021
	String APPENVDATA_SELECTBYENVIDS_STOREDPROC = "proserv.enable.appenvdata.select.byenvelopeids.storedproc";

	String CUSTENVDATA_UPDATEPROCESSSTATUS_STARTDATETIME_BYENVIDS_STOREDPROC = "proserv.enable.custenvdata.updateprocessstartdatetime.byenvelopeids.storedproc";
	String CUSTENVDATA_UPDATEPROCESSSTATUS_ENDDATETIME_BYENVIDS_STOREDPROC = "proserv.enable.custenvdata.updateprocessenddatetime.byenvelopeids.storedproc";
	String CUSTENVDATA_UPDATEDOCDOWNLOAD_ENDDATETIME_BYENVIDS_STOREDPROC = "proserv.enable.custenvdata.updatedocdownloadenddatetime.byenvelopeids.storedproc";
	String CUSTENVDATA_UPDATEDOCDOWNLOAD_ENDDATETIME_BUCKETNAME_BYENVIDS_STOREDPROC = "proserv.enable.custenvdata.updatedocdownloadenddatetimebucketname.byenvelopeids.storedproc";

	String DSSEND_IGNOREAPPIDS_SUFFIX = ".ignorelist.appids";

	// 4/19/2021 Test Mock propertied for CP
	String DSAPI_ENABLE_TEST_MOCK = "proserv.mock.test.enabled";
	String DSAPI_MOCK_BASEURL = "proserv.mock.sign.baseurl";

	// 4/20/2021

	String MAX_OFFHOUR_PREFETCH_COUNT = "max.offhour.prefetch.count";
	String MAX_PEAKHOUR_PREFETCH_COUNT = "max.peakhour.prefetch.count";

	// 4/21/2021
	String ENVDATA_SELECTBYENVIDS_STOREDPROC = "proserv.enable.envdata.select.byenvelopeids.storedproc";
	String ENVDATA_CUSTFIELDS_SELECTBYENVIDS_STOREDPROC = "proserv.enable.envdata.select.customfields.byenvelopeids.storedproc";
	String ENVDATA_RECIPIENTS_SELECTBYENVIDS_STOREDPROC = "proserv.enable.envdata.select.recipients.byenvelopeids.storedproc";
	String ENVDATA_DSCUSTOMFIELD_SAVE_STOREDPROC = "proserv.enable.dscustomfield.save.storedproc";

	String CUSTENVDATA_SELECTBYENVID_STOREDPROC = "proserv.enable.custenvdata.select.byenvelopeid.storedproc";

	// 5/5/2021
	String DSBULKSEND_TRACKIDS = "proserv.bulksend.trackids";
	String DSBULKSEND_TRACKIDS_WITH_TODATE = "proserv.bulksend.trackids.withtodate";

	String NOTIFICATION_CLMMIGRATION_EMAILSUBJECT = "proserv.notification.clmmigration.job.emailsubject";
	String NOTIFICATION_DEADQUEUES_EMAILSUBJECT = "proserv.notification.deadqueues.job.emailsubject";
}