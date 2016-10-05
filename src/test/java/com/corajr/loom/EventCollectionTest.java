package com.corajr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.corajr.loom.EventCollection;
import com.corajr.loom.LEvent;
import com.corajr.loom.time.Interval;

import java.util.Arrays;
import java.util.Collection;

public class EventCollectionTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private EventCollection events;

	@Before
	public void setUp() throws Exception {
		events = new EventCollection();
	}

	@After
	public void tearDown() throws Exception {
		events = null;
	}

	@Test
	public void canAddEvents() throws Exception {
		LEvent e = new LEvent(new Interval(0, 1), 0);
		events.add(e);
		assertThat(events.size(), is(equalTo(1)));
	}

	@Test
	public void overlapppingEventsCannotBeAdded() {
		LEvent e = new LEvent(new Interval(0, 1), 0);
		LEvent e1 = new LEvent(new Interval(0, 2), 0);
		events.add(e);

		thrown.expect(IllegalStateException.class);
		events.add(e1); // should tell the user to create a new pattern
	}

	@Test
	public void initializeFromSingleZero() {
		String sample = "0";
		events = EventCollection.fromString(sample);
		assertThat(events.size(), is(equalTo(sample.length())));
	}

	@Test
	public void initializeFromString() {
		String sample = "0101";
		events = EventCollection.fromString(sample);
		assertThat(events.size(), is(equalTo(sample.length())));
	}

	@Test
	public void initializeFromInts() {
		Integer[] sample = new Integer[] { 0, 1, 0, 1 };
		events = EventCollection.fromInts(sample);
		assertThat(events.size(), is(equalTo(sample.length)));
	}

	@Test
	public void initializeFromDoubles() {
		Double[] sample = new Double[] { 0.0, 1.0, 0.0, 1.0 };
		events = EventCollection.fromDoubles(sample);
		assertThat(events.size(), is(equalTo(sample.length)));
	}

	private LEvent[] makeArray(int n) {
		Interval start = new Interval(0, 1);
		LEvent[] sample = new LEvent[n];
		for (int i = 0; i < n; i++) {
			sample[i] = new LEvent(start, (double) i / n);
			start = start.add(1);
		}
		return sample;
	}

	@Test
	public void initializeFromEventArray() {
		LEvent[] sample = makeArray(5);
		events = EventCollection.fromEvents(sample);
		assertThat(events.size(), is(equalTo(sample.length)));
	}

	@Test
	public void initializeFromCollection() {
		LEvent[] sampleArray = makeArray(5);
		Collection<LEvent> sample = Arrays.asList(sampleArray);
		events = EventCollection.fromEvents(sample);
		assertThat(events.size(), is(equalTo(sample.size())));
	}

	@Test
	public void shouldNotInitializeFromInvalidDoubles() {
		thrown.expect(IllegalArgumentException.class);
		Double[] sample = new Double[] { -1.0, 1.5, 0.0, 1.0 };
		events = EventCollection.fromDoubles(sample);
	}

	@Test
	public void getActiveEvents() {
		String sample = "0101";
		events = EventCollection.fromString(sample);
		Interval interval = new Interval(new BigFraction(1, 8),
				new BigFraction(3, 8));
		Collection<LEvent> results = events.getForInterval(interval);
		assertThat(results.size(), is(equalTo(2)));
	}

	@Test
	public void getTotalInterval() {
		events = new EventCollection();
		events.add(new LEvent(new Interval(0, 1), 0.0));
		events.add(new LEvent(new Interval(99, 100), 0.0));
		assertThat(events.getTotalInterval(), is(equalTo(new Interval(0, 100))));
	}
}
