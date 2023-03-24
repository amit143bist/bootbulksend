package com.ds.proserv.docmigration.consumer.service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.script.ScriptEngine;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.ds.proserv.broker.config.service.QueueService;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.docmigration.consumer.client.CoreConcurrentProcessLogClient;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessLogDefinition;
import com.ds.proserv.feign.report.domain.ConcurrentDocDownloadDataMessageDefinition;
import com.ds.proserv.feign.report.domain.DownloadDataMessage;
import com.ds.proserv.feign.report.domain.DownloadDocs;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.util.FileUtil;
import com.ds.proserv.feign.util.ReportAppUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MigrationDocDownloadService {

	@Autowired
	private TaskExecutor xmlTaskExecutor;

	@Autowired
	private DownloadDocs downloadDocs;

	@Autowired
	private ScriptEngine scriptEngine;

	@Autowired
	private QueueService queueService;

	@Autowired
	private CoreConcurrentProcessLogClient coreConcurrentProcessLogClient;

	public void prepareAndSendDataForDocDownload(List<Map<String, Object>> envelopeDataList, String processId,
			String batchId) {

		log.info("Calling prepareAndSendDataForDocDownload for processId -> {}", processId);

		String downloadFolderPath = findDownloadFolderPath(downloadDocs.getDownloadParams());

		Path parentDirectory = Paths.get(downloadFolderPath);
		log.info("ParentDirectory in prepareAndSendDataForDocDownload is {}", parentDirectory.toString());

		List<PathParam> downloadParams = downloadDocs.getDownloadParams();

		String fileSaveFormat = ReportAppUtil.findPathParam(downloadParams, AppConstants.FILE_SAVE_FORMAT)
				.getParamValue();

		String downloadFileName = extractPathParamValue(downloadParams, AppConstants.DOWNLOAD_FILE_NAME);
		String downloadFolderName = extractPathParamValue(downloadParams, AppConstants.DOWNLOAD_FOLDER_NAME);

		List<DownloadDataMessage> downloadDataMessages = new ArrayList<DownloadDataMessage>();
		for (Map<String, Object> envelopeDataMap : envelopeDataList) {

			String fileName = null;
			String folderName = null;
			Map<String, Object> inputParams = new HashMap<String, Object>();

			FileUtil.populateInputParams(envelopeDataMap, inputParams, downloadDocs);

			fileName = FileUtil.evaluateFileName(downloadParams, scriptEngine, downloadFileName, envelopeDataMap,
					fileName);

			folderName = FileUtil.evaluateFileFolderName(downloadParams, scriptEngine, downloadFolderName,
					envelopeDataMap, folderName);

			DownloadDataMessage downloadDataMessage = FileUtil.createDownloadDocMessage(downloadDocs, parentDirectory,
					fileSaveFormat, fileName, folderName, inputParams);

			downloadDataMessages.add(downloadDataMessage);

		}

		if (null != downloadDataMessages && !downloadDataMessages.isEmpty()) {

			if (!StringUtils.isEmpty(processId)) {

				ConcurrentProcessLogDefinition newProcess = createConcurrentProcess(
						Long.valueOf(downloadDataMessages.size()), batchId, processId);

				callDocDownloadQueue(downloadDataMessages, newProcess.getProcessId(), batchId, processId, downloadDocs);
			} else {

				callDocDownloadQueue(downloadDataMessages, null, batchId, processId, downloadDocs);
			}
		}

	}

	private void callDocDownloadQueue(List<DownloadDataMessage> downloadDataMessages, String processId, String batchId,
			String groupId, DownloadDocs downloadDocs) {

		CompletableFuture.runAsync(() -> {

			log.info("Calling callDocDownloadQueue for processId -> {}", processId);

			ConcurrentDocDownloadDataMessageDefinition concurrentDocDownloadDataMessageDefinition = new ConcurrentDocDownloadDataMessageDefinition();
			concurrentDocDownloadDataMessageDefinition.setBatchId(batchId);
			concurrentDocDownloadDataMessageDefinition.setProcessId(processId);
			concurrentDocDownloadDataMessageDefinition.setGroupId(groupId);
			concurrentDocDownloadDataMessageDefinition.setDownloadDocs(downloadDocs);
			concurrentDocDownloadDataMessageDefinition.setDownloadDataMessages(downloadDataMessages);

			queueService.findQueueNameAndSend(PropertyCacheConstants.PROCESS_DOCDOWNLOAD_QUEUE_NAME, processId, batchId,
					concurrentDocDownloadDataMessageDefinition);
		}, xmlTaskExecutor);
	}

	public ConcurrentProcessLogDefinition createConcurrentProcess(Long batchSize, String batchId, String groupId) {

		log.info("New ConcurrentProcess created with batchSize {} for batchId -> {} and groupId -> {}", batchSize,
				batchId, groupId);

		ConcurrentProcessLogDefinition concurrentProcessLogDefinition = new ConcurrentProcessLogDefinition();
		concurrentProcessLogDefinition.setBatchId(batchId);
		concurrentProcessLogDefinition.setProcessStatus(ProcessStatus.INPROGRESS.toString());
		concurrentProcessLogDefinition.setGroupId(groupId);
		concurrentProcessLogDefinition.setTotalRecordsInProcess(batchSize);

		return coreConcurrentProcessLogClient.saveConcurrentProcess(concurrentProcessLogDefinition).getBody();

	}

	private String extractPathParamValue(List<PathParam> downloadParams, String paramName) {

		PathParam pathParam = ReportAppUtil.findPathParam(downloadParams, paramName);
		if (null != pathParam) {
			return pathParam.getParamValue();
		}

		return null;
	}

	private String findDownloadFolderPath(List<PathParam> pathParamList) {

		PathParam downloadFolderPathParam = ReportAppUtil.findPathParam(pathParamList,
				AppConstants.DOC_DOWNLOAD_FOLDER_PATH);

		if (null == downloadFolderPathParam || (StringUtils.isEmpty(downloadFolderPathParam.getParamValue()))) {

			log.info("downloadFolderPathParam param is missing or is null");
			return null;

		}

		log.info("Download Folder Path is {}", downloadFolderPathParam.getParamValue());
		return modifyDownloadFolderPath(downloadFolderPathParam.getParamValue(), pathParamList);
	}

	private String modifyDownloadFolderPath(String downloadFolderPath, List<PathParam> pathParamList) {

		log.info("Modifying the Doc Downlod Folder Path");
		PathParam disableDocFilePathParam = ReportAppUtil.findPathParam(pathParamList,
				AppConstants.DISABLE_CURR_DATE_IN_DOC_FOLDER_PATH_FLAG);

		if (null != disableDocFilePathParam && "true".equalsIgnoreCase(disableDocFilePathParam.getParamValue())) {

			return downloadFolderPath;
		} else {

			String directoryPath = downloadFolderPath + File.separator + DateTimeUtil.currentLocalDateInString();

			ReportAppUtil.createDirectory(directoryPath);

			return directoryPath;
		}
	}
}