package rd.dap.support;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

public class AlbumFolderFilter implements FileFilter{
	@Override
	public boolean accept(File file) {
		if(file.isDirectory()){
			for(File f : file.listFiles()){
				if(f.getName().toLowerCase(Locale.getDefault()).endsWith(".mp3")) return true;
			}
		}
		return false;
	}
}