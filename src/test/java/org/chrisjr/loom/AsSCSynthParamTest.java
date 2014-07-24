package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.chrisjr.loom.recording.OscScore;
import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.chrisjr.loom.wrappers.OscP5Impl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import oscP5.OscMessage;
import oscP5.OscP5;
import supercollider.Synth;

public class AsSCSynthParamTest {
	private OscP5 oscP5, oscP52;
	private File oscFile;
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;
	private Synth synth;

	private final AtomicInteger eventsCounter = new AtomicInteger();

	@Before
	public void setUp() throws Exception {
		oscP5 = new OscP5(this, 57110);

		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		pattern = new Pattern(loom);

		synth = new Synth("sine");
		synth.set("amp", 0.5f);
		synth.set("freq", 220);

		pattern.extend("0101");
	}

	@After
	public void tearDown() throws Exception {
		loom.dispose();
		oscP5.dispose();
	}

	@Test
	public void synthSetFreqRecorded() {
		try {
			oscFile = File.createTempFile("recording", "osc");
			loom.record(oscFile, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		pattern.asSynthParam(synth, "freq", 220, 440);

		synth.create();

		scheduler.setElapsedMillis(1001);
		loom.dispose();

		OscScore result = OscScore.fromFile(oscFile);

		assertThat(result.size(), is(greaterThan(0)));

	}

	@Test
	public void synthSetFreqLive() {
		oscP52 = new OscP5(this, 57151);

		loom.oscP5Wrapper.set(new OscP5Impl(oscP52));

		loom.play();

		synth.create();

		pattern.asSynthParam(synth, "freq", 220, 440);

		scheduler.setElapsedMillis(1001);

		assertThat(eventsCounter.get(), is(greaterThan(0)));
	}

	void oscEvent(OscMessage theOscMessage) {
		eventsCounter.incrementAndGet();
	}

}
