package com.ds.proserv.common.lambda;

import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LambdaExceptionWrappers {

	public static <T> Consumer<T> throwingConsumerWrapper(ThrowingConsumer<T, Exception> throwingConsumer) {
		return i -> {
			try {
				
				throwingConsumer.accept(i);
			} catch (Exception ex) {

				log.error("Exception {} occured in throwingConsumerWrapper: {}", ex, ex.getMessage());
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
		};
	}

	public static <T, E extends Exception> Consumer<T> handlingConsumerWrapper(ThrowingConsumer<T, E> throwingConsumer,
			Class<E> exceptionClass) {
		return i -> {
			try {
				
				throwingConsumer.accept(i);
			} catch (Exception ex) {
				try {
					E exCast = exceptionClass.cast(ex);
					log.error("{} occured in handlingConsumerWrapper: {}", exceptionClass.getSimpleName(),
							exCast.getMessage());
					exCast.printStackTrace();
				} catch (ClassCastException ccEx) {
					throw new RuntimeException(ex);
				}
			}
		};
	}
}