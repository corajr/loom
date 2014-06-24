package org.chrisjr.loom.recording;

import org.chrisjr.loom.*;

import oscP5.*;
import netP5.*;

import java.io.*;

public class OscP5Recorder extends OscP5 {
	File outputFile;
	PrintWriter output;

	public OscP5Recorder(Loom loom, File outputFile) throws IOException {
		super(loom, 12000);
		this.outputFile = outputFile;
		output = new PrintWriter(new FileWriter(outputFile));
	}

	public OscP5Recorder(Loom loom, String outputFilename) throws IOException {
		this(loom, new File(outputFilename));
	}

	public void send(OscPacket packet, NetAddress addr) {

	}

	private void write(OscPacket packet) {
		if (!(packet instanceof OscBundle))
			return;
		
		OscBundle bundle = (OscBundle) packet;
		
		for (int i = 0; i < bundle.size(); i++) {
			OscMessage message = bundle.getMessage(i);
		}
	}
}
