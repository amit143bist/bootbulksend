package com.ds.proserv.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.ALREADY_REPORTED)
public class NewVersionExistException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6056131556878754818L;

	public NewVersionExistException(String message) {
		super(message);
	}

	public NewVersionExistException(String message, Throwable cause) {
		super(message, cause);
	}

}