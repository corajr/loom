package com.corajr.loom.util;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class StatefulCallable implements Callable<Void> {
	final protected AtomicInteger lastValue;
	final int index;

	public StatefulCallable(final AtomicInteger lastValue, final int index) {
		this.lastValue = lastValue;
		this.index = index;
	}

	public Void call() throws Exception {
		lastValue.set(index);
		return null;
	}
}
