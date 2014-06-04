package org.chrisjr.loom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import netP5.NetAddress;

import org.chrisjr.loom.continuous.ConstantFunction;
import org.chrisjr.loom.continuous.ContinuousFunction;
import org.chrisjr.loom.mappings.ColorMapping;
import org.chrisjr.loom.mappings.IntMapping;
import org.chrisjr.loom.mappings.Mapping;
import org.chrisjr.loom.mappings.NoopMapping;
import org.chrisjr.loom.mappings.ObjectMapping;
import org.chrisjr.loom.time.Interval;
import org.chrisjr.loom.time.Scheduler;
import org.chrisjr.loom.util.CallableOnChange;
import org.chrisjr.loom.util.MathOps;
import org.chrisjr.loom.util.StatefulCallable;

import oscP5.OscBundle;
import oscP5.OscMessage;

public class PrimitivePattern extends Pattern {
	private ConcurrentMap<MappingType, Mapping<?>> outputMappings = new ConcurrentHashMap<MappingType, Mapping<?>>();

	protected EventCollection events = null;
	protected ContinuousFunction function = null;

	public PrimitivePattern(Loom loom) {
		super(loom, null, null, true);
	}

	public PrimitivePattern(Loom loom, double defaultValue) {
		super(loom, null, null, true);
		this.function = new ConstantFunction(defaultValue);
	}

	public PrimitivePattern(Loom loom, EventCollection events) {
		super(loom, null, null, true);
		this.events = events;
	}

	public PrimitivePattern(Loom loom, ContinuousFunction function) {
		super(loom, null, null, true);
		this.function = function;
	}

	private Object getAs(MappingType mapping) throws IllegalStateException {
		return getAs(mapping, getValue());
	}

	@SuppressWarnings("unchecked")
	private Object getAs(MappingType mapping, double value)
			throws IllegalStateException {
		Mapping<Object> cb = (Mapping<Object>) outputMappings.get(mapping);

		if (cb == null)
			throw new IllegalStateException("No mapping available for "
					+ mapping.toString());

		Object result = null;
		try {
			result = cb.call(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public Pattern asInt(int lo, int hi) {
		outputMappings.put(MappingType.INTEGER, new IntMapping(lo, hi));
		return this;
	}

	public int asInt() {
		Integer result = (Integer) getAs(MappingType.INTEGER);
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
		outputMappings.put(MappingType.MIDI, new NoopMapping());
		return this;
	}

	public Pattern asOscMessage(final String addr, final MappingType mapping) {
		final PrimitivePattern original = this;
		outputMappings.put(MappingType.OSC_MESSAGE, new Mapping<OscMessage>() {
			public OscMessage call(double value) {
				return new OscMessage(addr, new Object[] { original
						.getAs(mapping) });
			};
		});
		return this;
	}

	public OscMessage asOscMessage() {
		return (OscMessage) getAs(MappingType.OSC_MESSAGE);
	}

	public Pattern asOscBundle(final NetAddress remoteAddress,
			final Pattern... patterns) {
		final PrimitivePattern original = this;

		final PatternCollection oscPatterns = new PatternCollection();

		boolean hasOscMapping = false;
		for (Pattern pat : patterns) {
			if (pat.hasMapping(MappingType.OSC_MESSAGE)) {
				hasOscMapping = true;
				oscPatterns.add(pat);
			}
		}

		if (!hasOscMapping)
			throw new IllegalArgumentException(
					"None of the patterns have an OSC mapping!");

		outputMappings.put(MappingType.OSC_BUNDLE, new Mapping<OscBundle>() {
			public OscBundle call(double value) {
				OscBundle bundle = new OscBundle();
				for (Pattern pat : oscPatterns) {
					bundle.add(pat.asOscMessage());
				}
				return bundle;
			}
		});

		asStatefulCallable(CallableOnChange.fromCallable(new Callable<Void>() {
			public Void call() {
				loom.getOscP5().send(original.asOscBundle(), remoteAddress);
				return null;
			}
		}));
		return this;
	}

	public OscBundle asOscBundle() {
		return (OscBundle) getAs(MappingType.OSC_BUNDLE);
	}

	/**
	 * Set a mapping from the pattern's events to colors, blending between them
	 * using <code>lerpColor</code> in HSB mode.
	 * 
	 * @param colors
	 *            a list of colors to represent each state
	 * @return the updated pattern
	 */
	public Pattern asColor(final int... colors) {
		outputMappings.put(MappingType.COLOR, new ColorMapping(colors));
		return this;
	}

	public int asColor() {
		Integer result = (Integer) getAs(MappingType.COLOR);
		return result != null ? result.intValue() : 0x00000000;
	}

	public Pattern asObject(Object... objects) {
		outputMappings.put(MappingType.OBJECT, new ObjectMapping<Object>(
				objects));
		return this;
	}

	public Object asObject() {
		return getAs(MappingType.OBJECT);
	}

	public Pattern asCallable(Callable<?>... callables) {
		outputMappings.put(MappingType.CALLABLE,
				new ObjectMapping<Callable<?>>(callables));
		return this;
	}

	public StatefulCallable asStatefulCallable() {
		return (StatefulCallable) getAs(MappingType.STATEFUL_CALLABLE);
	}

	public Pattern asStatefulCallable(StatefulCallable... callables) {
		outputMappings.put(MappingType.STATEFUL_CALLABLE,
				new ObjectMapping<StatefulCallable>(callables));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Callable<Object> asCallable() {
		return (Callable<Object>) getAs(MappingType.CALLABLE);
	}

	public Collection<Callable<?>> getExternalMappings() {
		Collection<Callable<?>> callbacks = new ArrayList<Callable<?>>();
		for (MappingType mapping : externalMappings) {
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
		for (MappingType mapping : externalMappings) {
			if (outputMappings.containsKey(mapping)) {
				result = true;
				break;
			}
		}
		return result;
	}

	public boolean hasMapping(MappingType mapping) {
		return outputMappings.containsKey(mapping);
	}
	
	public double getValueFor(Interval now) {
		double value = defaultValue;
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

		// apply transformations
		value *= valueScale;
		value += valueOffset;

		// constrain to [0.0, 1.0]
		value = MathOps.modInterval(value);

		return value;
	}

	public static PrimitivePattern forEach(Pattern other) {
		if (!other.isDiscretePattern())
			throw new IllegalArgumentException(
					"Other pattern in forEach is not made of discrete events!");

		EventCollection events = new EventCollection();

		for (Event event : other.getEvents().values()) {
			if (event.getValue() != 0.0) {
				Interval[] longShort = Interval.shortenBy(event.getInterval(),
						Scheduler.minimumResolution.multiply(other
								.getTimeScale()));
				events.add(new Event(longShort[0], 1.0));
				events.add(new Event(longShort[1], 0.0));
			}
		}

		return new PrimitivePattern(other.loom, events);
	}

	public PrimitivePattern clone() throws CloneNotSupportedException {
		PrimitivePattern copy = new PrimitivePattern(loom);
		if (events != null)
			copy.events = (EventCollection) events.clone();
		else if (function != null)
			copy.function = function; // immutable
		return copy;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PrimitivePattern(");
		if (events != null)
			sb.append(events.toString());
		else if (function != null)
			sb.append(function.toString());
		sb.append(")@");
		sb.append(Integer.toHexString(hashCode()));
		return sb.toString();
	}
}
