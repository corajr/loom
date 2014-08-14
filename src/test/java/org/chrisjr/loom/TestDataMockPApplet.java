package org.chrisjr.loom;

import java.io.File;

import processing.core.PApplet;

public class TestDataMockPApplet extends PApplet {
	private static final long serialVersionUID = 4527002226607060274L;

	@Override
	public File dataFile(String filename) {
		File file = new File(filename);
		if (file.isAbsolute()) {
			return file;
		} else {
			try {
				File resources_code = new File(getClass().getResource("/")
						.toURI());
				File datadir = new File(resources_code.getParentFile()
						.getParentFile(), "data");
				file = new File(datadir, filename);
				return file;
			} catch (Exception e) {
				return null;
			}
		}
	}
}
