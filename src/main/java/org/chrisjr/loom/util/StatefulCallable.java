package org.chrisjr.loom.util;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class StatefulCallable implements Callable<Void> {
	final protected AtomicInteger lastValue;
	
	public StatefulCallable(final AtomicInteger lastValue) {
		this.lastValue = lastValue;
	}

	public Void call() throws Exception {
		lastValue.set(0);
		return null;
	}
}
