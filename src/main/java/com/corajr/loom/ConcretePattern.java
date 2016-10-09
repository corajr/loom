package com.corajr.loom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.corajr.loom.continuous.ConstantFunction;
import com.corajr.loom.continuous.ContinuousFunction;
import com.corajr.loom.mappings.*;
import com.corajr.loom.time.Interval;
import com.corajr.loom.time.IntervalMath;

/**
 * The implementation of a Pattern, which stores its output mappings and
 * associated events/function. ConcretePatterns have no children and hold
 * exactly one value at any given time.
 * 
 * @author corajr
 */
public class ConcretePattern extends Pattern {
	protected ConcurrentMap<MappingType, Mapping<?>> outputMappings = new ConcurrentHashMap<MappingType, Mapping<?>>();

	protected EventQueryable events = null;
	protected ContinuousFunction function = null;

	public ConcretePattern(Loom loom) {
		super(loom, null, null, true);
	}

	public ConcretePattern(Loom loom, double defaultValue) {
		super(loom, null, null, true);
		this.function = new ConstantFunction(defaultValue);
	}

	public ConcretePattern(Loom loom, EventQueryable events) {
		super(loom, null, null, true);
		this.events = events;
	}

	public ConcretePattern(Loom loom, ContinuousFunction function) {
		super(loom, null, null, true);
		this.function = function;
	}

	@Override
	public ConcurrentMap<MappingType, Mapping<?>> getOutputMappings() {
		return outputMappings;
	}

	@Override
	public Collection<Callable<?>> getActiveMappingsFor(Interval interval) {
		if (this.events != null)
			return getAllCallablesInInterval(interval);

		Collection<Callable<?>> callbacks = new ArrayList<Callable<?>>();
		for (MappingType mapping : activeMappings) {
			if (outputMappings.containsKey(mapping))
				callbacks.add((Callable<?>) getAs(mapping,
						getValueFor(interval)));
		}
		return callbacks;
	}

	private Collection<Callable<?>> getAllCallablesInInterval(Interval interval) {
		Collection<Callable<?>> callables = new ArrayList<Callable<?>>();
		Collection<LEvent> activeEvents = this.events.getForInterval(interval);
		Collection<MappingType> myMappings = new ArrayList<MappingType>();

		for (MappingType mapping : activeMappings) {
			if (outputMappings.containsKey(mapping))
				myMappings.add(mapping);
		}

		for (LEvent e : activeEvents) {
			double eventValue = transformValue(e.getValue());

			for (MappingType mapping : myMappings) {
				if (mapping == MappingType.CALLABLE_WITH_ARG) {
					EventMapping<Callable<?>> callMap = (EventMapping<Callable<?>>) outputMappings
							.get(MappingType.CALLABLE_WITH_ARG);
					Callable<?> sc = callMap.call(e);
					if (sc != null)
						callables.add(sc);
				} else {
					callables.add((Callable<?>) getAs(mapping, eventValue));
				}
			}

		}

		return callables;
	}

	@Override
	public Collection<DrawCommand> getDrawCommands() {
		if (outputMappings.containsKey(MappingType.DRAW_COMMAND))
			return Collections
					.singletonList((DrawCommand) getAs(MappingType.DRAW_COMMAND));
		else
			return Collections.emptyList();
	}

	@Override
	public boolean hasActiveMappings() {
		boolean result = false;
		for (MappingType mapping : activeMappings) {
			if (outputMappings.containsKey(mapping)) {
				result = true;
				break;
			}
		}
		return result;
	}

	@Override
	public boolean hasMapping(MappingType mapping) {
		return outputMappings.containsKey(mapping);
	}

	@Override
	public double getValueFor(Interval now) {
		double value = defaultValue;
		if (this.function != null) {
			try {
				// averages the function's value at either endpoint of interval
				value = function.call(now);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (this.events != null) {
			Collection<LEvent> activeEvents = this.events.getForInterval(now);
			for (LEvent e : activeEvents) {
				value = e.getValue();
			}
		}
		value = transformValue(value);

		return value;
	}

	private double transformValue(double value) {
		// apply transformations
		value *= valueScale;
		value += valueOffset;

		// constrain to [0.0, 1.0]
		value = IntervalMath.modInterval(value);
		return value;
	}

	/**
	 * Generate a pattern of note on and off events from the events of another
	 * pattern.
	 * 
	 * @param other
	 *            the pattern to get events from
	 * @return a new pattern
	 * @see EventBoundaryProxy
	 */
	public static ConcretePattern forEach(Pattern other) {
		return forEach(other, null);
	}

	/**
	 * Generate a pattern of note on or note off events from the events of
	 * another pattern, depending on the boundary type desired.
	 * 
	 * @param other
	 *            the pattern to get events from
	 * @param boundaryType
	 *            the boundary type (note on == 1.0, note off == 0.5)
	 * @return a new pattern
	 * @see EventBoundaryProxy
	 */

	public static ConcretePattern forEach(Pattern other, Double boundaryType) {
		if (!other.isDiscretePattern())
			throw new IllegalArgumentException(
					"Other pattern in forEach is not made of discrete events!");

		EventQueryable proxy = new EventBoundaryProxy(other,
				other.getConcretePattern().events);

		if (boundaryType != null)
			proxy = new EventMatchFilter(proxy, boundaryType);

		return new ConcretePattern(other.loom, proxy);
	}

	@Override
	public ConcretePattern clone() {
		ConcretePattern copy = new ConcretePattern(loom);
		if (events != null) {
			EventCollection collection = getEvents();
			copy.events = (EventQueryable) (collection != null ? collection
					.clone() : events);
		} else if (function != null) {
			copy.function = function;
		}

		copy.outputMappings.putAll(outputMappings);
		return copy;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ConcretePattern(");
		if (events != null)
			sb.append(events.toString());
		else if (function != null)
			sb.append(function.toString());
		sb.append(")@");
		sb.append(Integer.toHexString(hashCode()));
		return sb.toString();
	}
}
