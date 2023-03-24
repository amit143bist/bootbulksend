package com.ds.proserv.coredata.validator;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogDefinition;
import com.ds.proserv.feign.validator.IDocuSignValidator;

@Service
public class FailureLogValidator implements IDocuSignValidator<ConcurrentProcessFailureLogDefinition> {

	@Override
	public void validateSaveData(ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogRequest) {

		Assert.notNull(concurrentProcessFailureLogRequest.getFailureCode(), "FailureCode cannot be null");
		Assert.notNull(concurrentProcessFailureLogRequest.getFailureDateTime(), "FailureDateTime cannot be null");
		Assert.notNull(concurrentProcessFailureLogRequest.getFailureStep(), "FailureStep cannot be null");
	}

}