package com.corajr.loom.wrappers;

import java.io.File;
import java.io.IOException;

import com.corajr.loom.Loom;
import com.corajr.loom.recording.MidiBusRecorder;

/**
 * @author corajr
 * 
 *         Holds either an actual MidiBus instance or a MidiBusRecorder. This
 *         allows us to avoid importing themidibus in sketches where it is not
 *         used.
 * 
 */
public class MidiBusWrapper {
	private IMidiBus midiBus;

	public MidiBusWrapper() {
		this(null);
	}

	public MidiBusWrapper(Loom loom, File file) throws IOException {
		this(new MidiBusRecorder(loom, file));
	}

	public MidiBusWrapper(IMidiBus midiBus) {
		this.midiBus = midiBus;
	}

	public IMidiBus get() {
		if (midiBus == null)
			throw new IllegalStateException("MidiBus not set!");
		return midiBus;
	}

	public void set(IMidiBus midiBus) {
		this.midiBus = midiBus;
	}

	public void dispose() {
		if (midiBus != null)
			midiBus.dispose();
	}
}
