package org.chrisjr.loom.wrappers;

import javax.sound.midi.MidiMessage;

import org.chrisjr.loom.Loom;

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

}
