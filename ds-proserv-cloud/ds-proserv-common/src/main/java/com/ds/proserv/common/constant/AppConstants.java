package com.ds.proserv.common.constant;

public interface AppConstants {

	String USER_ACTIVE = "active";
	String DOCUMENT_FILE_NAME_SUFFIX = ".pdf";
	String SAVE_LOCK_NAME = "SAVE_LOCK";
	String SUCCESS_VALUE = "SUCCESS";
	String QUEUED_VALUE = "QUEUED";
	String DB_QUEUED_VALUE = "DBQUEUED";
	String MB_QUEUED_VALUE = "MBQUEUED";
	String DB_QUEUED_AFTER_MB_ERROR_VALUE = "DBQUEUEDAFTERMBERROR";
	String FAILURE_VALUE = "FAILURE";
	String REQUIRED_PARAM_MISSING = "FAILURE";

	String ROLE_LANDLORD = "landlord";
	String ROLE_TENANT = "tenant";

	String PROCESSID_PARAM_NAME = "processId";
	String TABLABELS_PARAM_NAME = "tabLabels";
	String EXCEPTIONIDS_PARAM_NAME = "exceptionIds";
	String ENVELOPEIDS_PARAM_NAME = "envelopeIds";
	String FROMDATETIME_PARAM_NAME = "fromDateTime";
	String TODATETIME_PARAM_NAME = "toDateTime";
	String CUSTOMFIELD_PARAM_NAME = "customFieldName";

	String APPLICATIONIDS_PARAM_NAME = "applicationIds";
	String APPLICATIONSTATUS_PARAM_NAME = "applicationStatus";
	String APPLICATIONSTATUSES_PARAM_NAME = "applicationStatuses";
	String LANGUAGECODES_PARAM_NAME = "languageCodes";
	String AGENTCODES_PARAM_NAME = "agentCodes";
	String DRAWREFERENCE_PARAM_NAME = "drawReference";
	String DRAWREFERENCES_PARAM_NAME = "drawReferences";
	String PROGRAMTYPES_PARAM_NAME = "programTypes";

	String BULKBATCHIDS_PARAM_NAME = "bulkBatchIds";
	String RETRYSTATUSES_PARAM_NAME = "retryStatuses";
	String BATCHSTATUSES_PARAM_NAME = "batchStatuses";
	String BATCHNOTCREATED = "BATCHNOTCREATED";
	String PROCESSNOTCREATED = "PROCESSNOTCREATED";

	String PIPE_DELIMITER = "|";
	String COMMA_DELIMITER = ",";
	String BULK_CSV_DELIMITER = "::";
	String SEMI_COLON_DELIMITER = ";";
	String WHITESPACE_REGEX = "[\\s|\\u00A0]+";

	String ENVELOPE_EXPIRED_STATUS = "Expired";
	String TABVALUE_X = "X";
	String ECFNAME_APPLICATIONID = "applicationId";
	String ECFNAME_BULKBATCHID = "BulkBatchId";

	String NO_DATA_AVAILABLE_TO_SAVE = "No Data available to save";
	String NO_DATA_AVAILABLE_TO_PROCESS = "No Data available to process";

	String PRIMARYIDS_PARAM_NAME = "primaryIds";

	// ReportUtility constants
	String NAF = "naf";

	String COLON = ":";

	String APP_TRUE = "true";

	String APP_FALSE = "false";

	String FORWARD_SLASH = "/";

	String DS_ACCOUNT_ID = "dsAccountId";

	String JWT_SCOPES = "scopes";

	String JWT_USER_ID = "userId";

	String AUTH_CLIENTID = "clientId";

	String INPUT_ORG_ID = "inputOrgId";

	String INPUT_TO_DATE = "inputToDate";

	String SCRIPT_ENGINE_NAME = "nashorn";

	String ACCOUNT_ADMIN = "accountAdmin";

	String ORG_ADMIN = "organizationAdmin";

	String INPUT_FROM_DATE = "inputFromDate";

	String SELECT_USER_IDS = "selectUserIds";

	String FILTER_USER_IDS = "filterUserIds";

	String AUTH_CLIENTSECRET = "clientSecret";

	String AUTH_API_USERNAME = "apiUserName";

	String AUTH_API_PASSWORD = "apiPassword";

	String AUTH_API_BASEURL = "apiBaseUrl";

	String FILE_SAVE_FORMAT = "fileSaveFormat";

	String UNZIP_ARCHIVE_FLAG = "unzipArchive";

	String DELETE_ARCHIVE_FLAG = "deleteArchive";

	String REFRESH_DATA_BASE = "refreshDataBase";

