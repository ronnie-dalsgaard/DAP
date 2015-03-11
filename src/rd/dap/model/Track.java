package rd.dap.model;

import java.io.Serializable;

import rd.dap.support.AudiobookSupport;
//import java.util.Locale;

public class Track implements Serializable{
	private static final long serialVersionUID = 1547714780068230994L;
	private String path; //mandatory
	private String title = "";
	private int duration = -1; //duration in milis
	private String cover = null;
	
	public Track(){} //default constructor
	public Track(Track original){ //Copy constructor
		setTrack(original);
	}
	public void setTrack(Track original){ //copy method
		setPath(new String(original.getPath()));
		setTitle(new String(original.getTitle()));
		setDuration(original.getDuration());
		setCover(new String(original.getCover()));
	}
	
	/*
	 * Tracks are immutable
	 */
	public String getTitle() { return new String(title); }
	public void setTitle(String title) { this.title = new String(title); }
	public String getPath() { return new String(path); }
	public void setPath(String path) { this.path = new String(path); }
	public int getDuration() { return duration; }
	public void setDuration(int duration) { this.duration = duration; }
	public String getCover() { return cover == null ? null : new String(cover); }
	public void setCover(String cover) { this.cover = new String(cover); }
	
	public String toString(){
		String out = ""; 
		out += " " + title + "\n";
		out+= "Duration: " + AudiobookSupport.prettyDuration(duration);
		out += "File: " + path.substring(0, path.length()); 
		return out;
	}
}
