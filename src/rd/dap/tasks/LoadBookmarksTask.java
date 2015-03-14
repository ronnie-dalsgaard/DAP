package rd.dap.tasks;

import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class LoadBookmarksTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "LoadBookmarksTask";
	private Activity activity;
	private Callback callback;
	
	public interface Callback{
		public void complete();
		public void noAudiobooks();
		public void noBookmarks();
	}
	
	public LoadBookmarksTask(Activity activity, Callback callback){
		this.activity = activity;
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
		AudiobookManager am = AudiobookManager.getInstance();
		if(am.getAudiobooks() == null || am.getAudiobooks().isEmpty()) callback.noAudiobooks();
		BookmarkManager bm = BookmarkManager.getInstance();
		if(bm.getBookmarks() == null || bm.getBookmarks().isEmpty()) callback.noBookmarks();

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
			}
		});
		callback.complete();
	}
}
