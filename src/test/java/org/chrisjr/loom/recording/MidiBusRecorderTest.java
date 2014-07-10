package org.chrisjr.loom.recording;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sound.midi.*;

import org.chrisjr.loom.Loom;
import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.chrisjr.loom.util.MidiTools;

public class MidiBusRecorderTest {
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;
	private File midiFile;

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		pattern = new Pattern(loom);

		midiFile = File.createTempFile("recording", ".mid");
		loom.record(null, midiFile);
	}

	@After
	public void tearDown() throws Exception {
		midiFile.delete();
	}

	@Test
	public void recordOutgoingMidi() {
		pattern.extend("0242");

		pattern.loop();

		pattern.asMidiNote(60, 64, 67);
		pattern.asMidiMessage(pattern);

		scheduler.setElapsedMillis(1001);

		loom.dispose(); // must call this in order to save!

		List<MidiEvent> events = MidiTools.readFile(midiFile);
		assertThat(events.size(), is(equalTo(10)));
	}
}
