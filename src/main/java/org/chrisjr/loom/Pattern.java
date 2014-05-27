package org.chrisjr.loom;

import processing.core.PApplet;
import processing.core.PConstants;

import java.util.*;
import java.util.concurrent.*;

import org.chrisjr.loom.time.Interval;

/**
 * The base class for patterns in Loom. Patterns may be discrete or continuous.
 * A DiscretePattern is a series of Events, while a ContinuousPattern is a
 * (closed-form) function of time.
 * 
 * @author chrisjr
 */
public abstract class Pattern {
	Loom myLoom;

	protected double defaultValue;

	boolean isLooping = false;
	Interval loopInterval = new Interval(0, 1);

	public static final Callable<Void> NOOP = new Callable<Void>() {
		public Void call() {
			return null;
		}
	};

	public final class PickFromArray<E> implements Callable<E> {
		final private E[] myArray;

		public PickFromArray(E[] _array) {
			myArray = _array;
		}

		public E call() {
			int i = (int) (getValue() * (myArray.length - 1));
			return myArray[i];
		}
	}

	public enum Mapping {
		INTEGER, FLOAT, COLOR, COLOR_BLEND, MIDI, OSC, CALLABLE, OBJECT
	}

	final private Mapping[] externalMappings = new Mapping[] { Mapping.MIDI,
			Mapping.OSC, Mapping.CALLABLE };

	private ConcurrentMap<Mapping, Callable<?>> outputMappings = new ConcurrentHashMap<Mapping, Callable<?>>();

	/**
	 * Constructor for an empty Pattern.
	 * 
	 * @param loom
	 *            the loom that holds this pattern (can be null)
	 */
	public Pattern(Loom loom) {
		this(loom, 0.0);
	}

	public Pattern(Loom loom, double _defaultValue) {
		myLoom = loom;
		if (myLoom != null)
			addTo(myLoom);
		defaultValue = _defaultValue;
	}

	protected void addTo(Loom loom) {
		loom.patterns.add(this);
	}
	
	public Interval getCurrentInterval() {
		Interval interval = myLoom.getCurrentInterval();
		if (isLooping) {
			interval = interval.modulo(loopInterval);
		}
		return interval;
	}

	public double getValue() {
		return defaultValue;
	}

	public void once() {
		isLooping = false;
	}

	public void loop() {
		isLooping = true;
	}

	@SuppressWarnings("unchecked")
	private Object getAs(Mapping mapping) throws IllegalStateException {
		Callable<Object> cb = (Callable<Object>) outputMappings.get(mapping);

		if (cb == null)
			throw new IllegalStateException("No mapping available for "
					+ mapping.toString());

		Object result = null;
		try {
			result = cb.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public Pattern asInt(int _lo, int _hi) {
		final int lo = _lo;
		final int hi = _hi;
		outputMappings.put(Mapping.INTEGER, new Callable<Integer>() {
			public Integer call() {
				return (int) ((hi - lo) * getValue()) + lo;
			}
		});
		return this;
	}

	public int asInt() {
		Integer result = (Integer) getAs(Mapping.INTEGER);
		return result != null ? result.intValue() : Integer.MIN_VALUE;
	}

	/**
	 * Set a mapping from the pattern's events to sounds
	 * 
	 * @param instrument
	 *            the name of a MIDI instrument to trigger
	 * @return the updated pattern
	 */
	public Pattern asMIDI(String _instrument) {
		final String instrument = _instrument;
		outputMappings.put(Mapping.MIDI, new Callable<Void>() {
			// TODO actually do something here
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
	public Pattern asColor(Integer... _colors) {
		outputMappings.put(Mapping.COLOR, new PickFromArray<Integer>(_colors));
		return this;
	}

	/**
	 * @return an the "color" data type (32-bit int)
	 */
	public int asColor() {
		Integer result = (Integer) getAs(Mapping.COLOR);
		return result != null ? result : 0x00000000;
	}

	/**
	 * Set a mapping from the pattern's events to colors, blending between them
	 * using <code>lerpColor</code>.
	 * 
	 * @param colors
	 *            a list of colors to represent each state
	 * @return the updated pattern
	 */
	public Pattern asColorBlend(int... _colors) {
		final int[] colors = _colors;
		outputMappings.put(Mapping.COLOR_BLEND, new Callable<Integer>() {
			public Integer call() {
				float position = (float) getValue() * (colors.length - 1);
				int i = (int) position;
				float diff = position - i;

				int result = 0x00000000;
				if (colors.length == 1) {
					result = colors[0];
				} else if (i + 1 < colors.length) {
					result = PApplet.lerpColor(colors[i], colors[i + 1], diff,
							PConstants.HSB);
				}
				return result;
			}
		});
		return this;
	}

	public int asColorBlend() {
		Integer result = (Integer) getAs(Mapping.COLOR_BLEND);
		return result != null ? result.intValue() : 0x00000000;
	}

	public Pattern asObject(Object... objects) {
		outputMappings.put(Mapping.OBJECT, new PickFromArray<Object>(objects));
		return this;
	}

	public Object asObject() {
		return getAs(Mapping.OBJECT);
	}

	public Pattern asCallable(Callable<?>... callables) {
		outputMappings.put(Mapping.CALLABLE, new PickFromArray<Callable<?>>(
				callables));
		return this;

	}

	@SuppressWarnings("unchecked")
	public Callable<Object> asCallable() {
		return (Callable<Object>) getAs(Mapping.CALLABLE);
	}

	public Collection<Callable<?>> getExternalMappings() {
		Collection<Callable<?>> callbacks = new ArrayList<Callable<?>>();
		for (Mapping mapping : externalMappings) {
			if (outputMappings.containsKey(mapping))
				callbacks.add((Callable<?>) getAs(mapping));
		}
		return callbacks;
	}

	/**
	 * Originally called getExternalMappings, but the new collection created
	 * slowed things down.
	 * 
	 * @return true if external mappings are present
	 */
	public Boolean hasExternalMappings() {
		boolean result = false;
		for (Mapping mapping : externalMappings) {
			if (outputMappings.containsKey(mapping))
				result = true;
		}
		return result;
	}
}
