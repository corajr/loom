package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.chrisjr.loom.util.MidiTools;
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
		myBus = loom.getMidiBus();
		myBus.addMidiListener(this);
		loom.play();
	}

	@After
	public void tearDown() throws Exception {
		loom.dispose();
	}

	@Test
	public void noteOnAndOffMessagesSent() throws InterruptedException {
		pattern.extend("1353");

		pattern.asMidiNote(60, 64, 67);

		pattern.asMidiMessage(pattern);

		scheduler.setElapsedMillis(251);

		assertThat(notesOnReceived.get(), is(equalTo(2)));
		assertThat(notesOffReceived.get(), is(equalTo(1)));

		scheduler.setElapsedMillis(1000);

		assertThat(notesOnReceived.get(), is(equalTo(4)));
		assertThat(notesOffReceived.get(), is(equalTo(4)));
	}

	public void midiMessage(MidiMessage message, long timeStamp) {
		byte[] data = message.getMessage();
		if ((int) (data[0] & 0x90) == ShortMessage.NOTE_ON)
			notesOnReceived.getAndIncrement();
		else if ((int) (data[0] & 0x80) == ShortMessage.NOTE_OFF)
			notesOffReceived.getAndIncrement();
		System.out.print("recv ");
		MidiTools.printMidiRaw(data);
	}

}
