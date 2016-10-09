package com.corajr.loom.mappings;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.midi.*;

import com.corajr.loom.*;
import com.corajr.loom.time.Interval;
import com.corajr.loom.util.MidiTools;
import com.corajr.loom.wrappers.MidiBusWrapper;

/**
 * Collects the MIDI message parameters (command, channel, data) from the
 * specified patterns and returns a javax.sound.midi.ShortMessage when queried.
 * 
 * @author corajr
 */
public class MidiMessageMapping implements Mapping<MidiMessage>,
		EventMapping<Callable<Void>> {
	final MidiBusWrapper midiBusWrapper;
	final Pattern[] patterns;
	int lastHash = -1;

	public MidiMessageMapping(MidiBusWrapper midiBusWrapper,
			final Pattern... patterns) {
		this.midiBusWrapper = midiBusWrapper;
		this.patterns = patterns;
	}

	@Override
	public MidiMessage call(double value) {
		return null;
	}

	@Override
	public Callable<Void> call(LEvent event) {
		Interval now = event.getInterval();

		int command = patterns[0].asMidiCommand(now);
		int channel = patterns[1].asMidiChannel(now);
		int data1 = patterns[2].asMidiData1(now);

		if (command < 0 || channel < 0 || data1 < 0 || command > 255
				|| channel > 15 || data1 > 127 || event.getValue() == 0.0)
			return null;

		int data2 = patterns.length > 3 && patterns[3] != null ? patterns[3]
				.asMidiData2(now) : 0x00;

		final ShortMessage message = new ShortMessage();
		try {
			message.setMessage(command, channel, data1, data2);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}

		// ensure this is only sent once

		int hash = MidiTools.hash(message) ^ now.hashCode();
		if (hash == lastHash)
			return null;

		lastHash = hash;

		return new Callable<Void>() {
			@Override
			public Void call() {
				midiBusWrapper.get().sendMessage(message);
				return null;
			}
		};

	}
}
