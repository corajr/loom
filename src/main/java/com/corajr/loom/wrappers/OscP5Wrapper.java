package com.corajr.loom.wrappers;

import java.io.File;
import java.io.IOException;

import com.corajr.loom.Loom;
import com.corajr.loom.recording.OscP5Recorder;

import oscP5.OscMessage;

/**
 * @author corajr
 * 
 *         Holds either an actual oscP5 instance or an OscP5Recorder. This
 *         allows us to avoid importing oscP5 in sketches where it is not used.
 * 
 */
public class OscP5Wrapper {
	private IOscP5 oscP5;

	public OscP5Wrapper() {
		this(null);
	}

	public OscP5Wrapper(Loom loom, File file) throws IOException {
		this(new OscP5Recorder(loom, file));
	}

	public OscP5Wrapper(IOscP5 oscP5) {
		this.oscP5 = oscP5;
	}

	public void oscEvent(OscMessage theOscMessage) {
		// TODO handle incoming messages
		System.out.println(theOscMessage.addrPattern());
	}

	public IOscP5 get() {
		if (oscP5 == null)
			throw new IllegalStateException("OscP5 not set!");
		return oscP5;
	}

	public void set(IOscP5 oscP5) {
		this.oscP5 = oscP5;
	}

	public void dispose() {
		if (oscP5 != null)
			oscP5.dispose();
	}
}
