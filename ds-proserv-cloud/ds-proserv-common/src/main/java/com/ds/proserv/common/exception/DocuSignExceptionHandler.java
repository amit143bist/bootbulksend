package com.ds.proserv.common.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ds.proserv.common.util.DateTimeUtil;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@RestController
@Slf4j
public class DocuSignExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ Exception.class })
	public final ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({ IllegalArgumentException.class })
	public final ResponseEntity<ErrorDetails> handleIllegalArgumentException(IllegalArgumentException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ IllegalStateException.class })
	public final ResponseEntity<ErrorDetails> handleIllegalStateException(IllegalStateException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ ConsentRequiredException.class })
	public final ResponseEntity<ErrorDetails> handleConsentRequiredException(ConsentRequiredException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ InvalidInputException.class })
	public final ResponseEntity<ErrorDetails> handleInvalidInputException(InvalidInputException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ ResourceConditionFailedException.class })
	public final ResponseEntity<ErrorDetails> handleResourceConditionFailedException(
			ResourceConditionFailedException ex, WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.PRECONDITION_FAILED);
	}

	@ExceptionHandler({ ResourceNotFoundException.class })
	public final ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler({ ResourceNotSavedException.class })
	public final ResponseEntity<ErrorDetails> handleResourceNotSavedException(ResourceNotSavedException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({ RunningBatchException.class })
	public final ResponseEntity<ErrorDetails> handleRunningBatchException(ResourceNotSavedException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.NOT_ACCEPTABLE);
	}

	@ExceptionHandler({ AsyncInterruptedException.class })
	public final ResponseEntity<ErrorDetails> handleAsyncInterruptedException(AsyncInterruptedException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler({ DocumentNotFoundException.class })
	public final ResponseEntity<ErrorDetails> handleDocumentNotFoundException(DocumentNotFoundException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler({ DocumentValidationFailureException.class })
	public final ResponseEntity<ErrorDetails> handleDocumentValidationFailureException(
			DocumentValidationFailureException ex, WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.PRECONDITION_FAILED);
	}

	@ExceptionHandler({ EnvelopeNotCreatedException.class })
	public final ResponseEntity<ErrorDetails> handleEnvelopeNotCreatedException(EnvelopeNotCreatedException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({ JSONConversionException.class })
	public final ResponseEntity<ErrorDetails> handleJSONConversionException(JSONConversionException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({ URLConnectionException.class })
	public final ResponseEntity<ErrorDetails> handleURLConnectionException(URLConnectionException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.SEE_OTHER);
	}

	@ExceptionHandler({ ListenerProcessingException.class })
	public final ResponseEntity<ErrorDetails> handleListenerProcessingException(ListenerProcessingException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({ PendingQueueMessagesException.class })
	public final ResponseEntity<ErrorDetails> handlePendingQueueMessagesException(PendingQueueMessagesException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.FAILED_DEPENDENCY);
	}

	@ExceptionHandler({ AuthenticationTokenException.class })
	public final ResponseEntity<ErrorDetails> handleAuthenticationTokenException(AuthenticationTokenException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.FAILED_DEPENDENCY);
	}

	@ExceptionHandler({ MissingSignatureDetailsException.class })
	public final ResponseEntity<ErrorDetails> handleMissingSignatureDetailsException(
			MissingSignatureDetailsException ex, WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.FAILED_DEPENDENCY);
	}

	@ExceptionHandler({ DateTimeParseException.class })
	public final ResponseEntity<ErrorDetails> handleDateTimeParseException(DateTimeParseException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ InvalidMessageException.class })
	public final ResponseEntity<ErrorDetails> handleInvalidMessageException(InvalidMessageException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ InvalidProgramTypeException.class })
	public final ResponseEntity<ErrorDetails> handleInvalidProgramTypeException(InvalidProgramTypeException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ AgentCodeMissingException.class })
	public final ResponseEntity<ErrorDetails> handleAgentCodeMissingException(AgentCodeMissingException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ NewVersionExistException.class })
	public final ResponseEntity<ErrorDetails> handleNewVersionExistException(NewVersionExistException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.ALREADY_REPORTED);
	}

	@ExceptionHandler({ BatchAlgorithmException.class })
	public final ResponseEntity<ErrorDetails> handleBatchAlgorithmException(BatchAlgorithmException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ BatchNotAuthorizedException.class })
	public final ResponseEntity<ErrorDetails> handleBatchNotAuthorizedException(BatchNotAuthorizedException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ CreateTableException.class })
	public final ResponseEntity<ErrorDetails> handleCreateTableException(CreateTableException ex, WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.PRECONDITION_FAILED);
	}

	@ExceptionHandler({ UserDoesNotExistException.class })
	public final ResponseEntity<ErrorDetails> handleUserDoesNotExistException(UserDoesNotExistException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler({ MissingQueueConfigurationException.class })
	public final ResponseEntity<ErrorDetails> handleMissingQueueConfigurationException(
			MissingQueueConfigurationException ex, WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.FAILED_DEPENDENCY);
	}

	@ExceptionHandler({ JSFunctionProcessException.class })
	public final ResponseEntity<ErrorDetails> handleJSFunctionProcessException(JSFunctionProcessException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.NOT_ACCEPTABLE);
	}

	@ExceptionHandler({ NoDataProcessingException.class })
	public final ResponseEntity<ErrorDetails> handleNoDataProcessingException(NoDataProcessingException ex,
			WebRequest request) {

		return populateResponseEntity(ex, request, HttpStatus.NO_CONTENT);
	}

	private ResponseEntity<ErrorDetails> populateResponseEntity(Exception ex, WebRequest request,
			HttpStatus httpStatus) {

		List<String> details = new ArrayList<>();

		details.add(httpStatus.toString());
		details.add(ex.toString());
		details.add(ex.getMessage());
		details.add(ex.getLocalizedMessage());
		details.add(request.getDescription(false));

		ErrorDetails errorDetails = new ErrorDetails(DateTimeUtil.convertToString(LocalDateTime.now()), ex.getMessage(),
				details);

		log.info("ErrorDetails message in populateResponseEntity is {}, and httpStatus is {}", errorDetails,
				httpStatus);
		ex.printStackTrace();

		if (null != ex.getStackTrace() && ex.getStackTrace().length > 0) {

			StackTraceElement stackTraceElement = ex.getStackTrace()[0];

			log.error("Exception occurred in ClassName -> {}, FileName -> {}, MethodName -> {} and LineNumber -> {}",
					stackTraceElement.getClassName(), stackTraceElement.getFileName(),
					stackTraceElement.getMethodName(), stackTraceElement.getLineNumber());

		}
		return new ResponseEntity<ErrorDetails>(errorDetails, httpStatus);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus httpStatus, WebRequest request) {

		List<String> details = new ArrayList<>();

		details.add(httpStatus.toString());
		details.add(ex.getMessage());
		details.add(request.getDescription(false));

		for (ObjectError error : ex.getBindingResult().getAllErrors()) {
			details.add(error.getDefaultMessage());
		}

		ErrorDetails error = new ErrorDetails(DateTimeUtil.convertToString(LocalDateTime.now()), "Validation Failed",
				details);
		ex.printStackTrace();
		return new ResponseEntity<Object>(error, httpStatus);
	}
}