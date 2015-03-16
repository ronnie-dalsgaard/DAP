package rd.dap.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rd.dap.R;
import rd.dap.dialogs.Dialog_bookmark_details;
import rd.dap.dialogs.Dialog_delete_bookmark;
import rd.dap.dialogs.Dialog_disabled_bookmarks;
import rd.dap.dialogs.Dialog_expired;
import rd.dap.dialogs.Dialog_import_export;
import rd.dap.fragments.AudiobooksFragment4;
import rd.dap.fragments.AudiobooksFragment4.OnAudiobookSelectedListener;
import rd.dap.fragments.TimerFragment.TimerListener;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkEvent;
import rd.dap.model.BookmarkManager;
import rd.dap.model.Track;
import rd.dap.monitors.BookmarkMonitor;
import rd.dap.monitors.BookmarkMonitor.BookmarkMonitorListener;
import rd.dap.monitors.DisplayMonitor;
import rd.dap.monitors.DisplayMonitor.DisplayMoniterListener;
import rd.dap.monitors.Monitor;
import rd.dap.services.HeadSetReceiver;
import rd.dap.services.PlayerService;
import rd.dap.services.PlayerService.DAPBinder;
import rd.dap.support.MainDriveHandler;
import rd.dap.support.Time;
import rd.dap.tasks.LoadBookmarksAndAudiobooksTask;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends MainDriveHandler implements OnClickListener, OnLongClickListener, 
ServiceConnection, PlayerService.PlayerObserver, OnSeekBarChangeListener,
OnAudiobookSelectedListener, TimerListener, DisplayMoniterListener, BookmarkMonitorListener,
Dialog_import_export.Callback {
	private static final String TAG = "MainActivity";
	private static final int TRACKNO = 1111;
	private static final int BOOKMARK = 2222;
	public static final String END = "/END";
	public static final double ANIMATION_SPEED = 1.5;
	private static Drawable noCover, drw_play, drw_pause, drw_play_on_cover, drw_pause_on_cover;
	private ViewGroup base;
	private Monitor displayMonitor;
	private PlayerService player;
	private boolean bound = false, locked = true;
	private Monitor bookmark_monitor = null;
	private LinearLayout bookmark_list;
	private SeekBar progress_seeker, track_seeker;
	private View timer_layout, timer_thumb_iv, timer_thumb_back_iv;
	private CountDownLatch latch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controller);

		version();

		//*******************************// BETA //*****************************************//
		beta(false);
		//*********************************************************************************//

		base = (ViewGroup) findViewById(R.id.controller_base);

		//No cover
		if(noCover == null || drw_play == null || drw_pause == null
				|| drw_play_on_cover == null || drw_pause_on_cover == null){
			noCover = getResources().getDrawable(R.drawable.ic_action_help);
			drw_play = getResources().getDrawable(R.drawable.ic_action_play);
			drw_pause = getResources().getDrawable(R.drawable.ic_action_pause);
			drw_play_on_cover = getResources().getDrawable(R.drawable.ic_action_play_over_video);
			drw_pause_on_cover = getResources().getDrawable(R.drawable.ic_action_pause_over_video);
		}

		audio_properties();

		//Buttons
		ImageButton btn_cover = (ImageButton) findViewById(R.id.audiobook_basics_btn_cover);
		btn_cover.setImageDrawable(null);
		btn_cover.setOnClickListener(this);

		((ImageButton) findViewById(R.id.track_btn_next)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.track_btn_previous)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.seeker_btn_forward)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.seeker_btn_rewind)).setOnClickListener(this);

		progress_seeker = (SeekBar) findViewById(R.id.seeker_progress_seeker);
		progress_seeker.setEnabled(!locked);
		progress_seeker.setOnSeekBarChangeListener(this);

		track_seeker = (SeekBar) findViewById(R.id.track_seeker);
		track_seeker.setEnabled(!locked);
		track_seeker.setOnSeekBarChangeListener(this);

		timer_layout = findViewById(R.id.timer_layout);
		timer_layout.setVisibility(View.GONE);

		timer_thumb_iv = findViewById(R.id.timer_thumb_iv);
		timer_thumb_iv.setOnClickListener(this);

		timer_thumb_back_iv = findViewById(R.id.timer_thumb_back_iv);
		timer_thumb_back_iv.setOnClickListener(this);

		bookmark_list = (LinearLayout) findViewById(R.id.controller_bookmark_list);



		latch = new CountDownLatch(2);

		//Display loading dialog
		final Dialog loading_dialog = new Dialog(this);
		loading_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(this);
		View dv = inflater.inflate(R.layout.loading, base, false);
		loading_dialog.setContentView(dv);
		loading_dialog.show();

		//Start playerservice
		new AsyncTask<Void, Void, Void>() {
			@Override protected Void doInBackground(Void... params) {
				Intent serviceIntent = new Intent(MainActivity.this, PlayerService.class);
				startService(serviceIntent);
				return null;
			}
		}.execute();

		System.out.println("MainActivity - savedInstanceState == null:   "+(savedInstanceState == null));
		if(savedInstanceState != null){ 
			latch.countDown();
			displayBookmarks();
		} else {
			//Load Audiobooks and Bookmarks
			new LoadBookmarksAndAudiobooksTask(this, new LoadBookmarksAndAudiobooksTask.Callback() {
				@Override public void complete() { 
					MainActivity.this.displayBookmarks();
					latch.countDown(); 
				}
				@Override
				public void noAudiobooks() {
					Intent intent = new Intent(MainActivity.this, AudiobooksActivity.class);
					startActivityForResult(intent, AudiobooksActivity.REQUEST_AUDIOBOOK);
				}
				@Override
				public void noBookmarks() {
					Intent intent = new Intent(MainActivity.this, AudiobooksActivity.class);
					startActivityForResult(intent, AudiobooksActivity.REQUEST_AUDIOBOOK);
				}
			}).execute();

		}

		new AsyncTask<Void, Void, Void>(){
			@Override protected Void doInBackground(Void... params) {
				try { latch.await(); } 
				catch (InterruptedException e) { e.printStackTrace(); }

				SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
				String author = pref.getString("author", null);
				String album = pref.getString("album", null);
				BookmarkManager bm = BookmarkManager.getInstance();
				Bookmark bookmark = bm.getBookmark(author, album);
				AudiobookManager am = AudiobookManager.getInstance();
				Audiobook audiobook = am.getAudiobook(bookmark);
				int trackno = bookmark.getTrackno();
				int progress = bookmark.getProgress();
				if(audiobook != null) player.setAudiobook(audiobook, trackno, progress);
				return null;
			}
			@Override protected void onPostExecute(Void result){
				loading_dialog.dismiss();
				AudiobooksFragment4 frag = (AudiobooksFragment4) getFragmentManager().findFragmentById(R.id.controller_audiobooks_fragment);
				if(frag != null){ 
					frag.displayAudiobooks();
				}
			}
		}.execute();


		//Start monitor
		if(displayMonitor != null) displayMonitor.kill();
		displayMonitor = new DisplayMonitor(this, this);
		displayMonitor.start();

		if(bookmark_monitor != null) bookmark_monitor.kill();
		bookmark_monitor = new BookmarkMonitor(this, this);
		bookmark_monitor.start();
	}
