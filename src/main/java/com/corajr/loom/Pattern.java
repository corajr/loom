package com.corajr.loom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.midi.*;

import netP5.NetAddress;

import org.apache.commons.math3.fraction.BigFraction;

import com.corajr.loom.continuous.*;
import com.corajr.loom.mappings.*;
import com.corajr.loom.time.*;
import com.corajr.loom.transforms.*;
import com.corajr.loom.util.*;
import com.corajr.loom.util.MidiTools.Instrument;
import com.corajr.loom.util.MidiTools.Percussion;
import com.corajr.loom.wrappers.OscP5Impl;

import oscP5.*;
import processing.core.PApplet;
import supercollider.*;
import ddf.minim.*;
import processing.sound.SoundFile;

/**
 * The base class for patterns in Loom. A Pattern may either be: discrete,
 * containing a non-overlapping series of events; continuous, its value a
 * function of time; or compound, containing a combination of the two.
 * 
 * A single pattern can only have one value at a time, and thus only a single
 * mapping of a given type. However, by adding child patterns or creating a new
 * Pattern using {@link #following}, multiple mappings can be assigned.
 * 
 * @see ConcretePattern
 * @author corajr
 */
public class Pattern implements Cloneable {
	Loom loom;

	public Turtle turtle = null;

	protected PatternCollection children = null;
	protected Pattern parent = null;
	private int selectedChild = -1;

	protected Pattern timeMatch = null;
	protected boolean useParentOffset = true;

	protected double defaultValue;

	boolean isLooping = false;
	BigFraction timeOffset = BigFraction.ZERO;
	BigFraction timeScale = BigFraction.ONE;
	Interval loopInterval = new Interval(0, 1);

	protected double valueOffset = 0.0;
	protected double valueScale = 1.0;

	protected Integer transposition = null;

	protected final AtomicInteger repeats = new AtomicInteger();
	private boolean repeaterSet = false;

	protected boolean isConcrete;

	/**
	 * Constants for each possible mapping from floating-point values to output.
	 * Only one mapping of each type is allowed per pattern.
	 * 
	 * @author corajr
	 */
	public enum MappingType {
		INTEGER, // inclusive range
		FLOAT, // inclusive range
		COLOR, // 32-bit integer (ARGB) format
		DRAW_COMMAND, // instruction to draw a shape, line, etc.
		TURTLE_DRAW_COMMAND, // stateful instruction to draw
		MIDI_COMMAND, // NOTE_ON, NOTE_OFF, etc.
		MIDI_CHANNEL, // 0-15
		MIDI_DATA1, // byte 1 of command
		MIDI_DATA2, // byte 2 of command (optional)
		MIDI_MESSAGE, // javax.sound.midi.MidiMessage suitable for sending
		OSC_MESSAGE, // OscMessage with arbitrary data
		OSC_BUNDLE, // collection of OscMessages
		CALLABLE, // a function object
		CALLABLE_WITH_ARG, // a function object that takes an argument
		STATEFUL_CALLABLE, // a function object that has internal state
		OBJECT // generic object
	}

	/**
	 * Mappings that are to be actively invoked by the scheduler, rather than
	 * passively queried.
	 */
	final protected MappingType[] activeMappings = new MappingType[] {
			MappingType.CALLABLE, // a Callable<Void>
			MappingType.CALLABLE_WITH_ARG, // a callable that depends on a value
			MappingType.STATEFUL_CALLABLE // a callable that keeps state
	};

	/**
	 * Constructor for an empty Pattern.
	 * 
	 * @param loom
	 *            the {@link Loom} that holds this pattern (can be null)
	 */
	public Pattern(Loom loom) {
		this(loom, null, null, false);
	}

	/**
	 * A pattern that returns a constant value between 0.0 and 1.0.
	 * 
	 * @param loom
	 *            the {@link Loom} that holds this pattern (can be null)
	 * @param defaultValue
	 *            the value of this pattern
	 */
	public Pattern(Loom loom, double defaultValue) {
		this(loom, null, new ConstantFunction(defaultValue), false);
	}

	/**
	 * A pattern with a value that is a continuous function of time.
	 * 
	 * @param loom
	 *            the {@link Loom} that holds this pattern (can be null)
	 * @param function
	 *            the value of the pattern as a function of time
	 */
	public Pattern(Loom loom, ContinuousFunction function) {
		this(loom, null, function, false);
	}

	/**
	 * A pattern comprised of the specified events.
	 * 
	 * @param loom
	 *            the {@link Loom} that holds this pattern (can be null)
	 * @param events
	 *            the events to be added
	 */
	public Pattern(Loom loom, LEvent... events) {
		this(loom, EventCollection.fromEvents(events));
	}

	/**
	 * A pattern comprised of the specified events.
	 * 
	 * @param loom
	 *            the {@link Loom} that holds this pattern (can be null)
	 * @param events
	 *            the events to be added
	 */
	public Pattern(Loom loom, Collection<LEvent> events) {
		this(loom, EventCollection.fromEvents(events));
	}

	/**
	 * A pattern comprised of the specified events.
	 * 
	 * @param loom
	 *            the {@link Loom} that holds this pattern (can be null)
	 * @param events
	 *            the {@link EventCollection} to be added
	 */

	public Pattern(Loom loom, EventCollection events) {
		this(loom, events, null, false);
	}

	/**
	 * Creates a Pattern and adds itself to the {@link Loom}.
	 * 
	 * Either a {@link EventCollection} or {@link ContinuousFunction} (not both)
	 * can be passed in, in which case it will create an underlying
	 * ConcretePattern.
	 * 
	 * @param loom
	 *            the {@link Loom} that holds this pattern (can be null)
	 * @param events
	 *            an {@link EventCollection} for the pattern
	 * @param function
	 *            a {@link ContinuousFunction} for the pattern
	 * @param isConcrete
	 *            is the pattern concrete (i.e. does it contain events/a
	 *            function directly)
	 */
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

	/**
	 * Takes an ABC tune as input and creates a new pattern suitable for MIDI
	 * output. For a quick overview of ABC, see:
	 * <http://trillian.mit.edu/~jc/doc/doc/ABCprimer.html>.
	 * 
	 * A minimal valid tune must contain three header lines: "X:" (a reference
	 * number), "T:" (a title) and "K:" (a key signature), followed by the
	 * notes. The key must be last; tempo, meter, and other headers are
	 * currently ignored. If the headers are omitted, the parser will do its
	 * best to add them appropriately.
	 * 
	 * Examples: A rising scale in C natural minor: "K:Cm\nCDEF|GABc"
	 * 
	 * The BACH motive (B-flat, A, C, B natural): "_BAc=B"
	 * 
	 * Beginning to "Maria" from West Side Story: "C|^FG2C|(3^FGA(3^FGA|^FG2||"
	 * 
	 * @param loom
	 *            the Loom on which the new pattern should be created
	 * @param tune
	 *            a string containing a valid ABC tune
	 * @return a new pattern constructed from the tune
	 */
	public static Pattern fromABC(Loom loom, String tune) {
		return AbcTools.fromString(loom, tune);
	}

