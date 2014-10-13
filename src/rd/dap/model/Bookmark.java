package rd.dap.model;

import rd.dap.support.Time;

public class Bookmark {
	private String author, album;
	private int trackno, progress;
	// trackno is actually position (0-indexed)

	public Bookmark(String author, String album, int trackno, int progress) {
		this.author = author;
		this.album = album;
		this.trackno = trackno;
		this.progress = progress;
	}

	public final String getAuthor() { return new String(author); } //return defensive copy
	public final String getAlbum() { return new String(album); } //return defensive copy
	
	/* 
	 * trackno and progress (and thereby Bookmark) are mutable since
	 * constant recreation might be too expensive!
	 */
	public final int getTrackno() { return trackno; }
	public final void setTrackno(int trackno) { this.trackno = trackno; }
	public final int getProgress() { return progress; }
	public final void setProgress(int progress) { this.progress = progress; }
	
	public boolean matches(String author, String album) {
		if(this.author == author && this.album == album) return true;
		if(author == null || album == null) return false; //this is an asumption
		if(author.equals(this.author) && album.equals(this.album)) return true;
		return false;
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bookmark other = (Bookmark) obj;
		if (album == null) {
			if (other.album != null)
				return false;
		} else if (!album.equals(other.album))
			return false;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		return true;
	}
	public String toString(){
		return author + " - " + album + " -> (" + trackno + ") " + Time.toString(progress);
	}

	
}
