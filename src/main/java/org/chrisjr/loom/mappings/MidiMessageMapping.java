package org.chrisjr.loom.mappings;

import javax.sound.midi.*;

import org.chrisjr.loom.*;

/**
 * Collects the MIDI message parameters (command, channel, data) from the
 * specified patterns and returns a javax.sound.midi.ShortMessage when queried.
 * 
 * @author chrisjr
 */
public class MidiMessageMapping implements Mapping<MidiMessage> {
	final Pattern[] patterns;

	public MidiMessageMapping(final Pattern... patterns) {
		this.patterns = patterns;
	}

	@Override
	public MidiMessage call(double value) {
		int command = patterns[0].asMidiCommand();
		int channel = patterns[1].asMidiChannel();
		int data1 = patterns[2].asMidiData1();

		if (command < 0 || channel < 0 || data1 < 0)
			return null;

		int data2 = patterns.length > 3 && patterns[3] != null ? patterns[3]
				.asMidiData2() : 0x00;

		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(command, channel, data1, data2);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}

		return message;
	}
}
