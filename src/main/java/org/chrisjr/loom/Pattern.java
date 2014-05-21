package org.chrisjr.loom;

import processing.core.PApplet;
import processing.core.PConstants;
import java.awt.Color;
import java.util.concurrent.*;

/**
 * The base class for patterns in Loom. Patterns may be discrete or continuous.
 * A DiscretePattern is a series of Events, while a ContinuousPattern is a
 * (closed-form) function of time.
 * 
 * @author chrisjr
 */
public abstract class Pattern {
	Loom myLoom;
	
	private double defaultValue;

	public enum Mapping {
		INTEGER, FLOAT, COLOR, COLOR_BLEND, MIDI_ON, OBJECT
	}

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

	public double getValue() {
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	private Object getAs(Mapping mapping) {
		Callable<Object> cb = (Callable<Object>) outputMappings.get(mapping);
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
		outputMappings.put(Mapping.MIDI_ON, new Callable<Void>() {
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
	public Pattern asColor(Color... _colors) {
		return asObject((Object[]) _colors);
	}

	/**
	 * @return an the "color" data type (32-bit int)
	 */
	public int asColor() {
		Color result = (Color) getAs(Mapping.COLOR);
		return result != null ? result.getRGB() : Color.BLACK.getRGB();
	}

	/**
	 * Set a mapping from the pattern's events to colors, blending between them
	 * using <code>lerpColor</code>.
	 * 
	 * @param colors
	 *            a list of colors to represent each state
	 * @return the updated pattern
	 */
	public Pattern asColorBlended(int... _colors) {
		final int[] colors = _colors;
		outputMappings.put(Mapping.COLOR_BLEND, new Callable<Integer>() {
			public Integer call() {
				float position = (float) getValue() * colors.length;
				int i = (int) position;
				float diff = position - i;

				int result = 0x00000000;
				if (colors.length == 1) {

				} else if (i + 1 < colors.length - 1) {
					result = PApplet.lerpColor(colors[i], colors[i + 1], diff,
							PConstants.HSB);
				}
				return result;
			}
		});
		return this;
	}

	public int asColorBlended() {
		Integer result = (Integer) getAs(Mapping.COLOR_BLEND);
		return result != null ? result.intValue() : 0x00000000;
	}

	public Pattern asObject(Object... _objects) {
		final Object[] objects = _objects;
		outputMappings.put(Mapping.OBJECT, new Callable<Object>() {
			public Object call() {
				int i = (int) getValue() * objects.length;
				return objects[i];
			}
		});
		return this;
	}

	public Object asObject() {
		return getAs(Mapping.OBJECT);
	}
}
