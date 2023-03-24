package com.ds.proserv.appdata.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.appdata.model.DrawApplication;
import com.ds.proserv.appdata.projection.DrawApplicationIdProjection;
import com.ds.proserv.appdata.repository.DrawApplicationPagingAndSortingRepository;
import com.ds.proserv.appdata.transformer.DrawApplicationTransformer;
import com.ds.proserv.appdata.transformer.PageableTransformer;
import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.constant.ApplicationStatus;
import com.ds.proserv.common.constant.PropertyCacheConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.feign.appdata.domain.DrawApplicationDefinition;
import com.ds.proserv.feign.appdata.domain.DrawApplicationIdResult;
import com.ds.proserv.feign.appdata.domain.DrawApplicationInformation;
import com.ds.proserv.feign.appdata.service.DrawApplicationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Transactional
@Slf4j
public class DrawApplicationController implements DrawApplicationService {

	@Autowired
	private DSCacheManager dsCacheManager;

	@Autowired
	private PageableTransformer pageableTransformer;

	@Autowired
	private DrawApplicationTransformer drawApplicationTransformer;

	@Autowired
	private DrawApplicationPagingAndSortingRepository drawApplicationPagingAndSortingRepository;

	@Override
	public ResponseEntity<DrawApplicationDefinition> saveApplication(
			DrawApplicationDefinition drawApplicationDefinition) {

		log.info("Saving DrawApplication for triggerEnvelopeId -> {}",
				drawApplicationDefinition.getTriggerEnvelopeId());
		return Optional
				.ofNullable(drawApplicationPagingAndSortingRepository
						.save(drawApplicationTransformer.transformToDrawApplication(drawApplicationDefinition)))
				.map(savedAppData -> {

					Assert.notNull(savedAppData.getApplicationId(),
							"ApplicationId cannot be null for triggerEnvelopeId "
									+ drawApplicationDefinition.getTriggerEnvelopeId());

					return new ResponseEntity<DrawApplicationDefinition>(
							drawApplicationTransformer.transformToDrawApplicationDefinition(savedAppData),
							HttpStatus.CREATED);
				}).orElseThrow(() -> new ResourceNotSavedException(
						"Draw Application not saved for " + drawApplicationDefinition.getTriggerEnvelopeId()));

	}

