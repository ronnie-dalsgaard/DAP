package rd.dap.activities;

import java.util.concurrent.TimeUnit;

import rd.dap.R;
import rd.dap.activities.old_MainActivity.displayMonitor;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.model.Data;
import rd.dap.services.HeadSetReceiver;
import rd.dap.services.PlayerService;
import rd.dap.support.Changer;
import rd.dap.support.Monitor;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private static Monitor monitor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controller);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		final ComponentName receiver = new ComponentName(getPackageName(), HeadSetReceiver.class.getName());

		// Request audio focus for playback
		final OnAudioFocusChangeListener l = new OnAudioFocusChangeListener() {

			@Override
			public void onAudioFocusChange(int focusChange) {
				if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
					// Pause playback
				} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
					am.registerMediaButtonEventReceiver(receiver);
					// Resume playback 
				} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
					am.unregisterMediaButtonEventReceiver(receiver);
					am.abandonAudioFocus(this);
					// Stop playback
				}
			}
		};
		int result = am.requestAudioFocus(l, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		System.out.println("Audiofocus: "+result);
		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			throw new RuntimeException("Unable to obtaing audio focus");
		}
		Log.d(TAG, "Audio focus gained");





		Intent serviceIntent = new Intent(this, PlayerService.class);
		startService(serviceIntent);


		//Load Audiobooks and Bookmarks
		new AsyncTask<Activity, Void, Bookmark>(){
			Activity activity;
			@Override
			protected Bookmark doInBackground(Activity... params) {
				activity = params[0];
				AudiobookManager.getInstance().loadAudiobooks(activity);
				BookmarkManager.getInstance().loadBookmarks(activity.getFilesDir()); 

				SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
				String author = pref.getString("author", null);
				String album = pref.getString("album", null);
				Bookmark bookmark = null;
				if(author != null && album != null){
					BookmarkManager bm = BookmarkManager.getInstance();
					AudiobookManager am = AudiobookManager.getInstance();

					bookmark = bm.getBookmark(author, album);
					if(bookmark != null){
						Audiobook audiobook = am.getAudiobook(bookmark);
						Data.setCurrentAudiobook(audiobook);
						int trackno = bookmark.getTrackno();
						Data.setCurrentTrack(audiobook.getPlaylist().get(trackno));
						Data.setCurrentPosition(trackno);
					}
				}
				return bookmark;
			}
			@Override 
			protected void onPostExecute(final Bookmark bookmark){
				Log.d(TAG, "onPostExecute - audiobooks loaded");

				runOnUiThread(new Runnable() {

					@Override 
					public void run() {
						if(bookmark != null){
							//displayBookmark
						}
					}
				});
			}
		}.execute(this);

		//Start monitor
		if(monitor == null){
			monitor = new displayMonitor(this);
			monitor.start();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	class displayMonitor extends Monitor{
		private Activity activity;
		
		public displayMonitor(Activity activity) {
			super(1, TimeUnit.SECONDS);
			this.activity = activity;
		}

		@Override
		public void execute() {
			if(activity == null) return;
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					//update Views
				}
			});
		}
		
	}
}
