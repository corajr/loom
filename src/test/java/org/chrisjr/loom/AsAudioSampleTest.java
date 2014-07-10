package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ddf.minim.*;

import java.io.*;
import java.net.URISyntaxException;

public class AsAudioSampleTest {
	private Minim minim;
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

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
		minim = new Minim(this);

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
	public void audioSample() {
		AudioSample snare = minim.loadSample("snare.aif");
		pattern.extend("1101");

		pattern.asSample(snare);

		scheduler.setElapsedMillis(1001);
	}
}
