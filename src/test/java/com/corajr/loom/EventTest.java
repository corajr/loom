package com.corajr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import com.corajr.loom.LEvent;
import com.corajr.loom.time.Interval;
import com.corajr.loom.util.MidiTools.Note;

public class EventTest {

	private final static double EPSILON = 1E-3;

	@Test
	public void testContainedBy() {
		Interval interval = new Interval(0, 1);
		Interval queryInterval1 = new Interval(0.99, 1.0);
		Interval queryInterval2 = new Interval(1.0, 1.01);

		LEvent event = new LEvent(interval, 0.0);
		assertTrue(event.containedBy(queryInterval1));
		assertFalse(event.containedBy(queryInterval2));
	}

	@Test
	public void testNote() {
		LEvent event = LEvent.note(2.0, Note.C4);
		assertThat(event.getInterval().getSize().doubleValue(),
				is(equalTo(2.0)));
		assertThat(event.getValue(), is(closeTo(0.4724, EPSILON)));
	}

	@Test
	public void testRest() {
		LEvent event = LEvent.rest(2.0);
		assertThat(event.getInterval().getSize().doubleValue(),
				is(equalTo(2.0)));
	}

	@Test
	public void testSeq() {
		LEvent[] eventsOld = new LEvent[] { LEvent.note(0.25, Note.C4),
				LEvent.note(0.25, Note.E4), LEvent.note(0.25, Note.G4),
				LEvent.rest(0.25) };
		LEvent[] events = LEvent.seq(eventsOld);
		for (int i = 1; i < events.length; i++) {
			assertThat(events[i].getInterval().getStart(),
					is(equalTo(events[i - 1].getInterval().getEnd())));
		}
	}
}
