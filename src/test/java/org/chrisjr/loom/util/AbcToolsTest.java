package org.chrisjr.loom.util;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;

import org.junit.Test;
import org.chrisjr.loom.*;
import org.chrisjr.loom.mappings.IntMapping;
import org.chrisjr.loom.time.NonRealTimeScheduler;

import abc.notation.*;

public class AbcToolsTest {
	String tuneString = "X:1\nT:\nK:D\nDEFG|ABcd||";
	int[] noteValues = new int[] { 62, 64, 66, 67, 69, 71, 73, 74 };

	@Test
	public void eventsFromString() {
		EventCollection events = AbcTools.eventsFromString(tuneString);
		assertThat(events.size(), is(equalTo(8)));
		assertThat(events.lastKey().doubleValue(), is(equalTo(1.75)));

		Iterator<Event> it = events.values().iterator();

		IntMapping m = new IntMapping(0, 127);

		for (int i = 0; i < noteValues.length; i++) {
			int value = m.call(it.next().getValue());
			assertThat(value, is(equalTo(noteValues[i])));
		}
	}

	@Test
	public void fromString() {
		NonRealTimeScheduler scheduler = new NonRealTimeScheduler();
		Loom loom = new Loom(null, scheduler);
		Pattern notes = Pattern.fromABC(loom, tuneString);
		notes.asMidiNote(0, 127);
		loom.play();

		for (int i = 0; i < 8; i++) {
			scheduler.setElapsedMillis(i * 250);
			assertThat(notes.asMidiNote(), is(equalTo(noteValues[i])));
		}

		loom.dispose();
	}
}
