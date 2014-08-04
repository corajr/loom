package org.chrisjr.loom.recording;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.chrisjr.loom.Loom;
import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.chrisjr.loom.util.MidiTools;
import org.junit.*;

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
	public void reader() {
		File resources_code;
		try {
			resources_code = new File(getClass().getResource("/").toURI());
		} catch (URISyntaxException e1) {
			throw new IllegalStateException("Could not get resource directory.");
		}
		File datadir = new File(resources_code.getParentFile().getParentFile(),
				"data");
		File scoreFile = new File(datadir, "score-test.osc");

		OscScore testScore = OscScore.fromFile(scoreFile);
		assertThat(testScore.size(), is(greaterThan(0)));
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
