package org.chrisjr.loom.transforms;

import org.chrisjr.loom.Pattern;

public abstract class Transform {
	/**
	 * @author chrisjr
	 * 
	 *         A Transform must define <code>call</code> as a function that
	 *         takes a Pattern as input and returns a new Pattern.
	 */

	/**
	 * @param original
	 *            the original Pattern
	 * @return an updated Pattern
	 */
	public abstract Pattern call(Pattern original);
}
