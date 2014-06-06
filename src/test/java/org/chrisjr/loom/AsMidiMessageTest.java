package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AsMidiMessageTest {

	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	AtomicInteger notesOnReceived = new AtomicInteger();
	AtomicInteger notesOffReceived = new AtomicInteger();

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		pattern = new Pattern(loom);

		loom.play();
	}

	@After
	public void tearDown() throws Exception {
		loom.dispose();
	}

	@Test
	public void noteOnAndOffMessagesSent() {
		pattern.extend(60, 64, 67, 64);

		pattern.asMidiMessage();

		scheduler.setElapsedMillis(251);
		assertThat(notesOnReceived.get(), is(equalTo(2)));
		assertThat(notesOffReceived.get(), is(equalTo(1)));

		scheduler.setElapsedMillis(1000);

		assertThat(notesOnReceived.get(), is(equalTo(4)));
		assertThat(notesOffReceived.get(), is(equalTo(4)));
	}

}
