package com.ds.proserv.feign.util;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;
import com.ds.proserv.feign.coredata.domain.ConcurrentProcessFailureLogDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageUtil {

	public static ConcurrentProcessFailureLogDefinition createFailureMessage(String accountId, String batchId,
			String processId, Throwable exp, FailureCode failureCode, FailureStep failureStep, String failureReason) {

		log.error(
				"Failure occurred for accountId -> {} and batchId {} with failureCode -> {}, failureReason -> {}, exceptionMessage is {} and cause is {}",
				accountId, batchId, failureCode, exp.getMessage(), exp.getMessage(), exp);

		ConcurrentProcessFailureLogDefinition concurrentProcessFailureLogDefinition = new ConcurrentProcessFailureLogDefinition();

		concurrentProcessFailureLogDefinition.setBatchId(batchId);
		concurrentProcessFailureLogDefinition.setFailureCode(failureCode.toString());
		concurrentProcessFailureLogDefinition.setFailureDateTime(LocalDateTime.now().toString());

		if(!StringUtils.isEmpty(failureReason)) {
			
			concurrentProcessFailureLogDefinition.setFailureReason(failureReason);
		}else {
			
			if (StringUtils.isEmpty(exp.getMessage())) {

				concurrentProcessFailureLogDefinition.setFailureReason(exp.toString());
			} else {

				concurrentProcessFailureLogDefinition.setFailureReason(exp.getMessage());
			}
		}
		
		concurrentProcessFailureLogDefinition.setFailureRecordId(accountId);
		concurrentProcessFailureLogDefinition.setFailureStep(failureStep.toString());

		if (!StringUtils.isEmpty(processId)) {

			concurrentProcessFailureLogDefinition.setProcessId(processId);
		} else {

			concurrentProcessFailureLogDefinition.setProcessId("PROCESSNOTCREATED");
		}

		return concurrentProcessFailureLogDefinition;
	}
}