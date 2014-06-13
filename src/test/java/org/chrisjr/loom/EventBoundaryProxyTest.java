package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class EventBoundaryProxyTest {

	private EventCollection originalEvents;
	private EventQueryable eventProxy;

	@Before
	public void setUp() throws Exception {
		originalEvents = EventCollection.fromString("10");
		eventProxy = new EventBoundaryProxy(originalEvents);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void noteOnQueries() {
		double value = -1.0;

		int noteOns = 0;
		int noteOffs = 0;

		for (int i = 0; i <= 1000; i++) {
			BigFraction start = new BigFraction(i, 1000);
			BigFraction end = start.add(new BigFraction(1, 1000));
			Interval query = new Interval(start, end);

			Collection<Event> events = eventProxy.getForInterval(query);
			for (Event e : events) {
				value = e.getValue();
			}
			if (value == 1.0)
				noteOns++;
			if (value == 0.5)
				noteOffs++;
		}
		assertThat(noteOns, is(equalTo(2)));
		assertThat(noteOffs, is(equalTo(2)));
	}

}
