package rd.dap.events;

import rd.dap.model.Bookmark;

public class HasBookmarkEvent extends Event {
	private Bookmark bookmark;
	public HasBookmarkEvent(String sourceName, Type type, Bookmark bookmark) {
		super(sourceName, type);
		this.bookmark = bookmark;
	}
	public Bookmark getBookmark(){
		return bookmark;
	}
}