package com.ds.proserv.envelopedata.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.IterableUtil;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.ResourceNotFoundException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.envelopedata.model.DSTab;
import com.ds.proserv.envelopedata.repository.DSTabPagingAndSortingRepository;
import com.ds.proserv.envelopedata.repository.DSTabRepository;
import com.ds.proserv.envelopedata.transformer.DSTabTransformer;
import com.ds.proserv.envelopedata.transformer.PageableTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSTabDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSTabInformation;
import com.ds.proserv.feign.envelopedata.service.DSTabService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Transactional
@Slf4j
public class DSTabController implements DSTabService {

	@Autowired
	private DSTabTransformer dsTabTransformer = null;

	@Autowired
	private PageableTransformer pageableTransformer = null;

	@Autowired
	private DSTabRepository dsTabRepository = null;

	@Autowired
	private DSTabPagingAndSortingRepository dsTabPagingAndSortingRepository = null;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSTabDefinition> saveTabData(DSTabDefinition dsTabDefinition) {

		log.info("Saving DSTabDefinition for tabLabel -> {} and for envelopeId -> {}, recipientId -> {}",
				dsTabDefinition.getTabLabel(), dsTabDefinition.getEnvelopeId(), dsTabDefinition.getRecipientId());
		return Optional.ofNullable(dsTabRepository.save(dsTabTransformer.transformToDSTab(dsTabDefinition)))
				.map(dsTab -> {

					Assert.notNull(dsTab.getId(),
							"Id cannot be null for tablabel " + dsTabDefinition.getTabLabel() + " for envelopeId "
									+ dsTabDefinition.getEnvelopeId() + " and recipientId "
									+ dsTabDefinition.getRecipientId());

					return new ResponseEntity<DSTabDefinition>(dsTabTransformer.transformToDSTabDefinition(dsTab),
							HttpStatus.CREATED);
				})
				.orElseThrow(() -> new ResourceNotSavedException("DSTab not saved for tabLabel "
						+ dsTabDefinition.getTabLabel() + " and for envelopeId " + dsTabDefinition.getEnvelopeId()
						+ " and recipientId# " + dsTabDefinition.getRecipientId()));
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSTabDefinition> updateTabData(DSTabDefinition dsTabDefinition, String id) {

		log.info("updateTabData called for recipientId -> {} and for envelopeId -> {}",
				dsTabDefinition.getRecipientId(), dsTabDefinition.getEnvelopeId());

		return dsTabRepository.findById(id).map(dsTab -> {

			dsTabDefinition.setId(id);
			return new ResponseEntity<DSTabDefinition>(dsTabTransformer.transformToDSTabDefinition(
					dsTabRepository.save(dsTabTransformer.transformToDSTab(dsTabDefinition))), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No tab found with id# " + id + " for envelopeId# "
				+ dsTabDefinition.getEnvelopeId() + " and recipientId# " + dsTabDefinition.getRecipientId()));

	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSTabDefinition> findTabByTabId(String id) {

		log.info("findTabByTabId called for id -> {}", id);

		return dsTabRepository.findById(id).map(tabData -> {

			return new ResponseEntity<DSTabDefinition>(dsTabTransformer.transformToDSTabDefinition(tabData),
					HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException("No tab found in findTabByTabId with id# " + id));
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSTabInformation> findTabsByRecipientId(String recipientId) {

		log.info("findTabsByRecipientId called for recipientId -> {}", recipientId);

		Iterable<DSTab> dsTabIterable = dsTabRepository.findAllByRecipientId(recipientId);

		if (IterableUtil.isNullOrEmpty(dsTabIterable)) {

			throw new ResourceNotFoundException("dsTabList is empty or null for recipientId# " + recipientId);
		} else {

			return prepareDSTabInformation(dsTabIterable);

		}
	}

	private ResponseEntity<DSTabInformation> prepareDSTabInformation(Iterable<DSTab> dsTabIterable) {

		List<DSTabDefinition> dsTabDefinitionList = new ArrayList<DSTabDefinition>();
		dsTabIterable.forEach(dsTab -> {

			dsTabDefinitionList.add(dsTabTransformer.transformToDSTabDefinition(dsTab));
		});

		DSTabInformation dsTabInformation = new DSTabInformation();

		dsTabInformation.setDsTabDefinitions(dsTabDefinitionList);
		dsTabInformation.setTotalRecords(Long.valueOf(dsTabDefinitionList.size()));

		return new ResponseEntity<DSTabInformation>(dsTabInformation, HttpStatus.OK);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSTabInformation> findTabsByEnvelopeId(String envelopeId) {

		log.info("findTabsByEnvelopeId called for envelopeId -> {}", envelopeId);

		Iterable<DSTab> dsTabIterable = dsTabRepository.findAllByEnvelopeId(envelopeId);

		if (IterableUtil.isNullOrEmpty(dsTabIterable)) {

			throw new ResourceNotFoundException("dsTabList is empty or null for envelopeId# " + envelopeId);
		} else {

			return prepareDSTabInformation(dsTabIterable);
		}

	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSTabDefinition> findTabByTabLabelAndEnvelopeId(String tabLabel, String envelopeId) {

		log.info("findTabByTabLabelAndEnvelopeId called for tabLabel -> {] and envelopeId -> {}", tabLabel, envelopeId);

		return dsTabRepository.findByTabLabelAndEnvelopeId(tabLabel, envelopeId).map(dsTab -> {

			return new ResponseEntity<DSTabDefinition>(dsTabTransformer.transformToDSTabDefinition(dsTab),
					HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException(
				"No tab found with tabLabel# " + tabLabel + " and envelopeId# " + envelopeId));

	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT)
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSTabInformation> findTabsByEnvelopeIds(PageInformation pageInformation) {

		log.info("findTabsByEnvelopeIds -> {}", pageInformation.getPageQueryParams());

		DSTabInformation dsTabInformation = new DSTabInformation();

		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			PageQueryParam envelopeIdsParam = pageQueryParams.stream()
					.filter(pageQueryParam -> AppConstants.ENVELOPEIDS_PARAM_NAME
							.equalsIgnoreCase(pageQueryParam.getParamName()))
					.findAny().orElse(null);

			List<String> envelopeIdList = null;

			if (null == envelopeIdsParam || StringUtils.isEmpty(envelopeIdsParam.getParamValue())) {

				throw new InvalidInputException("envelopeIdsParam cannot be empty or null");
			} else {

				envelopeIdList = Stream.of(envelopeIdsParam.getParamValue().trim().split(","))
						.collect(Collectors.toList());
			}

			log.info("{} param value is {}", AppConstants.ENVELOPEIDS_PARAM_NAME, envelopeIdsParam);

			Slice<DSTab> dsTabSlice = dsTabPagingAndSortingRepository.findAllByEnvelopeIdIn(envelopeIdList, pageable);

			prepareResponseFromSlice(dsTabInformation, dsTabSlice);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DSTabInformation>(dsTabInformation, HttpStatus.OK);
	}

	private void prepareResponseFromSlice(DSTabInformation dsTabInformation, Slice<DSTab> dsTabSlice) {

		log.info("prepareResponseFromSlice called for {}", dsTabSlice);

		if (null != dsTabSlice && !dsTabSlice.isEmpty() && dsTabSlice.hasContent()) {

			List<DSTabDefinition> dsTabDefinitionList = new ArrayList<DSTabDefinition>();
			dsTabSlice.getContent().forEach(dsTab -> {

				dsTabDefinitionList.add(dsTabTransformer.transformToDSTabDefinition(dsTab));
			});

			dsTabInformation.setTotalRecords(Long.valueOf(dsTabDefinitionList.size()));
			dsTabInformation.setCurrentPage(Long.valueOf(dsTabSlice.getNumber()));
			dsTabInformation.setNextAvailable(dsTabSlice.hasNext());
			dsTabInformation.setContentAvailable(true);
			dsTabInformation.setDsTabDefinitions(dsTabDefinitionList);

		} else {

			dsTabInformation.setContentAvailable(false);
			dsTabInformation.setDsTabDefinitions(null);
		}
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, isolation = Isolation.READ_UNCOMMITTED)
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSTabInformation> findTabsByEnvelopeIdsAndTabLabels(PageInformation pageInformation) {

		log.info("findTabsByEnvelopeIdsAndTabLabels -> {}", pageInformation.getPageQueryParams());

		DSTabInformation dsTabInformation = new DSTabInformation();

		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> envelopeIdList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.ENVELOPEIDS_PARAM_NAME);

			log.info("{} param value is {}", AppConstants.ENVELOPEIDS_PARAM_NAME, envelopeIdList);

			List<String> tabLabelList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.TABLABELS_PARAM_NAME);

			log.info("{} param value is {}", AppConstants.TABLABELS_PARAM_NAME, tabLabelList);

			Slice<DSTab> dsTabSlice = dsTabPagingAndSortingRepository.findAllByEnvelopeIdInAndTabLabelIn(envelopeIdList,
					tabLabelList, pageable);

			prepareResponseFromSlice(dsTabInformation, dsTabSlice);

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DSTabInformation>(dsTabInformation, HttpStatus.OK);
	}

}