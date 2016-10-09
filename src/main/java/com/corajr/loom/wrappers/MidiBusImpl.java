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
 * virtual), as well as to set the output device correctly.
 * 
 * MIDI setup instructions differ based on your operating system:
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
 * there. Finally, the basic initialization should work:
 * 
 *   myBus = new MidiBus(this, -1, "Bus 1");
 * 
 * <h2>Windows:</h2>
 * 
 * You shouldn't have to install anything, just change "Bus 1" to "Microsoft GS Wavetable Synth":
 * 
 * myBus = new MidiBus(this, -1, "Microsoft GS Wavetable Synth");
 *
 * <h2>Linux (or if the above instructions don't work):</h2>
 * 
 * Once you have a valid MIDI device of some kind attached, you should uncomment the
 * MidiBus.list() line to find the names of the devices, then replace them in the initializer:
 * 
 * myBus = new MidiBus(this, -1, "Insert Your Device Name Here");
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
}
