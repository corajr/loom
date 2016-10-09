package com.corajr.loom.mappings;

import com.corajr.loom.*;

import oscP5.OscMessage;

/**
 * Create an OSC message by using a second pattern and mapping to provide the
 * arguments for the message.
 * 
 * @author corajr
 */
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

	@Override
	public OscMessage call(double value) {
		return new OscMessage(addressPattern,
				new Object[] { mapping.call(original.getValue()) });
	};
}
