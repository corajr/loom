package org.chrisjr.loom;

import processing.core.PApplet;
import processing.core.PConstants;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.continuous.ConstantFunction;
import org.chrisjr.loom.continuous.ContinuousFunction;
import org.chrisjr.loom.time.Interval;
import org.chrisjr.loom.transforms.*;
import org.chrisjr.loom.util.*;

/**
 * The base class for patterns in Loom. Primitive Patterns may be discrete or
 * continuous, while compound patterns can contain a combination of both.
 * 
 * @author chrisjr
 */
public class Pattern implements Cloneable {
	Loom loom;

	protected PatternCollection children = null;
	protected Pattern parent = null;

	protected double defaultValue;

	boolean isLooping = false;
	BigFraction timeOffset = new BigFraction(0);
	BigFraction timeScale = new BigFraction(1);
	Interval loopInterval = new Interval(0, 1);

	public enum Mapping {
		INTEGER, FLOAT, COLOR, COLOR_BLEND, MIDI, OSC, CALLABLE, STATEFUL_CALLABLE, OBJECT
	}

	final protected Mapping[] externalMappings = new Mapping[] { Mapping.MIDI,
			Mapping.OSC, Mapping.CALLABLE, Mapping.STATEFUL_CALLABLE };

	/**
	 * Constructor for an empty Pattern.
	 * 
	 * @param loom
	 *            the loom that holds this pattern (can be null)
	 */
	public Pattern(Loom loom) {
		this(loom, null, null);
	}

	public Pattern(Loom loom, double defaultValue) {
		this(loom, null, new ConstantFunction(defaultValue));
	}

	public Pattern(Loom loom, ContinuousFunction function) {
		this(loom, null, function);
	}

	public Pattern(Loom loom, EventCollection events) {
		this(loom, events, null);
	}

	public Pattern(Loom loom, EventCollection events,
			ContinuousFunction function) {
		this.loom = loom;
		if (this.loom != null)
			addSelfTo(loom);

		if (events != null || function != null) {
			this.children = new PatternCollection();
			if (events != null)
				addChild(new PrimitivePattern(loom, events));
			else if (function != null)
				addChild(new PrimitivePattern(loom, function));
		}
	}

	protected void addSelfTo(Loom loom) {
		loom.patterns.add(this);
	}

	protected void addChild(Pattern child) {
		if (children == null)
			children = new PatternCollection();
		child.parent = this;
		children.add(child);
	}

	protected void removeChild(Pattern child) {
		if (children != null)
			children.remove(child);
	}

	protected Pattern getChild(int i) {
		if (children == null)
			return null;
		return children.get(i);
	}

	protected PrimitivePattern getPrimitivePattern() {
		if (isPrimitivePattern()) {
			return (PrimitivePattern) this;
		} else if (children != null && children.size() > 0) {
			return (PrimitivePattern) getChild(0);
		} else {
			return null;
		}
	}

	/**
	 * @param string
	 *            a string such as "10010010" describing a pattern to be tacked
	 *            on at the end
	 * @return the updated pattern
	 */
	public Pattern extend(String string) {
		EventCollection newEvents = EventCollection.fromString(string);
		addChild(new PrimitivePattern(loom, newEvents));
		return this;
	}

	public Pattern extend(Integer... values) {
		EventCollection newEvents = EventCollection.fromInts(values);
		addChild(new PrimitivePattern(loom, newEvents));
		return this;
	}

	public double getValue() {
		PrimitivePattern pattern = getPrimitivePattern();
		if (pattern == null)
			throw new IllegalStateException(
					"Cannot get value from empty Pattern!");
		return getPrimitivePattern().getValue();
	}

	public Interval getCurrentInterval() {
		Interval interval;
		if (this.parent != null) {
			interval = parent.getCurrentInterval();
		} else {
			interval = loom.getCurrentInterval();
		}

		if (timeScale.compareTo(BigFraction.ZERO) > 0)
			interval = interval.multiply(timeScale);
		else
			interval = interval.multiplyMod(timeScale, loopInterval);
		interval = interval.add(timeOffset);

		if (isLooping) {
			interval = interval.modulo(loopInterval);
		}

		return interval;
	}

	public void once() {
		isLooping = false;
	}

	public void loop() {
		isLooping = true;
	}

	// Mappings

	public Pattern asInt(int lo, int hi) {
		getPrimitivePattern().asInt(lo, hi);
		return this;
	}

	public int asInt() {
		return getPrimitivePattern().asInt();
	}

