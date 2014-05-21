package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
	public void initializeFromString() {
		String sample = "0101";
		events = EventCollection.fromString(sample);
		assertThat(events.size(), is(equalTo(sample.length())));		
	}

	@Test
	public void getActiveEvents() {
		String sample = "0101";
		events = EventCollection.fromString(sample);
		Interval interval = new Interval(new BigFraction(1,8), new BigFraction(3,8));
		Collection<Event> results = events.getForInterval(interval);
		assertThat(results.size(), is(equalTo(2)));
	}
	
}
