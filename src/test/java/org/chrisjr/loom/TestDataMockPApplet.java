package org.chrisjr.loom;

import java.io.File;
import java.net.URISyntaxException;

import processing.core.PApplet;

public class TestDataMockPApplet extends PApplet {
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
