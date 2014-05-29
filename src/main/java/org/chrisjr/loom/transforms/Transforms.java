package org.chrisjr.loom.transforms;

import org.chrisjr.loom.Pattern;

public class Transforms {
	public static class Noop extends Transform {
		public Pattern call(Pattern original) {
			return original;
		}
	}

	public static class Reverse extends Transform {
		public Pattern call(Pattern original) {
			return original.reverse();
		}
	}
}
