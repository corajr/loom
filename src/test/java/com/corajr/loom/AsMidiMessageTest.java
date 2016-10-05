package com.corajr.loom;

import static org.junit.Assert.*;
import static com.corajr.loom.LEvent.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.corajr.loom.LEvent;
import com.corajr.loom.Loom;
import com.corajr.loom.Pattern;
import com.corajr.loom.time.NonRealTimeScheduler;
import com.corajr.loom.time.RealTimeScheduler;
import com.corajr.loom.util.MidiTools;
import com.corajr.loom.util.MidiTools.Instrument;
import com.corajr.loom.util.MidiTools.Note;
import com.corajr.loom.util.MidiTools.Percussion;

import themidibus.*;

public class AsMidiMessageTest implements StandardMidiListener {

	private static class Counter {
		public AtomicInteger[] noteOns, noteOffs;

		public Counter() {
			noteOns = new AtomicInteger[128];
			noteOffs = new AtomicInteger[128];
			for (int i = 0; i < 128; i++) {
				noteOns[i] = new AtomicInteger();
				noteOffs[i] = new AtomicInteger();
			}
		}

		public synchronized void on(int i) {
			noteOns[i].incrementAndGet();
		}

		public synchronized void off(int i) {
			noteOffs[i].incrementAndGet();
		}

		public void clear() {
			for (int i = 0; i < 128; i++) {
				noteOns[i].set(0);
				noteOffs[i].set(0);
			}
		}

		public void check(int value) {
			for (int i = 0; i < 128; i++) {
				assertThat(String.format("%d turned on %d times", i, value),
						noteOns[i].get(), is(equalTo(value)));
				assertThat(String.format("%d turned off %d times", i, value),
						noteOffs[i].get(), is(equalTo(value)));
			}
		}
	}

	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;
	private MidiBus myBus = null;

	private final AtomicInteger notesOnReceived = new AtomicInteger();
	private final AtomicInteger notesOffReceived = new AtomicInteger();
	private final Counter counter = new Counter();

	private CopyOnWriteArrayList<Integer> notes = null;
	int program1ChangedTo = -1;
	int program2ChangedTo = -1;

	@Before
	public void setUp() throws Exception {
		notesOnReceived.set(0);
		notesOffReceived.set(0);
		counter.clear();

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
		notes = null;
		notesOnReceived.set(0);
		notesOffReceived.set(0);
		program1ChangedTo = -1;
		program2ChangedTo = -1;
	}

	@Test
	public void noteOnAndOffMessagesSent() throws InterruptedException {
		pattern.extend("0242");

		pattern.loop();

		pattern.asMidiNote(60, 64, 67);
		pattern.asMidiMessage(pattern);

		scheduler.setElapsedMillis(250);

		Thread.sleep(2);

		assertThat(notesOnReceived.get(), is(equalTo(2)));
		assertThat(notesOffReceived.get(), is(equalTo(1)));

		scheduler.setElapsedMillis(999);

		Thread.sleep(2);

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
		pattern.loop();
		pattern.asMidiPercussion(Percussion.CLAVES);

		scheduler.setElapsedMillis(2999);

		Thread.sleep(1);

		assertThat(notesOnReceived.get(), is(equalTo(9)));
		assertThat(notesOffReceived.get(), is(equalTo(9)));

	}

	private void testTranspose(int semitones, boolean midiNote)
			throws InterruptedException {
		if (midiNote) {
			pattern.extend("0242");
			pattern.asMidiNote(60, 64, 67);
		} else {
			pattern.extend(seq(note(0.25, Note.C4), note(0.25, Note.E4),
					note(0.25, Note.G4), note(0.25, Note.E4)));
			pattern.asMidiData1(0, 127);
		}
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
		testTranspose(7, false);
	}

	@Test
	public void transposeUpMidiNoteTest() throws InterruptedException {
		testTranspose(7, true);
	}

	@Test
	public void transposeDownTest() throws InterruptedException {
		testTranspose(-7, false);
	}

	@Test
	public void transposeDownMidiNoteTest() throws InterruptedException {
		testTranspose(7, true);
	}