	@Override
	public ResponseEntity<DrawApplicationDefinition> updateApplication(
			DrawApplicationDefinition drawApplicationDefinition, String applicationId) {

		log.info("Updating DrawApplication for applicationId -> {}", applicationId);
		return drawApplicationPagingAndSortingRepository.findById(applicationId).map(drawApplicationData -> {

			drawApplicationDefinition.setApplicationId(applicationId);

			if (ApplicationStatus.APP_DRAW_DUPLICATE.toString()
					.equalsIgnoreCase(drawApplicationDefinition.getApplicationStatus())) {

				drawApplicationDefinition.setDuplicateRecord(true);
			}

			return new ResponseEntity<DrawApplicationDefinition>(drawApplicationTransformer
					.transformToDrawApplicationDefinition(drawApplicationPagingAndSortingRepository.save(
							drawApplicationTransformer.transformToDrawApplicationAsUpdate(drawApplicationDefinition,
									drawApplicationData))),
					HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException(
				"No drawapplication found for applicationId# " + drawApplicationDefinition.getApplicationId()));

	}

	@Override
	public ResponseEntity<DrawApplicationInformation> bulkUpdateSaveApplication(
			DrawApplicationInformation drawApplicationInformation) {

		log.info("bulkUpdateSaveApplication called for processId -> {}", drawApplicationInformation.getProcessId());
		List<DrawApplicationDefinition> drawApplicationDefinitionList = drawApplicationInformation
				.getDrawApplicationDefinitions();

		List<String> drawApplicationIds = drawApplicationDefinitionList.stream()
				.filter(drawApplication -> drawApplication.getApplicationId() != null)
				.map(DrawApplicationDefinition::getApplicationId).collect(Collectors.toList());

		Iterable<DrawApplication> drawApplicationIterable = drawApplicationPagingAndSortingRepository
				.findAllById(drawApplicationIds);

		List<DrawApplication> drawApplicationList = new ArrayList<DrawApplication>(
				drawApplicationDefinitionList.size());

		drawApplicationIterable.forEach(drawApplication -> {

			DrawApplicationDefinition drawApplicationDefinition = null;

			for (DrawApplicationDefinition drawApplicationDefinitionSubmitted : drawApplicationDefinitionList) {

				if (null != drawApplicationDefinitionSubmitted && null != drawApplication
						&& !StringUtils.isEmpty(drawApplicationDefinitionSubmitted.getApplicationId())
						&& !StringUtils.isEmpty(drawApplication.getApplicationId())
						&& drawApplication.getApplicationId()
								.equalsIgnoreCase(drawApplicationDefinitionSubmitted.getApplicationId())) {

					drawApplicationDefinition = drawApplicationDefinitionSubmitted;
					break;
				}
			}

			if (null != drawApplicationDefinition
					&& !StringUtils.isEmpty(drawApplicationDefinition.getApplicationId())) {// Update scenario

				log.debug("drawApplication's application Id -> {} will be updated for processId -> {}",
						drawApplication.getApplicationId(), drawApplicationInformation.getProcessId());
				drawApplicationList.add(drawApplicationTransformer
						.transformToDrawApplicationAsUpdate(drawApplicationDefinition, drawApplication));
			}

		});

		drawApplicationDefinitionList.forEach(drawApplicationDefinition -> {

			if (StringUtils.isEmpty(drawApplicationDefinition.getApplicationId())) {// Save scenario

				log.debug("drawApplication will be saved for processId -> {}",
						drawApplicationInformation.getProcessId());

				DrawApplication savedDrawApplication = drawApplicationTransformer
						.transformToDrawApplication(drawApplicationDefinition);
				drawApplicationList.add(savedDrawApplication);

			}

		});

		Iterable<DrawApplication> processedDrawApplicationIterable = drawApplicationPagingAndSortingRepository
				.saveAll(drawApplicationList);

		List<DrawApplicationDefinition> processedDrawApplicationDefinitionList = new ArrayList<DrawApplicationDefinition>();
		processedDrawApplicationIterable.forEach(processedDrawApplication -> {

			processedDrawApplicationDefinitionList
					.add(drawApplicationTransformer.transformToDrawApplicationDefinition(processedDrawApplication));
		});

		DrawApplicationInformation processedDrawApplicationInformation = new DrawApplicationInformation();
		drawApplicationInformation.setDrawApplicationDefinitions(processedDrawApplicationDefinitionList);

		return new ResponseEntity<DrawApplicationInformation>(processedDrawApplicationInformation, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> bulkUpdateApplicationsOptional(PageInformation pageInformation) {

		log.info("Calling bulkUpdateApplications for pageInformation -> {}", pageInformation.getPageQueryParams());

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> appIdList = DSUtil.extractPageQueryParamValueOptionalAsList(pageQueryParams,
					AppConstants.APPLICATIONIDS_PARAM_NAME);

			List<String> drawReferenceList = DSUtil.extractPageQueryParamValueOptionalAsList(pageQueryParams,
					AppConstants.DRAWREFERENCE_PARAM_NAME);

			List<String> applicationStatusList = DSUtil.extractPageQueryParamValueOptionalAsList(pageQueryParams,
					AppConstants.APPLICATIONSTATUS_PARAM_NAME);

			List<String> bulkBatchIdList = DSUtil.extractPageQueryParamValueOptionalAsList(pageQueryParams,
					AppConstants.ECFNAME_BULKBATCHID);

			if (null != drawReferenceList && !drawReferenceList.isEmpty()) {

				if (drawReferenceList.size() > 1) {

					throw new InvalidInputException("Only one Draw Reference allowed");
				}
			}

			if (null != applicationStatusList && !applicationStatusList.isEmpty()) {

				if (applicationStatusList.size() > 1) {

					throw new InvalidInputException("Only one ApplicationStatus allowed");
				}
			}

			if (null != bulkBatchIdList && !bulkBatchIdList.isEmpty()) {

				if (bulkBatchIdList.size() > 1) {

					throw new InvalidInputException("Only one BulkBatchIdList allowed");
				}
			}

			AtomicReference<String> drawReference = new AtomicReference<String>();
			if (null != drawReferenceList && !drawReferenceList.isEmpty()) {

				drawReference.set(drawReferenceList.get(0));
			}

			AtomicReference<String> applicationStatus = new AtomicReference<String>();
			if (null != applicationStatusList && !applicationStatusList.isEmpty()) {

				applicationStatus.set(applicationStatusList.get(0));
			}

			AtomicReference<String> bulkBatchId = new AtomicReference<String>();
			if (null != bulkBatchIdList && !bulkBatchIdList.isEmpty()) {

				bulkBatchId.set(bulkBatchIdList.get(0));
			}

			doBulkUpdateApplication(appIdList, drawReference, applicationStatus, bulkBatchId);

		}

		return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.OK);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<String> bulkUpdateSaveApplicationStatus(PageInformation pageInformation) {

		log.info("Calling bulkUpdateSaveApplicationStatus for pageInformation -> {}",
				pageInformation.getPageQueryParams());

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> appIdList = DSUtil.extractPageQueryParamValueOptionalAsList(pageQueryParams,
					AppConstants.APPLICATIONIDS_PARAM_NAME);

			List<String> applicationStatusList = DSUtil.extractPageQueryParamValueOptionalAsList(pageQueryParams,
					AppConstants.APPLICATIONSTATUS_PARAM_NAME);

			String processId = DSUtil
					.extractPageQueryParamValueOptionalAsList(pageQueryParams, AppConstants.PROCESSID_PARAM_NAME).get(0);

			log.info("bulkUpdateSaveApplicationStatus called for processId -> {}", processId);

			if (null != applicationStatusList && !applicationStatusList.isEmpty()) {

				if (applicationStatusList.size() > 1) {

					throw new InvalidInputException("Only one ApplicationStatus allowed");
				}
			}

			drawApplicationPagingAndSortingRepository.updateDrawApplicationStatus(applicationStatusList.get(0),
					appIdList);
		}

		return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> bulkUpdateApplications(PageInformation pageInformation) {

		log.info("Calling bulkUpdateApplications for pageInformation -> {}", pageInformation.getPageQueryParams());

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> appIdList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.APPLICATIONIDS_PARAM_NAME);

			List<String> drawReferenceList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.DRAWREFERENCE_PARAM_NAME);

			List<String> applicationStatusList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.APPLICATIONSTATUS_PARAM_NAME);

			if (null != drawReferenceList) {

				if (drawReferenceList.size() > 1) {

					throw new InvalidInputException("Only one Draw Reference allowed");
				}
			}

			if (null != applicationStatusList) {

				if (applicationStatusList.size() > 1) {

					throw new InvalidInputException("Only one ApplicationStatus allowed");
				}
			}

			AtomicReference<String> drawReference = new AtomicReference<String>();
			if (null != drawReferenceList && !drawReferenceList.isEmpty()) {

				drawReference.set(drawReferenceList.get(0));
			}

			AtomicReference<String> applicationStatus = new AtomicReference<String>();
			if (null != applicationStatusList && !applicationStatusList.isEmpty()) {

				applicationStatus.set(applicationStatusList.get(0));
			}

			doBulkUpdateApplication(appIdList, drawReference, applicationStatus, null);

		}

