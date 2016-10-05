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

import ddf.minim.*;
import ddf.minim.javasound.JSMinim;
import ddf.minim.spi.AudioOut;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AsAudioSampleTest {
	private JSMinim jsMinim;
	private Minim minim;
	private final TestDataMockPApplet testApp = new TestDataMockPApplet();
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	private final AtomicInteger samplesTriggered = new AtomicInteger();
	private AudioOut fakeOut;

	private class SampleWrapper extends AudioSample {
		private final AudioSample original;

		public SampleWrapper(AudioOut out, AudioSample original) {
			super(out);
			this.original = original;
		}

		@Override
		public float[] getChannel(int arg0) {
			return original.getChannel(arg0);
		}

		@Override
		public AudioMetaData getMetaData() {
			return original.getMetaData();
		}

		@Override
		public int length() {
			return original.length();
		}

		@Override
		public void stop() {
			original.stop();
		}

		@Override
		public void trigger() {
			samplesTriggered.incrementAndGet();
			original.trigger();
		}

	}

	// require methods for creating Minim on this
	public String sketchPath(String fileName) {
		return "";
	}

	public InputStream createInput(String fileName)
			throws FileNotFoundException {
		return new FileInputStream(testApp.dataFile(fileName));
	}

	@Before
	public void setUp() throws Exception {
		jsMinim = new JSMinim(testApp);
		minim = new Minim(jsMinim);

		scheduler = new NonRealTimeScheduler();
		loom = new Loom(testApp, scheduler);
		pattern = new Pattern(loom);

		fakeOut = jsMinim.getAudioOutput(2, 1024, 44100, 8);

		loom.play();
	}

	@After
	public void tearDown() throws Exception {
		loom.dispose();
	}

	@Test
	public void audioSample() {
		AudioSample snareReal = minim.loadSample("snare.aif");
		AudioSample snare = new SampleWrapper(fakeOut, snareReal);
		pattern.extend("1101");

		pattern.asSample(snare);

		scheduler.setElapsedMillis(1001);
		assertThat(samplesTriggered.get(), is(equalTo(3)));
	}
}
