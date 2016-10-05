package com.corajr.loom.wrappers;

import oscP5.*;
import netP5.*;

public class OscP5Impl implements IOscP5 {

	private final OscP5 oscP5;

	public OscP5Impl(OscP5 oscP5) {
		this.oscP5 = oscP5;
	}

	@Override
	public void send(OscPacket packet, NetAddress addr) {
		oscP5.send(packet, addr);
	}

	@Override
	public void dispose() {
		oscP5.dispose();
	}

}
