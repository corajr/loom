/**
 * 
 */
package org.chrisjr.loom;

import java.util.concurrent.*;

/**
 * @author chrisjr
 * 
 * Default holder for a set of Patterns. Follows the Collections API,
 * with possible extensions for efficient querying. Must be concurrent.
 *
 */
public class PatternCollection extends CopyOnWriteArrayList<Pattern> {
	private static final long serialVersionUID = -6951516407692217125L;
	
	/**
	 * 
	 * 
	 * @return the relevant subset of patterns
	 */
	public PatternCollection getPatternsWithExternalMappings() {
		PatternCollection actives = new PatternCollection();
		for (Pattern pattern : this) {
			if (pattern.hasExternalMappings()) actives.add(pattern);
		}
		return actives;
	}
}
