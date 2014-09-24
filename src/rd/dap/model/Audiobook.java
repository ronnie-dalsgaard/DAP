package rd.dap.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class Audiobook implements Serializable{
	private static final long serialVersionUID = 6956470301541977175L;
	private String author, album;
	private File cover;
	private ArrayList<Track> playlist = new ArrayList<Track>();
	
	public String getAuthor() { return author; }
	public void setAuthor(String author) { this.author = author; }
	public String getAlbum() { return album; }
	public void setAlbum(String album) { this.album = album; }
	public ArrayList<Track> getPlaylist() { return playlist; }
	public void setPlaylist(ArrayList<Track> playlist) { this.playlist = playlist; }
	public File getCover() { return cover; }
	public void setCover(File cover) { this.cover = cover; }
	
	public String toString(){
		String out = author + " : " + album;
		out += "Track count = " + playlist.size();
		return out;
	}
}
