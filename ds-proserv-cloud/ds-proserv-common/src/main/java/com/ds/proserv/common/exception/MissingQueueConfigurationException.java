package com.ds.proserv.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
public class MissingQueueConfigurationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6079810806978947139L;

	public MissingQueueConfigurationException(String message) {
		super(message);
	}

	public MissingQueueConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}