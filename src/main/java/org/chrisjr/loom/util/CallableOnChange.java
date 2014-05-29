package org.chrisjr.loom.util;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.transforms.Transform;

public class CallableOnChange extends StatefulCallable {
	final private Callable<?> inner;

	public CallableOnChange(final AtomicInteger lastValue,
			final Callable<?> inner) {
		super(lastValue);
		this.inner = inner;
	}

	public Void call() throws Exception {
		int priorValue = lastValue.getAndSet(1);
		if (priorValue == 0) {
//			System.out.println("Change");
			inner.call();
		}
		return null;
	}

	public static CallableOnChange fromTransform(final AtomicInteger lastValue,
			final Transform transform, final Pattern original) {
		return new CallableOnChange(lastValue, new Callable<Void>() {
			public Void call() {
				transform.call(original);
				return null;
			}
		});
	}
}
