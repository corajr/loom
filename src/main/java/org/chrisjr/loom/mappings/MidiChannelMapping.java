package org.chrisjr.loom.mappings;

public class MidiChannelMapping extends ObjectMapping<Integer> {
	public MidiChannelMapping(Integer[] channels) {
		this.objects = channels;
	}
}
