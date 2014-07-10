package org.chrisjr.loom.wrappers;

import javax.sound.midi.MidiMessage;

public interface IMidiBus {
	void sendMessage(MidiMessage message);

	void dispose();
}
