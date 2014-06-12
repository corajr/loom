package org.chrisjr.loom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

import javax.sound.midi.*;

import netP5.NetAddress;

import org.apache.commons.math3.fraction.BigFraction;

import org.chrisjr.loom.continuous.*;
import org.chrisjr.loom.mappings.*;
import org.chrisjr.loom.time.*;
import org.chrisjr.loom.transforms.*;
import org.chrisjr.loom.util.*;

import oscP5.OscBundle;
import oscP5.OscMessage;

/**
 * The base class for patterns in Loom. ConcretePatterns may be discrete or
 * continuous, while compound patterns (the default) can contain a combination
 * of both.
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

	protected boolean isConcrete;

	public enum MappingType {
		INTEGER, // inclusive range
		FLOAT, // inclusive range
		COLOR, // 32-bit integer (ARGB) format
		MIDI_COMMAND, // NOTE_ON, NOTE_OFF, etc.
		MIDI_CHANNEL, // 0-15
		MIDI_DATA1, // byte 1 of command
		MIDI_DATA2, // byte 2 of command (optional)
		MIDI_MESSAGE, // javax.sound.midi.MidiMessage suitable for sending
		OSC_MESSAGE, // OscMessage with arbitrary data
		OSC_BUNDLE, // collection of OscMessages
		CALLABLE, // a function object
		STATEFUL_CALLABLE, // a function object that has internal state
		OBJECT // generic object
	}

	final protected MappingType[] externalMappings = new MappingType[] {
			MappingType.CALLABLE, MappingType.STATEFUL_CALLABLE };

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
			ContinuousFunction function, boolean isConcrete) {
		this.loom = loom;
		this.isConcrete = isConcrete;

		if (this.loom != null && !isConcrete)
			addSelfTo(loom);

		if (events != null || function != null) {
			this.children = new PatternCollection();
			if (events != null)
				addChild(new ConcretePattern(loom, events));
			else if (function != null)
				addChild(new ConcretePattern(loom, function));
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

	protected ConcretePattern getConcretePattern() {
		if (isConcretePattern()) {
			return (ConcretePattern) this;
		} else if (children != null && children.size() > 0) {
			return (ConcretePattern) getChild(0);
		} else {
			throw new IllegalStateException("Pattern is empty!");
		}
	}

	public Pattern putMapping(MappingType mappingType, Mapping<?> mapping) {
		getOutputMappings().put(mappingType, mapping);
		return this;
	}

	public ConcurrentMap<MappingType, Mapping<?>> getOutputMappings() {
		return getConcretePattern().getOutputMappings();
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
		addChild(new ConcretePattern(loom, newEvents));
		return this;
	}

	public Pattern extend(Integer... values) {
		EventCollection newEvents = EventCollection.fromInts(values);
		addChild(new ConcretePattern(loom, newEvents));
		return this;
	}

	public double getValue() {
		return getValueFor(getCurrentInterval());
	}

	public double getValueFor(Interval now) {
		ConcretePattern pattern = getConcretePattern();
		if (pattern == null)
			throw new IllegalStateException(
					"Cannot get value from empty Pattern!");

		return getConcretePattern().getValueFor(now);
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

	private static int getIntOrElse(Integer result, int defaultInt) {
		return result != null ? result.intValue() : defaultInt;
	}

	public Pattern asInt(int lo, int hi) {
		putMapping(MappingType.INTEGER, new IntMapping(lo, hi));
		return this;
	}

	public int asInt() {
		Integer result = (Integer) getAs(MappingType.INTEGER);
		return getIntOrElse(result, Integer.MIN_VALUE);
	}

	/**
	 * Set a mapping from the pattern's events to sounds
	 * 
	 * @param instrument
	 *            the name of a MIDI instrument to trigger
	 * @return the updated pattern
	 */
	public Pattern asMidi(String instrument) {
		// TODO program change message
		return asMidiMessage(this);
	}

	public Pattern asMidiCommand(Integer... commands) {
		putMapping(MappingType.MIDI_COMMAND, new MidiCommandMapping(commands));
		return this;
	}

	public int asMidiCommand() {
		Integer result = (Integer) getAs(MappingType.MIDI_COMMAND);
		return getIntOrElse(result, Integer.MIN_VALUE);
	}

	public Pattern asMidiChannel(Integer... channels) {
		putMapping(MappingType.MIDI_CHANNEL, new MidiChannelMapping(channels));
		return this;
	}

	public int asMidiChannel() {
		Integer result = (Integer) getAs(MappingType.MIDI_CHANNEL);
		return getIntOrElse(result, Integer.MIN_VALUE);
	}

	public Pattern asMidiData1(int lo, int hi) {
		putMapping(MappingType.MIDI_DATA1, new IntMapping(lo, hi));
		return this;
	}

	public int asMidiData1() {
		Integer result = (Integer) getAs(MappingType.MIDI_DATA1);
		return getIntOrElse(result, Integer.MIN_VALUE);
	}

	public Pattern asMidiNote(Integer... values) {
		Arrays.sort(values);
		return asMidiData1(values[0], values[values.length - 1]);
	}

	public int asMidiNote() {
		return asMidiData1();
	}

	public Pattern asMidiData2(int lo, int hi) {
		putMapping(MappingType.MIDI_DATA2, new IntMapping(lo, hi));
		return this;
	}

	public int asMidiData2() {
		Integer result = (Integer) getAs(MappingType.MIDI_DATA2);
		return getIntOrElse(result, Integer.MIN_VALUE);
	}

	public Pattern asMidiMessage(Pattern notes) {
		ConcretePattern commands = ConcretePattern.forEach(
				getConcretePattern(), 2);
		commands.asMidiCommand(-1, ShortMessage.NOTE_OFF, ShortMessage.NOTE_ON);

		Pattern channels = (new Pattern(loom, 1.0)).asMidiChannel(0);
		ContinuousFunction velocityFunc = new ThresholdFunction(commands, 1.0);

		Pattern velocities = (new Pattern(loom, velocityFunc)).asMidiData2(0,
				127);
		return asMidiMessage(commands, channels, notes, velocities);
	}

	@SuppressWarnings("unchecked")
	public Pattern asMidiMessage(ConcretePattern commands, Pattern channels,
			Pattern notes, Pattern velocities) {

		if (isConcretePattern()) {
			putMapping(MappingType.MIDI_MESSAGE, new MidiMessageMapping(
					commands, channels, notes, velocities));

			final Pattern original = this;

			Callable<Void> sendMidi = new Callable<Void>() {
				public Void call() {
					MidiMessage mess = original.asMidiMessage();
					if (mess != null)
						loom.getMidiBus().sendMessage(mess);
					return null;
				}
			};
			asStatefulCallable(CallableOnChange.fromCallables(sendMidi,
					sendMidi));
		} else {
			commands.asMidiMessage(commands, channels, notes, velocities);

			addChild(commands);
		}

		return this;
	}

	public MidiMessage asMidiMessage() {
		return (MidiMessage) getAs(MappingType.MIDI_MESSAGE);
	}

	public Pattern asOscMessage(String addressPattern) {
		return asOscMessage(addressPattern, 1);
	}

	public Pattern asOscMessage(String addressPattern, int value) {
		ConcretePattern subPattern = new ConcretePattern(loom, 1.0);
		return asOscMessage(addressPattern, subPattern,
				new IntMapping(0, value));
	}

	public Pattern asOscMessage(String addressPattern, Mapping<?> mapping) {
		final Pattern original = this;
		putMapping(MappingType.OSC_MESSAGE, new OscMessageMapping(original,
				addressPattern, mapping));
		return this;
	}

	public Pattern asOscMessage(String addressPattern,
			ConcretePattern subPattern, Mapping<?> mapping) {
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
		if (isConcretePattern()) {
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
			ConcretePattern bundleTrigger = ConcretePattern
					.forEach(getConcretePattern());
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
		return getIntOrElse(result, 0x00000000);
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
		if (isConcretePattern())
			return getConcretePattern().getExternalMappings();

		Collection<Callable<?>> callables = new ArrayList<Callable<?>>();
		if (children != null) {
			for (Pattern child : children) {
				callables.addAll(child.getExternalMappings());
			}
		}

		return callables;
	}

	public boolean hasMapping(MappingType mapping) {
		return getConcretePattern().hasMapping(mapping);
	}

	public Boolean hasExternalMappings() {
		boolean result = false;
		if (isConcretePattern()) {
			result = getConcretePattern().hasExternalMappings();
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

	public boolean isConcretePattern() {
		return isConcrete;
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

		ConcretePattern concrete = new ConcretePattern(loom, events);
		concrete.loop();
		concrete.setLoopInterval(interval);

		final Pattern original = this;

		StatefulCallable[] ops = CallableOnChange.fromTransform(transform,
				original);

		concrete.asStatefulCallable(ops);

		addChild(concrete);

		return this;
	}

	public Pattern rewrite(EventRewriter eventRewriter) {
		EventCollection events = getEvents();
		if (events != null) {
			getConcretePattern().events = eventRewriter.apply(events);
		}
		return this;
	}

	public Pattern forEach(Callable<Void> callable) {

		ConcretePattern concrete = ConcretePattern
				.forEach(getConcretePattern());

		StatefulCallable[] ops = CallableOnChange.fromCallable(callable);
		concrete.asStatefulCallable(ops);

		addSibling(concrete);

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
		if (!isConcretePattern())
			getConcretePattern().setValueOffset(valueOffset);
	}

	public double getValueScale() {
		return valueScale;
	}

	public void setValueScale(double valueScale) {
		this.valueScale = valueScale;
		if (!isConcretePattern())
			getConcretePattern().setValueScale(valueScale);
	}

	public EventCollection getEvents() {
		EventCollection events = null;
		if (isConcretePattern())
			events = getConcretePattern().events;
		return events;
	}

	public Pattern clone() throws CloneNotSupportedException {
		if (isConcretePattern())
			return getConcretePattern().clone();

		Pattern copy = new Pattern(loom);

		// TODO add other fields here
		for (Pattern pattern : children) {
			copy.addChild(pattern.clone());
		}
		return copy;
	}
}