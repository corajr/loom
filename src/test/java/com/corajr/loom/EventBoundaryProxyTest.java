package com.corajr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.apache.commons.math3.fraction.BigFraction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.corajr.loom.EventBoundaryProxy;
import com.corajr.loom.EventCollection;
import com.corajr.loom.EventQueryable;
import com.corajr.loom.LEvent;
import com.corajr.loom.time.Interval;

import java.util.*;

public class EventBoundaryProxyTest {

	private EventCollection originalEvents;
	private EventQueryable eventProxy;
	private final BigFraction minimumResolution = new BigFraction(1, 1000);
	private final BigFraction halfMinimum = minimumResolution.divide(2);

	@Before
	public void setUp() throws Exception {
		originalEvents = EventCollection.fromString("10");
		eventProxy = new EventBoundaryProxy(null, originalEvents);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void noteOnAndOffQueries() {
		double value = 0.0;
		double priorValue = 0.0;

		int noteOns = 0;
		int noteOffs = 0;

		for (int i = 0; i < 1000; i++) {
			BigFraction start = new BigFraction(i, 1000);
			Interval query = new Interval(start.subtract(halfMinimum),
					start.add(halfMinimum));

			Collection<LEvent> events = eventProxy.getForInterval(query);
			for (LEvent e : events) {
				value = e.getValue();
				if (value == 1.0)
					assertThat(e.getParentEvent().getValue(),
							is(equalTo(i < 500 ? 1.0 : 0.0)));
			}

			if (value == 1.0 && priorValue != 1.0)
				noteOns++;
			else if (value == 0.5 && priorValue != 0.5)
				noteOffs++;

			priorValue = value;
			value = 0.0;
		}
		assertThat(noteOns, is(equalTo(2)));
		assertThat(noteOffs, is(equalTo(2)));
	}
}
