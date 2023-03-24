package com.ds.proserv.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class JSFunctionProcessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8895452419456378441L;

	public JSFunctionProcessException(String message) {
		super(message);
	}

	public JSFunctionProcessException(String message, Throwable cause) {
		super(message, cause);
	}
}