	/**
	 * Creates a new pattern using a numerical string, which will be turned into
	 * equally spaced events. Values are scaled to the maximum present in the
	 * string. Example: <code>Pattern.fromString(loom, "024");</code> creates a
	 * pattern with three events, each 1/3 a cycle long, with values 0.0, 0.5,
	 * and 1.0.
	 * 
	 * @param loom
	 *            the Loom on which the new pattern should be created
	 * @param string
	 *            a string with digits
	 * @return a new pattern
	 * @see #extend(String)
	 */
	public static Pattern fromString(Loom loom, String string) {
		Pattern pattern = new Pattern(loom);
		return pattern.extend(string);
	}

	/**
	 * Creates a new pattern using a sequence of integers, which will be turned
	 * into equally spaced events. Values are scaled to the maximum present in
	 * the sequence. Example: <code>Pattern.fromString(loom, 0, 2, 4);</code>
	 * creates a pattern with three events, each 1/3 a cycle long, with values
	 * 0.0, 0.5, and 1.0.
	 * 
	 * @param loom
	 *            the Loom on which the new pattern should be created
	 * @param ints
	 *            the integer values for each event
	 * @return a new pattern with the specified events
	 */
	public static Pattern fromInts(Loom loom, Integer... ints) {
		Pattern pattern = new Pattern(loom);
		return pattern.extend(ints);
	}

	/**
	 * Create a new pattern that returns the same value as another. Useful for
	 * creating multiple mappings of the same type.
	 * 
	 * @param other
	 *            the pattern to follow
	 * @return a pattern that returns the same value as the specified pattern
	 */
	public static Pattern following(Pattern other) {
		return new Pattern(other.loom, new FollowerFunction(other));
	}

	/**
	 * Called on initialization, or when adding a sibling (pattern at the same
	 * level in the hierarchy). This abstracts away the use of the Loom's
	 * <code>patterns</code> field in case the interface needs to change. Only
	 * adds if not already added.
	 * 
	 * @param loom
	 *            the loom to which this pattern should be added
	 */
	protected void addSelfTo(Loom loom) {
		if (!loom.patterns.contains(this))
			loom.patterns.add(this);
	}

	/**
	 * Adds a pattern as the current pattern's child.
	 * 
	 * @param child
	 *            the pattern to be added
	 * @returns the index of the new child
	 */
	protected int addChild(Pattern child) {
		if (child == this)
			throw new IllegalArgumentException(
					"A pattern cannot be its own child.");
		if (isConcretePattern())
			throw new IllegalStateException(
					"A ConcretePattern cannot have children.");

		if (children == null)
			children = new PatternCollection();
		child.parent = this;
		children.add(child);
		return children.size() - 1;
	}

	protected void addSibling(Pattern sibling) {
		if (parent != null)
			parent.addChild(sibling);
		else
			sibling.addSelfTo(loom);
	}

	protected void removeChild(Pattern child) {
		if (children != null)
			children.remove(child);
	}

	/**
	 * Retrieve the ith child pattern. Returns null if there are no children, or
	 * throws an exception if the index is out of bounds.
	 * 
	 * @param i
	 *            the index of pattern to retrieve
	 * @return the ith child
	 * @throws IndexOutOfBoundsException
	 */
	protected Pattern getChild(int i) throws IndexOutOfBoundsException {
		if (children == null)
			return null;
		return children.get(i);
	}

	/**
	 * Casts this pattern to a ConcretePattern instance, or returns the first
	 * concrete pattern among its children.
	 * 
	 * @return the concrete pattern underlying this pattern, if any
	 */
	protected ConcretePattern getConcretePattern() {
		if (isConcretePattern()) {
			return (ConcretePattern) this;
		} else if (children != null && children.size() > 0) {
			if (selectedChild != -1) {
				return getChild(selectedChild).getConcretePattern();
			} else {
				// look for a concrete child
				for (Pattern child : children) {
					if (child.isConcretePattern())
						return (ConcretePattern) child;
				}
			}
		}

		return null;
	}

	/**
	 * Sets the {@link Mapping} of this pattern for a certain
	 * {@link MappingType}.
	 * 
	 * @param mappingType
	 *            the type of mapping that will be set
	 * @param mapping
	 *            a {@link Mapping}
	 * @return the current pattern
	 * @see MappingType
	 */
	public Pattern putMapping(MappingType mappingType, Mapping<?> mapping) {
		getOutputMappings().put(mappingType, mapping);
		return this;
	}

	public ConcurrentMap<MappingType, Mapping<?>> getOutputMappings() {
		ConcretePattern concrete = getConcretePattern();
		if (concrete == null)
			throw new IllegalStateException(
					"Mappings cannot be set on an empty Pattern.");
		return concrete.getOutputMappings();
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
	 * Extends the current pattern by treating the input as equally spaced
	 * events within a single cycle. For example, "01234" produces a set of 5
	 * events, each 1/5 a cycle's duration, with values [0.0, 0.25, 0.5, 0.75,
	 * 1.0]. Values are scaled so that the maximum listed.
	 * 
	 * @param string
	 *            a string such as "10010010" describing a pattern to be tacked
	 *            on at the end
	 * @return the updated pattern
	 * @see EventCollection#fromString(String)
	 */
	public Pattern extend(String string) {
		return extend(EventCollection.fromString(string).values());
	}

	/**
	 * Extends the current pattern by treating the input as equally spaced
	 * events within a single cycle. For example, extend(0, 1, 2, 3, 4) will
	 * create 5 events, each 1/5 a cycle's duration, with values [0.0, 0.25,
	 * 0.5, 0.75, 1.0].
	 * 
	 * @param ints
	 *            the integer values of each event
	 * @return the updated pattern
	 * @see EventCollection#fromInts(Integer...)
	 */
	public Pattern extend(Integer... ints) {
		return extend(EventCollection.fromInts(ints).values());
	}

	/**
	 * Extends the pattern by adding the events specified after the last event
	 * currently in the pattern. (If the pattern has no events yet, it will
	 * create a new EventCollection for this pattern and add the events to it.)
	 * 
	 * @param newEvents
	 *            the events to be appended
	 * @return the current pattern with events added
	 * @see #extend(Collection)
	 */
	public Pattern extend(LEvent... events) {
		return extend(Arrays.asList(events));
	}

	/**
	 * Extends the pattern by adding the events specified after the last event
	 * currently in the pattern. (If the pattern has no events yet, it will
	 * create a new EventCollection for this pattern and add the events to it.)
	 * 
	 * @param newEvents
	 *            the events to be appended
	 * @return the current pattern with events added
	 */
	public Pattern extend(Collection<LEvent> newEvents) {
		EventCollection events = getEvents();
		if (events != null) {
			events.addAfterwards(newEvents);
		} else {
			events = EventCollection.fromEvents(newEvents);
			addChild(new ConcretePattern(loom, events));
		}
		return this;
	}

