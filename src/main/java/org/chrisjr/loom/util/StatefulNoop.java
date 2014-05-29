package org.chrisjr.loom.util;

import java.util.concurrent.atomic.AtomicInteger;

public class StatefulNoop extends StatefulCallable {
	public StatefulNoop(final AtomicInteger lastValue) {
		super(lastValue);
	}
}
