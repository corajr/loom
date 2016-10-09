package com.corajr.loom.recording;

import java.net.*;
import oscP5.OscBundle;

/**
 * @author corajr
 * 
 *         Exposes the OscBundle(DatagramPacket) constructor from oscP5.
 * 
 */
public class BinaryOscBundle extends OscBundle {
	public static final InetAddress addr = InetAddress.getLoopbackAddress();

	public BinaryOscBundle(byte[] bytes) {
		super(makePacket(bytes));
	}

	private static DatagramPacket makePacket(byte[] bytes) {
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
		packet.setAddress(addr);
		return packet;
	}
}
