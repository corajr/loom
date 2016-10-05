package com.corajr.loom.util;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import com.corajr.loom.Pattern;
import com.corajr.loom.transforms.Transform;

public class CallableOnChange extends StatefulCallable {
	final private Callable<?> inner;

	public CallableOnChange(final AtomicInteger lastValue,
			final Callable<?> inner, final int index) {
		super(lastValue, index);
		this.inner = inner;
	}

	@Override
	public Void call() throws Exception {
		int priorValue = lastValue.getAndSet(index);
		if (priorValue != index) {
			inner.call();
		}
		return null;
	}

	public static StatefulCallable[] fromCallables(Callable<Void>... callables) {
		ArrayList<StatefulCallable> result = new ArrayList<StatefulCallable>();

		int i = 0;

		final AtomicInteger lastValue = new AtomicInteger();

		result.add(new StatefulNoop(lastValue, i++));
		for (Callable<Void> callable : callables) {
			result.add(new CallableOnChange(lastValue, callable, i++));
		}

		return result.toArray(new StatefulCallable[] {});
	}

	public static StatefulCallable[] fromTransform(final Transform transform,
			final Pattern original) {
		return fromCallables(Transform.toCallable(transform, original));
	}

	@Override
	public String toString() {
		return "CallableOnChange(" + inner.toString() + ")" + "@"
				+ Integer.toHexString(hashCode());
	}
}
