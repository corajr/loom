package org.chrisjr.loom.wrappers;

import oscP5.*;
import netP5.*;

public class OscP5Impl extends OscP5 implements IOscP5 {

	private final OscP5 oscP5;

	public OscP5Impl(Object parent, OscP5 oscP5) {
		super(parent, 13000);
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
