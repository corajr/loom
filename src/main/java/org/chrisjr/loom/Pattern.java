package org.chrisjr.loom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

import netP5.NetAddress;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.continuous.ConstantFunction;
import org.chrisjr.loom.continuous.ContinuousFunction;
import org.chrisjr.loom.mappings.*;
import org.chrisjr.loom.time.Interval;
import org.chrisjr.loom.time.Scheduler;
import org.chrisjr.loom.transforms.Transform;
import org.chrisjr.loom.util.CallableOnChange;
import org.chrisjr.loom.util.StatefulCallable;

import oscP5.OscBundle;
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

	public Pattern putMapping(MappingType mappingType, Mapping<?> mapping) {
		getOutputMappings().put(mappingType, mapping);
		return this;
	}

	public ConcurrentMap<MappingType, Mapping<?>> getOutputMappings() {
		return getPrimitivePattern().getOutputMappings();
	}

	protected Object getAs(MappingType mapping) throws IllegalStateException {
		return getAs(mapping, getValue());
	}

	@SuppressWarnings("unchecked")
	protected Object getAs(MappingType mapping, double value)
			throws IllegalStateException {
		Mapping<Object> cb = (Mapping<Object>) getOutputMappings().get(mapping);

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
		putMapping(MappingType.INTEGER, new IntMapping(lo, hi));
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
		PrimitivePattern beats = new PrimitivePattern(loom);
		beats.putMapping(MappingType.MIDI, new NoopMapping());
		addChild(beats);
		return this;
	}

	public Pattern asOscMessage(String addressPattern) {
		return asOscMessage(addressPattern, 1);
	}

	public Pattern asOscMessage(String addressPattern, int value) {
		PrimitivePattern subPattern = new PrimitivePattern(loom, 1.0);
		return asOscMessage(addressPattern, subPattern,
				new IntMapping(0, value));
	}

	public Pattern asOscMessage(String addressPattern, Mapping<?> mapping) {
		final PrimitivePattern original = getPrimitivePattern();
		putMapping(MappingType.OSC_MESSAGE, new OscMessageMapping(original,
				addressPattern, mapping));
		return this;
	}

	public Pattern asOscMessage(String addressPattern,
			PrimitivePattern subPattern, Mapping<?> mapping) {
		subPattern.asOscMessage(addressPattern, mapping);
		addChild(subPattern);
		return this;
	}

	public OscMessage asOscMessage() {
		return (OscMessage) getAs(MappingType.OSC_MESSAGE);
	}

	public Pattern asOscBundle(NetAddress remoteAddress) {
		return asOscBundle(remoteAddress, children.toArray(new Pattern[] {}));
	}

	public Pattern asOscBundle(final NetAddress remoteAddress,
			final Pattern... patterns) {
		if (isPrimitivePattern()) {
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

			putMapping(MappingType.OSC_BUNDLE,
					new OscBundleMapping(oscPatterns));

			final Pattern original = this;

			// It seemed like this might be overriding whatever else is set as a
			// StatefulCallable, but in fact the current pattern would have been
			// created especially to trigger the bundle sending. Clumsy but it
			// works?

			asStatefulCallable(CallableOnChange
					.fromCallable(new Callable<Void>() {
						public Void call() {
							loom.getOscP5().send(original.asOscBundle(),
									remoteAddress);
							return null;
						}
					}));

		} else {
			PrimitivePattern bundleTrigger = PrimitivePattern
					.forEach(getPrimitivePattern());
			bundleTrigger.asOscBundle(remoteAddress, patterns);

			addChild(bundleTrigger);
		}

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
		putMapping(MappingType.COLOR, new ColorMapping(colors));
		return this;
	}

	public int asColor() {
		Integer result = (Integer) getAs(MappingType.COLOR);
		return result != null ? result.intValue() : 0x00000000;
	}

	public Pattern asObject(Object... objects) {
		putMapping(MappingType.OBJECT, new ObjectMapping<Object>(objects));
		return this;
	}

	public Object asObject() {
		return getAs(MappingType.OBJECT);
	}

	public Pattern asCallable(Callable<?>... callables) {
		putMapping(MappingType.CALLABLE, new ObjectMapping<Callable<?>>(
				callables));
		return this;
	}

	public StatefulCallable asStatefulCallable() {
		return (StatefulCallable) getAs(MappingType.STATEFUL_CALLABLE);
	}

	public Pattern asStatefulCallable(StatefulCallable... callables) {
		putMapping(MappingType.STATEFUL_CALLABLE,
				new ObjectMapping<StatefulCallable>(callables));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Callable<Object> asCallable() {
		return (Callable<Object>) getAs(MappingType.CALLABLE);
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