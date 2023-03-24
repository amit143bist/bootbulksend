package com.ds.proserv.feign.listener;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import com.ds.proserv.common.constant.AppConstants;
import com.ds.proserv.common.exception.InvalidInputException;
import com.ds.proserv.common.exception.InvalidMessageException;
import com.ds.proserv.common.exception.JSONConversionException;
import com.ds.proserv.common.exception.ListenerProcessingException;
import com.ds.proserv.common.exception.RunningBatchException;
import com.ds.proserv.common.exception.URLConnectionException;
import com.ds.proserv.feign.domain.IDocuSignInformation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMigrationListener<T extends IDocuSignInformation> {

	protected long retryLimit = 3;

	protected abstract void processMessage(T docuSignInformation, List<Map<String, Object>> xDeath);

	protected abstract void callService(T docuSignInformation);

	protected abstract void sendToDeadQueue(T docuSignInformation, String httpStatus, String errorHeaderMessage);

	protected abstract void logErrorMessage(long retryCount, Exception exp, String expReason, String httpStatus,
			T docuSignInformation);

	protected HttpHeaders createHeaders(String username, String password) {
		return new HttpHeaders() {

			private static final long serialVersionUID = -4222926994007005994L;

			{
				String auth = username + AppConstants.COLON + password;
				byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
				String authHeader = AppConstants.AUTH_HEADER_VALUE_PREFIX_CONST + new String(encodedAuth);
				set(AppConstants.AUTH_HEADER_NAME_CONST, authHeader);
			}
		};
	}

	protected void processMessage(List<Map<String, Object>> xDeath, long retryLimit, T docuSignInformation) {

		long retryCount = 0;
		if (xDeath != null) {
			Optional<Long> count = xDeath.stream().flatMap(m -> m.entrySet().stream())
					.filter(e -> e.getKey().equals("count")).findFirst().map(e -> (Long) e.getValue());
			if (count.isPresent()) {
				retryCount = count.get().longValue();
				log.debug("RetryCount is -> {}", retryCount);
			}
		}

		try {

			callService(docuSignInformation);
			log.debug("Succesfully completed the callService and processMessage for {}", docuSignInformation);

		} catch (InvalidMessageException exp) {

			logErrorMessage(retryCount, exp, exp.getMessage(), "", docuSignInformation);
			sendToDeadQueue(docuSignInformation, "", exp + "_" + exp.getMessage());
		} catch (ListenerProcessingException exp) {

			logErrorMessage(retryCount, exp, exp.getMessage(), "", docuSignInformation);
			sendToDeadQueue(docuSignInformation, "", exp + "_" + exp.getMessage());
		} catch (InvalidInputException exp) {

			logErrorMessage(retryCount, exp, exp.getMessage(), "", docuSignInformation);
			sendToDeadQueue(docuSignInformation, "", exp + "_" + exp.getMessage());
		} catch (RunningBatchException exp) {

			logErrorMessage(retryCount, exp, exp.getMessage(), "", docuSignInformation);
			sendToDeadQueue(docuSignInformation, "", exp + "_" + exp.getMessage());
		} catch (JSONConversionException exp) {

			logErrorMessage(retryCount, exp, exp.getMessage(), "", docuSignInformation);
			sendToDeadQueue(docuSignInformation, "", exp + "_" + exp.getMessage());
		} catch (HttpClientErrorException exp) {

			StringBuilder builder = new StringBuilder();
			builder.append(exp.getStatusCode());
			builder.append("_");
			builder.append(exp.getRawStatusCode());
			builder.append("_");
			builder.append(exp.getResponseBodyAsString());
			builder.append("_");
			builder.append(exp.getResponseHeaders());

			logErrorMessage(retryCount, exp, builder.toString(), "", docuSignInformation);
			sendToDeadQueue(docuSignInformation, "", exp.getMessage());

		} catch (ResponseStatusException exp) {

			logErrorMessage(retryCount, exp, exp.getReason(), null != exp.getStatus() ? exp.getStatus().toString() : "",
					docuSignInformation);
			if (retryCount >= retryLimit) {

				sendToDeadQueue(docuSignInformation, null != exp.getStatus() ? exp.getStatus().toString() : "",
						exp.getReason());
			} else {

				log.error(
						"Since retryCount -> {} is not more than retryLimit -> {} so throwing ResponseStatusException -> {} again",
						retryCount, retryLimit, exp);
				throw exp;
			}
		} catch (URLConnectionException exp) {

			logErrorMessage(retryCount, exp, exp.getMessage(), "", docuSignInformation);
			if (retryCount >= retryLimit) {

				sendToDeadQueue(docuSignInformation, "", exp + "_" + exp.getMessage());
			} else {

				log.error(
						"Since retryCount -> {} is not more than retryLimit -> {} so throwing URLConnectionException -> {} again",
						retryCount, retryLimit, exp);
				throw exp;
			}
		} catch (Exception exp) {

			logErrorMessage(retryCount, exp, exp.getMessage(), "", docuSignInformation);
			if (retryCount >= retryLimit) {

				sendToDeadQueue(docuSignInformation, "", exp + "_" + exp.getMessage());
			} else {

				log.error(
						"Since retryCount -> {} is not more than retryLimit -> {} so throwing generic exception -> {} again",
						retryCount, retryLimit, exp);
				throw exp;
			}
		}
	}

	protected long getRetryLimit(String cachedRetryLimit, String cacheKey) {

		if (!StringUtils.isEmpty(cachedRetryLimit)) {

			retryLimit = Long.valueOf(cachedRetryLimit);
		}

		log.info("For cacheKey -> {}, cachedRetryLimit is {} and applied retryLimit is {}", cacheKey, cachedRetryLimit,
				retryLimit);

		return retryLimit;
	}

}