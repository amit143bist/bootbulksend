package com.ds.proserv.common.lambda;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

	void accept(T t) throws E;

}