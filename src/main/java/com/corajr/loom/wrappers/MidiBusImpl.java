package com.corajr.loom.wrappers;

import processing.app.Platform;
import processing.core.PApplet;
import javax.sound.midi.MidiMessage;

import themidibus.MidiBus;

/**
 * <a id="midi"></a>
 * 
 * <a href="https://github.com/sparks/themidibus">The MidiBus</a> provides a
 * Processing wrapper for Java's MIDI facilities. However in order to actually
 * play MIDI audio, you will need a MIDI synthesizer connected (real or
 * virtual).
 * 
 * This class offers a function {@link #getDefaultMidiBus(PApplet)} that tries
 * to find a suitable default output device for The MidiBus, but it may require
 * some configuration in advance. Instructions differ based on your operating
 * system:
 * 
 * <h2>OS X:</h2>
 * 
 * You will need to run a software synth, as the OS does not provide one. You
 * can use <a href="http://notahat.com/simplesynth/">SimpleSynth</a> for free;
 * GarageBand or a variety of other apps would also work.
 * 
 * First, make sure that your MIDI bus is active. Search for "Audio MIDI Setup"
 * using Spotlight, then go to the Window menu and open the MIDI Studio.
 * Double-click on the IAC Driver and check the box that says "Device is
 * online."
 * 
 * Then, download and open SimpleSynth. You can set the instrument and sound set
 * there.
 * 
 * <h2>Windows:</h2>
 * 
 * You should be able to simply use the sketches as-is.
 *
 * <h2>Linux (or if the above instructions don't work):</h2>
 * 
 * Once you have a valid MIDI device of some kind attached, you can create the
 * MidiBus yourself like so:
 * 
 * myBus = new MidiBus(this, -1, "My Output Device");
 * 
 * If you run one of the MIDI sketches, you should be given a list of valid
 * devices on your system to use as the string sent to MidiBus.
 */
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
			System.out.println("Please create a MidiBus with the appropriate" + " outputs for your operating system.");
			System.out.println("e.g. myBus = new MidiBus(this, -1, \"My Output\")");
			System.out.println("Here are the currently available MIDI devices:");
			MidiBus.list();
			app.exit();
		}
		return theBus;
	}
}
