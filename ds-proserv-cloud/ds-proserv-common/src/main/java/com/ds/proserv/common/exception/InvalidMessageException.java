package com.ds.proserv.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.ds.proserv.common.constant.FailureCode;
import com.ds.proserv.common.constant.FailureStep;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidMessageException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8895452419456378441L;

	private FailureCode failureCode;
	private FailureStep failureStep;

	public InvalidMessageException(String message) {
		super(message);
	}

	public InvalidMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidMessageException(String message, FailureCode failureCode, FailureStep failureStep) {

		super(message);
		this.failureCode = failureCode;
		this.failureStep = failureStep;
	}
}