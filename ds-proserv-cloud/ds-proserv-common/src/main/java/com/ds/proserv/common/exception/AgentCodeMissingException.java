package com.ds.proserv.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AgentCodeMissingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4079066286709532919L;

	public AgentCodeMissingException(String message) {
		super(message);
	}

	public AgentCodeMissingException(String message, Throwable cause) {
		super(message, cause);
	}
}