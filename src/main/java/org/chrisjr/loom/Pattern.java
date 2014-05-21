package org.chrisjr.loom;

import java.awt.Color;
import java.util.concurrent.*;

/**
 *        The base class for patterns in Loom. Patterns may be discrete or
 *        continuous. A DiscretePattern is a series of Events, while a 
 *        ContinuousPattern is a (closed-form) function of time.
 *         
 * @author chrisjr
 */
public abstract class Pattern {
	Loom myLoom;

	private ConcurrentMap<String, Callable<?>> outputMappings;

	/**
	 * Constructor for an empty Pattern.
	 * 
	 * @param loom
	 *            the loom that holds this pattern (can be null)
	 */
	public Pattern(Loom loom) {
		myLoom = loom;
		if (myLoom != null)
			addTo(myLoom);
		outputMappings = new ConcurrentHashMap<String, Callable<?>>();
	}

	protected void addTo(Loom loom) {
		loom.patterns.add(this);
	}
	
	public abstract double getValue();

	/**
	 * @param string
	 *            a string such as "10010010" describing a pattern
	 * @return the updated pattern
	 */
	public Pattern extend(String string) {
		return this;
	}
	
	public Pattern asInt(int _lo, int _hi) {
		final int lo = _lo;
		final int hi = _hi;
		outputMappings.put("int", new Callable<Integer>() {
			public Integer call() {
				return (int) ((hi - lo) * getValue()) + lo;
			}
		});
		return this;
	}
	
	public int asInt() {
		Callable<Integer> cb = (Callable<Integer>) outputMappings.get("int");
		int result = 0;
		try {
			result = cb.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Set a mapping from the pattern's events to sounds
	 * 
	 * @param instrument
	 *            the name of a MIDI instrument to trigger
	 * @return the updated pattern
	 */
	public Pattern asSound(String _instrument) {
		final String instrument = _instrument;
		outputMappings.put("sound", new Callable<Void>() {
			public Void call() {
				return null;
			}
		});
		return this;
	}

	/**
	 * Set a mapping from the pattern's events to colors
	 * 
	 * @param colors
	 *            a list of colors to represent each state
	 * @return the updated pattern
	 */
	public Pattern asColor(Color... _colors) {
		final Color[] colors = _colors;
		outputMappings.put("color", new Callable<Color>() {
			public Color call() {
				return Color.black;
			}
		});
		return this;
	}

	public Color asColor() {
		return Color.black;
	}
	
	public Pattern asCallable(Callable callable) {
		outputMappings.put("callable", callable);
		return this;
	}
	
	public Callable asCallable() {
		return (Callable) outputMappings.get("runnable");
	}
}