	/**
	 * Extends the pattern by adding the given events after a specified offset.
	 * 
	 * @param offset
	 *            the amount to delay the new events
	 * @param newEvents
	 *            the events to be added
	 * @return the current pattern with events added
	 */
	public Pattern extend(double offset, LEvent... newEvents) {
		return extend(IntervalMath.toFraction(offset), newEvents);
	}

	/**
	 * Extends the pattern by adding the given events after a specified offset.
	 * 
	 * @param offset
	 *            the amount to delay the new events
	 * @param newEvents
	 *            the events to be added
	 * @return the current pattern with events added
	 */
	public Pattern extend(BigFraction offset, LEvent... newEvents) {
		return extend(offset, Arrays.asList(newEvents));
	}

	/**
	 * Extends the pattern by adding the given events after a specified offset.
	 * 
	 * @param offset
	 *            the amount to delay the new events
	 * @param newEvents
	 *            the events to be added
	 * @return the current pattern with events added
	 */
	public Pattern extend(BigFraction offset, Collection<LEvent> newEvents) {
		EventCollection events = getEvents();
		if (events == null) {
			events = new EventCollection();
			addChild(new ConcretePattern(loom, events));
		}

		events.addWithOffset(offset, newEvents);
		return this;
	}

	/**
	 * Returns the value of this Pattern at the current interval, a number
	 * between 0.0 and 1.0 inclusive.
	 * 
	 * @return the value as a double
	 */
	public double getValue() {
		return getValueFor(getCurrentInterval());
	}

	/**
	 * Returns the value of the pattern for a specified {@link Interval}.
	 * 
	 * @param now
	 *            the interval to query
	 * @return the value of the Pattern
	 */
	public double getValueFor(Interval now) {
		ConcretePattern pattern = getConcretePattern();
		if (pattern == null)
			throw new IllegalStateException(
					"Cannot get value from empty Pattern!");

		return getConcretePattern().getValueFor(now);
	}

	/**
	 * Returns the current interval from the perspective of this pattern,
	 * transformed by its time scale, offset, and loop interval (if applicable).
	 * Each pattern queries its parent, if it has one, or the Loom's scheduler
	 * if not; in this way, nested time transformations are possible.
	 * 
	 * @return the current interval
	 * @see #getCurrentInterval(boolean)
	 */
	public Interval getCurrentInterval() {
		return getCurrentInterval(true);
	}

	/**
	 * Returns the current interval from the perspective of this pattern,
	 * transformed by its time scale, offset, and loop interval (if applicable).
	 * Each pattern queries its parent, if it has one, or the Loom's scheduler
	 * if not; in this way, nested time transformations are possible.
	 * 
	 * If <code>useOffset</code> is false, it will ignore the time offset in its
	 * calculations. This is needed so that {@link #every} and other functions
	 * act in the expected manner when a parent pattern is transformed.
	 * 
	 * @param useOffset
	 *            use the time offset when calculating interval
	 * @return a transformed interval
	 * @see #transform(Interval, boolean)
	 */
	public Interval getCurrentInterval(boolean useOffset) {
		Interval interval;
		if (this.parent != null) {
			interval = parent.getCurrentInterval(useParentOffset);
		} else {
			interval = loom.getCurrentInterval();
		}

		return transform(interval, useOffset);
	}

	/**
	 * Performs the transformation of a given interval, according to the
	 * pattern's time scaling, offset, and loop interval.
	 * 
	 * @param interval
	 *            the interval to be transformed
	 * @return the transformed interval
	 * @see #transform(Interval, boolean)
	 */
	public Interval transform(Interval interval) {
		return transform(interval, true);
	}

	/**
	 * Performs the transformation of a given interval, according to the
	 * pattern's time scaling, offset, and loop interval. If
	 * <code>useOffset</code> is false, the offset will be ignored.
	 * 
	 * @param interval
	 *            the interval to be transformed
	 * @param useOffset
	 *            whether or not to use the offset
	 * @return the transformed interval
	 */
	public Interval transform(Interval interval, boolean useOffset) {
		BigFraction scale = getTimeScale();

		boolean positiveScale = scale.compareTo(BigFraction.ZERO) > 0;

		if (positiveScale) {
			interval = interval.multiply(scale);
		} else {
			interval = interval.multiplyMod(scale, loopInterval);
		}

		if (useOffset)
			interval = interval.add(getTimeOffset());

		if (getRepeats() > 0 || (isLooping && positiveScale)) {
			interval = interval.modulo(loopInterval);
		}

		return interval;
	}

	/**
	 * Turns looping off.
	 * 
	 * @return the current pattern
	 */
	public Pattern once() {
		isLooping = false;
		return this;
	}

	/**
	 * Turns looping on.
	 * 
	 * @return the current pattern
	 */
	public Pattern loop() {
		isLooping = true;
		if (getEvents() != null) {
			setLoopInterval(getEvents().getTotalInterval());
		}
		return this;
	}

	/**
	 * Repeats this pattern a specified number of times.
	 * 
	 * @param n
	 *            the number of times to loop
	 * @return the current pattern
	 */
	public Pattern repeat(int n) {
		if (getEvents() != null) {
			EventCollection events = getEvents();
			Collection<LEvent> eventsToRepeat = events.values();

			EventCollection newEvents = new EventCollection();

			for (int i = 1; i < n; i++) {
				newEvents.addAfterwards(eventsToRepeat);
			}

			events.addAfterwards(newEvents.values());
		} else {
			repeats.set(n);
			if (!repeaterSet) {
				every(loopInterval.getSize(), new Callable<Void>() {
					@Override
					public Void call() {
						if (repeats.get() > 0)
							repeats.decrementAndGet();
						return null;
					}
				});
				repeaterSet = true;
			}
		}

		return this;
	}

	public int getRepeats() {
		if (timeMatch != null)
			return timeMatch.repeats.get();
		else
			return repeats.get();
	}

	// Mappings

	/**
	 * Retrieves the int value of <code>result</code>, or a default value if it
	 * is null.
	 * 
	 * @param result
	 *            the object to be queried
	 * @param defaultInt
	 *            a default if result is null (usually 0)
	 * @return the value of result, or 0
	 */
	private static int getIntOrElse(Integer result, int defaultInt) {
		return result != null ? result.intValue() : defaultInt;
	}

	/**
	 * Set the mapping from this pattern's values to integers. The range from
	 * <code>lo</code> to <code>hi</code> is inclusive, so 1.0 will be mapped to
	 * <code>hi</code>; values are rounded to the nearest whole number using
	 * Math.round.
	 * 
	 * @param lo
	 *            the low end of the range
	 * @param hi
	 *            the high end of the range (inclusive)
	 * @return the current pattern
	 */
	public Pattern asInt(int lo, int hi) {
		putMapping(MappingType.INTEGER, new IntMapping(lo, hi));
		return this;
	}

