package org.chrisjr.loom.mappings;

public class NoopMapping implements Mapping<Void> {
	@Override
	public Void call(double value) {
		return null;
	}
}
