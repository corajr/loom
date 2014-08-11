/**
 * 
 */
package org.chrisjr.loom;

import java.util.concurrent.*;

/**
 * Default holder for a set of Patterns. Follows the Collections API, with
 * extensions for application-specific queries. Must be concurrent, as it will
 * be queried from multiple threads.
 * 
 * @author chrisjr
 */
public class PatternCollection extends CopyOnWriteArrayList<Pattern> {
	private static final long serialVersionUID = -6951516407692217125L;

	/**
	 * Finds all the patterns that have external mappings, i.e. those with
	 * functions that must be triggered at each update rather than passively
	 * queried.
	 * 
	 * @return the relevant subset of patterns
	 */
	public PatternCollection getPatternsWithExternalMappings() {
		PatternCollection actives = new PatternCollection();
		for (Pattern pattern : this) {
			if (pattern.hasExternalMappings())
				actives.add(pattern);
		}
		return actives;
	}
}
