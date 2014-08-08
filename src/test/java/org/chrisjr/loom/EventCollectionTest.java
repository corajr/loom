package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
		Event e = new Event(new Interval(0, 1), 0);
		events.add(e);
		assertThat(events.size(), is(equalTo(1)));
	}

	@Test
	public void overlapppingEventsCannotBeAdded() {
		Event e = new Event(new Interval(0, 1), 0);
		Event e1 = new Event(new Interval(0, 2), 0);
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

	private Event[] makeArray(int n) {
		Interval start = new Interval(0, 1);
		Event[] sample = new Event[n];
		for (int i = 0; i < n; i++) {
			sample[i] = new Event(start, (double) i / n);
			start = start.add(1);
		}
		return sample;
	}

	@Test
	public void initializeFromEventArray() {
		Event[] sample = makeArray(5);
		events = EventCollection.fromArray(sample);
		assertThat(events.size(), is(equalTo(sample.length)));
	}

	@Test
	public void initializeFromCollection() {
		Event[] sampleArray = makeArray(5);
		Collection<Event> sample = Arrays.asList(sampleArray);
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
		Collection<Event> results = events.getForInterval(interval);
		assertThat(results.size(), is(equalTo(2)));
	}

	@Test
	public void getTotalInterval() {
		events = new EventCollection();
		events.add(new Event(new Interval(0, 1), 0.0));
		events.add(new Event(new Interval(99, 100), 0.0));
		assertThat(events.getTotalInterval(), is(equalTo(new Interval(0, 100))));
	}
}
