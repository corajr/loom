package org.chrisjr.loom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ddf.minim.*;
import ddf.minim.javasound.JSMinim;
import ddf.minim.spi.AudioOut;

import java.io.*;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

public class AsAudioSampleTest {
	private JSMinim jsMinim;
	private Minim minim;
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

	public InputStream createInput(String fileName) {
		File resources_code;
		try {
			resources_code = new File(getClass().getResource("/").toURI());
		} catch (URISyntaxException e1) {
			throw new IllegalStateException("Could not get resource directory.");
		}
		File datadir = new File(resources_code.getParentFile().getParentFile(),
				"data");
		File file = new File(datadir, fileName);
		System.out.println(file.toString());
		InputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}

	@Before
	public void setUp() throws Exception {
		jsMinim = new JSMinim(this);
		minim = new Minim(jsMinim);

		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
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
