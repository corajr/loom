package com.corajr.loom.mappings;

import com.corajr.loom.*;

import oscP5.OscBundle;

/**
 * Collects OSC messages from the specified patterns and wraps them in a single
 * OSC bundle.
 * 
 * @author corajr
 */
public class OscBundleMapping implements Mapping<OscBundle> {
	private final PatternCollection oscPatterns;

	public OscBundleMapping(final PatternCollection oscPatterns) {
		this.oscPatterns = oscPatterns;
	}

	@Override
	public OscBundle call(double value) {
		OscBundle bundle = new OscBundle();
		for (Pattern pat : oscPatterns) {
			bundle.add(pat.asOscMessage());
		}
		return bundle;
	}
}
