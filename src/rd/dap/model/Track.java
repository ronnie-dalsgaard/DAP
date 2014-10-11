package rd.dap.model;

import java.io.Serializable;

import rd.dap.support.AudiobookSupport;
//import java.util.Locale;

public class Track implements Serializable{
	private static final long serialVersionUID = 1547714780068230994L;
	private String path; //mandatory
	private String title = "";
	private long duration = -1; //duration in milis
	private String cover = null;
	
	public Track(){} //default constructor
	public Track(Track original){ //Copy constructor
		setTrack(original);
	}
	public void setTrack(Track original){ //copy method
		path = new String(original.getPath());
		title = new String(original.getTitle());
		duration = original.getDuration();
		cover = new String(original.getCover());
	}
	
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getPath() { return path; }
	public void setPath(String path) { this.path = path; }
	public long getDuration() { return duration; }
	public void setDuration(long duration) { this.duration = duration; }
	public String getCover() { return cover; }
	public void setCover(String cover) { this.cover = cover; }
	
	public String toString(){
		String out = ""; 
		out += " " + title + "\n";
		out+= "Duration: " + AudiobookSupport.prettyDuration(duration);
		out += "File: " + path.substring(0, path.length());
		//TODO use HOME.getAbsolutePath().length as start value in substring, where HOME is Audiobooks-folder 
		return out;
	}
}
