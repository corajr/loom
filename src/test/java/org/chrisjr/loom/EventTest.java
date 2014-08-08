package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.chrisjr.loom.time.Interval;
import org.chrisjr.loom.util.MidiTools.Note;
import org.junit.Test;

public class EventTest {

	private final static double EPSILON = 1E-3;

	@Test
	public void testContainedBy() {
		Interval interval = new Interval(0, 1);
		Interval queryInterval1 = new Interval(0.99, 1.0);
		Interval queryInterval2 = new Interval(1.0, 1.01);

		Event event = new Event(interval, 0.0);
		assertTrue(event.containedBy(queryInterval1));
		assertFalse(event.containedBy(queryInterval2));
	}

	@Test
	public void testNote() {
		Event event = Event.note(2.0, Note.C4);
		assertThat(event.getInterval().getSize().doubleValue(),
				is(equalTo(2.0)));
		assertThat(event.getValue(), is(closeTo(0.4724, EPSILON)));
	}

	@Test
	public void testRest() {
		Event event = Event.rest(2.0);
		assertThat(event.getInterval().getSize().doubleValue(),
				is(equalTo(2.0)));
	}

	@Test
	public void testSeq() {
		Event[] eventsOld = new Event[] { Event.note(0.25, Note.C4),
				Event.note(0.25, Note.E4), Event.note(0.25, Note.G4),
				Event.rest(0.25) };
		Event[] events = Event.seq(eventsOld);
		for (int i = 1; i < events.length; i++) {
			assertThat(events[i].getInterval().getStart(),
					is(equalTo(events[i - 1].getInterval().getEnd())));
		}
	}
}
