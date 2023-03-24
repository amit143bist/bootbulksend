package com.ds.proserv.cache.feign;

import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.ds.proserv.common.exception.ErrorDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

	private ObjectMapper objectMapper;

	@Override
	public Exception decode(String methodKey, Response response) {

		log.error("methodKey is {}, response.status is {} and response.reason is {}", methodKey, response.status(),
				response.reason());

		ErrorDetails errorDetails = null;
		try {

			if (null != response.body()) {

				String body = Util.toString(response.body().asReader(Charset.defaultCharset()));

				errorDetails = objectMapper.readValue(body, ErrorDetails.class);

				log.error("ErrorDetails is {}, Timestamp is {}, details is {}, message is {}", errorDetails,
						errorDetails.getTimestamp(), errorDetails.getDetails(), errorDetails.getMessage());

				return new ResponseStatusException(HttpStatus.valueOf(response.status()), errorDetails.getTimestamp()
						+ "_" + errorDetails.getMessage() + "_" + errorDetails.getDetails());
			} else {

				new ResponseStatusException(HttpStatus.valueOf(response.status()), methodKey + response.body());
			}
		} catch (IOException ex) {

			log.error("IOException {} in FeignErrorDecoder.decode() with errorMessage -> {} ", ex, ex.getMessage());
			ex.printStackTrace();
		} catch (Exception ex) {

			log.error("Exception {} in FeignErrorDecoder.decode() with errorMessage -> {} ", ex, ex.getMessage());
			ex.printStackTrace();
		}

		return null;
	}

}