//	@Override
//	public void onSaveInstanceState(Bundle savedInstanceState) {
//		savedInstanceState.putInt(STATE_SCORE, mCurrentScore);
//		savedInstanceState.putInt(STATE_LEVEL, mCurrentLevel);
//		super.onSaveInstanceState(savedInstanceState);
//	}

	@Override
	public void onStart(){
		Log.d(TAG, "onStart");
		super.onStart();
		//Bind to PlayerService
		Intent intent = new Intent(this, PlayerService.class);
		bindService(intent, this, Context.BIND_AUTO_CREATE);	
	}
	@Override
	public void onStop(){
		Log.d(TAG, "onStop");
		super.onStop();
		//Unbind from PlayerService
		if(bound){
			unbindService(this);
			bound = false;
		}
	}
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected");
		DAPBinder binder = (DAPBinder) service;
		player = binder.getPlayerService();
		bound = true;

		player.addObserver(this);		
		latch.countDown();
		System.out.println("MainActivity - PlayerService latch released");
	}
	@Override
	public void onServiceDisconnected(ComponentName name) {
		bound = false;
		ImageButton cover_btn = (ImageButton) findViewById(R.id.audiobook_basics_btn_cover);
		if(cover_btn != null) cover_btn.setImageDrawable(null);
	}
	@Override 
	public void onStopTrackingTouch(SeekBar seekBar) { } //Never used
	@Override 
	public void onStartTrackingTouch(SeekBar seekBar) { } //Never used
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(locked) return;
		if(!fromUser) return;
		if(seekBar == progress_seeker){
			player.seekProgressTo(progress);			
		} else if(seekBar == track_seeker){
			int trackno = progress;
			player.seekTrackTo(trackno);
		}
	}
	@Override
	public boolean onLongClick(View v) {
		switch(v.getId()){
		case BOOKMARK:
			final Bookmark bookmark = (Bookmark) v.getTag();
			if(bookmark == null) break;
			new Dialog_bookmark_details(this, base, bookmark, new Dialog_bookmark_details.Callback() {
				@Override public void onDeleteBookmark() {
					new Dialog_delete_bookmark(MainActivity.this, base, bookmark, new Dialog_delete_bookmark.Callback() {
						@Override
						public void onDeleteBookmarkConfirmed() {
							//Remove the bookmark
							BookmarkManager.getInstance().removeBookmark(MainActivity.this, bookmark);
							displayBookmarks();
							if(player == null || 
									(player.getAudiobook().getAuthor().equals(bookmark.getAuthor())
									&& player.getAudiobook().getAlbum().equals(bookmark.getAlbum()))){
								displayNoInfo();
								displayNoTracks();
								displayNoTime();
								displayNoPlayButton();
							}
						}
					}).show();
				}
				@Override
				public void onItemSelected(BookmarkEvent event) {
					if(player == null) {
						Toast.makeText(MainActivity.this, "Unable to undo - No media player found", Toast.LENGTH_LONG).show();
						return;
					}
					int new_trackno = event.getTrackno();
					int new_progress = event.getProgress();
					player.undoTo(new_trackno, new_progress);
				}
			}).show();
		}
		return true;
	}
	@Override
	public void onClick(View v) {
		if(player == null) return;
		switch(v.getId()){
		case R.id.audiobook_basics_btn_cover: click_cover(); break;
		case R.id.track_btn_next: click_next(); break;
		case R.id.track_btn_previous: click_prev(); break;
		case R.id.seeker_btn_forward: click_forward(); break;
		case R.id.seeker_btn_rewind: click_rewind(); break;
		case TRACKNO: click_track((int) v.getTag()); break;
		case BOOKMARK: click_bookmark((Bookmark) v.getTag()); break;
		case R.id.timer_thumb_iv: click_timer_thumb(); break;
		case R.id.timer_thumb_back_iv: click_timer_thumb_back(); break;
		}
	}
	//Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_audiobooks:
			Intent intent = new Intent(this, AudiobooksActivity.class);
			startActivityForResult(intent, AudiobooksActivity.REQUEST_AUDIOBOOK);
			break;

		case R.id.menu_item_import_export:
			new Dialog_import_export(this, base, this).show();
			break;
		}
		return false;
	}

	private void click_bookmark(Bookmark bookmark) {
		Audiobook audiobook;
		if(bookmark == null) return;
		audiobook = AudiobookManager.getInstance().getAudiobook(bookmark);
		if(audiobook == null) return;
		player.setAudiobook(audiobook, bookmark.getTrackno(), bookmark.getProgress());
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		pref.edit().putString("author", bookmark.getAuthor()).putString("album", bookmark.getAlbum()).commit();
	}
	private void click_cover() {
		if(player == null) return;
		Audiobook audiobook = player.getAudiobook();
		Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
		if(bookmark == null) return;
		player.toggle();
	}
	private void click_next() {
		if(locked) { emphasizeLock(); return; }
		player.next();
	}
	private void click_prev() {
		if(locked) { emphasizeLock(); return; }
		player.prev();
	}
	private void click_forward() {
		if(locked) { emphasizeLock(); return; }
		player.forward(Time.toMillis(1, TimeUnit.MINUTES));
	}
	private void click_rewind() {
		if(locked) { emphasizeLock(); return; }
		player.rewind(Time.toMillis(1, TimeUnit.MINUTES));
	}
	private void click_track(int trackno) {
		if(locked) { emphasizeLock(); return; }
		player.selectTrack(trackno);
	}
	private void click_timer_thumb() {
		timer_layout.setVisibility(View.VISIBLE);
		timer_thumb_iv.setVisibility(View.GONE);

		float fromXDelta = timer_layout.getWidth(); 
		if(fromXDelta < 1) fromXDelta = 500;
		Animation show_timer = new TranslateAnimation(fromXDelta, 0, 0, 0);
		show_timer.setDuration((int)(250*ANIMATION_SPEED));
		show_timer.setInterpolator(this, android.R.anim.linear_interpolator);
		show_timer.setAnimationListener(new AnimationListener() {
			@Override public void onAnimationStart(Animation animation) { }
			@Override public void onAnimationRepeat(Animation animation) { }
			@Override public void onAnimationEnd(Animation animation) {
				timer_thumb_back_iv.setVisibility(View.VISIBLE);
				Animation show_back = new AlphaAnimation(0f, 1f);
				show_back.setDuration((int)(250*ANIMATION_SPEED));
				show_back.setInterpolator(MainActivity.this, android.R.anim.linear_interpolator);
				timer_thumb_back_iv.startAnimation(show_back);
			}
		});

		timer_layout.startAnimation(show_timer);
	}
	private void click_timer_thumb_back() {
		timer_thumb_back_iv.setVisibility(View.GONE);

		float toXDelta = timer_layout.getWidth();
		Animation hide_timer = new TranslateAnimation(0, toXDelta, 0, 0);
		hide_timer.setDuration(250);
		hide_timer.setInterpolator(this, android.R.anim.linear_interpolator);
		hide_timer.setAnimationListener(new AnimationListener() {
			@Override public void onAnimationStart(Animation animation) { }
			@Override public void onAnimationRepeat(Animation animation) { }
			@Override public void onAnimationEnd(Animation animation) {
				timer_layout.setVisibility(View.GONE);
				timer_thumb_iv.setVisibility(View.VISIBLE);
				Animation show_thumb = new AlphaAnimation(0f, 1f);
				show_thumb.setDuration((int)(250*ANIMATION_SPEED));
				show_thumb.setInterpolator(MainActivity.this, android.R.anim.linear_interpolator);
				timer_thumb_iv.startAnimation(show_thumb);
			}
		});

		timer_layout.startAnimation(hide_timer);
	}

	//Callbacks
	@Override
	public void onSetAudiobook(final Audiobook audiobook){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				int trackno = 0;
				Track track = audiobook.getPlaylist().get(trackno);
				displayInfo(audiobook, trackno);
				displayPlayButton();
				displayTracks(audiobook, trackno);
				displayTime(track);
			}
		});
	}
	@Override
	public void onSetBookmark(final Audiobook audiobook, final int trackno, int progress){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Track track = audiobook.getPlaylist().get(trackno);
				displayInfo(audiobook, trackno);
				displayPlayButton();
				displayTracks(audiobook, trackno);
				displayTime(track);
			}
		});
	}
	@Override
	public void onPlayAudiobook() { 
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ImageButton cover_btn = (ImageButton) findViewById(R.id.audiobook_basics_btn_cover);
				cover_btn.setImageDrawable(drw_pause_on_cover);
			}
		});
	}
	@Override
	public void onPauseAudiobook() { 
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ImageButton cover_btn = (ImageButton) findViewById(R.id.audiobook_basics_btn_cover);
				cover_btn.setImageDrawable(drw_play_on_cover);
			}
		});
	}
	@Override
	public void onNext(final Audiobook audiobook, final int new_trackno) { 
		Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
		if(bookmark == null) return;
		bookmark.setTrackno(new_trackno);
		BookmarkManager bm = BookmarkManager.getInstance();
		bm.createOrUpdateBookmark(getFilesDir(), bookmark, true);
		bookmark.addEvent(new BookmarkEvent(BookmarkEvent.Function.NEXT, new_trackno, 0));
		BookmarkManager.getInstance().createOrUpdateBookmark(getFilesDir(), bookmark, true);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				displayBookmarks();
				Track track = audiobook.getPlaylist().get(new_trackno);
				displayPlayButton();
				displayTracks(audiobook, new_trackno);
				displayTime(track);
			}
		});
	}
	@Override
	public void onPrev(final Audiobook audiobook, final int new_trackno) { 
		Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
		if(bookmark == null) return;
		bookmark.setTrackno(new_trackno);
		BookmarkManager bm = BookmarkManager.getInstance();
		bm.createOrUpdateBookmark(getFilesDir(), bookmark, true);
		bookmark.addEvent(new BookmarkEvent(BookmarkEvent.Function.PREV, new_trackno, 0));
		BookmarkManager.getInstance().createOrUpdateBookmark(getFilesDir(), bookmark, true);
		runOnUiThread(new Runnable() {
			@Override public void run() { 
				displayBookmarks();
				Track track = audiobook.getPlaylist().get(new_trackno);
				displayPlayButton();
				displayTracks(audiobook, new_trackno);
				displayTime(track);
			}
		});
	}
	@Override
	public void onForward(final Audiobook audiobook, final int trackno, int new_progress){
		Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
		if(bookmark == null) return;
		bookmark.addEvent(new BookmarkEvent(BookmarkEvent.Function.FORWARD, trackno, new_progress));
		bookmark.setProgress(new_progress);
		BookmarkManager bm = BookmarkManager.getInstance();
		bm.createOrUpdateBookmark(getFilesDir(), bookmark, true);
		runOnUiThread(new Runnable() { 
			@Override public void run() {
				displayBookmarks();
				Track track = audiobook.getPlaylist().get(trackno);
				displayTime(track);
			} 
		});
	}
	@Override
	public void onRewind(final Audiobook audiobook, final int trackno, int new_progress){
		Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
		if(bookmark == null) return;
		bookmark.addEvent(new BookmarkEvent(BookmarkEvent.Function.REWIND, trackno, new_progress));
		bookmark.setProgress(new_progress);
		BookmarkManager bm = BookmarkManager.getInstance();
		bm.createOrUpdateBookmark(getFilesDir(), bookmark, true);
		runOnUiThread(new Runnable() { 
			@Override public void run() {
				displayBookmarks();
				Track track = audiobook.getPlaylist().get(trackno);
				displayTime(track);
			} 
		});
	}
	@Override
	public void onSelectTrack(final Audiobook audiobook, final int new_trackno){
		Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
		if(bookmark == null) return;
		bookmark.setTrackno(new_trackno);
		BookmarkManager bm = BookmarkManager.getInstance();
		bm.createOrUpdateBookmark(getFilesDir(), bookmark, true);
		bookmark.addEvent(new BookmarkEvent(BookmarkEvent.Function.SELECT, new_trackno, 0));
		BookmarkManager.getInstance().createOrUpdateBookmark(getFilesDir(), bookmark, true);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				displayBookmarks();
				Track track = audiobook.getPlaylist().get(new_trackno);
				displayPlayButton();
				displayTracks(audiobook, new_trackno);
				displayTime(track);
			}
		});
	}
	@Override
	public void onSeekProgress(final Audiobook audiobook, final int trackno, int new_progress){
		Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
		if(bookmark == null) return;
		bookmark.setProgress(new_progress);
		BookmarkManager bm = BookmarkManager.getInstance();
		bm.createOrUpdateBookmark(getFilesDir(), bookmark, true);
		bookmark.addEvent(new BookmarkEvent(BookmarkEvent.Function.SEEK_PROGRESS, trackno, new_progress));
		BookmarkManager.getInstance().createOrUpdateBookmark(getFilesDir(), bookmark, true);
		runOnUiThread(new Runnable() { 
			@Override public void run() { 
				displayBookmarks();
				Track track = audiobook.getPlaylist().get(trackno);
				displayTime(track); 
			} 
		});  
	}
	@Override
	public void onSeekTrack(final Audiobook audiobook,final int new_trackno){
		Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
		if(bookmark == null) return;
		bookmark.setTrackno(new_trackno);
		BookmarkManager bm = BookmarkManager.getInstance();
		bm.createOrUpdateBookmark(getFilesDir(), bookmark, true);
		bookmark.addEvent(new BookmarkEvent(BookmarkEvent.Function.SEEK_TRACK, new_trackno, 0));
		BookmarkManager.getInstance().createOrUpdateBookmark(getFilesDir(), bookmark, true);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				displayBookmarks();
				Track track = audiobook.getPlaylist().get(new_trackno);
				displayPlayButton();
				displayTracks(audiobook, new_trackno);
				displayTime(track);
			}
		});
	}
	@Override
	public void onUndo(final Audiobook audiobook, final int new_trackno, int new_progress){
		Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
		if(bookmark == null) return;
		bookmark.setTrackno(new_trackno);
		bookmark.setProgress(new_progress);
		BookmarkManager bm = BookmarkManager.getInstance();
		bm.createOrUpdateBookmark(getFilesDir(), bookmark, true);
		bookmark.addEvent(new BookmarkEvent(BookmarkEvent.Function.UNDO, new_trackno, new_progress));
		BookmarkManager.getInstance().createOrUpdateBookmark(getFilesDir(), bookmark, true);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				displayBookmarks();
				Track track = audiobook.getPlaylist().get(new_trackno);
				displayPlayButton();
				displayTracks(audiobook, new_trackno);
				displayTime(track);
			}
		});
	}
	@Override
	public void onComplete(final Audiobook audiobook, final int new_trackno) {
		runOnUiThread(new Runnable() {
			@Override public void run() {
				if(new_trackno == -1) return;
				Track track = audiobook.getPlaylist().get(new_trackno);
				displayTracks(audiobook, new_trackno);
				displayTime(track);
			}
		});
	}
	@Override
	public void onAudiobookSelected(Audiobook audiobook) {
		selectAudiobook(audiobook);
	}
	@Override
	public void onTimerTerminate() {
		player.pause();
	}

	//Update views
	private void displayNoInfo(){
		//Author
		TextView author_tv = (TextView) findViewById(R.id.audiobook_basics_author_tv);
		if(author_tv != null) author_tv.setText("");

		//Album
		TextView album_tv = (TextView) findViewById(R.id.audiobook_basics_album_tv);
		if(album_tv != null) album_tv.setText("");

		//Cover
		ImageView cover_iv = (ImageView) findViewById(R.id.audiobook_basics_cover_iv);
		if(cover_iv != null) cover_iv.setImageDrawable(noCover);
	}
	private void displayInfo(Audiobook audiobook, int trackno){
		Track track = audiobook.getPlaylist().get(trackno);

		//Author
		TextView author_tv = (TextView) findViewById(R.id.audiobook_basics_author_tv);
		if(author_tv != null) author_tv.setText(audiobook.getAuthor());

		//Album
		TextView album_tv = (TextView) findViewById(R.id.audiobook_basics_album_tv);
		if(album_tv != null) album_tv.setText(audiobook.getAlbum());

		//Cover
		ImageView cover_iv = (ImageView) findViewById(R.id.audiobook_basics_cover_iv);
		String cover = track.getCover();
		if(cover == null) cover = audiobook.getCover();
		if(cover != null) {
			Bitmap bitmap = BitmapFactory.decodeFile(cover);
			if(cover_iv != null) cover_iv.setImageBitmap(bitmap);
		} else {
			if(cover_iv != null) cover_iv.setImageDrawable(noCover);
		}
	}
	private void displayNoTracks(){
		TextView title_tv = (TextView) findViewById(R.id.track_title);
		LinearLayout tracks_gv = (LinearLayout) findViewById(R.id.controller_tracks_grid);
		track_seeker.setMax(0);
		track_seeker.setProgress(0);

		//Title
		if(title_tv != null) title_tv.setText("");

		//Tracks
		if(tracks_gv != null){
			tracks_gv.removeAllViews();
		}
	}
	private void displayTracks(Audiobook audiobook, int trackno){
		Track track = audiobook.getPlaylist().get(trackno);
		TextView title_tv = (TextView) findViewById(R.id.track_title);
		LinearLayout tracks_gv = (LinearLayout) findViewById(R.id.controller_tracks_grid);
		track_seeker.setMax(audiobook.getPlaylist().size()-1);
		track_seeker.setProgress(trackno);

		//Title
		String _trackno = String.format(Locale.US, "%02d", trackno+1);
		if(title_tv != null) title_tv.setText(_trackno + " " + track.getTitle());

		//Tracks
		if(tracks_gv != null){
			int trackCount = audiobook.getPlaylist().size();
			tracks_gv.removeAllViews();
			final int COLUMNS = 8;
			LinearLayout row = null;
			int m = LayoutParams.MATCH_PARENT;
			int w = LayoutParams.WRAP_CONTENT;
			LayoutParams row_p = new LinearLayout.LayoutParams(m, w);
			LayoutParams p = new LinearLayout.LayoutParams(0, 80, 1);
			for(int i = 0; i < trackCount; i++){
				if(i % COLUMNS == 0){
					row = new LinearLayout(this);
					row.setOrientation(LinearLayout.HORIZONTAL);
					tracks_gv.addView(row, row_p);
				}
				TextView cell = new TextView(this);
				cell.setTextColor(getResources().getColor(R.color.white));
				cell.setGravity(Gravity.CENTER);
				cell.setText(String.format("%02d", i+1));
				if(i == trackno){
					cell.setBackgroundResource(R.drawable.circle);
				}
				cell.setId(TRACKNO);
				cell.setTag(i); //Autoboxing
				cell.setOnClickListener(this);
				row.addView(cell, p);
			}
			if(trackCount % COLUMNS > 0){
				View space = new View(this);
				int weight = COLUMNS - (trackCount % COLUMNS);
				LinearLayout.LayoutParams space_p = new LinearLayout.LayoutParams(0, 75, weight);
				row.addView(space, space_p);
			}
		}
	}
	@Override
	public void displayNoTime(){
		//Progress
		TextView progress_tv = (TextView) findViewById(R.id.seeker_progress_tv);
		SeekBar seeker = (SeekBar) findViewById(R.id.seeker_progress_seeker);

		progress_tv.setText(Time.toString(0));
		seeker.setMax(0);
		seeker.setProgress(0);
	}
	@Override
	public void displayTime(Track track){
		//Progress
		TextView progress_tv = (TextView) findViewById(R.id.seeker_progress_tv);
		SeekBar seeker = (SeekBar) findViewById(R.id.seeker_progress_seeker);

		if(player == null) {
			progress_tv.setText(Time.toString(0));
			seeker.setProgress(0);
		} else {
			int progress = player.getCurrentProgress();
			String _progress = Time.toString(progress);
			int duration = track.getDuration();
			String _duration = Time.toString(duration);
			progress_tv.setText(_progress + " / " + _duration);
			seeker.setMax(duration);
			seeker.setProgress(progress);
		}
	}
	private void displayNoPlayButton(){
		ImageButton cover_btn = (ImageButton) findViewById(R.id.audiobook_basics_btn_cover);
		cover_btn.setImageDrawable(null);
	}
	private void displayPlayButton(){
		//Cover button
		ImageButton cover_btn = (ImageButton) findViewById(R.id.audiobook_basics_btn_cover);
		if(player == null || player.getAudiobook() == null) {
			cover_btn.setImageDrawable(null);
		} else {
			cover_btn.setImageDrawable(player.isPlaying() ? drw_pause_on_cover : drw_play_on_cover);
		}
	}
	public void displayBookmarks(){
		if(bookmark_list == null) { System.out.println("no bookmark list!"); return; }
		bookmark_list.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(this);
		ArrayList<Bookmark> bookmarks = BookmarkManager.getInstance().getBookmarks();
		ArrayList<Bookmark> disabledBookmarks = new ArrayList<Bookmark>();
		for(Bookmark bookmark : bookmarks){
			View v = inflater.inflate(R.layout.bookmark_item, bookmark_list, false);
			v.setId(BOOKMARK);
			v.setTag(bookmark);
			v.setOnClickListener(this);
			v.setOnLongClickListener(this);

			//Cover
			Audiobook audiobook = AudiobookManager.getInstance().getAudiobook(bookmark);
			if(audiobook == null) { disabledBookmarks.add(bookmark); continue; }

			ImageView cover_iv = (ImageView) v.findViewById(R.id.bookmark_cover_iv);
			String cover = audiobook.getCover();
			if(cover != null) {
				Bitmap bitmap = BitmapFactory.decodeFile(cover);
				if(cover_iv != null) cover_iv.setImageBitmap(bitmap);
			} else {
				if(cover_iv != null) cover_iv.setImageDrawable(noCover);
			}

			//Track no
			TextView track_tv = (TextView) v.findViewById(R.id.bookmark_track_tv);
			track_tv.setText(String.format("%02d", bookmark.getTrackno()+1));

			//Progress
			TextView progress_tv = (TextView) v.findViewById(R.id.bookmark_progress_tv);
			progress_tv.setText(Time.toString(bookmark.getProgress()));

			bookmark_list.addView(v);

			View div = inflater.inflate(R.layout.divider_vertical, bookmark_list, false);
			bookmark_list.addView(div);
		}
		if(!disabledBookmarks.isEmpty()){
			new Dialog_disabled_bookmarks(this, base, disabledBookmarks, new Dialog_disabled_bookmarks.Callback() {
				@Override
				public void onBookmarkDeleted() {
					displayBookmarks();
					displayNoInfo();
					displayNoTracks();
					displayNoTime();
					displayNoPlayButton();
				}
			}).show();



		}
	}
	private void emphasizeLock(){
		ImageView lock_iv = (ImageView) findViewById(R.id.controller_lock);
		AnimationSet set = new AnimationSet(false);

		Animation translate = new TranslateAnimation(0, -200, 0, 0);
		translate.setDuration(500);
		translate.setInterpolator(this, android.R.anim.decelerate_interpolator);
		set.addAnimation(translate);

		Animation translateBack = new TranslateAnimation(0, 200, 0, 0);
		translateBack.setDuration(500);
		translateBack.setInterpolator(this, android.R.anim.accelerate_interpolator);
		translateBack.setStartOffset(500);
		set.addAnimation(translateBack);

		lock_iv.startAnimation(set);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		Log.d(TAG, "onActivityResult");
		if(data == null) return;
		Audiobook audiobook = (Audiobook) data.getSerializableExtra("result");
		if(audiobook == null) return;
		selectAudiobook(audiobook);
		if(latch != null) latch.countDown(); 
	}
	public void selectAudiobook(Audiobook audiobook){
		Bookmark bookmark = new Bookmark(audiobook.getAuthor(), audiobook.getAlbum(), 0, 0);
		BookmarkManager.getInstance().createOrUpdateBookmark(getFilesDir(), bookmark, true);
		displayBookmarks();

		player.setAudiobook(audiobook, bookmark.getTrackno(), bookmark.getProgress());
	}
	@Override 
	public PlayerService getPlayer() { return player; }

	@Override
	public boolean dispatchTouchEvent(MotionEvent event){
		final int MIN_DIST = 200;
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			x1 = event.getX(); 
			y1 = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			x2 = event.getX();
			y2 = event.getY();
			if(Math.abs(x2 - x1) < MIN_DIST) break;
			if(Math.abs(y2 - y1) > MIN_DIST) break;

			final ImageView lock_iv = (ImageView) findViewById(R.id.controller_lock);
			if(x2 > x1){ //Right
				if(locked) break;
				locked = true;

				Animation translate = new TranslateAnimation(-125, 0, 0, 0); 
				translate.setInterpolator(MainActivity.this, android.R.anim.bounce_interpolator);
				translate.setDuration((int)(1000*ANIMATION_SPEED));

				Animation translatey = new TranslateAnimation(0, 0, 40, 0); 
				translatey.setInterpolator(MainActivity.this, android.R.anim.decelerate_interpolator);
				translatey.setDuration((int)(250*ANIMATION_SPEED));

				Animation fade = new AlphaAnimation(0, 1);
				fade.setInterpolator(MainActivity.this, android.R.anim.decelerate_interpolator);
				fade.setDuration((int)(1000*ANIMATION_SPEED));

				Animation scale = new ScaleAnimation(5, 1, 5, 1);
				scale.setInterpolator(MainActivity.this, android.R.anim.bounce_interpolator);
				scale.setDuration((int)(1000*ANIMATION_SPEED));

				AnimationSet set = new AnimationSet(false);
				set.addAnimation(translate);
				set.addAnimation(translatey);
				set.addAnimation(fade);
				set.addAnimation(scale);
				set.setAnimationListener(new AnimationListener() {
					@Override public void onAnimationStart(Animation animation) {
						lock_iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_secure));						
					}
					@Override public void onAnimationRepeat(Animation animation) { }
					@Override public void onAnimationEnd(Animation animation) { }
				});
				lock_iv.startAnimation(set);

			} else { //Left
				if(!locked) break;
				locked = false;

				Animation translate = new TranslateAnimation(0, -125, 0, 40); 
				translate.setInterpolator(MainActivity.this, android.R.anim.accelerate_interpolator);

				Animation fade = new AlphaAnimation(1, 0);
				fade.setInterpolator(MainActivity.this, android.R.anim.accelerate_interpolator);

				Animation scale = new ScaleAnimation(1, 5, 1, 5);
				scale.setInterpolator(MainActivity.this, android.R.anim.accelerate_interpolator);

				AnimationSet set = new AnimationSet(false);
				set.addAnimation(translate);
				set.addAnimation(fade);
				set.addAnimation(scale);
				set.setDuration((int)(350*ANIMATION_SPEED));
				set.setAnimationListener(new AnimationListener() {
					@Override public void onAnimationStart(Animation animation) { }
					@Override public void onAnimationRepeat(Animation animation) { }
					@Override public void onAnimationEnd(Animation animation) {
						lock_iv.setImageDrawable(null);
					}
				});
				lock_iv.startAnimation(set);
			}
		}
		progress_seeker.setEnabled(!locked);
		track_seeker.setEnabled(!locked);
		super.dispatchTouchEvent(event);
		super.onTouchEvent(event);
		return false;
	}
	private float x1 = 0, x2 = 0, y1 = 0, y2 = 0;

	private void version() {
		TextView version_tv = (TextView) findViewById(R.id.version);
		String versionName;
		try { versionName = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) { versionName = "Unknown"; }
		if(version_tv != null) version_tv.setText(versionName);
	}
	private void beta(boolean isBeta) {
		TextView beta = (TextView) findViewById(R.id.controller_beta);
		beta.setVisibility(isBeta ? View.VISIBLE : View.GONE);
		if(isBeta){
			Calendar expiration = Calendar.getInstance(Locale.getDefault());
			expiration.set(Calendar.YEAR, 2015);
			expiration.set(Calendar.MONTH, Calendar.APRIL);
			expiration.set(Calendar.DAY_OF_MONTH, 1);
			beta.setText("BETA (expires on April 1st 2015)");

			if(System.currentTimeMillis() >= expiration.getTimeInMillis()) {
				new Dialog_expired(this, base).show();
			}
		}
	}
	private void audio_properties() {
		//Set stream for hardware volume buttons
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Request audio focus for playback
		final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		final ComponentName receiver = new ComponentName(getPackageName(), HeadSetReceiver.class.getName());

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
		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			throw new RuntimeException("Unable to obtaing audio focus");
		}
		Log.d(TAG, "Audio focus gained");
	}
}