	@Test
	public void asMidiInstrumentTest() throws InterruptedException {
		pattern.extend("0242");
		pattern.asMidiNote(60, 64, 67);
		pattern.asMidiInstrument("ACOUSTIC_GRAND_PIANO");

		scheduler.setElapsedMillis(1000);

		Thread.sleep(1);

		assertThat(program1ChangedTo, is(equalTo(0)));
		assertThat(notesOnReceived.get(), is(equalTo(4)));
		assertThat(notesOffReceived.get(), is(equalTo(4)));
	}

	@Test
	public void asMidiInstrumentTwoChannelsTest() throws InterruptedException {
		Instrument instOne = Instrument.BRIGHT_ACOUSTIC_PIANO;
		Instrument instTwo = Instrument.ELECTRIC_GRAND_PIANO;

		pattern.extend("0242");
		pattern.asMidiNote(60, 64, 67);
		pattern.asMidiInstrument(instOne);

		Pattern pattern2 = Pattern.fromString(loom, "0242");
		pattern2.asMidiNote(60, 64, 67);
		pattern2.asMidiChannel(1).asMidiInstrument(instTwo);

		scheduler.setElapsedMillis(1000);

		Thread.sleep(1);

		assertThat(program1ChangedTo, is(equalTo(instOne.ordinal())));
		assertThat(program2ChangedTo, is(equalTo(instTwo.ordinal())));
		assertThat(notesOnReceived.get(), is(equalTo(8)));
		assertThat(notesOffReceived.get(), is(equalTo(8)));
	}

	@Test
	public void asMidiInstrumentRealtimeTest() throws InterruptedException {
		loom = new Loom(null, new RealTimeScheduler());
		loom.setMidiBus(myBus);

		pattern = Pattern.fromString(loom, "0242").repeat(2);
		pattern.asMidiNote(60, 64, 67).asMidiInstrument(
				Instrument.ACOUSTIC_GRAND_PIANO);

		loom.play();
		Thread.sleep(2002);

		assertThat(notesOnReceived.get(), is(equalTo(8)));
		assertThat(notesOffReceived.get(), is(equalTo(8)));
	}

	@Test
	public void asMidiPercussionRealtimeTest() throws InterruptedException {
		loom = new Loom(null, new RealTimeScheduler());
		loom.setMidiBus(myBus);

		pattern = Pattern.fromString(loom, "111011010110").repeat(2);
		pattern.asMidiPercussion(Percussion.CLAVES);

		loom.play();
		Thread.sleep(2002);

		assertThat(notesOnReceived.get(), is(equalTo(16)));
		assertThat(notesOffReceived.get(), is(equalTo(16)));
	}

	private void makeManyNotes(int n) {
		LEvent[] events = new LEvent[128 * n];
		double duration = 1.0 / events.length;
		for (int i = 0; i < events.length; i++) {
			events[i] = evt(duration, (i % 128) / 127.0);
		}
		pattern.extend(seq(events)).asMidiData1(0, 127).asMidiMessage(pattern);
	}

	@Test
	public void asMidiManyNotes() throws InterruptedException {
		int n = 3;
		makeManyNotes(n);

		scheduler.setElapsedMillis(1000);
		Thread.sleep(1);

		counter.check(n);
	}

	@Test
	public void asMidiManyNotesRealtime() throws InterruptedException {
		int n = 3;

		loom = new Loom(null, new RealTimeScheduler());
		loom.setMidiBus(myBus);

		pattern = new Pattern(loom);
		makeManyNotes(n);

		loom.play();
		Thread.sleep(1005);
		loom.stop();

		counter.check(n);
	}

	@Override
	public void midiMessage(MidiMessage message, long timeStamp) {
		byte[] data = message.getMessage();
		// MidiTools.printMidi(message);
		if ((data[0] & 0xC0) == ShortMessage.PROGRAM_CHANGE) {
			int channel = (data[0] & 0xFF) - 0xC0;
			if (channel == 0)
				program1ChangedTo = data[1] & 0xFF;
			else if (channel == 1)
				program2ChangedTo = data[1] & 0xFF;
		} else if ((data[0] & 0x90) == ShortMessage.NOTE_ON) {
			counter.on(data[1] & 0xFF);
			notesOnReceived.getAndIncrement();
			if (notes != null)
				notes.add(data[1] & 0xFF);
		} else if ((data[0] & 0x80) == ShortMessage.NOTE_OFF) {
			counter.off(data[1] & 0xFF);
			notesOffReceived.getAndIncrement();
		}
	}
}