	String RESTRICTED_CHARACTER_REPLACEMENT = "_";

	String SECRETKEY_TYPE = "PBKDF2WithHmacSHA256";

	String SELECT_ACCOUNT_IDS = "selectAccountIds";

	String FILTER_ACCOUNT_IDS = "filterAccountIds";

	String DOWNLOAD_FILE_NAME = "downloadFileName";

	String PROCESS_ALL_USERS_FLAG = "processAllUsers";

	String REFRESH_DATA_BASE_FLAG = "refreshDataBase";

	String DOWNLOAD_FOLDER_NAME = "downloadFolderName";

	String CIPHER_INSTANCE_TYPE = "AES/CBC/PKCS5Padding";

	String CSV_DOWNLOAD_FOLDER_PATH = "csvDownloadFolderPath";// CSV Download Directory path

	String CSV_DOWNLOAD_ROWS_LIMT_PER_FILE = "csvDownloadRowsLimit";// CSV Download rows limit per file

	String CSV_FETCH_COUNT_COLUMN_NAME = "csvCountColumnName";// CSV Column name to count the limit

	String DOC_DOWNLOAD_FOLDER_PATH = "docDownloadFolderPath";// Doc download path

	String DOC_DOWNLOAD_DESTINATION = "docDownloadDestination";// Sharepoint, Disk, S3 et al

	String ACCOUNTS_FETCH_API_TO_USE_TYPE = "accountsFetchAPIToUse";

	String DISABLE_CURR_DATE_IN_CSV_FOLDER_PATH_FLAG = "disableCurrentDateInCSVFolderPath";// If set as True then no
																							// directory will be created
																							// with currdate

	String DISABLE_CURR_DATE_IN_DOC_FOLDER_PATH_FLAG = "disableCurrentDateInDocFolderPath";// If set as True then no
																							// directory will be created
																							// with currdate

	String DISABLE_ACCOUNTID_IN_DOC_FOLDER_PATH_FLAG = "disableAccountIdInDocFolderPath";// If set as True then no
	// directory will be created
	// with currdate

	String DOWNLOAD_FOLDER_FILE_RESTRICTED_REGEX_PARAM_NAME = "downloadFolderFileRestrictedRegex";

	String ACCOUNT_ID_COL_NAME = "accountIdColumnName";

	String ENVELOPE_ID_COL_NAME = "envelopeIdColumnName";

	String AUTH_HEADER_NAME_CONST = "Authorization";

	String AUTH_HEADER_VALUE_PREFIX_CONST = "Basic ";

	String NOT_AVAILABLE_CONST = "NOTAVAILABLE";

	String DOC_DOWNLOAD_COLUMN_LABELS = "docDownloadColumnLabels";

	String DOC_DOWNLOAD_RECORDID_LABEL = "docDownloadRecordId";

	String DOC_DOWNLOAD_ACCOUNTID_LABEL = "docDownloadAccountId";

	String SQL_QUERY_CLAUSE = "selectSQL";

	String APPLICATIONTYPE_PARAM_NAME = "applicationType";

	String APPLICATIONTYPE_ECF_NAME = "applicationType";

	String APPLICATION_COMMUNITYPARTNER_CODE = "communitypartnercode";

	String APP_QUERY_IDENTIFIER = "queryIdentifier";

	String APP_QUERY_TYPE = "fetchQueryType";

	String ESIGN_SCOPES = "signature impersonation";

	String SQL_QUERY_OFFSET = "offset";
	String SQL_QUERY_LIMIT = "limit";

	String DSAPI_HOURLY_LIMIT_EXCEEDED_ERROR = "HOURLY_APIINVOCATION_LIMIT_EXCEEDED";
	String RECORDIDS_PARAM_NAME = "RecordIds";
	String PAGINATIONLIMIT_PARAM_NAME = "paginationLimit";
	String PAGENUMBER_PARAM_NAME = "pageNumber";

	String ENVELOPE_COMPLETED_STATUS = "Completed";

	String DS_NOTIFICATION_BATCH_NAME = "dsnotificationbatch";

	String DEFAULT_DOWNLOAD_BUCKETNAME = "mig";

	String BUCKET_PARAM_NAME = "downloadBucket";

	String EMPTY_VALUE = "empty";

	String TABLABEL_HREF = "#HREF";

	String APP_PROCESS_STATUS = "appProcessStatus";
	String IN_PROGRESS = "INPROGRESS";
	String STALE_MESSAGE = "StaleMessage";

	String REPORT_DATA_PAGINATION_LIMIT = "reportDataPaginationLimit";// CSV Download rows limit per file
}