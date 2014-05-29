package org.chrisjr.loom.util;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class CallableOnChange extends StatefulCallable {
	final private Callable<?> inner;

	public CallableOnChange(final AtomicInteger lastValue, final Callable<?> inner) {
		super(lastValue);
		this.inner = inner;
	}
	
	public Void call() throws Exception {
		int priorValue = lastValue.getAndSet(1);
		if (priorValue == 0) {
			inner.call();			
		}
		return null;
	}
}
