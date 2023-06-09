package com.ds.proserv.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ResourceNotSavedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3330089138654965021L;

	public ResourceNotSavedException(String message) {
		super(message);
	}

	public ResourceNotSavedException(String message, Throwable cause) {
		super(message, cause);
	}
}