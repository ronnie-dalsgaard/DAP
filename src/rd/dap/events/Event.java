package rd.dap.events;

import java.io.File;
import java.util.ArrayList;

import rd.dap.model.Audiobook;
import rd.dap.model.Bookmark;

public class Event {
	public enum Type {
		TIME_OUT_EVENT,
		NO_AUDIOBOOKS_FOUND_EVENT,
		NO_BOOKMARKS_FOUND_EVENT,
		FILE_FOUND_EVENT,
		AUDIOBOOKS_DISCOVERED_EVENT,
		AUDIOBOOKS_DISCOVER_ELEMENT_EVENT,
		AUDIOBOOKS_LOADED_EVENT,
		AUDIOBOOKS_SELECTED_EVENT,
		AUDIOBOOKS_SAVED_EVENT,
		BOOKMARKS_LOADED_EVENT,
		BOOKMARK_SELECTED_EVENT,
		BOOKMARK_DELETED_EVENT, 
		BOOKMARK_UPDATED_EVENT,
		BOOKMARKS_UPDATED_EVENT,
		DURATION_SET_EVENT,
		TRACKCOUNT_SET_EVENT,
		
		REQUEST_TOGGLE,
		REQUEST_PLAY,
		REQUEST_PAUSE,
		REQUEST_PREV,
		REQUEST_NEXT,
		REQUEST_SEEK_TO_TRACK,
		REQUEST_REWIND,
		REQUEST_FORWARD,
		REQUEST_SEEK_TO,
		
		ON_PLAY,
		ON_PAUSE,
		ON_TRACK_CHANGED,
		ON_PROGRESS_CHANGED
	}
		
	private String sourceName;
	private Type type;
	private Integer i = null;
	private Boolean bool = null;
	private String str = null;
	private Audiobook audiobook = null;
	private Bookmark bookmark = null;
	private ArrayList<Audiobook> audiobooks = null;
	private ArrayList<Bookmark> bookmarks = null;
	private File file = null;

	public Event(String sourceName, Type type){
		this.sourceName = sourceName;
		this.type = type;
	}
	public String getSourceName(){ return sourceName; }
	public Type getType(){ return type; }

	
	
	public Integer getInteger() { return i; }
	public Event setInteger(Integer i) { this.i = i; return this; }
	
	public Boolean getBoolean() { return bool; }
	public Event setBoolean(Boolean bool) { this.bool = bool; return this; }
	
	public String getString() { return str; }
	public Event setString(String str) { this.str = str; return this; }
	
	public Audiobook getAudiobook() { return audiobook; }
	public Event setAudiobook(Audiobook audiobook) { this.audiobook = audiobook; return this; }
	
	public Bookmark getBookmark() { return bookmark; }
	public Event setBookmark(Bookmark bookmark) { this.bookmark = bookmark; return this; }
	
	public ArrayList<Audiobook> getAudiobooks() { return audiobooks; }
	public Event setAudiobooks(ArrayList<Audiobook> audiobooks) { this.audiobooks = audiobooks; return this; }
	
	public ArrayList<Bookmark> getBookmarks() { return bookmarks; }
	public Event setBookmarks(ArrayList<Bookmark> bookmarks) { this.bookmarks = bookmarks; return this; }
	
	public File getFile() { return file; }
	public Event setFile(File file) { this.file = file; return this; }
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("========================================================")
				.append("\nEvent sourcenName = ").append(sourceName)
				.append("\n\teventID = ").append(type)
				.append("\n\tevent class=").append(getClass().getSimpleName())
				.append("\n========================================================");
		return builder.toString();
	}
}
