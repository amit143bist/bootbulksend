package com.ds.proserv.shell.controller;

import static com.ds.proserv.common.constant.AppConstants.USER_ACTIVE;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.ApplicationStatus;
import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.common.constant.ProcessStatus;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.RunningBatchException;
import com.ds.proserv.feign.appdata.domain.DrawApplicationDefinition;
import com.ds.proserv.feign.appdata.domain.DrawApplicationInformation;
import com.ds.proserv.feign.shell.domain.Recipients;
import com.ds.proserv.feign.shell.domain.Signer;
import com.ds.proserv.feign.shell.domain.Tabs;
import com.ds.proserv.feign.shell.domain.TextTab;
import com.ds.proserv.feign.shell.domain.UpdateApplicationResponse;
import com.ds.proserv.feign.shell.domain.UpdateRecipientRequest;
import com.ds.proserv.feign.shell.domain.User;
import com.ds.proserv.feign.shell.service.ApplicationAssigmentService;
import com.ds.proserv.feign.shell.service.DocuSignService;
import com.ds.proserv.shell.client.DrawApplicationClient;
import com.ds.proserv.shell.domain.Application;
import com.ds.proserv.shell.service.CoreBatchDataService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ApplicationsAssigmentController implements ApplicationAssigmentService {

	@Autowired
	private DrawApplicationClient drawApplicationClient;

	@Autowired
	private DocuSignService docuSignService;

	@Value("${docusign.api.group.groupid}")
	private String defaultGroup;

	@Value("${bulk.update.rolename}")
	private String rolename;

	@Value("${ds.api.bulksend.recordperpage}")
	private Integer pageSize;

	@Value("${ds.api.bulksend.batchnameprefix}")
	private String batchNamePrefix;

	@Autowired
	private TaskExecutor recordTaskExecutor;

	@Autowired
	private CoreBatchDataService coreBatchDataService;

	@Override
	public ResponseEntity<String> updateApplicationDrawStatus(@RequestParam("file") MultipartFile file,
			@PathVariable() String drawReference) {

		List<Application> applications = new ArrayList<>();

		try {

			log.info("Starting UpdateEnvelope process");

			log.info("Inputs to updateApplicationDrawStatus are csvFullPath-> {} drawReferenceId-> {}", file,
					drawReference);

			if (StringUtils.isEmpty(file.isEmpty()) || StringUtils.isEmpty(drawReference)) {
				log.info(
						"ValidCommand sample is -> markSelectedApplications csvFullPath $csvFullPath lotteryReferenceId $lotteryReferenceId");

				log.error("Inputs are wrong to trigger this batch");
				throw new InvalidInputException("Inputs are wrong to trigger this batch");
			}
			Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));

			// create csv bean reader
			CsvToBean<Application> csvToBean = new CsvToBeanBuilder<Application>(reader).withType(Application.class)
					.withIgnoreLeadingWhiteSpace(true).build();

			// convert `CsvToBean` object to list of users
			applications = csvToBean.parse();
			Assert.notEmpty(applications, "No Application read from loaded file");

			if (!applications.isEmpty()) {
				if (null == applications.get(0) || null == applications.get(0).getApplication()) {
					return new ResponseEntity<>("Input file does not comply with the required format",
							HttpStatus.LOCKED);
				}

			}

			List<String> applStringList = applications.stream().map(Application::getApplication)
					.collect(Collectors.toList());

			final AtomicInteger counter = new AtomicInteger(0);
			final Collection<List<String>> appBatches = applStringList.stream()
					.collect(Collectors.groupingBy(it -> counter.getAndIncrement() / pageSize)).values();

			appBatches.forEach(batch -> {
				log.info("Reading batch -> {}", batch);
				drawApplicationClient.bulkUpdateApplications(createDrawSelectedPageInformation(batch, drawReference));
			});

			return new ResponseEntity<>("Number of application assigned: " + applStringList.size(), HttpStatus.OK);
		} catch (FileNotFoundException e) {
			log.info("We cannot find the inout CVS file supplied as {}", file.getName());
			e.printStackTrace();
			throw new InvalidInputException("Fail opening file " + file.getName());
		} catch (RunningBatchException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.LOCKED);
		}
		return new ResponseEntity<>("Application set to DRAW_SELECTED: " + applications.size(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> assignApplicationToReviewerGroup() {
		return assignApplicationToGroup(defaultGroup);
	}

	private int createAsyncBatch(int pageNumber, List<User> reviewers, int totalBatchSize,
			DrawApplicationInformation drawApplicationInformation,
			List<CompletableFuture<String>> reportDataFutureAppAssignList, String batchId) {
		List<DrawApplicationDefinition> drawApplicationDefinitionList = drawApplicationInformation
				.getDrawApplicationDefinitions();

		totalBatchSize = totalBatchSize + drawApplicationDefinitionList.size();

		String processId = coreBatchDataService.createConcurrentProcess((long) totalBatchSize, batchId).getProcessId();

		reportDataFutureAppAssignList
				.add(fethApplicationsDrawnAndAAssignToReviewers(reviewers, drawApplicationDefinitionList, processId));
		return totalBatchSize;

	}

	private CompletableFuture<String> fethApplicationsDrawnAndAAssignToReviewers(List<User> activeReviewers,
			List<DrawApplicationDefinition> drawApplicationList, String processId) {
		return CompletableFuture.supplyAsync((Supplier<String>) () -> {
			String asyncStatus = AppConstants.SUCCESS_VALUE;
			int lastReviewerAssigned = 0;
			User newReviewer;

			for (DrawApplicationDefinition app : drawApplicationList) {
				Assert.notNull(app.getBridgeEnvelopeId(),
						" BridgeEnvelopeId  was null, I cannot update the reviewer for a missing envelope for application: "
								+ app.getApplicationId());
				app.setApplicationStatus(ApplicationStatus.APP_REVIEWER_SUBMITTED.toString());
				if (lastReviewerAssigned >= activeReviewers.size()) {
					lastReviewerAssigned = 0;
				}
				newReviewer = activeReviewers.get(lastReviewerAssigned++);
				Assert.notNull(newReviewer, "Reviewer could not be retrieve from list of reviewers");
				// Get the reviewer assigned to this bridge envelope
				Signer currentReviewer = docuSignService.getRecipientByRole(rolename, app.getBridgeEnvelopeId())
						.getBody();
				currentReviewer.setName(newReviewer.getUserName());
				currentReviewer.setEmail(newReviewer.getEmail());
				currentReviewer.setClientUserId("");

				// We look for the texttab using the tablabel, I need to set the value for
				// AppConstants.DRAWREFERENCE_PARAM_NAME
				TextTab drawReferenceTextTtab = findByTabLabel(currentReviewer.getTabs(),
						AppConstants.DRAWREFERENCE_PARAM_NAME);
				drawReferenceTextTtab.setValue(app.getDrawReference());

				Recipients recipients = new Recipients();
				recipients.setSigners(new ArrayList<Signer>());
				recipients.getSigners().add(currentReviewer);

				UpdateRecipientRequest request = new UpdateRecipientRequest();
				request.setRecipients(recipients);
				log.info("Assigning envelope -> {} to {}", app.getBridgeEnvelopeId(), currentReviewer.getEmail());
				docuSignService.updateRecipient(request, app.getBridgeEnvelopeId());
			}
			setApplicationStatus(drawApplicationList, ApplicationStatus.APP_REVIEWER_SUBMITTED.toString());

			return asyncStatus;
		}, recordTaskExecutor).handleAsync((asyncStatus, exp) -> {
			if (null == exp) {
				coreBatchDataService.finishConcurrentProcess(processId, ProcessStatus.COMPLETED.toString());

			} else {
				log.error("some error occured: {}", exp.getMessage());
				exp.printStackTrace();
				coreBatchDataService.createFailureProcess(processId, FailureCode.ERROR_212.toString(),
						FailureCode.ERROR_212.getFailureCodeDescription(),
						FailureStep.BULK_REVIEWER_REASSIGNMENT.toString(), processId);

			}

			return asyncStatus;

		}, recordTaskExecutor);

	}

	@Override
	public ResponseEntity<String> assignApplicationToGroup(String groupid) {

		try {
			log.info("Calling assignReviewer with group {}", groupid);

			// Now retrieve the list of reviewers, they belong to the reviewer group
			List<User> activeReviewers = docuSignService.getUsersByGroup(groupid, USER_ACTIVE).getBody();

			log.info("Calling assignReviewer");

			// fetch applications from database whose status is DRAW_SELECTED
			int pageNumber = 0;
			int totalBatchSize = 0;
			String batchId = coreBatchDataService.checkOrCreateBatch();

			PageInformation pageInformation = preparePageInformation(pageNumber);
			DrawApplicationInformation drawApplicationInformation = drawApplicationClient
					.findAllByApplicationStatuses(pageInformation).getBody();

			List<CompletableFuture<String>> reportDataFutureBulkSendList = new ArrayList<CompletableFuture<String>>();

			if (null != drawApplicationInformation && drawApplicationInformation.getContentAvailable()
					&& null != drawApplicationInformation.getDrawApplicationDefinitions()) {
				// totalApplications =
				// applicationInformation.getDrawApplicationDefinitions().size();
				totalBatchSize = createAsyncBatch(pageNumber, activeReviewers, totalBatchSize,
						drawApplicationInformation, reportDataFutureBulkSendList, batchId);

				while (null != drawApplicationInformation.getNextAvailable()
						&& drawApplicationInformation.getNextAvailable()) {

					pageNumber = pageNumber + 1;
					pageInformation = preparePageInformation(pageNumber);
					drawApplicationInformation = drawApplicationClient.findAllByApplicationStatuses(pageInformation)
							.getBody();

					if (null != drawApplicationInformation && drawApplicationInformation.getContentAvailable()
							&& null != drawApplicationInformation.getDrawApplicationDefinitions()) {

						totalBatchSize = createAsyncBatch(pageNumber, activeReviewers, totalBatchSize,
								drawApplicationInformation, reportDataFutureBulkSendList, batchId);

					}
				}
			}

			if (totalBatchSize == 0) {
				return new ResponseEntity<>("No Applications found with status DRAW_SELECTED ", HttpStatus.OK);
			}

			if (!org.apache.commons.lang3.StringUtils.isEmpty(batchId) && null != reportDataFutureBulkSendList
					&& !reportDataFutureBulkSendList.isEmpty()) {

				log.info("Waiting for all Async job to complete for batchId -> {}", batchId);
				CompletableFuture.allOf(reportDataFutureBulkSendList
						.toArray(new CompletableFuture[reportDataFutureBulkSendList.size()])).get();
			}
			log.info("All Asynch threads completed");

			coreBatchDataService.finishNewBatch(batchId, Long.valueOf(totalBatchSize));
			return new ResponseEntity<>("Reviewers reassigned completed:  " + totalBatchSize, HttpStatus.OK);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RunningBatchException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.LOCKED);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.LOCKED);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// setApplicationStatus(drawApplicationList,
		// ApplicationStatus.APP_REVIEWER_SUBMITTED.toString());
		// return new ResponseEntity<>("Number of applications assigned: " +
		// drawApplicationList.size() , HttpStatus.OK);
		return new ResponseEntity<>("Number of applications assigned: ", HttpStatus.OK);
	}

	private TextTab findByTabLabel(Tabs tabs, String tabbLabel) {
		for (TextTab textTab : tabs.getTextTabs()) {
			if (textTab.getTabLabel().equalsIgnoreCase(tabbLabel)) {
				return textTab;
			}
		}
		return null;
	}

	private PageInformation preparePageInformation(int pageNumber) {

		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageNumber(pageNumber);
		pageInformation.setRecordsPerPage(pageSize);

		PageQueryParam pageQueryParam = new PageQueryParam();
		pageQueryParam.setParamName(AppConstants.APPLICATIONSTATUSES_PARAM_NAME);
		pageQueryParam.setParamValue(ApplicationStatus.APP_DRAW_SELECTED.toString());

		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		pageQueryParamList.add(pageQueryParam);

		pageInformation.setPageQueryParams(pageQueryParamList);

		return pageInformation;
	}

	PageInformation createDrawSelectedPageInformation(List<String> applicationBulkList, String drawReferenceId) {
		Map<String, String> params = new HashMap<>();
		params.put(AppConstants.APPLICATIONSTATUS_PARAM_NAME, ApplicationStatus.APP_DRAW_SELECTED.toString());
		params.put(AppConstants.DRAWREFERENCE_PARAM_NAME, drawReferenceId);
		String applicationsByComa = String.join(",", applicationBulkList);
		params.put(AppConstants.APPLICATIONIDS_PARAM_NAME, applicationsByComa);
		return createPageInformationWithParam(params);
	}

	private ResponseEntity<UpdateApplicationResponse> setApplicationStatus(
			List<DrawApplicationDefinition> applicationBulkList, String status) {
		log.info("calling setApplicationStatus for {} applications setting status {}", applicationBulkList.size(),
				status);
		Map<String, String> params = new HashMap<>();
		params.put(AppConstants.APPLICATIONSTATUS_PARAM_NAME, status);
		applicationBulkList.forEach(drawApplicationDefinition -> {
			params.put(AppConstants.DRAWREFERENCE_PARAM_NAME, drawApplicationDefinition.getDrawReference());
		});

		List<String> applicationList = new ArrayList<>();
		applicationBulkList.forEach(e -> applicationList.add(e.getApplicationId()));

		String applicationsByComa = String.join(",", applicationList);
		params.put(AppConstants.APPLICATIONIDS_PARAM_NAME, applicationsByComa);
		UpdateApplicationResponse updateApplicationResponse = new UpdateApplicationResponse();
		updateApplicationResponse.numberRecordsRead = applicationBulkList.size();

		log.info("setApplicationStatus params: status -> {}, drawReference -> {}, Application IDs -> {} ",
				params.get(AppConstants.APPLICATIONSTATUS_PARAM_NAME),
				params.get(AppConstants.DRAWREFERENCE_PARAM_NAME), params.get(AppConstants.APPLICATIONIDS_PARAM_NAME));

		updateApplicationResponse.setStatus(
				drawApplicationClient.bulkUpdateApplications(createPageInformationWithParam(params)).getBody());
		return new ResponseEntity<>(updateApplicationResponse, HttpStatus.OK);
	}

	private PageInformation createPageInformationWithParam(Map<String, String> params) {

		PageInformation pageInformation = new PageInformation();
		pageInformation.setRecordsPerPage(pageSize);
		List<PageQueryParam> pageQueryParamList = new ArrayList<PageQueryParam>();
		params.entrySet().forEach(e -> {
			PageQueryParam pageQueryParam = new PageQueryParam();
			pageQueryParam.setParamName(e.getKey());
			pageQueryParam.setParamValue(e.getValue());
			pageQueryParamList.add(pageQueryParam);
		});
		pageInformation.setPageQueryParams(pageQueryParamList);
		pageInformation.setPageNumber(0);
		return pageInformation;
	}

}
