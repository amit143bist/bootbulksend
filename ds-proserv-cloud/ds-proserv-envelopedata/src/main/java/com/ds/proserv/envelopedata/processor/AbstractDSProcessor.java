package com.ds.proserv.envelopedata.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.exception.ResourceNotSavedException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public abstract class AbstractDSProcessor implements IDSProcessor {

	@Autowired
	protected TaskExecutor processorAsyncExecutor;

	public void handleAsyncStatus(String asyncStatus, Throwable exp, String callingMethodName, String processId) {

		if (null != exp) {

			log.info("Async processing got exception in {} for processId -> {}", callingMethodName, processId);

			exp.printStackTrace();
			throw new ResourceNotSavedException(exp.getMessage());

		} else {

			if (AppConstants.SUCCESS_VALUE.equalsIgnoreCase(asyncStatus)) {

				log.info(
						" $$$$$$$$$$$$$$$$$$$$$$$$$ Async processing completed in {} for processId -> {} $$$$$$$$$$$$$$$$$$$$$$$$$ ",
						callingMethodName, processId);
			} else {

				log.warn("Result is NOT success, it is {}, check logs for more information for {} and processId -> {}",
						asyncStatus, callingMethodName, processId);
			}

		}
	}

}