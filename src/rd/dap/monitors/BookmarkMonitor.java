package rd.dap.monitors;

import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;

import rd.dap.events.Event;
import rd.dap.events.Event.Type;
import rd.dap.events.EventBus;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkEvent;
import rd.dap.model.BookmarkManager;
import rd.dap.model.BookmarkEvent.Function;
import rd.dap.services.PlayerService;

public class BookmarkMonitor extends Monitor {
	private PlayerService player;
	private int lifetime;

	public BookmarkMonitor(PlayerService player) {
		super(5, TimeUnit.SECONDS);
		this.player = player;
	}

	@Override
	public void execute() {
		if(player == null) kill();
		if(player.isPlaying()) lifetime = 2;
		else lifetime--;
		if(lifetime <= 0) return;

		Bookmark bookmark = player.getBookmark();
		int progress = player.getProgress();
		int trackno = bookmark.getTrackno();
		bookmark.setProgress(progress);
		bookmark.addEvent(new BookmarkEvent(Function.PLAY, trackno, progress));
		
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				BookmarkManager.getInstance().saveBookmarks(player.getFilesDir());
				return null;
			}
		}.execute();
		EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.BOOKMARK_UPDATED_EVENT).setBookmark(bookmark));
	}

}
