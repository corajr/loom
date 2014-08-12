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
import org.chrisjr.loom.wrappers.OscP5Impl;

import oscP5.*;
import processing.core.PApplet;
import supercollider.*;
import ddf.minim.*;

/**
 * The base class for patterns in Loom. A Pattern may either be: discrete,
 * containing a non-overlapping series of events; continuous, its value a
 * function of time; or compound, containing a combination of the two.
 * 
 * A single pattern can only have one value at a time, but by adding child
 * patterns, multiple mappings can be assigned.
 * 
 * @see ConcretePattern
 * @author chrisjr
 */
public class Pattern implements Cloneable {
	Loom loom;

	public Turtle turtle = null;

	protected PatternCollection children = null;
	protected Pattern parent = null;

	protected Pattern timeMatch = null;
	protected boolean useParentOffset = true;

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

	final protected MappingType[] externalMappings = new MappingType[] {
			MappingType.CALLABLE, MappingType.CALLABLE_WITH_ARG,
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

	public Pattern(Loom loom, Event... events) {
		this(loom, EventCollection.fromEvents(events));
	}

	public Pattern(Loom loom, Collection<Event> events) {
		this(loom, EventCollection.fromEvents(events));
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
	 * @return a new pattern
	 */
	public static Pattern fromInts(Loom loom, Integer... ints) {
		Pattern pattern = new Pattern(loom);
		return pattern.extend(ints);
	}

	/**
	 * Called on initialization, or when adding a sibling (pattern at the same
	 * level in the hierarchy). This abstracts away the use of the Loom's
	 * <code>patterns</code> field in case the interface needs to change.
	 * 
	 * @param loom
	 *            the loom to which this pattern should be added
	 */
	protected void addSelfTo(Loom loom) {
		loom.patterns.add(this);
	}

	/**
	 * Adds a pattern as the current pattern's child.
	 * 
	 * @param child
	 *            the pattern to be added
	 */
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
	 * child if it is a concrete pattern.
	 * 
	 * @return the concrete pattern underlying this pattern, if any
	 */
	protected ConcretePattern getConcretePattern() {
		if (isConcretePattern()) {
			return (ConcretePattern) this;
		} else if (children != null && children.size() > 0
				&& getChild(0).isConcretePattern()) {
			return (ConcretePattern) getChild(0);
		}

		return null;
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
	 * Extends the current pattern by treating the input as equally spaced
	 * events within a single cycle. For example, "01234" produces a set of 5
	 * events, each 1/5 a cycle's duration, with values [0.0, 0.25, 0.5, 0.75,
	 * 1.0]. Values are scaled so that the maximum listed.
	 * 
	 * @param string
	 *            a string such as "10010010" describing a pattern to be tacked
	 *            on at the end
	 * @return the updated pattern
	 */
	public Pattern extend(String string) {
		return extend(EventCollection.fromString(string).values());
	}

	public Pattern extend(Integer... ints) {
		return extend(EventCollection.fromInts(ints).values());
	}

	public Pattern extend(Event... events) {
		return extend(Arrays.asList(events));
	}

	public Pattern extend(Collection<Event> newEvents) {
		EventCollection events = getEvents();
		if (events != null) {
			events.addAfterwards(newEvents);
		} else {
			events = EventCollection.fromEvents(newEvents);
			addChild(new ConcretePattern(loom, events));
		}
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
		return getCurrentInterval(true);
	}

	public Interval getCurrentInterval(boolean useOffset) {
		Interval interval;
		if (this.parent != null) {
			interval = parent.getCurrentInterval(useParentOffset);
		} else {
			interval = loom.getCurrentInterval();
		}

		return transform(interval, useOffset);
	}

	public Interval transform(Interval interval) {
		return transform(interval, true);
	}

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

		if (isLooping && positiveScale) {
			interval = interval.modulo(loopInterval);
		}

		return interval;
	}

	public Pattern once() {
		isLooping = false;
		return this;
	}

	public Pattern loop() {
		isLooping = true;
		return this;
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

	public Pattern asFloat(float lo, float hi) {
		putMapping(MappingType.FLOAT, new FloatMapping(lo, hi));
		return this;
	}

	public float asFloat() {
		Float result = (Float) getAs(MappingType.FLOAT);
		return result.floatValue();
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

	/**
	 * Set a mapping from the pattern's events to a percussive sound.
	 * 
	 * @param sound
	 *            a MIDI percussion instrument
	 * @return the updated pattern
	 */
	public Pattern asMidi(MidiTools.Percussion sound) {
		Pattern hits = this.rewrite(new MatchRewriter(1.0));

		ConcretePattern commands = ConcretePattern.forEach(hits);
		commands.asMidiCommand(-1, ShortMessage.NOTE_OFF, ShortMessage.NOTE_ON);

		Pattern channels = (new Pattern(loom, 1.0)).asMidiChannel(9);
		Pattern notes = (new Pattern(loom, 1.0))
				.asMidiData1(0, sound.getNote());

		ContinuousFunction velocityFunc = new ThresholdFunction(commands, 1.0);
		Pattern velocities = (new Pattern(loom, velocityFunc)).asMidiData2(0,
				127);

		addChild(commands);
		addChild(channels);
		addChild(velocities);

		return asMidiMessage(commands, channels, notes, velocities);
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
		ConcretePattern commands = ConcretePattern.forEach(notes);
		commands.asMidiCommand(-1, ShortMessage.NOTE_OFF, ShortMessage.NOTE_ON);

		Pattern channels = (new Pattern(loom, 1.0)).asMidiChannel(0);

		ContinuousFunction velocityFunc = new ThresholdFunction(commands, 1.0);
		Pattern velocities = (new Pattern(loom, velocityFunc)).asMidiData2(0,
				127);

		addChild(commands);
		addChild(channels);
		addChild(velocities);

		return asMidiMessage(commands, channels, notes, velocities);
	}

	public Pattern asMidiMessage(Pattern commands, Pattern channels,
			Pattern notes, Pattern velocities) {

		putMapping(MappingType.MIDI_MESSAGE, new MidiMessageMapping(commands,
				channels, notes, velocities));

		final Pattern original = this;

		Callable<Void> sendMidi = new Callable<Void>() {
			@Override
			public Void call() {
				MidiMessage mess = original.asMidiMessage();
				if (mess != null)
					loom.midiBusWrapper.get().sendMessage(mess);
				return null;
			}
		};

		commands.asStatefulCallable(CallableOnChange.fromCallables(sendMidi,
				sendMidi));

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

	public OscBundle asOscBundle() {
		return (OscBundle) getAs(MappingType.OSC_BUNDLE);
	}

	public Pattern asSynthParam(final Synth synth, final String param,
			float lo, float hi) {

		// ensure that the "server" is set to the local OscP5 wrapper
		// TODO this method wouldn't obviously have this as a side effect
		// should move into separate function
		try {
			Server.osc = (OscP5) loom.oscP5Wrapper.get();
		} catch (Exception e) {
			OscP5 osc = new OscP5(loom, 57151);
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

	public Pattern asSample(final AudioSample sample) {
		Pattern hits = new Pattern(loom,
				new MatchRewriter(1.0).apply(getEvents()));

		hits.onOnset(new Callable<Void>() {
			@Override
			public Void call() {
				sample.trigger();
				return null;
			}
		});

		addChild(hits);

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

		result.setParent(loom.getParent());

		return result;
	}

	public Pattern asTurtleDrawCommand(TurtleDrawCommand... commands) {
		return asTurtleDrawCommand(true, commands);
	}

	public Pattern asTurtleDrawCommand(boolean doUpdates,
			TurtleDrawCommand... commands) {
		turtle = new Turtle(loom.getParent());

		for (int i = 0; i < commands.length; i++) {
			commands[i].setParent(loom.getParent());
			commands[i].setTurtle(turtle);
		}

		putMapping(MappingType.TURTLE_DRAW_COMMAND,
				new ObjectMapping<TurtleDrawCommand>(commands));

		if (doUpdates) {
			onOnset(commands);

			every(1, new Callable<Void>() {
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
		for (Event e : events.values()) {
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

	public Collection<Callable<?>> getExternalMappingsFor(Interval interval) {
		if (isConcretePattern())
			return getConcretePattern().getExternalMappingsFor(interval);

		Collection<Callable<?>> callables = new ArrayList<Callable<?>>();
		if (children != null) {
			for (Pattern child : children) {
				Interval transformed = transform(interval,
						child.useParentOffset);
				callables.addAll(child.getExternalMappingsFor(transformed));
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
		Callable<Void> callable = Transform.toCallable(transform, this);
		return every(cycles, callable);
	}

	public Pattern every(double cycles, Callable<Void> callable) {
		return every(new BigFraction(cycles), callable);
	}

	public Pattern every(BigFraction fraction, Callable<Void> callable) {
		EventCollection events = new EventCollection();

		Interval interval = new Interval(BigFraction.ZERO, fraction);

		events.add(new Event(interval, 1.0));

		Pattern trigger = new Pattern(loom, events);
		trigger.loop();
		trigger.setLoopInterval(interval);

		addSibling(trigger);

		trigger.setTimeMatch(this);

		trigger.onRelease(callable);

		return this;
	}

	public Pattern rewrite(EventRewriter eventRewriter) {
		EventCollection events = getEvents();
		if (events != null) {
			getConcretePattern().events = eventRewriter.apply(events);
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
		if (!isDiscretePattern())
			throw new IllegalArgumentException(
					"Pattern is not made of discrete events!");

		EventQueryable proxy = new EventBoundaryProxy(this, this.getEvents());

		ConcretePattern concrete = new ConcretePattern(loom,
				new EventMatchFilter(proxy, boundaryType));

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
		EventQueryable proxy = new EventBoundaryProxy(this, this.getEvents());

		ConcretePattern concrete = new ConcretePattern(loom,
				new EventMatchFilter(proxy, boundaryType));

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
		return Scheduler.minimumResolution.multiply(getTimeScale().abs());
	}

	public BigFraction getTimeOffset() {
		if (timeMatch != null)
			return timeMatch.getTimeOffset();
		else
			return timeOffset;
	}

	public void setTimeOffset(double i) {
		setTimeOffset(new BigFraction(i));
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
		setTimeScale(new BigFraction(i));
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
		if (pat != null && pat.events instanceof EventCollection)
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
		rect(x, y, width, height, loopInterval);
	}

	public void rect(float x, float y, float width, float height,
			Interval interval) {
		if (!hasMapping(MappingType.COLOR))
			throw new IllegalStateException("Must have colors defined to draw!");

		PApplet sketch = loom.myParent;

		BigFraction unit = interval.getSize().divide(new BigFraction(width));
		BigFraction start = interval.getStart();

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