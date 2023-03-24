package com.ds.proserv.envelopedata.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.NewVersionExistException;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.envelopedata.model.DSEnvelope;
import com.ds.proserv.envelopedata.projection.DSEnvelopeProjection;
import com.ds.proserv.envelopedata.repository.DSEnvelopePagingAndSortingRepository;
import com.ds.proserv.envelopedata.repository.DSEnvelopeRepository;
import com.ds.proserv.envelopedata.service.DSDataHelperService;
import com.ds.proserv.envelopedata.service.EnvelopeBulkDataService;
import com.ds.proserv.envelopedata.service.PrepareEnvelopeTreeService;
import com.ds.proserv.envelopedata.transformer.DSEnvelopeTransformer;
import com.ds.proserv.envelopedata.transformer.PageableTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSEnvelopeInformation;
import com.ds.proserv.feign.envelopedata.service.DSEnvelopeService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Slf4j
public class DSEnvelopeController implements DSEnvelopeService {

	/*
	 * @Autowired private DSCacheManager dsCacheManager;
	 */

	@Autowired
	private DSDataHelperService dsDataHelperService;

	@Autowired
	private EnvelopeBulkDataService envelopeBulkDataService;

	@Autowired
	private PrepareEnvelopeTreeService prepareEnvelopeTreeService;

	@Autowired
	private PageableTransformer pageableTransformer;

	@Autowired
	private DSEnvelopeTransformer dsEnvelopeTransformer;

	@Autowired
	private DSEnvelopeRepository dsEnvelopeRepository;

