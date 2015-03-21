package rd.dap.events;

import java.io.File;

public class FileFoundEvent extends Event {
	private File file;

	public FileFoundEvent(String sourceName, Type type, File file) {
		super(sourceName, type);
		this.file = file;
	}

	public File getFile() {
		return file;
	}
}
