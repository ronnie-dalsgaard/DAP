package rd.dap.events;

import java.util.ArrayList;

import rd.dap.model.Bookmark;

public class BookmarksLoadedEvent extends Event {
	private ArrayList<Bookmark> bookmarks;
	
	public BookmarksLoadedEvent(String sourceName, ArrayList<Bookmark> bookmarks) {
		super(sourceName, Event.BOOKMARKS_LOADED_EVENT);
		this.bookmarks = bookmarks;
	}

	public ArrayList<Bookmark> getBookmarks() {
		return bookmarks;
	}
}
