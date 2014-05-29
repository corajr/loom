package org.chrisjr.loom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.chrisjr.loom.Pattern.Mapping;
import org.chrisjr.loom.continuous.ConstantFunction;
import org.chrisjr.loom.continuous.ContinuousFunction;
import org.chrisjr.loom.time.Interval;
import org.chrisjr.loom.util.StatefulCallable;

import processing.core.PApplet;
import processing.core.PConstants;

public class PrimitivePattern extends Pattern {
	private ConcurrentMap<Mapping, Callable<?>> outputMappings = new ConcurrentHashMap<Mapping, Callable<?>>();

	protected EventCollection events = null;
	protected ContinuousFunction function = null;

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

	public PrimitivePattern(Loom loom) {
		super(loom);
	}

	public PrimitivePattern(Loom loom, double defaultValue) {
		super(loom);
		this.function = new ConstantFunction(defaultValue);
	}

	public PrimitivePattern(Loom loom, EventCollection events) {
		super(loom);
		this.events = events;
	}

	public PrimitivePattern(Loom loom, ContinuousFunction function) {
		super(loom);
		this.function = function;
	}

	public boolean isPrimitivePattern() {
		return true;
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

	public Pattern asInt(int lo, int hi) {
		final int _lo = lo;
		final int _hi = hi;
		outputMappings.put(Mapping.INTEGER, new Callable<Integer>() {
			public Integer call() {
				return (int) ((_hi - _lo) * getValue()) + _lo;
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
	public Pattern asMidi(String instrument) {
		final String _instrument = instrument;
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
	public Pattern asColor(Integer... colors) {
		outputMappings.put(Mapping.COLOR, new PickFromArray<Integer>(colors));
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
	public Pattern asColorBlend(int... colors) {
		final int[] _colors = colors;
		outputMappings.put(Mapping.COLOR_BLEND, new Callable<Integer>() {
			public Integer call() {
				float position = (float) getValue() * (_colors.length - 1);
				int i = (int) position;
				float diff = position - i;

				int result = 0x00000000;
				if (_colors.length == 1) {
					result = _colors[0];
				} else if (i + 1 < _colors.length) {
					result = PApplet.lerpColor(_colors[i], _colors[i + 1],
							diff, PConstants.HSB);
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

	public StatefulCallable asStatefulCallable() {
		return (StatefulCallable) getAs(Mapping.STATEFUL_CALLABLE);
	}

	public Pattern asStatefulCallable(StatefulCallable... callables) {
		outputMappings.put(Mapping.STATEFUL_CALLABLE,
				new PickFromArray<StatefulCallable>(callables));
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

	public double getValue() {
		double value = defaultValue;
		Interval now = getCurrentInterval();
		if (this.function != null) {
			try {
				// midpoint of function within current interval
				value = function.call(now.getStart());
				value += function.call(now.getEnd());
				value /= 2.0;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (this.events != null) {
			Collection<Event> activeEvents = this.events.getForInterval(now);
			for (Event e : activeEvents) {
				value = e.getValue();
			}
		}
		return value;
	}

	public PrimitivePattern clone() throws CloneNotSupportedException {
		PrimitivePattern copy = new PrimitivePattern(loom);
		if (events != null)
			copy.events = (EventCollection) events.clone();
		else if (function != null)
			copy.function = function; // immutable
		return copy;
	}
}
