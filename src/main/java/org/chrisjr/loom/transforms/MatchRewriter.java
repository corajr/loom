package org.chrisjr.loom.transforms;

/**
 * @author chrisjr
 * 
 *         Keep only those events that match the specified value.
 * 
 */
public class MatchRewriter extends EventRewriter {
	public MatchRewriter(double value) {
		super(new Rule[] { new MatchRule(value) });
	}
}
