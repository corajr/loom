package com.corajr.loom.util;

import java.util.concurrent.Callable;

public class CallableNoop implements Callable<Void> {
	@Override
	public Void call() throws Exception {
		return null;
	}
}
