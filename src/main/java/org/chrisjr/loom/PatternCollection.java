/**
 * 
 */
package org.chrisjr.loom;

import java.util.concurrent.*;

import org.apache.commons.math3.fraction.Fraction;

/**
 * @author chrisjr
 * 
 * Default holder for a set of Patterns. Follows the Collections API,
 * with possible extensions for efficient querying.
 *
 */
public class PatternCollection extends ConcurrentSkipListMap<Fraction, Pattern> {
	private static final long serialVersionUID = -6951516407692217125L;
}
