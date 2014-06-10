package org.chrisjr.loom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.chrisjr.loom.continuous.ConstantFunction;
import org.chrisjr.loom.continuous.ContinuousFunction;
import org.chrisjr.loom.mappings.*;
import org.chrisjr.loom.time.Interval;
import org.chrisjr.loom.time.Scheduler;
import org.chrisjr.loom.transforms.EventRewriter;
import org.chrisjr.loom.transforms.SubdivideRewriter;
import org.chrisjr.loom.util.MathOps;

public class PrimitivePattern extends Pattern {
	protected ConcurrentMap<MappingType, Mapping<?>> outputMappings = new ConcurrentHashMap<MappingType, Mapping<?>>();

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
		return forEach(other, 1);
	}

	public static PrimitivePattern forEach(Pattern other, int divisions) {
		if (!other.isDiscretePattern())
			throw new IllegalArgumentException(
					"Other pattern in forEach is not made of discrete events!");

		EventRewriter rewriter = new SubdivideRewriter(
				Scheduler.minimumResolution.multiply(other.getTimeScale()),
				divisions);

		return new PrimitivePattern(other.loom, rewriter.apply(other
				.getEvents()));
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
