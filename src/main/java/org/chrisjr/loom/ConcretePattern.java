package org.chrisjr.loom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.chrisjr.loom.continuous.ConstantFunction;
import org.chrisjr.loom.continuous.ContinuousFunction;
import org.chrisjr.loom.mappings.*;
import org.chrisjr.loom.time.Interval;
import org.chrisjr.loom.util.MathOps;

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
	public Collection<Callable<?>> getExternalMappings() {
		Collection<Callable<?>> callbacks = new ArrayList<Callable<?>>();
		for (MappingType mapping : externalMappings) {
			if (outputMappings.containsKey(mapping))
				callbacks.add((Callable<?>) getAs(mapping));
		}
		return callbacks;
	}

	@Override
	public Collection<DrawCommand> getDrawCommands() {
		if (outputMappings.containsKey(MappingType.DRAW_COMMAND))
			return Collections
					.singletonList((DrawCommand) getAs(MappingType.DRAW_COMMAND));
		else
			return Collections.emptyList();
	}

	/**
	 * Originally called getExternalMappings, but the new collection created
	 * slowed things down.
	 * 
	 * @return true if external mappings are present
	 */
	@Override
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

	@Override
	public boolean hasMapping(MappingType mapping) {
		return outputMappings.containsKey(mapping);
	}

	@Override
	public double getValueFor(Interval now) {
		double value = defaultValue;
		if (this.function != null) {
			try {
				// midpoint of function within current interval
				value = function.call(now);
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

	public static ConcretePattern forEach(Pattern other) {
		return forEach(other, null);
	}

	public static ConcretePattern forEach(Pattern other, Pattern parent) {
		if (!other.isDiscretePattern())
			throw new IllegalArgumentException(
					"Other pattern in forEach is not made of discrete events!");

		EventQueryable proxy = new EventBoundaryProxy(parent != null ? parent
				: other, other.getEvents());

		return new ConcretePattern(other.loom, proxy);
	}

	@Override
	public ConcretePattern clone() throws CloneNotSupportedException {
		ConcretePattern copy = new ConcretePattern(loom);
		if (events != null)
			copy.events = events;
		else if (function != null)
			copy.function = function;

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
