package rd.dap.support;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

public class Mp3FileFilter implements FileFilter{
	@Override
	public boolean accept(File file) {
		return file.getName().toLowerCase(Locale.getDefault()).endsWith(".mp3");
	}
}