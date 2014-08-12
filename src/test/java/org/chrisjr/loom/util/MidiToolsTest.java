package org.chrisjr.loom.util;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.*;
import java.util.*;
import java.net.URISyntaxException;

import javax.sound.midi.*;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

public class MidiToolsTest {

	private final static ShortMessage message = new ShortMessage();
	private final static ByteArrayOutputStream testOutBytes = new ByteArrayOutputStream();
	private final static PrintStream testOut = new PrintStream(testOutBytes);

	private final static PrintStream originalOut = System.out;

	static {
		try {
			message.setMessage(144, 0, 60, 127);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	private File getMidiFile() {
		File resources_code;
		try {
			resources_code = new File(getClass().getResource("/").toURI());
		} catch (URISyntaxException e1) {
			throw new IllegalStateException("Could not get resource directory.");
		}
		File datadir = new File(resources_code.getParentFile().getParentFile(),
				"data");
		File midiFile = new File(datadir, "WTCBk2Fugue9.mid");
		return midiFile;
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		testOutBytes.reset();
		System.setOut(originalOut);
	}

	@Test
	public void testPrintMidi() {
		System.setOut(testOut);

		MidiTools.printMidi(message);
		assertThat(testOutBytes.toString(), is(equalTo("0x90 0x3c 0x7f\n")));
	}

	@Test
	public void testPrintMidiRaw() {
		System.setOut(testOut);

		MidiTools.printMidiRaw(message.getMessage());
		assertThat(testOutBytes.toString(), is(equalTo("0x90 0x3c 0x7f\n")));
	}

	@Test
	public void testReadTracksFrom() {
		File midiFile = getMidiFile();
		Track[] tracks = MidiTools.readTracksFrom(midiFile);
		assertThat(tracks.length, is(greaterThan(0)));
	}

	@Test
	public void testReadFile() {
		File midiFile = getMidiFile();
		List<MidiEvent> events = MidiTools.readFile(midiFile);
		assertThat(events.size(), is(greaterThan(0)));
	}
}
