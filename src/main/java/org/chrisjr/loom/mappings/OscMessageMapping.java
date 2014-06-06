package org.chrisjr.loom.mappings;

import org.chrisjr.loom.*;

import oscP5.OscMessage;

public class OscMessageMapping implements Mapping<OscMessage> {
	private final Pattern original;
	private final String addressPattern;
	private final Mapping<?> mapping;

	public OscMessageMapping(final Pattern original,
			final String addressPattern, final Mapping<?> mapping) {
		this.original = original;
		this.addressPattern = addressPattern;
		this.mapping = mapping;
	}

	public OscMessage call(double value) {
		return new OscMessage(addressPattern,
				new Object[] { mapping.call(original.getValue()) });
	};
}
