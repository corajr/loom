package org.chrisjr.loom.mappings;

import javax.sound.midi.*;

import org.chrisjr.loom.*;

public class MidiMessageMapping implements Mapping<MidiMessage> {
	final Pattern[] patterns;

	public MidiMessageMapping(final Pattern... patterns) {
		this.patterns = patterns;
	}

	public MidiMessage call(double value) {
		MidiMessage message = null;

		int command = patterns[0].asMidiCommand();

		if (command < 0x80)
			return null;
		int channel = patterns[1].asMidiChannel();
		int data1 = patterns[2].asMidiData1();
		int data2 = patterns.length > 3 ? patterns[3].asMidiData2() : 0x00;

		try {
			message = new ShortMessage(command, channel, data1, data2);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}

		return message;
	}
}
