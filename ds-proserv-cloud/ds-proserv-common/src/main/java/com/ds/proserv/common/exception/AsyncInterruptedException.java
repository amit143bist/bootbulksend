package com.ds.proserv.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class AsyncInterruptedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3434712540996918191L;

	public AsyncInterruptedException(String message) {
		super(message);
	}

	public AsyncInterruptedException(String message, Throwable cause) {
		super(message, cause);
	}
}