	/**
	 * Returns the current int value of this pattern according to the previously
	 * set IntMapping.
	 * 
	 * @return the current int value
	 */
	public int asInt() {
		Integer result = (Integer) getAs(MappingType.INTEGER);
		return getIntOrElse(result, Integer.MIN_VALUE);
	}

	/**
	 * Set the mapping from this pattern's values to floats. The range is
	 * inclusive, so 1.0 will be mapped to <code>hi</code>.
	 * 
	 * @param lo
	 *            the low end of the range
	 * @param hi
	 *            the high end of the range (inclusive)
	 * @return the current pattern
	 */
	public Pattern asFloat(float lo, float hi) {
		putMapping(MappingType.FLOAT, new FloatMapping(lo, hi));
		return this;
	}

	/**
	 * Returns the current float value of this pattern according to the
	 * previously set FloatMapping.
	 * 
	 * @return the current float value
	 */
	public float asFloat() {
		Float result = (Float) getAs(MappingType.FLOAT);
		return result.floatValue();
	}

	/**
	 * Set a mapping from the pattern's events to MIDI notes, using a specified
	 * instrument.
	 * 
	 * @param instrument
	 *            the name of a MIDI instrument to trigger as a string
	 * @return the updated pattern
	 * @see Instrument
	 * @see #asMidiInstrument(Instrument)
	 */
	public Pattern asMidiInstrument(String instrument) {
		return asMidiInstrument(Instrument.valueOf(instrument));
	}

	/**
	 * Set a mapping from the pattern's events to MIDI notes, using a specified
	 * instrument.
	 * 
	 * @param instrument
	 *            the name of a MIDI instrument to trigger
	 * @return the updated pattern
	 * @see Instrument
	 */
	public Pattern asMidiInstrument(Instrument instrument) {
		int midiInstrument = instrument.ordinal();

		if (!hasMapping(MappingType.MIDI_CHANNEL))
			asMidiChannel(0);

		Pattern setInstrument = Pattern.fromInts(loom, 1);
		setInstrument.asMidiCommand(ShortMessage.PROGRAM_CHANGE);
		setInstrument.asMidiNote(midiInstrument);

		setInstrument.asMidiMessage(setInstrument, this, setInstrument, null);

		addChild(setInstrument);
		return asMidiMessage(this);
	}

	/**
	 * Set a mapping from the pattern's events to a percussive sound.
	 * 
	 * @param sound
	 *            a MIDI percussion instrument
	 * @return the updated pattern
	 * @see MidiTools.Percussion
	 */
	public Pattern asMidiPercussion(String sound) {
		return asMidiPercussion(Percussion.valueOf(sound));
	}

	/**
	 * Set a mapping from the pattern's events to a percussive sound.
	 * 
	 * @param sound
	 *            a MIDI percussion instrument
	 * @return the updated pattern
	 * @see MidiTools.Percussion
	 */
	public Pattern asMidiPercussion(MidiTools.Percussion sound) {
		this.rewrite(new MatchRewriter(1.0));
		asMidiChannel(9);
		asMidiNote(sound.getNote());

		return asMidiMessage(this);
	}

	/**
	 * Maps this pattern's values to the specified MIDI commands. Use -1 if a
	 * certain value should be a no-op.
	 * 
	 * Example:
	 * <code>asMidiCommand(-1, ShortMessage.NOTE_OFF, ShortMessage.NOTE_ON);</code>
	 * maps the values 0.0, 0.5, and 1.0 (output from
	 * {@link ConcretePattern#forEach(Pattern)}) to the appropriate MIDI
	 * commands for each note.
	 * 
	 * @param commands
	 *            the commands to execute
	 * @return the current pattern
	 */
	public Pattern asMidiCommand(Integer... commands) {
		putMapping(MappingType.MIDI_COMMAND, new ObjectMapping<Integer>(
				commands));
		return this;
	}

	/**
	 * Gets the current MIDI command for this pattern.
	 * 
	 * @return the current MIDI command
	 */
	public int asMidiCommand() {
		return asMidiCommand(getCurrentInterval());
	}

	public int asMidiCommand(Interval now) {
		Integer result = (Integer) getAs(MappingType.MIDI_COMMAND,
				getValueFor(now));
		return getIntOrElse(result, Integer.MIN_VALUE);
	}

	/**
	 * Sets the mapping of this pattern's values to MIDI channels. This can be
	 * used if you which to switch channels on certain notes, for example to
	 * create a Klangfarbenmelodie (or most likely, to render percussion on MIDI
	 * channel 10).
	 * 
	 * @param channels
	 *            the channels to map to
	 * @return the current pattern
	 */
	public Pattern asMidiChannel(Integer... channels) {
		putMapping(MappingType.MIDI_CHANNEL, new ObjectMapping<Integer>(
				channels));
		return this;
	}

	/**
	 * Gets the current MIDI channel for this pattern.
	 * 
	 * @return the current MIDI channel
	 */
	public int asMidiChannel() {
		return asMidiChannel(getCurrentInterval());
	}

	public int asMidiChannel(Interval now) {
		Integer result = (Integer) getAs(MappingType.MIDI_CHANNEL,
				getValueFor(now));
		return getIntOrElse(result, Integer.MIN_VALUE);
	}

	/**
	 * Maps the pattern's values to MIDI data byte 1. For NOTE_ON and NOTE_OFF
	 * messages, this is the note to be played (from 0 to 127).
	 * 
	 * @param lo
	 *            the low end of the range
	 * @param hi
	 *            the high end of the range (inclusive)
	 * @return the current pattern
	 * @see IntMapping
	 */
	public Pattern asMidiData1(int lo, int hi) {
		putMapping(MappingType.MIDI_DATA1, new IntMapping(lo, hi));
		return this;
	}

	/**
	 * Returns the current value of MIDI data 1.
	 * 
	 * @return the current MIDI data 1 value
	 */
	public int asMidiData1() {
		return asMidiData1(getCurrentInterval());
	}

	public int asMidiData1(Interval now) {
		Integer result = (Integer) getAs(MappingType.MIDI_DATA1,
				getValueFor(now));
		int outcome = getIntOrElse(result, Integer.MIN_VALUE)
				+ getIntOrElse(transposition, 0);
		return outcome;
	}

	/**
	 * Maps the values of this pattern to MIDI notes. Akin to
	 * {@link #asMidiData1(int, int)}, but output will be constrained to the
	 * possibilities specified. Useful to map to certain notes of the scale.
	 * 
	 * Example: asMidiNote(60, 64, 67) will map the values 0.0, 0.5, and 1.0 to
	 * the notes middle C, E, and G. Other values will be floored (e.g. 0.25 =>
	 * 60).
	 * 
	 * @param values
	 *            the notes to be mapped to
	 * @return the current pattern
	 */
	public Pattern asMidiNote(Integer... values) {
		putMapping(MappingType.MIDI_DATA1, new ObjectMapping<Integer>(values));
		return this;
	}

