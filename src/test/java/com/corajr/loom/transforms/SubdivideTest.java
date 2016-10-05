package com.corajr.loom.transforms;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Test;

import com.corajr.loom.*;
import com.corajr.loom.time.*;
import com.corajr.loom.transforms.EventRewriter;
import com.corajr.loom.transforms.Rule;
import com.corajr.loom.transforms.SubdivideRewriter;

public class SubdivideTest {
	@Test
	public void subdivideEvent() {
		LEvent event = new LEvent(new Interval(0, 1), 1.0);
		Rule subdivide = new SubdivideRewriter.SubdivideRule(new BigFraction(
				125, 1000), 1);
		Collection<LEvent> newEvents = subdivide.apply(event);
		assertThat(newEvents.size(), is(equalTo(2)));

		Iterator<LEvent> it = newEvents.iterator();
		LEvent event1 = it.next();
		LEvent event2 = it.next();

		assertThat(event1,
				is(equalTo(new LEvent(new Interval(0.0, 0.875), 1.0))));
		assertThat(event2,
				is(equalTo(new LEvent(new Interval(0.875, 1.0), 0.0))));
	}

	@Test
	public void subdivideEvents() {
		EventCollection events = EventCollection.fromString("1111");
		EventRewriter rewriter = new SubdivideRewriter(new BigFraction(125,
				1000), 1);
		EventCollection eventsHalved = rewriter.apply(events);
		EventCollection halfTriggers = EventCollection.fromString("10101010");

		assertTrue(eventsHalved.values().containsAll(halfTriggers.values()));

		EventCollection quarterTriggers = EventCollection
				.fromString("2010201020102010");

		EventRewriter rewriter2 = new SubdivideRewriter(new BigFraction(125,
				1000), 2);

		EventCollection eventsHalvedTwice = rewriter2.apply(events);
		assertTrue(eventsHalvedTwice.values().containsAll(
				quarterTriggers.values()));

	}
}
