package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import org.chrisjr.loom.time.NonRealTimeScheduler;

import static org.chrisjr.loom.Event.*;

import org.chrisjr.loom.util.MidiTools;
import org.chrisjr.loom.util.MidiTools.Note;
import org.chrisjr.loom.util.MidiTools.Percussion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import themidibus.*;

public class AsMidiMessageTest implements StandardMidiListener {

	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;
	private MidiBus myBus = null;

	private final AtomicInteger notesOnReceived = new AtomicInteger();
	private final AtomicInteger notesOffReceived = new AtomicInteger();
	private CopyOnWriteArrayList<Integer> notes = null;
	int programChangedTo = -1;

	@Before
	public void setUp() throws Exception {
		notesOnReceived.set(0);
		notesOffReceived.set(0);

		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		pattern = new Pattern(loom);
		if (myBus == null) {
			myBus = new MidiBus(null, "Bus 1", "Bus 1");
			myBus.addMidiListener(this);
		}

		loom.setMidiBus(myBus);
		loom.play();

		// allow a little time to initialize midi
		Thread.sleep(30);
	}

	@After
	public void tearDown() throws Exception {
		myBus = null;
		pattern = null;
		notesOnReceived.set(0);
		notesOffReceived.set(0);
		programChangedTo = -1;
	}

	@Test
	public void noteOnAndOffMessagesSent() throws InterruptedException {
		pattern.extend("0242");

		pattern.loop();

		pattern.asMidiNote(60, 64, 67);
		pattern.asMidiMessage(pattern);

		scheduler.setElapsedMillis(251);

		Thread.sleep(1);

		assertThat(notesOnReceived.get(), is(equalTo(2)));
		assertThat(notesOffReceived.get(), is(equalTo(1)));

		scheduler.setElapsedMillis(999);

		Thread.sleep(1);

		assertThat(notesOnReceived.get(), is(equalTo(4)));
		assertThat(notesOffReceived.get(), is(equalTo(4)));

		Thread.sleep(1);

		scheduler.setElapsedMillis(1999);

		Thread.sleep(1);

		assertThat(notesOnReceived.get(), is(equalTo(8)));
		assertThat(notesOffReceived.get(), is(equalTo(8)));
	}

	@Test
	public void asMidiPercussionTest() throws InterruptedException {
		pattern.extend("1101");
		pattern.asMidi(Percussion.CLAVES);

		scheduler.setElapsedMillis(1000);

		Thread.sleep(1);

		assertThat(notesOnReceived.get(), is(equalTo(3)));
		assertThat(notesOffReceived.get(), is(equalTo(3)));

	}

	private void testTranspose(int semitones) throws InterruptedException {
		pattern.extend(seq(note(0.25, Note.C4), note(0.25, Note.E4),
				note(0.25, Note.G4), note(0.25, Note.E4)));
		pattern.asMidiData1(0, 127);
		pattern.asMidiMessage(pattern);
		pattern.transpose(semitones);

		notes = new CopyOnWriteArrayList<Integer>();

		Thread.sleep(1);
		scheduler.setElapsedMillis(1000);

		assertThat(
				notes,
				hasItems(60 + semitones, 64 + semitones, 67 + semitones,
						64 + semitones));
	}

	@Test
	public void transposeUpTest() throws InterruptedException {
		testTranspose(7);
	}

	@Test
	public void transposeDownTest() throws InterruptedException {
		testTranspose(-7);
	}

	@Test
	public void asMidiInstrumentTest() throws InterruptedException {
		pattern.extend("0242");
		pattern.asMidiNote(60, 64, 67);
		pattern.asMidi("ACOUSTIC_GRAND_PIANO");

		scheduler.setElapsedMillis(1000);

		Thread.sleep(1);

		assertThat(programChangedTo, is(equalTo(0)));
		assertThat(notesOnReceived.get(), is(equalTo(4)));
		assertThat(notesOffReceived.get(), is(equalTo(4)));
	}

	@Override
	public void midiMessage(MidiMessage message, long timeStamp) {
		byte[] data = message.getMessage();
		if ((data[0] & 0xC0) == ShortMessage.PROGRAM_CHANGE) {
			programChangedTo = data[1] & 0xFF;
		} else if ((data[0] & 0x90) == ShortMessage.NOTE_ON) {
			notesOnReceived.getAndIncrement();
			if (notes != null)
				notes.add(data[1] & 0xFF);
		} else if ((data[0] & 0x80) == ShortMessage.NOTE_OFF)
			notesOffReceived.getAndIncrement();
	}
}
