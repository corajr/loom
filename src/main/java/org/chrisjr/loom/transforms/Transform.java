package org.chrisjr.loom.transforms;

import java.util.concurrent.Callable;

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

	public static Callable<Void> toCallable(final Transform transform,
			final Pattern original) {
		return new Callable<Void>() {
			@Override
			public Void call() {
				System.out.println(toString());
				transform.call(original);
				return null;
			}

			@Override
			public String toString() {
				return transform.toString();
			}
		};
	}
}
