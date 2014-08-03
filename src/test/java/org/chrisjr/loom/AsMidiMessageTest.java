package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.chrisjr.loom.util.MidiTools;
import org.chrisjr.loom.wrappers.MidiBusImpl;
import org.chrisjr.loom.wrappers.MidiBusWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import themidibus.*;

public class AsMidiMessageTest implements StandardMidiListener {

	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;
	private MidiBus myBus;

	AtomicInteger notesOnReceived = new AtomicInteger();
	AtomicInteger notesOffReceived = new AtomicInteger();

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		pattern = new Pattern(loom);
		myBus = new MidiBus(null, "Bus 1", "Bus 1");
		myBus.addMidiListener(this);

		loom.setMidiBus(myBus);
		loom.play();

		// allow a little time to initialize midi
		Thread.sleep(30);
	}

	@After
	public void tearDown() throws Exception {
		loom.dispose();
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

	@Override
	public void midiMessage(MidiMessage message, long timeStamp) {
		byte[] data = message.getMessage();
		if ((data[0] & 0x90) == ShortMessage.NOTE_ON)
			notesOnReceived.getAndIncrement();
		else if ((data[0] & 0x80) == ShortMessage.NOTE_OFF)
			notesOffReceived.getAndIncrement();
	}

}
