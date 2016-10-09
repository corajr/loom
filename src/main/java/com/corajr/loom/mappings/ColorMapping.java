package com.corajr.loom.mappings;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Mapping from doubles to colors. If the number of input colors is 1, always
 * return that color. Otherwise, take the input value and interpolates in HSB
 * space between the two colors closest to the value.
 * 
 * @author corajr
 */
public class ColorMapping implements Mapping<Integer> {
	final int[] colors;

	public ColorMapping(int... colors) {
		this.colors = colors;
	}

	@Override
	public Integer call(double value) {
		float position = (float) value * (colors.length - 1);
		int i = (int) position;
		float diff = position - i;

		int result = 0x00000000;
		if (colors.length == 1) {
			result = colors[0];
		} else if (i + 1 == colors.length) { // reached end of array
			result = colors[i];
		} else if (i + 1 < colors.length) {
			result = PApplet.lerpColor(colors[i], colors[i + 1], diff,
					PConstants.HSB);
		}
		return result;
	}
}
