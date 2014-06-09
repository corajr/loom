package org.chrisjr.loom.mappings;

public class MidiCommandMapping extends ObjectMapping<Integer> {
	public MidiCommandMapping(Integer[] commands) {
		this.objects = commands;
	}
}
