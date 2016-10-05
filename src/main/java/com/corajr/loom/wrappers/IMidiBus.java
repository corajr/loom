package com.corajr.loom.wrappers;

import javax.sound.midi.MidiMessage;

public interface IMidiBus {
	void sendMessage(MidiMessage message);

	void dispose();
}
