package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import themidibus.*;

public class AsMidiMessageTest {

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
		myBus = new MidiBus(this, 0, 0);
		loom.play();
	}

	@After
	public void tearDown() throws Exception {
		loom.dispose();
	}

	@Test
	public void noteOnAndOffMessagesSent() throws InterruptedException {
		pattern.extend("1353");

		pattern.asMidiNotes(60, 64, 67);

		pattern.asMidiMessage(pattern);

		scheduler.setElapsedMillis(251);

		Thread.sleep(100);

		assertThat(notesOnReceived.get(), is(equalTo(2)));
		assertThat(notesOffReceived.get(), is(equalTo(1)));

		scheduler.setElapsedMillis(1000);

		assertThat(notesOnReceived.get(), is(equalTo(4)));
		assertThat(notesOffReceived.get(), is(equalTo(4)));
	}

	public void noteOn(int channel, int pitch, int velocity) {
		System.out.println("Channel:" + channel);
		System.out.println("Pitch:" + pitch);
		System.out.println("Velocity:" + velocity);
		notesOnReceived.getAndIncrement();
	}

	public void noteOff(int channel, int pitch, int velocity) {
		notesOffReceived.getAndIncrement();
	}

}
