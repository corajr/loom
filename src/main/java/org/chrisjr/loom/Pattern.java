package org.chrisjr.loom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import netP5.NetAddress;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.continuous.ConstantFunction;
import org.chrisjr.loom.continuous.ContinuousFunction;
import org.chrisjr.loom.mappings.Mapping;
import org.chrisjr.loom.time.Interval;
import org.chrisjr.loom.time.Scheduler;
import org.chrisjr.loom.transforms.Transform;
import org.chrisjr.loom.util.CallableOnChange;
import org.chrisjr.loom.util.StatefulCallable;

import oscP5.OscMessage;

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

	protected double valueOffset = 0.0;
	protected double valueScale = 1.0;

	protected boolean isPrimitive;

	public enum MappingType {
		INTEGER, FLOAT, COLOR, MIDI, OSC_MESSAGE, OSC_BUNDLE, CALLABLE, STATEFUL_CALLABLE, OBJECT
	}

	final protected MappingType[] externalMappings = new MappingType[] {
			MappingType.MIDI, MappingType.CALLABLE,
			MappingType.STATEFUL_CALLABLE };

	/**
	 * Constructor for an empty Pattern.
	 * 
	 * @param loom
	 *            the loom that holds this pattern (can be null)
	 */
	public Pattern(Loom loom) {
		this(loom, null, null, false);
	}

	public Pattern(Loom loom, double defaultValue) {
		this(loom, null, new ConstantFunction(defaultValue), false);
	}

	public Pattern(Loom loom, ContinuousFunction function) {
		this(loom, null, function, false);
	}

	public Pattern(Loom loom, EventCollection events) {
		this(loom, events, null, false);
	}

	public Pattern(Loom loom, EventCollection events,
			ContinuousFunction function, boolean isPrimitive) {
		this.loom = loom;
		this.isPrimitive = isPrimitive;

		if (this.loom != null && !isPrimitive)
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

	protected void addSibling(Pattern sibling) {
		if (parent != null)
			parent.addChild(sibling);
		else
			loom.patterns.add(sibling);
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
			throw new IllegalStateException("Pattern is empty!");
		}
	}

	public Pattern putMapping(MappingType mappingType, Mapping mapping) {		
		getPrimitivePattern().outputMappings.put(mappingType, mapping);
		return this;
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
		return getValueFor(getCurrentInterval());
	}

	public double getValueFor(Interval now) {
		PrimitivePattern pattern = getPrimitivePattern();
		if (pattern == null)
			throw new IllegalStateException(
					"Cannot get value from empty Pattern!");

		return getPrimitivePattern().getValueFor(now);
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

	public Pattern asOscMessage(String addr) {
		return asOscMessage(addr, 1);
	}

	public Pattern asOscMessage(String addr, int value) {
		PrimitivePattern subPattern = new PrimitivePattern(loom, 1.0);
		subPattern.asInt(0, value);

		return asOscMessage(addr, subPattern, MappingType.INTEGER);
	}

	public Pattern asOscMessage(String addr, PrimitivePattern subPattern,
			MappingType mapping) {

		subPattern.asOscMessage(addr, mapping);
		addChild(subPattern);
		return this;
	}

	public OscMessage asOscMessage() {
		return getPrimitivePattern().asOscMessage();
	}

	public Pattern asOscBundle(NetAddress remoteAddress, Pattern... patterns) {
		PrimitivePattern bundleTrigger = PrimitivePattern
				.forEach(getPrimitivePattern());
		bundleTrigger.asOscBundle(remoteAddress, patterns);

		addChild(bundleTrigger);
		return this;
	}

	/**
	 * Set a mapping from the pattern's events to colors, blending between them
	 * using <code>lerpColor</code> in HSB mode.
	 * 
	 * @param colors
	 *            a list of colors to represent each state
	 * @return the updated pattern
	 */
	public Pattern asColor(int... colors) {
		getPrimitivePattern().asColor(colors);
		return this;
	}

	/**
	 * @return a color as 32-bit int
	 */
	public int asColor() {
		return getPrimitivePattern().asColor();
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

	public boolean hasMapping(MappingType mapping) {
		return getPrimitivePattern().hasMapping(mapping);
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
		return isPrimitive;
	}

	public boolean isDiscretePattern() {
		return getEvents() != null;
	}

	// Transformations

	public Pattern speed(double multiplier) {
		return speed(new BigFraction(multiplier));
	}

	public Pattern speed(BigFraction multiplier) {
		setTimeScale(getTimeScale().multiply(multiplier));
		return this;
	}

	public Pattern reverse() {
		return speed(-1);
	}

	public Pattern shift(double amt) {
		return shift(new BigFraction(amt));
	}

	public Pattern shift(BigFraction amt) {
		setTimeOffset(getTimeOffset().add(amt));
		return this;
	}

	public Pattern invert() {
		setValueScale(-1.0);
		setValueOffset(1.0);
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

		Interval[] longShort = Interval.shortenBy(interval,
				Scheduler.minimumResolution.multiply(getTimeScale()));

		events.add(new Event(longShort[0], 0.0));
		events.add(new Event(longShort[1], 1.0));

		PrimitivePattern primitive = new PrimitivePattern(loom, events);
		primitive.loop();
		primitive.setLoopInterval(interval);

		final Pattern original = this;

		StatefulCallable[] ops = CallableOnChange.fromTransform(transform,
				original);

		primitive.asStatefulCallable(ops);

		addChild(primitive);

		return this;
	}

	public Pattern forEach(Callable<Void> callable) {

		getPrimitivePattern();
		PrimitivePattern primitive = PrimitivePattern
				.forEach(getPrimitivePattern());

		StatefulCallable[] ops = CallableOnChange.fromCallable(callable);
		primitive.asStatefulCallable(ops);

		addSibling(primitive);

		return this;
	}

	public Pattern clear() {
		children.clear();
		return this;
	}

	// Time shifts

	public BigFraction getTimeOffset() {
		return timeOffset;
	}

	public void setTimeOffset(double i) {
		setTimeOffset(new BigFraction(i));
	}

	public void setTimeOffset(BigFraction timeOffset) {
		this.timeOffset = timeOffset;
	}

	public BigFraction getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(double i) {
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

	public double getValueOffset() {
		return valueOffset;
	}

	public void setValueOffset(double valueOffset) {
		this.valueOffset = valueOffset;
		if (!isPrimitivePattern())
			getPrimitivePattern().setValueOffset(valueOffset);
	}

	public double getValueScale() {
		return valueScale;
	}

	public void setValueScale(double valueScale) {
		this.valueScale = valueScale;
		if (!isPrimitivePattern())
			getPrimitivePattern().setValueScale(valueScale);
	}

	public EventCollection getEvents() {
		EventCollection events = null;
		if (isPrimitivePattern())
			events = getPrimitivePattern().events;
		return events;
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
