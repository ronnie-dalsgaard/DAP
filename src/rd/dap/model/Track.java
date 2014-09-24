package rd.dap.model;

import java.io.File;
import java.io.Serializable;
//import java.util.Locale;

import rd.dap.support.AudiobookSupport;

public class Track implements Serializable{
	private static final long serialVersionUID = 1547714780068230994L;
	private File file; //mandatory
	private String title = "";
	private long duration = -1; //duration in milis
	private File cover = null;
	
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public File getFile() { return file; }
	public void setFile(File file) { this.file = file; }
	public long getDuration() { return duration; }
	public void setDuration(long duration) { this.duration = duration; }
	public File getCover() { return cover; }
	public void setCover(File cover) { this.cover = cover; }
	
	public String toString(){
		String out = ""; 
		out += " " + title + "\n";
		out+= "Duration: " + AudiobookSupport.prettyDuration(duration);
		String path = file.getAbsolutePath();
		out += "File: " + path.substring(0, path.length());
		//TODO use HOME.getAbsolutePath().length as start value in substring, where HOME is Audiobooks-folder 
		return out;
	}
}
