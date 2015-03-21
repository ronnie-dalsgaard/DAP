package rd.dap.events;

import rd.dap.model.Bookmark;

public class BookmarkDeletedEvent extends Event {

	private Bookmark bookmark;

	public BookmarkDeletedEvent(String sourceName, Bookmark bookmark) {
		super(sourceName, Type.BOOKMARK_DELETED_EVENT);
		this.bookmark = bookmark;
	}

	public Bookmark getBookmark() {
		return bookmark;
	}
}
