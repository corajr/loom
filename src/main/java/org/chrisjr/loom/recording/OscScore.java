package org.chrisjr.loom.recording;

import java.io.*;
import java.util.Map;
import java.util.concurrent.*;

import oscP5.*;

public class OscScore extends ConcurrentSkipListMap<Double, OscBundle> {
	public static OscScore fromFile(File file) {
		OscScore score = new OscScore();

		// TODO parse the binary OSC format
		return score;
	}

	public void write(File file) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

		for (Map.Entry<Double, OscBundle> entry : this.entrySet()) {
			double timestamp = entry.getKey();
			OscBundle bundle = entry.getValue();

			// TODO this surely isn't enough
			out.write(bundle.getBytes());
		}

		out.close();
	}
}
