package com.ds.proserv.coredata.validator;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.ds.proserv.feign.coredata.domain.ScheduledBatchLogRequest;
import com.ds.proserv.feign.validator.IDocuSignValidator;

@Service
public class BatchLogValidator implements IDocuSignValidator<ScheduledBatchLogRequest> {

	@Override
	public void validateSaveData(ScheduledBatchLogRequest scheduledBatchLogRequest) {

		Assert.notNull(scheduledBatchLogRequest.getBatchType(), "BatchType cannot be null");
		Assert.notNull(scheduledBatchLogRequest.getBatchStartParameters(), "BatchStartParameters cannot be null");
	}

}