package org.chrisjr.loom.mappings;

import org.chrisjr.loom.*;

import oscP5.OscBundle;

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
