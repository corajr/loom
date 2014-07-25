package org.chrisjr.loom.mappings;

public class Draw {
	public static class Line extends DrawCommand {
		float x1, y1, x2, y2;

		public Line(float x1, float y1, float x2, float y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		@Override
		public void draw() {
			parent.line(x1, y1, x2, y2);
		}
	}

	public static class Rect extends DrawCommand {
		float x, y, w, h;

		public Rect(float x, float y, float w, float h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		@Override
		public void draw() {
			parent.rect(x, y, w, h);
		}
	}

	public static class Ellipse extends DrawCommand {
		float x, y, w, h;

		public Ellipse(float x, float y, float w, float h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		@Override
		public void draw() {
			parent.ellipse(x, y, w, h);
		}
	}

	public static Line line(float x1, float y1, float x2, float y2) {
		return new Line(x1, y1, x2, y2);
	}

	public static Rect rect(float x, float y, float w, float h) {
		return new Rect(x, y, w, h);
	}

	public static Ellipse ellipse(float x, float y, float w, float h) {
		return new Ellipse(x, y, w, h);
	}
}