	@Autowired
	private DSEnvelopePagingAndSortingRepository dsEnvelopePagingAndSortingRepository;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSEnvelopeInformation> bulkUpdateSaveEnvelopeDataV2(
			DSEnvelopeInformation dsEnvelopeInformation) {

		log.info("Inside bulkUpdateSaveEnvelopeDataV2 for processId -> {}", dsEnvelopeInformation.getProcessId());

		try {

			String result = envelopeBulkDataService.bulkUpdateSaveEnvelopeData(dsEnvelopeInformation).get();

			log.info("Envelopes successfully saved for processId -> {}", dsEnvelopeInformation.getProcessId());

			DSEnvelopeInformation dsEnvelopeInformationResponse = new DSEnvelopeInformation();
			dsEnvelopeInformationResponse.setResult(result);
			return new ResponseEntity<DSEnvelopeInformation>(dsEnvelopeInformationResponse, HttpStatus.OK);

		} catch (NewVersionExistException exp) {

			log.error("NewVersionExistException -> {} occurred for processId -> {}", exp,
					dsEnvelopeInformation.getProcessId());
			DSEnvelopeInformation dsEnvelopeInformationResponse = new DSEnvelopeInformation();
			dsEnvelopeInformationResponse.setResult(AppConstants.STALE_MESSAGE);
			return new ResponseEntity<DSEnvelopeInformation>(dsEnvelopeInformationResponse, HttpStatus.OK);
		} catch (Throwable exp) {

			if (exp.getCause() instanceof NewVersionExistException) {

				log.error("NewVersionExistException inside throwable -> {} occurred for processId -> {}", exp,
						dsEnvelopeInformation.getProcessId());
				DSEnvelopeInformation dsEnvelopeInformationResponse = new DSEnvelopeInformation();
				dsEnvelopeInformationResponse.setResult(AppConstants.STALE_MESSAGE);
				return new ResponseEntity<DSEnvelopeInformation>(dsEnvelopeInformationResponse, HttpStatus.OK);
			} else {

				log.error("Exception -> {} occurred for processId -> {}", exp, dsEnvelopeInformation.getProcessId());
				exp.printStackTrace();
				DSEnvelopeInformation dsEnvelopeInformationResponse = new DSEnvelopeInformation();
				dsEnvelopeInformationResponse.setResult(AppConstants.FAILURE_VALUE);
				return new ResponseEntity<DSEnvelopeInformation>(dsEnvelopeInformationResponse, HttpStatus.OK);
			}

		}

	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<DSEnvelopeDefinition> findEnvelopeByEnvelopeId(String envelopeId) {

		try {

			if (isReadEnvelopeByEnvIdsSPEnabled()) {

				List<String> envelopeIds = new ArrayList<String>();
				envelopeIds.add(envelopeId);

				List<DSEnvelope> savedDSEnvelopeList = dsDataHelperService.findAllEnvelopesByEnvelopeIdsBySP(null,
						envelopeIds);

				if (null != savedDSEnvelopeList && !savedDSEnvelopeList.isEmpty()) {

					log.info("Successfully fetched data for envelopeIds -> {}", envelopeIds);

					DSEnvelopeDefinition dsEnvelopeDefinition = dsEnvelopeTransformer
							.transformToDSEnvelopeDefinition(savedDSEnvelopeList.get(0));
					dsEnvelopeDefinition.setResult(AppConstants.SUCCESS_VALUE);
					return new ResponseEntity<DSEnvelopeDefinition>(dsEnvelopeDefinition, HttpStatus.OK);
				} else {

					log.info("No Data exists for envelopeIds -> {}", envelopeIds);
					return prepareEmptyDSEnvelopeDefinitionResponse();
				}

			} else {

				return dsEnvelopeRepository.findById(envelopeId).map(envelopeData -> {

					DSEnvelopeDefinition dsEnvelopeDefinition = dsEnvelopeTransformer
							.transformToDSEnvelopeDefinition(envelopeData);
					dsEnvelopeDefinition.setResult(AppConstants.SUCCESS_VALUE);
					return new ResponseEntity<DSEnvelopeDefinition>(dsEnvelopeDefinition, HttpStatus.OK);
				}).orElse(prepareEmptyDSEnvelopeDefinitionResponse());

			}

		} catch (Throwable exp) {

			log.error("Exception -> {} occurred for envelopeId -> {}", envelopeId);
			DSEnvelopeDefinition dsEnvelopeDefinition = new DSEnvelopeDefinition();
			dsEnvelopeDefinition.setResult(AppConstants.FAILURE_VALUE);

			return new ResponseEntity<DSEnvelopeDefinition>(dsEnvelopeDefinition, HttpStatus.NO_CONTENT);
		}

	}

	private ResponseEntity<DSEnvelopeDefinition> prepareEmptyDSEnvelopeDefinitionResponse() {

		DSEnvelopeDefinition dsEnvelopeDefinition = new DSEnvelopeDefinition();
		dsEnvelopeDefinition.setResult(AppConstants.EMPTY_VALUE);

		return new ResponseEntity<DSEnvelopeDefinition>(dsEnvelopeDefinition, HttpStatus.OK);
	}

	private boolean isReadEnvelopeByEnvIdsSPEnabled() {

		/*
		 * String enableEnvelopeEnvIdsBySP = dsCacheManager
		 * .prepareAndRequestCacheDataByKey(PropertyCacheConstants.
		 * ENVDATA_SELECTBYENVIDS_STOREDPROC);
		 * 
		 * if (!StringUtils.isEmpty(enableEnvelopeEnvIdsBySP)) {
		 * 
		 * return Boolean.parseBoolean(enableEnvelopeEnvIdsBySP); }
		 */

		return true;
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<DSEnvelopeInformation> findEnvelopesByEnvelopeIds(PageInformation pageInformation) {

		log.info("findEnvelopesByEnvelopeIds -> {}", pageInformation.getPageQueryParams());

		DSEnvelopeInformation dsEnvelopeInformation = new DSEnvelopeInformation();

		Pageable pageable = pageableTransformer.tranformToPageable(pageInformation);

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			Slice<DSEnvelope> dsEnvelopeSlice = fetchEnvelopesSlice(pageable, pageQueryParams);

			if (null != dsEnvelopeSlice && !dsEnvelopeSlice.isEmpty() && dsEnvelopeSlice.hasContent()) {

				setSliceInformation(dsEnvelopeInformation, dsEnvelopeSlice);

				List<DSEnvelopeDefinition> dsEnvelopeDefinitionList = new ArrayList<DSEnvelopeDefinition>();
				dsEnvelopeSlice.getContent().forEach(dsEnvelope -> {

					dsEnvelopeDefinitionList.add(dsEnvelopeTransformer.transformToDSEnvelopeDefinition(dsEnvelope));
				});
				dsEnvelopeInformation.setDsEnvelopeDefinitions(dsEnvelopeDefinitionList);
			} else {

				dsEnvelopeInformation.setContentAvailable(false);
				dsEnvelopeInformation.setDsEnvelopeDefinitions(null);
			}
		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

		return new ResponseEntity<DSEnvelopeInformation>(dsEnvelopeInformation, HttpStatus.OK);
	}

	private void setSliceInformation(DSEnvelopeInformation dsEnvelopeInformation, Slice<DSEnvelope> dsEnvelopeSlice) {

		dsEnvelopeInformation.setContentAvailable(true);
		dsEnvelopeInformation.setCurrentPage(Long.valueOf(dsEnvelopeSlice.getNumber()));
		dsEnvelopeInformation.setNextAvailable(dsEnvelopeSlice.hasNext());
	}

	private Slice<DSEnvelope> fetchEnvelopesSlice(Pageable pageable, List<PageQueryParam> pageQueryParams) {

		List<String> envelopeIdList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
				AppConstants.ENVELOPEIDS_PARAM_NAME);

		Slice<DSEnvelope> dsEnvelopeSlice = dsEnvelopePagingAndSortingRepository.findAllByEnvelopeIdIn(envelopeIdList,
				pageable);
		return dsEnvelopeSlice;
	}

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
	public ResponseEntity<DSEnvelopeInformation> findEnvelopesTreeListByEnvelopeIds(PageInformation pageInformation) {

		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		if (null != pageQueryParams && !pageQueryParams.isEmpty()) {

			List<String> envelopeIds = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
					AppConstants.ENVELOPEIDS_PARAM_NAME);

			log.info("Preparing tree structure for envelopeIds -> {}", envelopeIds);
			List<DSEnvelopeProjection> dsEnvelopeProjectionList = dsEnvelopeRepository
					.getDSEnvelopeTreeByProjection(envelopeIds);

			if (null != dsEnvelopeProjectionList && !dsEnvelopeProjectionList.isEmpty()) {

				DSEnvelopeInformation dsEnvelopeInformation = prepareEnvelopeTreeService
						.convertToDSEnvelopeInformation(dsEnvelopeProjectionList, envelopeIds.size());

				return new ResponseEntity<DSEnvelopeInformation>(dsEnvelopeInformation, HttpStatus.OK);

			} else {

				DSEnvelopeInformation dsEnvelopeInformation = new DSEnvelopeInformation();
				dsEnvelopeInformation.setTotalRecords(0L);
				return new ResponseEntity<DSEnvelopeInformation>(dsEnvelopeInformation, HttpStatus.NO_CONTENT);
			}

		} else {

			throw new InvalidInputException("pageQueryParams missing in the request body");
		}

	}

}