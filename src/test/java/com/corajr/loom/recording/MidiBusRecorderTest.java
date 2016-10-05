package com.corajr.loom.recording;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.corajr.loom.Loom;
import com.corajr.loom.Pattern;
import com.corajr.loom.TestDataMockPApplet;
import com.corajr.loom.time.NonRealTimeScheduler;
import com.corajr.loom.util.MidiTools;

import javax.sound.midi.*;

public class MidiBusRecorderTest {
	private Loom loom;
	private final TestDataMockPApplet testApp = new TestDataMockPApplet();
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;
	private File midiFile;

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(testApp, scheduler);
		pattern = new Pattern(loom);

		midiFile = File.createTempFile("recording", ".mid");
		loom.recordMidi(midiFile.getAbsolutePath());
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
