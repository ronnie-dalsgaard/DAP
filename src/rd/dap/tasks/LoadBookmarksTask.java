package rd.dap.tasks;

import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.services.PlayerService;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class LoadBookmarksTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "LoadBookmarksTask";
	private Activity activity;
	private PlayerService player;
	private Dialog dialog;
	private Callback callback;
	
	public interface Callback{
		public void displayBookmarks();
	}
	
	public LoadBookmarksTask(Activity activity, PlayerService player, Dialog dialog, Callback callback){
		this.activity = activity;
		this.player = player;
		this.dialog = dialog;
		this.callback = callback;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		AudiobookManager.getInstance().loadAudiobooks(activity);
		BookmarkManager.getInstance().loadBookmarks(activity.getFilesDir()); 
		return null;
	}
	@Override 
	protected void onPostExecute(Void obj){
		Log.d(TAG, "onPostExecute - audiobooks loaded");

		activity.runOnUiThread(new Runnable() {

			@Override 
			public void run() {
				if(BookmarkManager.getInstance().getBookmarks().isEmpty()){
					System.out.println("MainActivity AsyncTask - NO BOOKMARKS");
					return;
				}
				//At this point at least one bookmark exists
				SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
				String author = pref.getString("author", null);
				String album = pref.getString("album", null);
				Bookmark bookmark = BookmarkManager.getInstance().getBookmark(author, album);

				if(bookmark == null){
					bookmark = BookmarkManager.getInstance().getBookmarks().get(0);
					pref.edit().putString("author", bookmark.getAuthor()).putString("album", bookmark.getAlbum()).commit();
				}

				if(player != null && player.getAudiobook() == null) {
					Audiobook audiobook = AudiobookManager.getInstance().getAudiobook(bookmark);
					if(audiobook != null) {
						player.setAudiobook(audiobook, bookmark.getTrackno(), bookmark.getProgress());
					}
				}

				callback.displayBookmarks();
			}

		});
		dialog.dismiss();
	}
}
