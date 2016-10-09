package com.corajr.loom.transforms;

import java.util.concurrent.Callable;

import com.corajr.loom.Pattern;

/**
 * A Transform must define <code>call</code> as a function that takes a Pattern
 * as input and returns a new Pattern.
 * 
 * @author corajr
 */
public abstract class Transform {
	/**
	 * Executes the transform on the original pattern, and returns the modified
	 * version.
	 * 
	 * @param original
	 *            the original Pattern
	 * @return an updated Pattern
	 */
	public abstract Pattern call(Pattern original);

	/**
	 * Turns a {@link Transform} into a Callable&lt;Void&gt;.
	 * 
	 * @param transform
	 *            the {@link Transform} to use
	 * @param original
	 *            the pattern to which the transform should be applied
	 * @return a new Callable
	 */
	public static Callable<Void> toCallable(final Transform transform,
			final Pattern original) {
		return new Callable<Void>() {
			@Override
			public Void call() {
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