	/**
	 * Set a mapping from the pattern's events to sounds
	 * 
	 * @param instrument
	 *            the name of a MIDI instrument to trigger
	 * @return the updated pattern
	 */
	public Pattern asMidi(String instrument) {
		// TODO note on, note off
		// how to make compound mapping?

		addChild(new PrimitivePattern(loom).asMidi(instrument));
		addChild(new PrimitivePattern(loom, 1.0).asInt(0, 127));
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
		getPrimitivePattern().asColor(colors);
		return this;
	}

	/**
	 * @return an the "color" data type (32-bit int)
	 */
	public int asColor() {
		return getPrimitivePattern().asColor();
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
		getPrimitivePattern().asColorBlend(colors);
		return this;
	}

	public int asColorBlend() {
		return getPrimitivePattern().asColorBlend();
	}

	public Pattern asObject(Object... objects) {
		getPrimitivePattern().asObject(objects);
		return this;
	}

	public Object asObject() {
		return getPrimitivePattern().asObject();
	}

	public Pattern asCallable(Callable<?>... callables) {
		getPrimitivePattern().asCallable(callables);
		return this;
	}

	public Callable<Object> asCallable() {
		return getPrimitivePattern().asCallable();
	}

	public Pattern asStatefulCallable(StatefulCallable... callables) {
		getPrimitivePattern().asStatefulCallable(callables);
		return this;
	}

	public StatefulCallable asStatefulCallable() {
		return getPrimitivePattern().asStatefulCallable();
	}

	public Collection<Callable<?>> getExternalMappings() {
		if (isPrimitivePattern())
			return getPrimitivePattern().getExternalMappings();

		Collection<Callable<?>> callables = new ArrayList<Callable<?>>();
		if (children != null) {
			for (Pattern child : children) {
				callables.addAll(child.getExternalMappings());
			}
		}

		return callables;
	}

	public Boolean hasExternalMappings() {
		boolean result = false;
		if (isPrimitivePattern()) {
			result = getPrimitivePattern().hasExternalMappings();
		} else {
			if (children != null) {
				for (Pattern pattern : children) {
					if (pattern.hasExternalMappings()) {
						result = true;
						break;
					}
				}
			}
		}

		return result;
	}

	public boolean isPrimitivePattern() {
		return false;
	}

	// Transformations

	public Pattern reverse() {
		setTimeScale(-1);
		return this;
	}

	public Pattern every(double cycles, Transform transform) {
		return every(new BigFraction(cycles), transform);
	}

	public Pattern every(BigFraction fraction, Transform transform) {
		return every(new Interval(BigFraction.ZERO, fraction), transform);
	}

	public Pattern every(Interval interval, final Transform transform) {
		EventCollection events = new EventCollection();

		Interval after = new Interval(interval.getEnd(), interval.getEnd().add(
				new BigFraction(1, 1000)));

		events.add(new Event(interval, 0.0));
		events.add(new Event(after, 1.0));

		PrimitivePattern primitive = new PrimitivePattern(loom, events);

		final AtomicInteger v = new AtomicInteger();
		final Pattern original = this;
		StatefulCallable noop = new StatefulNoop(v);
		StatefulCallable doTransform = new CallableOnChange(v,
				new Callable<Void>() {
					public Void call() {
						transform.call(original);
						return null;
					}
				});

		primitive.asStatefulCallable(noop, doTransform);
		return this;
	}

	// Time shifts

	public BigFraction getTimeOffset() {
		return timeOffset;
	}

	public void setTimeOffset(int i) {
		setTimeOffset(new BigFraction(i));
	}

	public void setTimeOffset(BigFraction timeOffset) {
		this.timeOffset = timeOffset;
	}

	public BigFraction getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(int i) {
		setTimeScale(new BigFraction(i));
	}

	public void setTimeScale(BigFraction timeScale) {
		this.timeScale = timeScale;
	}

	public Interval getLoopInterval() {
		return loopInterval;
	}

	public void setLoopInterval(Interval loopInterval) {
		this.loopInterval = loopInterval;
	}

	public EventCollection getEvents() {
		return getPrimitivePattern().events;
	}

	public Pattern clone() throws CloneNotSupportedException {
		if (isPrimitivePattern())
			return getPrimitivePattern().clone();

		Pattern copy = new Pattern(loom);

		// TODO add other fields here
		for (Pattern pattern : children) {
			copy.addChild(pattern.clone());
		}
		return copy;
	}
}