		return new ResponseEntity<String>(AppConstants.SUCCESS_VALUE, HttpStatus.OK);
	}

	private void doBulkUpdateApplication(List<String> appIdList, AtomicReference<String> drawReference,
			AtomicReference<String> applicationStatus, AtomicReference<String> bulkBatchId) {

		log.info("Calling doBulkUpdateApplication for appIdList -> {}", appIdList);

		Pageable pageable = PageRequest.of(0, appIdList.size());
		Slice<DrawApplication> drawApplicationSlice = drawApplicationPagingAndSortingRepository
				.findAllByApplicationIdIn(appIdList, pageable);

		if (drawApplicationSlice.hasContent()) {

			List<DrawApplication> savedApplicationList = drawApplicationSlice.getContent();
			savedApplicationList.forEach(savedDrawApplication -> {

				if (!StringUtils.isEmpty(drawReference.get())) {

					savedDrawApplication.setDrawReference(drawReference.get());
				}
				if (!StringUtils.isEmpty(applicationStatus.get())) {

					savedDrawApplication.setApplicationStatus(applicationStatus.get());
				}

				if (null != bulkBatchId && !StringUtils.isEmpty(bulkBatchId.get())) {

					savedDrawApplication.setBulkBatchId(bulkBatchId.get());
				}

				if (ApplicationStatus.APP_DRAW_DUPLICATE.toString().equalsIgnoreCase(applicationStatus.get())) {

					savedDrawApplication.setDuplicateRecord(true);
				}
			});

			drawApplicationPagingAndSortingRepository.saveAll(savedApplicationList);
		}
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<DrawApplicationDefinition> findByApplicationId(String applicationId) {

		log.info("Finding DrawApplication for applicationId -> {}", applicationId);
		return drawApplicationPagingAndSortingRepository.findById(applicationId).map(envelopeData -> {

			return new ResponseEntity<DrawApplicationDefinition>(
					drawApplicationTransformer.transformToDrawApplicationDefinition(envelopeData), HttpStatus.OK);
		}).orElseThrow(
				() -> new ResourceNotFoundException("No drawapplication found for applicationId# " + applicationId));

	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<DrawApplicationDefinition> findByEnvelopeId(String envelopeId) {

		log.info("Finding DrawApplication for envelopeId -> {}", envelopeId);

		List<DrawApplication> drawApplicationList = drawApplicationPagingAndSortingRepository
				.findAllByTriggerEnvelopeIdOrBridgeEnvelopeId(envelopeId, envelopeId);

		if (null != drawApplicationList && !drawApplicationList.isEmpty()) {

			if (drawApplicationList.size() > 1) {

				String applicationIds = drawApplicationList.stream().map(DrawApplication::getApplicationId)
						.collect(Collectors.joining(AppConstants.PIPE_DELIMITER));

				String bridgeEnvelopeIds = drawApplicationList.stream().map(DrawApplication::getBridgeEnvelopeId)
						.filter(bridgeId -> !StringUtils.isEmpty(bridgeId))
						.collect(Collectors.joining(AppConstants.PIPE_DELIMITER));

				String applicationStatuses = drawApplicationList.stream().map(DrawApplication::getApplicationStatus)
						.collect(Collectors.joining(AppConstants.PIPE_DELIMITER));

				DrawApplicationDefinition drawApplicationDefinition = new DrawApplicationDefinition();
				drawApplicationDefinition.setApplicationStatus(applicationStatuses);
				drawApplicationDefinition.setApplicationId(applicationIds);
				drawApplicationDefinition.setBridgeEnvelopeId(bridgeEnvelopeIds);
				drawApplicationDefinition.setTriggerEnvelopeId(drawApplicationList.get(0).getTriggerEnvelopeId());
				drawApplicationDefinition.setNoteAttribute(
						dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.KNOWN_ISSUE_DESC));

				return new ResponseEntity<DrawApplicationDefinition>(drawApplicationDefinition, HttpStatus.OK);

			} else {

				return new ResponseEntity<DrawApplicationDefinition>(
						drawApplicationTransformer.transformToDrawApplicationDefinition(drawApplicationList.get(0)),
						HttpStatus.OK);
			}

		} else {

			return createEnvelopeUnknownResponse();
		}

	}

	private ResponseEntity<DrawApplicationDefinition> createEnvelopeUnknownResponse() {

		DrawApplicationDefinition drawApplicationDefinition = new DrawApplicationDefinition();
		drawApplicationDefinition.setApplicationStatus(ApplicationStatus.ENVELOPE_UNKNOWN.toString());
		return new ResponseEntity<DrawApplicationDefinition>(drawApplicationDefinition, HttpStatus.OK);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<DrawApplicationDefinition> findByTriggerEnvelopeId(String triggerEnvelopeId) {

		log.info("Finding DrawApplication for triggerEnvelopeId -> {}", triggerEnvelopeId);
		return drawApplicationPagingAndSortingRepository.findByTriggerEnvelopeId(triggerEnvelopeId)
				.map(envelopeData -> {

					return new ResponseEntity<DrawApplicationDefinition>(
							drawApplicationTransformer.transformToDrawApplicationDefinition(envelopeData),
							HttpStatus.OK);
				}).orElseThrow(() -> new ResourceNotFoundException(
						"No drawapplication found for triggerEnvelopeId# " + triggerEnvelopeId));
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<DrawApplicationDefinition> findByBridgeEnvelopeId(String bridgeEnvelopeId) {

		log.info("Finding DrawApplication for bridgeEnvelopeId -> {}", bridgeEnvelopeId);
		return drawApplicationPagingAndSortingRepository.findByBridgeEnvelopeId(bridgeEnvelopeId).map(envelopeData -> {

			return new ResponseEntity<DrawApplicationDefinition>(
					drawApplicationTransformer.transformToDrawApplicationDefinition(envelopeData), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException(
				"No drawapplication found for bridgeEnvelopeId# " + bridgeEnvelopeId));
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<DrawApplicationDefinition> findByBulkBatchId(String bulkBatchId) {

		log.info("Finding DrawApplication for bulkBatchId -> {}", bulkBatchId);
		return drawApplicationPagingAndSortingRepository.findByBulkBatchId(bulkBatchId).map(envelopeData -> {

			return new ResponseEntity<DrawApplicationDefinition>(
					drawApplicationTransformer.transformToDrawApplicationDefinition(envelopeData), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No drawapplication found for bulkBatchId# " + bulkBatchId));
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<DrawApplicationInformation> findAllByApplicationIdIn(PageInformation pageInformation) {

		log.info("FindAll DrawApplication for applicationIds -> {}", pageInformation.getPageQueryParams());

		DrawApplicationInformation drawApplicationInformation = new DrawApplicationInformation();
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> appIdList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.APPLICATIONIDS_PARAM_NAME);

			Slice<DrawApplication> drawApplicationSlice = drawApplicationPagingAndSortingRepository
					.findAllByApplicationIdIn(appIdList, pageable);

			prepareResponseFromSlice(drawApplicationInformation, drawApplicationSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DrawApplicationInformation>(drawApplicationInformation, HttpStatus.OK);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<Long> countByApplicationStatus(String applicationStatus) {

		log.info("Counting DrawApplication for applicationStatus -> {}", applicationStatus);

		return new ResponseEntity<Long>(
				drawApplicationPagingAndSortingRepository.countByApplicationStatus(applicationStatus), HttpStatus.OK);

	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<DrawApplicationInformation> findAllByApplicationStatuses(PageInformation pageInformation) {

		log.info("FindAll DrawApplication for applicationStatuses -> {}", pageInformation.getPageQueryParams());

		DrawApplicationInformation drawApplicationInformation = new DrawApplicationInformation();
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> appStatusList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.APPLICATIONSTATUSES_PARAM_NAME);

			Slice<DrawApplication> drawApplicationSlice = drawApplicationPagingAndSortingRepository
					.findAllByApplicationStatusIn(appStatusList, pageable);

			prepareResponseFromSlice(drawApplicationInformation, drawApplicationSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DrawApplicationInformation>(drawApplicationInformation, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<DrawApplicationIdResult> findAllDrawApplicationIdsByApplicationStatuses(
			PageInformation pageInformation) {

		DrawApplicationIdResult drawApplicationIdResult = new DrawApplicationIdResult();
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> appStatusList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.APPLICATIONSTATUSES_PARAM_NAME);

			Iterable<DrawApplicationIdProjection> drawApplicationIterable = drawApplicationPagingAndSortingRepository
					.findApplicationIdByApplicationStatusIn(appStatusList);

			prepareResponseFromSlice(drawApplicationIdResult, drawApplicationIterable);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DrawApplicationIdResult>(drawApplicationIdResult, HttpStatus.OK);
	}

	private void prepareResponseFromSlice(DrawApplicationIdResult drawApplicationIdResult,
			Iterable<DrawApplicationIdProjection> drawApplicationIterable) {

		if (null != drawApplicationIterable && null != drawApplicationIterable.iterator()
				&& drawApplicationIterable.iterator().hasNext()) {

			List<String> applicationIds = StreamSupport.stream(drawApplicationIterable.spliterator(), false)
					.map(DrawApplicationIdProjection::getApplicationId).collect(Collectors.toList());

			drawApplicationIdResult.setTotalRecords(Long.valueOf(applicationIds.size()));
			drawApplicationIdResult.setDrawApplicationIds(applicationIds);
		}
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<Long> countByLanguageCode(String languageCode) {

		log.info("Counting DrawApplication for languageCode -> {}", languageCode);

		return new ResponseEntity<Long>(drawApplicationPagingAndSortingRepository.countByLanguageCode(languageCode),
				HttpStatus.OK);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<DrawApplicationInformation> findAllByLanguageCodes(PageInformation pageInformation) {

		log.info("FindAll DrawApplication for languageCodes -> {}", pageInformation.getPageQueryParams());

		DrawApplicationInformation drawApplicationInformation = new DrawApplicationInformation();
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> langCodeList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.LANGUAGECODES_PARAM_NAME);

			Slice<DrawApplication> drawApplicationSlice = drawApplicationPagingAndSortingRepository
					.findAllByLanguageCodeIn(langCodeList, pageable);

			prepareResponseFromSlice(drawApplicationInformation, drawApplicationSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DrawApplicationInformation>(drawApplicationInformation, HttpStatus.OK);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<Long> countByAgentCode(String agentCode) {

		log.info("Counting DrawApplication for agentCode -> {}", agentCode);

		return new ResponseEntity<Long>(drawApplicationPagingAndSortingRepository.countByAgentCode(agentCode),
				HttpStatus.OK);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<DrawApplicationInformation> findAllByAgentCodes(PageInformation pageInformation) {

		log.info("FindAll DrawApplication for agentCodes -> {}", pageInformation.getPageQueryParams());

		DrawApplicationInformation drawApplicationInformation = new DrawApplicationInformation();
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> agentCodeList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.AGENTCODES_PARAM_NAME);

			Slice<DrawApplication> drawApplicationSlice = drawApplicationPagingAndSortingRepository
					.findAllByAgentCodeIn(agentCodeList, pageable);

			prepareResponseFromSlice(drawApplicationInformation, drawApplicationSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DrawApplicationInformation>(drawApplicationInformation, HttpStatus.OK);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<Long> countByDrawReference(String drawReference) {

		log.info("Counting DrawApplication for drawReference -> {}", drawReference);

		return new ResponseEntity<Long>(drawApplicationPagingAndSortingRepository.countByDrawReference(drawReference),
				HttpStatus.OK);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<DrawApplicationInformation> findAllByDrawReferences(PageInformation pageInformation) {

		log.info("FindAll DrawApplication for drawreferences -> {}", pageInformation.getPageQueryParams());

		DrawApplicationInformation drawApplicationInformation = new DrawApplicationInformation();
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> drawReferenceList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.DRAWREFERENCES_PARAM_NAME);

			Slice<DrawApplication> drawApplicationSlice = drawApplicationPagingAndSortingRepository
					.findAllByDrawReferenceIn(drawReferenceList, pageable);

			prepareResponseFromSlice(drawApplicationInformation, drawApplicationSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DrawApplicationInformation>(drawApplicationInformation, HttpStatus.OK);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<Long> countByProgramType(String programType) {

		log.info("Counting DrawApplication for programType -> {}", programType);

		return new ResponseEntity<Long>(drawApplicationPagingAndSortingRepository.countByProgramType(programType),
				HttpStatus.OK);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	public ResponseEntity<DrawApplicationInformation> findAllByProgramTypes(PageInformation pageInformation) {

		log.info("FindAll DrawApplication for programTypes -> {}", pageInformation.getPageQueryParams());

		DrawApplicationInformation drawApplicationInformation = new DrawApplicationInformation();
		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> programTypeList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.PROGRAMTYPES_PARAM_NAME);

			Slice<DrawApplication> drawApplicationSlice = drawApplicationPagingAndSortingRepository
					.findAllByProgramTypeIn(programTypeList, pageable);

			prepareResponseFromSlice(drawApplicationInformation, drawApplicationSlice);
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DrawApplicationInformation>(drawApplicationInformation, HttpStatus.OK);
	}

	private void prepareResponseFromSlice(DrawApplicationInformation drawApplicationInformation,
			Slice<DrawApplication> drawApplicationSlice) {

		log.info("Prepared Respose from Slice -> {}", drawApplicationSlice);

		if (null != drawApplicationSlice && !drawApplicationSlice.isEmpty() && drawApplicationSlice.hasContent()) {

			List<DrawApplicationDefinition> dsApplicationDefinitionList = new ArrayList<DrawApplicationDefinition>();
			drawApplicationSlice.getContent().forEach(drawApplication -> {

				dsApplicationDefinitionList
						.add(drawApplicationTransformer.transformToDrawApplicationDefinition(drawApplication));
			});

			drawApplicationInformation.setCurrentPage(Long.valueOf(drawApplicationSlice.getNumber()));
			drawApplicationInformation.setNextAvailable(drawApplicationSlice.hasNext());
			drawApplicationInformation.setContentAvailable(true);
			drawApplicationInformation.setDrawApplicationDefinitions(dsApplicationDefinitionList);

		} else {

			drawApplicationInformation.setContentAvailable(false);
			drawApplicationInformation.setDrawApplicationDefinitions(null);
		}
	}

}