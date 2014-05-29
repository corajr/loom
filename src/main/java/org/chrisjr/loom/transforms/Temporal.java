package org.chrisjr.loom.transforms;

import org.chrisjr.loom.Pattern;

public class Temporal {
	public static class Noop extends Transform {
		public Pattern call(Pattern original) {
			return original;
		}
	}

	public static class Reverse extends Transform {
		public Pattern call(Pattern original) {
			Pattern pattern = null;
			try {
				pattern = original.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return pattern != null ? pattern.reverse() : null;
		}
	}

}
