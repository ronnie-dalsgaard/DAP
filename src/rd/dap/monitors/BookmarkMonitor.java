package rd.dap.monitors;

import java.util.concurrent.TimeUnit;

import rd.dap.model.Audiobook;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkEvent;
import rd.dap.model.BookmarkManager;
import rd.dap.services.PlayerService;
import android.app.Activity;
import android.util.Log;

public class BookmarkMonitor extends Monitor {
	private static final String TAG = "Monitor_bookmarks";
	private boolean go_again = true;
	private Activity activity;
	private BookmarkMonitorListener bookmark_monitor_listener;
	
	public interface BookmarkMonitorListener {
		public PlayerService getPlayer();
		public void displayBookmarks();
	}

	public BookmarkMonitor(Activity activity, BookmarkMonitorListener bookmark_listener) {
		super(5, TimeUnit.SECONDS);
		this.activity = activity;
		this.bookmark_monitor_listener = bookmark_listener;
	}

	@Override
	public void execute() {
		PlayerService player = bookmark_monitor_listener.getPlayer();
		if(player == null) return;

		if(!go_again && !player.isPlaying()){
			return;
		}

		Audiobook audiobook = player.getAudiobook();
		if(audiobook == null) return;

		String author = audiobook.getAuthor();
		String album = audiobook.getAlbum();
		int trackno = player.getTrackno();
		int progress = player.getCurrentProgress();
		BookmarkManager bm = BookmarkManager.getInstance();
		if(trackno > 0 || progress > 0){
			Bookmark bookmark = bm.createOrUpdateBookmark(activity.getFilesDir(), author, album, trackno, progress, null, true);
			BookmarkManager.getInstance().saveBookmarks(activity.getFilesDir());
			Log.d(TAG, "Bookmark created or updated\n"+bookmark);
			
			bookmark.addEvent(new BookmarkEvent(BookmarkEvent.Function.PLAY, trackno, progress));
			
			//Update view
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					bookmark_monitor_listener.displayBookmarks();
				}
			});
		}
		go_again = player.isPlaying();
	}

}