	/**
	 * Returns the current MIDI note. An alias for {@link #asMidiData1}.
	 * 
	 * @return the current MIDI note
	 */
	public int asMidiNote() {
		return asMidiData1();
	}

	/**
	 * Maps the pattern's values to MIDI data 2. For NOTE_ON and NOTE_OFF
	 * messages, this corresponds to the velocity.
	 * 
	 * @param lo
	 *            the low end of the range
	 * @param hi
	 *            the high end of the range (inclusive)
	 * @return the current pattern
	 */
	public Pattern asMidiData2(int lo, int hi) {
		putMapping(MappingType.MIDI_DATA2, new IntMapping(lo, hi));
		return this;
	}

	/**
	 * Returns the current MIDI data 2 value for this pattern.
	 * 
	 * @return the current MIDI data 2 value
	 */
	public int asMidiData2() {
		return asMidiData2(getCurrentInterval());
	}

	public int asMidiData2(Interval now) {
		Integer result = (Integer) getAs(MappingType.MIDI_DATA2,
				getValueFor(now));
		return getIntOrElse(result, Integer.MIN_VALUE);
	}

	/**
	 * Maps the specified note pattern as a MIDI message, which will be
	 * Typically this will be called using the pattern itself as argument, i.e.
	 * <code>notePattern.asMidiMessage(notePattern);</code>
	 * 
	 * @param notes
	 *            the pattern that provides the MIDI notes (must have an
	 *            {@link #asMidiData1(int, int)} mapping).
	 * @return the current pattern
	 * @see #asMidiMessage(Pattern, Pattern, Pattern, Pattern)
	 */
	public Pattern asMidiMessage(Pattern notes) {
		Pattern commands = new Pattern(null);
		commands.addChild(ConcretePattern.forEach(notes));
		commands.asMidiCommand(-1, ShortMessage.NOTE_OFF, ShortMessage.NOTE_ON);

		addChild(commands);

		Pattern channels;
		if (hasMapping(MappingType.MIDI_CHANNEL)) {
			channels = this;
		} else {
			channels = (new Pattern(loom, 1.0)).asMidiChannel(0);
			addChild(channels);
		}

		Pattern velocities;
		if (hasMapping(MappingType.MIDI_DATA2)) {
			velocities = this;
		} else {
			ContinuousFunction velocityFunc = new ThresholdFunction(commands,
					1.0);
			velocities = (new Pattern(loom, velocityFunc)).asMidiData2(0, 127);
			addChild(velocities);
		}

		return asMidiMessage(commands, channels, notes, velocities);
	}

	/**
	 * Maps the current pattern to MIDI messages and schedules them to be sent
	 * out using the Loom's {@link com.corajr.loom.wrappers.MidiBusWrapper}. A
	 * new message is sent each time there is an onset in the
	 * <code>commands</code> pattern.
	 * 
	 * This takes four patterns as input (they need not be distinct). This
	 * allows each parameter of the MIDI message to be set separately if
	 * desired.
	 * 
	 * @param commands
	 *            the pattern that specifies the MIDI command
	 * @param channels
	 *            the pattern that specifies the MIDI channel
	 * @param notes
	 *            the pattern that specifies MIDI data 1
	 * @param velocities
	 *            the pattern that specifies MIDI data 2
	 * @return the current pattern
	 */
	public Pattern asMidiMessage(Pattern commands, Pattern channels,
			Pattern notes, Pattern velocities) {

		Pattern onsets = new Pattern(null);
		onsets.addChild(ConcretePattern.forEach(commands,
				EventBoundaryProxy.ONSET));
		commands.addChild(onsets);
		onsets.putMapping(MappingType.CALLABLE_WITH_ARG,
				new MidiMessageMapping(loom.midiBusWrapper, commands, channels,
						notes, velocities));

		return this;
	}

	/**
	 * Maps this pattern to an OSC message with a default argument of 1.
	 * 
	 * Example: <code>asOscMessage("/light")</code> will result in a message of
	 * "/light 1".
	 * 
	 * @param addressPattern
	 *            the address to send the message to
	 * @return the current pattern
	 * @see #asOscBundle(NetAddress, Pattern...)
	 */
	public Pattern asOscMessage(String addressPattern) {
		return asOscMessage(addressPattern, 1);
	}

	/**
	 * Maps this pattern to an OSC message with a single integer argument.
	 * 
	 * Example: <code>asOscMessage("/light", 1)</code> will result in a message
	 * of "/light 1".
	 * 
	 * @param addressPattern
	 *            the address to send the message to
	 * @param value
	 *            the integer value
	 * @return the current pattern
	 * @see #asOscBundle(NetAddress, Pattern...)
	 */
	public Pattern asOscMessage(String addressPattern, int value) {
		ConcretePattern subPattern = new ConcretePattern(loom, 1.0);
		return asOscMessage(addressPattern, subPattern,
				new IntMapping(0, value));
	}

	/**
	 * Sets a mapping of this pattern to OSC messages, with a new
	 * {@link Mapping} used to provide the arguments.
	 * 
	 * Example: <code>asOscMessage("/light", new IntMapping(0, 1));</code> will
	 * call the {@link IntMapping} on the current pattern, then create a message
	 * like "/light 0" or "/light 1" depending on its value.
	 * 
	 * @param addressPattern
	 *            the address of the message
	 * @param mapping
	 *            the mapping from this pattern's values to the OSC message's
	 *            arguments
	 * @return the current pattern
	 */
	public Pattern asOscMessage(String addressPattern, Mapping<?> mapping) {
		final Pattern original = this;
		putMapping(MappingType.OSC_MESSAGE, new OscMessageMapping(original,
				addressPattern, mapping));
		return this;
	}

	/**
	 * Sets this pattern's mapping to OSC messages, using a second pattern
	 * <code>subPattern</code> to provide the values that will be translated
	 * into the message's arguments.
	 * 
	 * The <code>mapping</code> will be called using the subpattern's current
	 * value when this pattern is queried. For example, the subpattern could
	 * have a mapping that turns 0.0 into the three floats [0.0, 0.0, 0.0] and
	 * 1.0 into the three floats [0.1, 0.2, 0.3], which would work independently
	 * of the values of the current pattern.
	 * 
	 * @param addressPattern
	 *            the address to send the message to
	 * @param subPattern
	 *            the pattern that will be used to provide the values for the
	 *            mapping
	 * @param mapping
	 *            the mapping from subPattern's values to the OSC messages'
	 *            arguments
	 * @return the current pattern
	 */
	public Pattern asOscMessage(String addressPattern, Pattern subPattern,
			Mapping<?> mapping) {
		subPattern.asOscMessage(addressPattern, mapping);
		addChild(subPattern);
		return this;
	}

