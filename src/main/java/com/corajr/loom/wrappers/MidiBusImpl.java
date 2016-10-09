package com.corajr.loom.wrappers;

import processing.app.Platform;
import processing.core.PApplet;
import javax.sound.midi.MidiMessage;

import themidibus.MidiBus;

public class MidiBusImpl implements IMidiBus {
	private final MidiBus midiBus;

	public MidiBusImpl(MidiBus midiBus) {
		this.midiBus = midiBus;
	}

	@Override
	public void sendMessage(MidiMessage message) {
		midiBus.sendMessage(message);
	}

	@Override
	public void dispose() {
		midiBus.dispose();
	}

	public static MidiBus getDefaultMidiBus(PApplet app) {
		MidiBus theBus = null;
		if (Platform.isMacOS()) {
			theBus = new MidiBus(app, "Bus 1", "Bus 1");
		} else if (Platform.isWindows()) {
			theBus = new MidiBus(app, -1, "Microsoft GS Wavetable Synth");			
		} else {
			System.err.println("Could not find MIDI device.");
			System.out.println("Please create a MidiBus with the appropriate"
					+ " outputs for your operating system.");
			System.out.println("e.g. myBus = new MidiBus(this, -1, \"My Output\")");
			System.out.println("Here are the currently available MIDI devices:");
			MidiBus.list();
			app.exit();
		}
		return theBus;
	}
}
