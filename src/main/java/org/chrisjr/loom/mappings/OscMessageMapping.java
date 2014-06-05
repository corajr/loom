package org.chrisjr.loom.mappings;

import org.chrisjr.loom.PrimitivePattern;

import oscP5.OscMessage;

public class OscMessageMapping implements Mapping<OscMessage> {
	private final PrimitivePattern original;
	private final String addressPattern;
	private final Mapping<?> mapping;

	public OscMessageMapping(final PrimitivePattern original,
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
