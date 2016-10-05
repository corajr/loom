package com.corajr.loom.wrappers;

import netP5.NetAddress;
import oscP5.OscPacket;

public interface IOscP5 {
	void send(OscPacket packet, NetAddress addr);

	void dispose();
}
