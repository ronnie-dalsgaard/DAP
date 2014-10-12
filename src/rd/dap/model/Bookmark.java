package rd.dap.model;

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



	public final int getTrackno() { return trackno; } //int are immutable/primitive



	public final int getProgress() { return progress; } //int are immutable/primitive
	



	@Override
	public int hashCode() {
		final int prime = 67;
		int result = 1;
		result = prime * result + ((album == null) ? 0 : album.hashCode());
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + progress;
		result = prime * result + trackno;
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
		if (progress != other.progress)
			return false;
		if (trackno != other.trackno)
			return false;
		return true;
	}
	public String toString(){
		return author + " - " + album + " -> " + trackno + ":" + progress;
	}
}
