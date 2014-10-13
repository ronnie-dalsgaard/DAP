package rd.dap.model;

import java.io.Serializable;

import rd.dap.support.TrackList;

public class Audiobook implements Serializable{
	private static final long serialVersionUID = 6956470301541977175L;
	private String author, album;
	private String cover;
	private TrackList playlist = new TrackList();
	
	public Audiobook(){} //default constructor
	public Audiobook(Audiobook original){ //copy constructor
		setAudiobook(original);
	}
	public void setAudiobook(Audiobook original){ //copy method
		author = original.getAuthor() == null ? null : new String(original.getAuthor());
		album = original.getAlbum() == null ? null : new String(original.getAlbum());
		cover = original.getCover() == null ? null : new String(original.getCover());
		playlist.clear();
		for(Track track : original.getPlaylist()){
			playlist.add(new Track(track));
		}
	}
	
	public String getAuthor() { return author; }
	public void setAuthor(String author) { this.author = author; }
	public String getAlbum() { return album; }
	public void setAlbum(String album) { this.album = album; }
	public TrackList getPlaylist() { return playlist; }
	public void setPlaylist(TrackList playlist) { this.playlist = playlist; }
	public String getCover() { return cover; }
	public void setCover(String cover) { this.cover = cover; }
	
	public String toString(){
		String out = author + " : " + album;
		out += "Track count = " + playlist.size();
		return out;
	}
	@Override
	public int hashCode() {
		final int prime = 67;
		int result = 1;
		result = prime * result + ((album == null) ? 0 : album.hashCode());
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Audiobook other = (Audiobook) obj;
		if (album == null) {
			if (other.album != null) return false;
		} else if (!album.equals(other.album)) return false;
		if (author == null) {
			if (other.author != null) return false;
		} else if (!author.equals(other.author)) return false;
		return true;
	}
	
}
