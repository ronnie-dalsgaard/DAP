package rd.dap.model;

import java.util.ArrayList;
import java.util.LinkedList;

import rd.dap.model.BookmarkEvent.Function;
import rd.dap.support.Time;

public class Bookmark implements Comparable<Bookmark>{
	private String author, album;
	private int trackno, progress;
	// trackno is actually position (0-indexed)
	private LinkedList<BookmarkEvent> events;

	public Bookmark(String author, String album, int trackno, int progress) {
		this.author = author;
		this.album = album;
		this.trackno = trackno;
		this.progress = progress;
		this.events = new LinkedList<BookmarkEvent>();
		addEvent(new BookmarkEvent(Function.CREATE, 0, 0));
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
	public ArrayList<BookmarkEvent> getEvents() { 
		ArrayList<BookmarkEvent> tmp = new ArrayList<>();
		if(events == null || events.isEmpty()) return tmp;
		for(BookmarkEvent event : events){ tmp.add(event); }
		return tmp;
	}
	public void setEvents(LinkedList<BookmarkEvent> events) { this.events = events; }

	public void addEvent(BookmarkEvent event) {
		if(events == null) events = new LinkedList<BookmarkEvent>();
		if(!events.isEmpty() && events.getFirst().getFunction().equals(event.getFunction())){
			events.removeFirst();
		}
		events.addFirst(event);
	}
	
	
	
	public boolean isSame(Bookmark bookmark){
		return isSame(bookmark.author, bookmark.album);
	}
	public boolean isSame(String author, String album){ //almost identical to equals
		if (this.album == null) {
			if (album != null) return false;
		} else if (!this.album.equals(album)) return false;
		if (this.author == null) {
			if (author != null) return false;
		} else if (!this.author.equals(author)) return false;
		return true;
	}
	@Override
	public int compareTo(Bookmark other) {
		if(this.equals(other)) return 0;
		if(this.isSame(other)){
			//sort by progress
			if(this.trackno == other.trackno){
				return this.progress - other.progress;
			}
			return this.trackno - other.trackno;
		} else {
			//sort alphabetically
			if(this.author.compareTo(other.author) == 0){
				return this.album.compareTo(other.album);
			}
			return this.author.compareTo(other.author);
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
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

	@Override
	public String toString(){
		return author + " - " + album + " -> (" + trackno + ") " + Time.toString(progress);
	}

	
}
