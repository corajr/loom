package org.chrisjr.loom.util;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;

import org.junit.Test;
import org.chrisjr.loom.*;
import org.chrisjr.loom.mappings.IntMapping;
import org.chrisjr.loom.time.NonRealTimeScheduler;

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
		loom.play();

		for (int i = 0; i < 8; i++) {
			scheduler.setElapsedMillis(i * 250);
			assertThat(notes.asMidiNote(), is(equalTo(noteValues[i])));
		}

		loom.dispose();
	}

	@Test
	public void patternsFromVariousStrings() {
		NonRealTimeScheduler scheduler = new NonRealTimeScheduler();
		Loom loom = new Loom(null, scheduler);

		Pattern[] patterns = new Pattern[] {
				Pattern.fromABC(loom, "CDEF|GABc"),
				Pattern.fromABC(loom, "_BAc=B") };

		int[][] notes = new int[][] {
				new int[] { 60, 62, 64, 65, 67, 69, 71, 72 },
				new int[] { 70, 69, 72, 71 } };

		loom.play();

		for (int i = 0; i < patterns.length; i++) {
			for (int j = 0; j < notes[i].length; j++) {
				scheduler.setElapsedMillis(j * 250);
				assertThat(patterns[i].asMidiNote(), is(equalTo(notes[i][j])));
			}
		}
	}

	@Test
	public void complexWithTuples() {
		EventCollection events = AbcTools
				.eventsFromString("C|^FG2C|(3^FGA(3^FGA|^FG2||");
		assertThat(events.size(), is(equalTo(12)));
	}
}
