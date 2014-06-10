package org.chrisjr.loom.util;

import java.util.ArrayList;
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
			inner.call();
		}
		return null;
	}

	public static StatefulCallable[] fromCallable(final Callable<Void> callable) {
		final AtomicInteger lastValue = new AtomicInteger();
		StatefulCallable noop = new StatefulNoop(lastValue);
		StatefulCallable doCall = new CallableOnChange(lastValue, callable);

		return new StatefulCallable[] { noop, doCall };
	}

	@SuppressWarnings("unchecked")
	public static StatefulCallable[] fromCallables(Callable<Void>... callables) {
		ArrayList<StatefulCallable> result = new ArrayList<StatefulCallable>();

		final AtomicInteger lastValue = new AtomicInteger();
		result.add(new StatefulNoop(lastValue));
		for (Callable<Void> callable : callables) {
			result.add(new CallableOnChange(lastValue, callable));
		}

		return result.toArray(new StatefulCallable[] {});
	}

	public static StatefulCallable[] fromTransform(final Transform transform,
			final Pattern original) {
		return fromCallable(new Callable<Void>() {
			public Void call() {
				transform.call(original);
				return null;
			}

			public String toString() {
				return transform.toString();
			}
		});
	}

	public String toString() {
		return "CallableOnChange(" + inner.toString() + ")" + "@"
				+ Integer.toHexString(hashCode());
	}
}
