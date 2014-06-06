package org.chrisjr.loom.mappings;

import javax.sound.midi.*;

import org.chrisjr.loom.*;

public class MidiMessageMapping implements Mapping<MidiMessage> {
	final Pattern[] patterns;
	
	public MidiMessageMapping(final Pattern[] patterns) {
		this.patterns = patterns;
	}

	public MidiMessage call(double value) {
		MidiMessage message = null;

		int command = patterns[0].asInt();
		int channel = patterns[1].asInt();
		int data1 = patterns[2].asInt();
		int data2 = patterns.length > 3 ? patterns[3].asInt() : 0x00;

		try {
			message = new ShortMessage(command, channel, data1, data2);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}

		return message;
	}
}
