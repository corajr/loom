package com.corajr.loom.util;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.corajr.loom.*;
import com.corajr.loom.mappings.IntMapping;
import com.corajr.loom.time.NonRealTimeScheduler;
import com.corajr.loom.util.AbcTools;

public class AbcToolsTest {
	String minimalTuneString = "K:D\nDEFG|ABcd|Z1|";
	String tuneString = "X:1\nT:\n" + minimalTuneString;
	String invalidTune = "HIJKLMN";
	int[] noteValues = new int[] { 62, 64, 66, 67, 69, 71, 73, 74 };

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void eventsFromString() {
		EventCollection events = AbcTools.eventsFromString(tuneString);
		assertThat(events.size(), is(equalTo(8)));
		assertThat(events.lastKey().doubleValue(), is(equalTo(1.75)));

		Iterator<LEvent> it = events.values().iterator();

		IntMapping m = new IntMapping(0, 127);

		for (int i = 0; i < noteValues.length; i++) {
			int value = m.call(it.next().getValue());
			assertThat(value, is(equalTo(noteValues[i])));
		}
	}

	public void fromTuneString(String string, int[] noteValues) {
		NonRealTimeScheduler scheduler = new NonRealTimeScheduler();
		Loom loom = new Loom(null, scheduler);

		Pattern notes = Pattern.fromABC(loom, string);
		loom.play();

		for (int i = 0; i < 8; i++) {
			scheduler.setElapsedMillis(i * 250);
			assertThat(notes.asMidiNote(), is(equalTo(noteValues[i])));
		}

		loom.dispose();
	}

	@Test
	public void fromMinimalString() {
		fromTuneString(minimalTuneString, noteValues);
	}

	@Test
	public void fromFullString() {
		fromTuneString(tuneString, noteValues);
	}

	@Test
	public void invalidString() {
		AbcTools.fromString(null, invalidTune);
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
