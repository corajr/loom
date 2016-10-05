package com.corajr.loom.recording;

import org.junit.*;

import com.corajr.loom.Loom;
import com.corajr.loom.Pattern;
import com.corajr.loom.TestDataMockPApplet;
import com.corajr.loom.recording.OscScore;
import com.corajr.loom.time.NonRealTimeScheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;

public class OscP5RecorderTest {
	private Loom loom;
	private final TestDataMockPApplet testApp = new TestDataMockPApplet();
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;
	private File oscFile;

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(testApp, scheduler);
		pattern = new Pattern(loom);

		oscFile = File.createTempFile("recording", ".osc");
		loom.recordOsc(oscFile.getAbsolutePath());
	}

	@After
	public void tearDown() throws Exception {
		oscFile.delete();
	}

	@Test
	public void reader() {
		File scoreFile = testApp.dataFile("score-test.osc");

		OscScore testScore = OscScore.fromFile(scoreFile);
		assertThat(testScore.size(), is(greaterThan(0)));

		loom.dispose();
	}

	@Test
	public void recordOscEvents() {
		pattern.extend("1101");

		Pattern messagePat = new Pattern(loom);
		messagePat.asOscMessage("/test", 123);

		pattern.asOscBundle(null, messagePat);

		scheduler.setElapsedMillis(1001);

		loom.dispose(); // must call this in order to save!

		OscScore score = OscScore.fromFile(oscFile);
		assertThat(score.size(), is(equalTo(3)));
	}

}
