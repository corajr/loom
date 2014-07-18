package org.chrisjr.loom.recording;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.DatagramPacket;

import oscP5.*;
import netP5.*;

public class OscScore extends ConcurrentSkipListMap<Double, OscBundle> {
	public static final Byte[] BUNDLE_BYTES = new Byte[] { 0x23, 0x62, 0x75,
			0x6e, 0x64, 0x6c, 0x65, 0x00 };
	public static final int BUNDLE_HEADER_SIZE = 16;

	public static OscScore fromFile(File file) {
		OscScore score = new OscScore();

		DataInputStream is = null;

		try {
			is = new DataInputStream(new FileInputStream(file));

			long total = file.length();

			int read = 0;
			while (read < total) {
				int packetSize = is.readInt();
				read += 4;
				byte[] packetBuf = new byte[packetSize];

				read += is.read(packetBuf);

				OscBundle bundle = new BinaryOscBundle(packetBuf);

				ByteBuffer bb = ByteBuffer.wrap(bundle.timetag());
				long timeValue = bb.getLong();
				long seconds = (timeValue >>> 32) & 0xffffffffL;
				long fraction = timeValue & 0xffffffffL;

				double timestamp = seconds + (fraction / Math.pow(2, 32));

				score.put(timestamp, bundle);
			}
		} catch (FileNotFoundException e) {
			System.err.println("File " + file.getPath() + " not found!");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return score;
	}

	public void write(File file) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

		for (OscBundle bundle : this.values()) {
			byte[] bytes = bundle.getBytes();
			out.write(Bytes.toBytes(bytes.length));
			out.write(bytes);
		}

		out.close();
	}
}
