package org.chrisjr.loom.recording;

import static org.junit.Assert.*;

import java.io.File;

import org.chrisjr.loom.Loom;
import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OscP5RecorderTest {
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;
	private File oscFile;

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		pattern = new Pattern(loom);

		oscFile = File.createTempFile("recording", "osc");
		loom.record(oscFile, null);
	}

	@After
	public void tearDown() throws Exception {
		oscFile.delete();
		loom.dispose();
	}

	@Test
	public void recordOscEvents() {
		fail("Not yet implemented");
	}

}