	/**
	 * Returns the current OSC message value of this pattern.
	 * 
	 * @return the current OSC message
	 */
	public OscMessage asOscMessage() {
		return (OscMessage) getAs(MappingType.OSC_MESSAGE);
	}

	/**
	 * Sets a mapping to wrap up the OSC messages for this pattern's onsets into
	 * bundles and schedules them to be sent using the Loom's
	 * {@link OscP5Wrapper}. The current pattern's children are assumed to
	 * contain mappings to OSC messages.
	 * 
	 * @param remoteAddress
	 *            the server to send the messages to
	 * @return the current pattern
	 * @see #asOscMessage(String, Mapping)
	 * @see #asOscBundle(NetAddress, Pattern...)
	 */
	public Pattern asOscBundle(NetAddress remoteAddress) {
		return asOscBundle(remoteAddress, children.toArray(new Pattern[] {}));
	}

	/**
	 * Sets a mapping to wrap up the OSC messages for this pattern's onsets into
	 * bundles and schedules them to be sent using the Loom's
	 * {@link OscP5Wrapper}.
	 * 
	 * @param remoteAddress
	 *            the address of the OSC server to send to
	 * @param patterns
	 *            the patterns that contain the OSC messages to be bundled
	 * @return the current pattern
	 */
	public Pattern asOscBundle(final NetAddress remoteAddress,
			final Pattern... patterns) {

		final Pattern original = this;

		Pattern hits = new Pattern(loom,
				new MatchRewriter(1.0).apply(getEvents()));
		addChild(hits);

		PatternCollection oscPatterns = new PatternCollection();

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

		putMapping(MappingType.OSC_BUNDLE, new OscBundleMapping(oscPatterns));

		hits.onOnset(new Callable<Void>() {
			@Override
			public Void call() {
				loom.oscP5Wrapper.get().send(original.asOscBundle(),
						remoteAddress);
				return null;
			}
		});

		return this;
	}

	/**
	 * Returns the pattern's value as an OSC bundle.
	 * 
	 * @return the current OSC bundle
	 */
	public OscBundle asOscBundle() {
		return (OscBundle) getAs(MappingType.OSC_BUNDLE);
	}

	/**
	 * Sets a mapping from this pattern to the parameters of a
	 * supercollider.Synth object. Parameters vary depending on the synth in
	 * question; common ones might include "amp" and "gate".
	 * 
	 * @param synth
	 *            the supercollider.Synth object
	 * @param param
	 *            the name of the parameter
	 * @param lo
	 *            the low range of the value
	 * @param hi
	 *            the high range of the value
	 * @return the current pattern
	 */
	public Pattern asSynthParam(final Synth synth, final String param,
			float lo, float hi) {

		// ensure that the "server" is set to the local OscP5 wrapper
		// TODO this method wouldn't obviously have this as a side effect
		// should move into separate function
		try {
			Server.osc = (OscP5) loom.oscP5Wrapper.get();
		} catch (Exception e) {
			OscP5 osc = new OscP5(loom.oscP5Wrapper, 57151);
			loom.oscP5Wrapper.set(new OscP5Impl(osc));
			Server.osc = osc;
		}

		final Pattern follow = new Pattern(loom, new FollowerFunction(this));
		follow.asFloat(lo, hi);
		follow.asCallable(new Callable<Void>() {
			@Override
			public Void call() {
				synth.set(param, follow.asFloat());
				return null;
			}
		});

		addChild(follow);

		return this;
	}

	/**
	 * Sets a mapping that triggers a Minim AudioSample when this pattern's
	 * events have a value of 1.0.
	 * 
	 * @param sample
	 *            the AudioSample
	 * @return the current pattern
	 */
	public Pattern asSample(final AudioSample sample) {
		Pattern hits = new Pattern(null, this.getEvents());
		hits.rewrite(new MatchRewriter(1.0));

		addChild(hits);

		hits.onOnset(new Callable<Void>() {
			@Override
			public Void call() {
				sample.trigger();
				return null;
			}
		});

		return this;
	}
	
