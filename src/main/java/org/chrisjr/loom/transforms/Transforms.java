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

	public class Rewrite extends Transform {
		EventRewriter eventRewriter;

		public Rewrite(EventRewriter eventRewriter) {
			this.eventRewriter = eventRewriter;
		}

		public Pattern call(Pattern original) {
			return original.rewrite(eventRewriter);
		}
	}
}
