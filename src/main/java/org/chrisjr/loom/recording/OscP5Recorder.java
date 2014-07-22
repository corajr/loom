package org.chrisjr.loom.recording;

import org.chrisjr.loom.*;
import org.chrisjr.loom.wrappers.*;

import oscP5.*;
import netP5.*;

import java.io.*;

public class OscP5Recorder extends OscP5 implements IOscP5 {
	Loom loom;
	File outputFile;
	OscScore score;

	public OscP5Recorder(Loom loom, File outputFile) throws IOException {
		super(loom, 13000);
		this.outputFile = outputFile;
		this.loom = loom;
		score = new OscScore();
	}

	public OscP5Recorder(Loom loom, String outputFilename) throws IOException {
		this(loom, new File(outputFilename));
	}

	@Override
	public void send(OscPacket packet, NetAddress addr) {
		if (!(packet instanceof OscBundle))
			return;
		TaggedOscBundle bundle = new TaggedOscBundle((OscBundle) packet);

		double timestamp = loom.getNow().doubleValue();

		long seconds = (long) timestamp;
		long fraction = (long) ((timestamp - seconds) * (1L << 32));
		long time = seconds << 32 | fraction;
		bundle.setTimetag(time);

		score.put(timestamp, bundle);
	}

	@Override
	public void dispose() {
		try {
			score.write(outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