	/**
	 * Sets a mapping that triggers a SoundFile when this pattern's
	 * events have a value of 1.0.
	 * 
	 * @param sample
	 *            the AudioSample
	 * @return the current pattern
	 */
	public Pattern asSoundFile(final SoundFile soundFile) {
		Pattern hits = new Pattern(null, this.getEvents());
		hits.rewrite(new MatchRewriter(1.0));

		addChild(hits);

		hits.onOnset(new Callable<Void>() {
			@Override
			public Void call() {
				soundFile.play();
				return null;
			}
		});

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
	public Pattern asColor(final int... colors) {
		putMapping(MappingType.COLOR, new ColorMapping(colors));
		return this;
	}

	public int asColor() {
		Integer result = (Integer) getAs(MappingType.COLOR);
		return getIntOrElse(result, 0x00000000);
	}

	public Pattern asDrawCommand(DrawCommand... commands) {
		for (int i = 0; i < commands.length; i++) {
			commands[i].setParent(loom.getParent());
		}
		putMapping(MappingType.DRAW_COMMAND, new ObjectMapping<DrawCommand>(
				commands));
		return this;
	}

	public DrawCommand asDrawCommand() {
		DrawCommand result = null;
		Collection<DrawCommand> commands = getDrawCommands();
		switch (commands.size()) {
		case 0:
			result = Draw.NOOP;
			break;
		case 1:
			result = commands.iterator().next();
			break;
		default:
			result = new Draw.Compound(commands);
		}

		if (result != null && loom != null && loom.getParent() != null)
			result.setParent(loom.getParent());

		return result;
	}

	public Pattern asTurtleDrawCommand(TurtleDrawCommand... commands) {
		return asTurtleDrawCommand(true, commands);
	}

	public Pattern asTurtleDrawCommand(boolean clearing,
			TurtleDrawCommand... commands) {
		turtle = new Turtle(loom.getParent());

		for (int i = 0; i < commands.length; i++) {
			commands[i].setParent(loom.getParent());
			commands[i].setTurtle(turtle);
		}

		putMapping(MappingType.TURTLE_DRAW_COMMAND,
				new ObjectMapping<TurtleDrawCommand>(commands));

		onOnset(commands);

		if (clearing) {
			every(getTotalInterval().getSize(), new Callable<Void>() {
				@Override
				public Void call() {
					turtle.clear();
					return null;
				}
			});
		}

		return this;
	}

	public TurtleDrawCommand asTurtleDrawCommand() {
		return (TurtleDrawCommand) getAs(MappingType.TURTLE_DRAW_COMMAND);
	}

	public void addAllTurtleDrawCommands() {
		turtle.clear();
		EventCollection events = getEvents();
		for (LEvent e : events.values()) {
			TurtleDrawCommand tdc = (TurtleDrawCommand) getAs(
					MappingType.TURTLE_DRAW_COMMAND, e.getValue());
			turtle.add(tdc);
		}
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

	public Collection<DrawCommand> getDrawCommands() {
		if (isConcretePattern())
			return getConcretePattern().getDrawCommands();

		Collection<DrawCommand> commands = new ArrayList<DrawCommand>();
		if (children != null) {
			for (Pattern child : children) {
				commands.addAll(child.getDrawCommands());
			}
		}

		return commands;
	}

	public Collection<Callable<?>> getActiveMappingsFor(Interval interval) {
		if (isConcretePattern())
			return getConcretePattern().getActiveMappingsFor(interval);

		Collection<Callable<?>> callables = new ArrayList<Callable<?>>();
		if (children != null) {
			for (Pattern child : children) {
				Interval transformed = transform(interval,
						child.useParentOffset);
				callables.addAll(child.getActiveMappingsFor(transformed));
			}
		}

		return callables;
	}

	public boolean hasMapping(MappingType mapping) {
		return getConcretePattern().hasMapping(mapping);
	}

	/**
	 * Check if this pattern or its children have any active mappings, i.e.
	 * those that must be triggered by the scheduler.
	 * 
	 * @return true if the pattern or its children have active mappings
	 */
	public boolean hasActiveMappings() {
		boolean result = false;
		if (isConcretePattern()) {
			result = getConcretePattern().hasActiveMappings();
		} else {
			if (children != null) {
				for (Pattern pattern : children) {
					if (pattern.hasActiveMappings()) {
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
		return getConcretePattern().events != null;
	}

	/**
	 * Creates a new pattern, using the current pattern as a selector to choose
	 * between the patterns provided as input. The output pattern will take on
	 * the values and mappings of its children, depending on this pattern's
	 * value.
	 * 
	 * <p>
	 * Let N be the number of input patterns. When this pattern is 0.0, the
	 * output pattern will return the values/mappings of its first child by
	 * default. When this pattern has a value of i/N, where i is a 1-based index
	 * into the input patterns, the output pattern will return the values and
	 * mappings of pattern i.
	 * </p>
	 * 
	 * @param patterns
	 *            the patterns from which to select
	 * @return a new pattern
	 */
	public Pattern selectFrom(Pattern... patterns) {
		final Pattern newPat = new Pattern(loom);

		Callable<Void>[] callables = new Callable[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			final int index = newPat.addChild(patterns[i]);
			callables[i] = new Callable<Void>() {
				@Override
				public Void call() {
					newPat.select(index);
					return null;
				}
			};
		}

		asStatefulCallable(CallableOnChange.fromCallables(callables));

		return newPat;
	}

	public Pattern select(int i) {
		selectedChild = i;
		timeMatch = getChild(i);
		return this;
	}

	// Transformations

	public Pattern speed(double multiplier) {
		return speed(IntervalMath.toFraction(multiplier));
	}

	public Pattern speed(BigFraction multiplier) {
		setTimeScale(getTimeScale().multiply(multiplier));
		return this;
	}

	public Pattern reverse() {
		return speed(-1);
	}

	public Pattern delay(double amt) {
		return delay(IntervalMath.toFraction(amt));
	}

	public Pattern delay(BigFraction amt) {
		return shift(amt.negate());
	}

	public Pattern shift(double amt) {
		return shift(IntervalMath.toFraction(amt));
	}

	public Pattern shift(BigFraction amt) {
		setTimeOffset(getTimeOffset().add(amt));
		return this;
	}

	/**
	 * Transposes the pattern by a specified number of semitones. Only operates
	 * on MIDI mappings.
	 * 
	 * @param semitones
	 *            the number of semitones to transpose by
	 * @return the current pattern
	 * @see #setValueOffset(double)
	 */
	public Pattern transpose(Integer semitones) {
		transposition = semitones;
		return this;
	}

	public Pattern invert() {
		setValueScale(-1.0);
		setValueOffset(1.0);
		return this;
	}

	public Pattern every(double cycles, Transform transform) {
		Callable<Void> callable = Transform.toCallable(transform, this);
		return every(cycles, callable);
	}

	public Pattern every(double cycles, Callable<Void> callable) {
		return every(new BigFraction(cycles), callable);
	}

	public Pattern every(BigFraction fraction, Callable<Void> callable) {
		Interval interval = new Interval(BigFraction.ZERO, fraction);

		Pattern trigger = new Pattern(null, new LEvent(interval, 1.0));
		trigger.loop();
		trigger.setLoopInterval(interval);

		addSibling(trigger);

		trigger.setTimeMatch(this);

		trigger.onRelease(callable);

		return this;
	}

	/**
	 * Triggers the specified callback after a given amount of time. It will be
	 * added to a time-matched sibling pattern, so it will be affected by the
	 * time scale and offset of this pattern (but not its loop interval).
	 * 
	 * @param offset
	 *            the duration after which to trigger the callable
	 * @param callable
	 *            the callable to trigger
	 * @return the current pattern
	 */
	public Pattern after(double time, Callable<Void> callable) {
		return after(IntervalMath.toFraction(time), callable);
	}

	/**
	 * Triggers the specified callback after a given amount of time. It will be
	 * added to a time-matched sibling pattern, so it will be affected by the
	 * time scale and offset of this pattern (but not its loop interval).
	 * 
	 * @param offset
	 *            the duration after which to trigger the callable
	 * @param callable
	 *            the callable to trigger
	 * @return the current pattern
	 */
	public Pattern after(BigFraction offset, Callable<Void> callable) {
		Interval interval = new Interval(BigFraction.ZERO, offset);

		Pattern trigger = new Pattern(null, new LEvent(interval, 1.0));
		trigger.setTimeMatch(this);

		addSibling(trigger);

		trigger.onRelease(callable);

		return this;
	}

	/**
	 * Triggers the specified transform after a given amount of time.
	 * 
	 * @param offset
	 *            the duration after which to trigger the transform
	 * @param transform
	 *            the transform to trigger
	 * @return the current pattern
	 */
	public Pattern after(double offset, Transform transform) {
		return after(IntervalMath.toFraction(offset), transform);
	}

	/**
	 * Triggers the specified transform after a given amount of time.
	 * 
	 * @param offset
	 *            the duration after which to trigger the transform
	 * @param transform
	 *            the transform to trigger
	 * @return the current pattern
	 */
	public Pattern after(BigFraction offset, Transform transform) {
		return after(offset, Transform.toCallable(transform, this));
	}

	/**
	 * Adds events to the pattern at a certain time offset. Synonym for
	 * {@link #extend(double, LEvent...)}.
	 * 
	 * @param offset
	 *            the time offset
	 * @param eventsToAdd
	 *            events to be added
	 * @return the updated attern
	 */
	public Pattern after(double time, LEvent... eventsToAdd) {
		return after(IntervalMath.toFraction(time), eventsToAdd);
	}

	/**
	 * Adds events to the pattern at a certain time offset. Synonym for
	 * {@link #extend(BigFraction, LEvent...)}.
	 * 
	 * @param offset
	 *            the time offset
	 * @param eventsToAdd
	 *            events to be added
	 * @return the updated pattern
	 */
	public Pattern after(BigFraction offset, LEvent... eventsToAdd) {
		return extend(offset, eventsToAdd);
	}

	/**
	 * Gives back a new pattern that switches from this pattern to another after
	 * the loop interval/total interval has elapsed.
	 * 
	 * @param other
	 *            the pattern to run after this one
	 * @return a new pattern
	 */
	public Pattern then(Pattern other) {
		Pattern selector = new Pattern(loom, LEvent.seq(
				new LEvent(this.getTotalInterval(), 0.5),
				new LEvent(other.getTotalInterval(), 1.0)));

		Pattern thenPat = selector.selectFrom(this,
				other.delay(this.getTotalInterval().getSize()));

		return thenPat;
	}

	public Pattern rewrite(EventRewriter eventRewriter) {
		EventCollection events = getEvents();
		if (events != null) {
			EventCollection newEvents = eventRewriter.apply(events);
			getConcretePattern().events = newEvents;
		} else {
			throw new IllegalStateException(
					"This pattern does not contain events.");
		}
		return this;
	}

	public Pattern onOnset(Callable<Void> callable) {
		return onBoundary(EventBoundaryProxy.ONSET, callable);
	}

	public Pattern onRelease(Callable<Void> callable) {
		return onBoundary(EventBoundaryProxy.RELEASE, callable);
	}

	private Pattern onBoundary(double boundaryType, Callable<Void> callable) {
		ConcretePattern concrete = ConcretePattern.forEach(this, boundaryType);
		concrete.asStatefulCallable(CallableOnChange.fromCallables(callable));

		concrete.useParentOffset = false;

		addChild(concrete);

		return this;
	}

	public Pattern onOnset(Callable<Void>... callables) {
		return onBoundary(EventBoundaryProxy.ONSET, callables);
	}

	public Pattern onRelease(Callable<Void>... callables) {
		return onBoundary(EventBoundaryProxy.RELEASE, callables);
	}

	private Pattern onBoundary(double boundaryType, Callable<Void>... callables) {
		ConcretePattern concrete = ConcretePattern.forEach(this, boundaryType);

		Mapping<StatefulCallable> callableMapping = new StatefulCallableMapping(
				CallableOnChange.fromCallables(callables));
		concrete.putMapping(MappingType.CALLABLE_WITH_ARG, callableMapping);

		concrete.useParentOffset = false;

		addChild(concrete);

		return this;
	}

	public Pattern clear() {
		children.clear();
		return this;
	}

	// Time shifts

	public BigFraction getMinimumResolution() {
		return (loom != null ? loom.getMinimumResolution()
				: Scheduler.DEFAULT_RESOLUTION).multiply(getTimeScale().abs());
	}

	public BigFraction getTimeOffset() {
		if (timeMatch != null)
			return timeMatch.getTimeOffset();
		else
			return timeOffset;
	}

	public void setTimeOffset(double i) {
		setTimeOffset(IntervalMath.toFraction(i));
	}

	public void setTimeOffset(BigFraction timeOffset) {
		this.timeOffset = timeOffset;
	}

	public BigFraction getTimeScale() {
		if (timeMatch != null)
			return timeMatch.getTimeScale().abs();
		else
			return timeScale;
	}

	public void setTimeScale(double i) {
		setTimeScale(IntervalMath.toFraction(i));
	}

	public void setTimeScale(BigFraction timeScale) {
		this.timeScale = timeScale;
	}

	public void setTimeMatch(Pattern pattern) {
		this.timeMatch = pattern;
	}

	public Interval getLoopInterval() {
		return loopInterval;
	}

	public void setLoopInterval(Interval loopInterval) {
		this.loopInterval = loopInterval;
	}

	/**
	 * Retrieve the total interval encompassed by this pattern's events (if
	 * applicable), or its loop interval.
	 * 
	 * @return the interval
	 */
	public Interval getTotalInterval() {
		Interval result;

		EventCollection events = getEvents();
		if (events != null)
			result = events.getTotalInterval();
		else
			result = loopInterval;

		int repeatN = repeats.get();
		if (repeatN == 0)
			repeatN = 1;

		result = new Interval(result.getStart(), result.getEnd().multiply(
				repeatN));
		return result;
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

	protected EventCollection getEvents() {
		EventCollection events = null;
		ConcretePattern pat = getConcretePattern();
		if (pat != null && pat.events != null
				&& pat.events instanceof EventCollection)
			events = (EventCollection) getConcretePattern().events;
		return events;
	}

	public void draw() {
		if (turtle != null) {
			turtle.draw();
		} else {
			asDrawCommand().draw();
		}
	}

	public void rect(float x, float y, float width, float height) {
		rect(x, y, width, height, getTotalInterval());
	}

	public void rect(float x, float y, float width, float height,
			Interval interval) {
		if (!hasMapping(MappingType.COLOR))
			throw new IllegalStateException("Must have colors defined to draw!");

		PApplet sketch = loom.myParent;

		BigFraction unit = interval.getSize().divide(
				IntervalMath.toFraction(width));

		Interval present = parent != null ? parent
				.getCurrentInterval(useParentOffset) : loom
				.getCurrentInterval();

		BigFraction start = interval.getStart().add(present.getEnd());

		Interval currentInterval = new Interval(start, start.add(unit));
		for (int i = 0; i < width; i++) {
			int color = ((Integer) (getAs(MappingType.COLOR,
					getValueFor(transform(currentInterval)))));
			sketch.stroke(color);
			sketch.line(x + i, y, x + i, y + height);
			currentInterval = currentInterval.add(unit);
		}
	}

	@Override
	public String toString() {
		if (isConcretePattern())
			return getConcretePattern().toString();

		StringBuilder sb = new StringBuilder();
		sb.append("Pattern@" + Integer.toHexString(hashCode()));
		if (children != null) {
			sb.append("(\n");
			boolean first = true;
			for (Pattern child : children) {
				if (!first) {
					sb.append(",\n");
				} else {
					first = false;
				}

				sb.append("\t");
				sb.append(child.toString().replaceAll("\n", "\n\t"));
			}
			sb.append("\n)");
		}
		return sb.toString();
	}

	@Override
	public Pattern clone() {
		if (isConcretePattern())
			return getConcretePattern().clone();

		Pattern copy = new Pattern(loom);

		copy.isLooping = isLooping;
		copy.loopInterval = loopInterval;
		copy.parent = parent;
		copy.repeats.set(repeats.get());
		copy.timeMatch = timeMatch;
		copy.timeOffset = timeOffset;
		copy.timeScale = timeScale;
		copy.transposition = transposition;
		copy.useParentOffset = useParentOffset;
		copy.valueOffset = valueOffset;
		copy.valueScale = valueScale;

		// TODO add other fields here
		for (Pattern pattern : children) {
			copy.addChild(pattern.clone());
		}
		return copy;
	}
}