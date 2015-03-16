package rd.dap.model;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import rd.dap.support.TrackList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Audiobook implements Comparable<Audiobook>, Serializable{
	private static final long serialVersionUID = 6956470301541977175L;
	private String author, album, series = "";
	private String cover;
	private byte[] thumbnail_data = null;
	private TrackList playlist = new TrackList();

	public Audiobook(){} //default constructor
	public Audiobook(Audiobook original){ //copy constructor
		setAudiobook(original);
	}
	public void setAudiobook(Audiobook original){ //copy method
		setAuthor(original.getAuthor() == null ? null : new String(original.getAuthor()));
		setAlbum(original.getAlbum() == null ? null : new String(original.getAlbum()));
		setCover(original.getCover() == null ? null : new String(original.getCover()));
		setPlaylist(original.getPlaylist());
		setThumbnail_data(original.getThumbnail_data());
	}

	/*
	 * Audiobooks are immutable
	 */
	public String getAuthor() { return new String(author); }
	public void setAuthor(String author) { this.author = new String(author); }
	public String getAlbum() { return new String(album); }
	public void setAlbum(String album) { this.album = new String(album); }
	public String getSeries() { return new String(series); }
	public void setSeries(String series) { this.series = new String(series); }
	public TrackList getPlaylist() { return new TrackList(playlist); }
	public void setPlaylist(TrackList playlist) { this.playlist = new TrackList(playlist); }
	public String getCover() { return cover == null ? null : new String(cover); }
	public void setCover(String cover) { if(cover != null) this.cover = new String(cover); }
	public byte[] getThumbnail_data() { return thumbnail_data; }
	public void setThumbnail_data(byte[] thumbnail_data) { this.thumbnail_data = thumbnail_data; }
	public void setThumbnail(Bitmap bm){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
		thumbnail_data = stream.toByteArray();
	}
	public Bitmap getThumbnail(){
		if(thumbnail_data == null || thumbnail_data.length == 0) return null;
		Bitmap bm = BitmapFactory.decodeByteArray(thumbnail_data, 0, thumbnail_data.length);
		return bm;
	}
	
	public String toString(){
		String out = author + " : " + album + (!series.isEmpty() ? "("+series+")" : "");
		out += " Track count = " + playlist.size();
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
	@Override
	public int compareTo(Audiobook other) {
		int p = this.getAuthor().compareTo(other.getAuthor());
		if(p == 0) p = this.getSeries().compareTo(other.getSeries());
		if(p == 0) p = this.getAlbum().compareTo(other.getAlbum());
		return p;
	}


}
