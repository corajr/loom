package com.corajr.loom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.corajr.loom.Loom;
import com.corajr.loom.Pattern;
import com.corajr.loom.TestDataMockPApplet;
import com.corajr.loom.time.NonRealTimeScheduler;

import processing.core.PApplet;
import processing.sound.SoundFile;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AsSoundFileTest {
	private final TestDataMockPApplet testApp = new TestDataMockPApplet();
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	private final AtomicInteger samplesTriggered = new AtomicInteger();

	private class SoundFileWrapper extends SoundFile {
		public SoundFileWrapper(PApplet applet, String path) {
			super(applet, path);
		}

		@Override
		public void play() {
			samplesTriggered.incrementAndGet();
			super.play();
		}

	}

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(testApp, scheduler);
		pattern = new Pattern(loom);

		loom.play();
	}

	@After
	public void tearDown() throws Exception {
		loom.dispose();
	}

	@Test
	public void audioSample() {
		SoundFile snare = new SoundFileWrapper(testApp, "snare.aif");
		pattern.extend("1101");

		pattern.asSoundFile(snare);

		scheduler.setElapsedMillis(1001);
		assertThat(samplesTriggered.get(), is(equalTo(3)));
	}
}
