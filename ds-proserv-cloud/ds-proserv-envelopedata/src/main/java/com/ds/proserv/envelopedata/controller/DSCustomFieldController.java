package com.ds.proserv.envelopedata.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.domain.PageInformation;
import com.ds.proserv.common.domain.PageQueryParam;
import com.ds.proserv.common.util.DSUtil;
import com.ds.proserv.envelopedata.model.DSCustomField;
import com.ds.proserv.envelopedata.repository.DSCustomFieldPagingAndSortingRepository;
import com.ds.proserv.envelopedata.transformer.DSCustomFieldTransformer;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldDefinition;
import com.ds.proserv.feign.envelopedata.domain.DSCustomFieldInformation;
import com.ds.proserv.feign.envelopedata.service.DSCustomFieldService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RolesAllowed("USER")
@Slf4j
@Transactional
public class DSCustomFieldController implements DSCustomFieldService {

	@Autowired
	private DSCustomFieldTransformer dsCustomFieldTransformer;

	@Autowired
	private DSCustomFieldPagingAndSortingRepository dsCustomFieldPagingAndSortingRepository;

	@Override
	@Retryable(value = { CannotAcquireLockException.class, PessimisticLockingFailureException.class,
			LockAcquisitionException.class }, backoff = @Backoff(delay = 500, maxDelay = 3000, multiplier = 2), maxAttempts = 5)
	public ResponseEntity<DSCustomFieldInformation> findCustomFieldsByEnvelopeIdsAndFieldName(
			PageInformation pageInformation) {

		log.info("Calling findCustomFieldsByEnvelopeIdsAndFieldName for pageInformation -> {}", pageInformation);
		List<PageQueryParam> pageQueryParams = pageInformation.getPageQueryParams();
		List<String> envelopeIdList = DSUtil.extractPageQueryParamValueAsList(pageQueryParams,
				AppConstants.ENVELOPEIDS_PARAM_NAME);

		String customFieldName = DSUtil.extractPageQueryParamValue(pageQueryParams,
				AppConstants.CUSTOMFIELD_PARAM_NAME);

		DSCustomFieldInformation dsCustomFieldInformation = new DSCustomFieldInformation();
		List<DSCustomField> savedDSCustomFields = dsCustomFieldPagingAndSortingRepository
				.findAllByEnvelopeIdInAndFieldName(envelopeIdList, customFieldName);

		if (null != savedDSCustomFields && !savedDSCustomFields.isEmpty()) {

			List<DSCustomFieldDefinition> dsCustomFieldDefinitionList = new ArrayList<DSCustomFieldDefinition>(
					envelopeIdList.size());
			savedDSCustomFields.forEach(savedDSCustomField -> {

				dsCustomFieldDefinitionList
						.add(dsCustomFieldTransformer.transformToDSCustomFieldDefinition(savedDSCustomField));
			});
			dsCustomFieldInformation.setDsCustomFieldDefinitions(dsCustomFieldDefinitionList);
			dsCustomFieldInformation.setTotalRecords(Long.valueOf(dsCustomFieldDefinitionList.size()));
			return new ResponseEntity<DSCustomFieldInformation>(dsCustomFieldInformation, HttpStatus.OK);
		} else {

			return new ResponseEntity<DSCustomFieldInformation>(dsCustomFieldInformation, HttpStatus.NO_CONTENT);
		}
	}

}