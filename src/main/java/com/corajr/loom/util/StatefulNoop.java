package com.corajr.loom.util;

import java.util.concurrent.atomic.AtomicInteger;

public class StatefulNoop extends StatefulCallable {
	public StatefulNoop(final AtomicInteger lastValue) {
		this(lastValue, 0);
	}

	public StatefulNoop(final AtomicInteger lastValue, final int index) {
		super(lastValue, index);
	}
}
