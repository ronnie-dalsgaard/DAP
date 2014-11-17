//package rd.dap.fragments;
//
//import static rd.dap.activities.AudiobookActivity.STATE_EDIT;
//
//import java.util.concurrent.TimeUnit;
//
//import rd.dap.R;
//import rd.dap.activities.AudiobookActivity;
//import rd.dap.model.Audiobook;
//import rd.dap.model.Data;
//import rd.dap.model.Track;
//import rd.dap.services.PlayerService;
//import rd.dap.services.PlayerService.DAPBinder;
//import rd.dap.support.Monitor;
//import rd.dap.support.Time;
//import android.app.Activity;
//import android.app.Fragment;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.drawable.Drawable;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.LinearLayout.LayoutParams;
//import android.widget.Space;
//import android.widget.TextView;
//
//public class ControllerFragment extends Fragment/*DriveHandler*/ implements ServiceConnection, OnClickListener {
//
//	private final String TAG = "ControllerActivity";
//	private boolean bound = false;
//	private PlayerService player;
//	private Monitor controllerMonitor;
//	private boolean timerOn = false;
//	private static Drawable noCover, drw_play, drw_pause, drw_play_on_cover, drw_pause_on_cover;
//	private LinearLayout info_layout, tracks_gv;
//	private TextView author_tv, album_tv, title_tv, progress_tv;
//	private ImageButton cover_btn, next_btn, prev_btn, forward_btn, rewind_btn;
//	private ImageView cover_iv;
//	private Timer timer;
//	private Menu menu;
//	private Activity activity;
//
//	private static final int REQUEST_FRAGMENT_BASICS_EDIT = 1701;
//	private static final int CELL = 1111;
//
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		Log.d(TAG, "onCreate");
//		super.onCreate(savedInstanceState);
//
//		setHasOptionsMenu(true);
//
//		if(noCover == null || drw_play == null || drw_pause == null
//				|| drw_play_on_cover == null || drw_pause_on_cover == null){
//			noCover = getResources().getDrawable(R.drawable.ic_action_help);
//			drw_play = getResources().getDrawable(R.drawable.ic_action_play);
//			drw_pause = getResources().getDrawable(R.drawable.ic_action_pause);
//			drw_play_on_cover = getResources().getDrawable(R.drawable.ic_action_play_over_video);
//			drw_pause_on_cover = getResources().getDrawable(R.drawable.ic_action_pause_over_video);
//		}
//
////		controllerMonitor = new ControllerMonitor(1, TimeUnit.SECONDS);
////		controllerMonitor.start();
//	}
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		Log.d(TAG, "onCreateView");
//		View v = (ViewGroup) inflater.inflate(R.layout.controller, container, false);
//
//		cover_iv = (ImageView) v.findViewById(R.id.audiobook_basics_cover_iv);
//		author_tv = (TextView) v.findViewById(R.id.audiobook_basics_author_tv);
//		album_tv = (TextView) v.findViewById(R.id.audiobook_basics_album_tv);
//		if(Data.getCurrentAudiobook() != null){
//			displayValues();
//		}
//
//		//Audiobook basics
//		cover_btn = (ImageButton) v.findViewById(R.id.audiobook_basics_cover_btn);
//		cover_btn.setImageDrawable(null);
//		cover_btn.setOnClickListener(this);
//
//		info_layout = (LinearLayout) v.findViewById(R.id.audiobook_basics_info_layout);
//		info_layout.setOnClickListener(this);
//
//		//Tracks
//		next_btn = (ImageButton) v.findViewById(R.id.track_next);
//		next_btn.setOnClickListener(this);
//
//		prev_btn = (ImageButton) v.findViewById(R.id.track_previous);
//		prev_btn.setOnClickListener(this);
//
//		title_tv = (TextView) v.findViewById(R.id.track_title);
//		if(Data.getCurrentTrack() != null){
//			title_tv.setText(Data.getCurrentTrack().getTitle());
//		}
//
//		tracks_gv = (LinearLayout) v.findViewById(R.id.tracks_grid);
//
//		displayTracks();
//
//		//Seeker
//		forward_btn = (ImageButton) v.findViewById(R.id.seeker_fast_forward);
//		forward_btn.setOnClickListener(this);
//
//		rewind_btn = (ImageButton) v.findViewById(R.id.seeker_rewind);
//		rewind_btn.setOnClickListener(this);
//
//		progress_tv = (TextView) v.findViewById(R.id.seeker_progress_tv);
//		progress_tv.setText(Time.toString(0));
//		progress_tv.setOnClickListener(this);
//
//		displayProgress();
//
//		return v;
//	}
//	@Override 
//	public void onAttach(Activity activity){
//		super.onAttach(activity);
//		this.activity = activity;
//		try {
//			changer = (Changer) activity;
//		} catch (ClassCastException e) {
//			throw new ClassCastException(activity.toString()
//					+ " must implement Callback");
//		}
//	}
//
//	//Menu
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		this.menu = menu;
//		// Inflate the menu; this adds items to the action bar if it is present.
//		inflater.inflate(R.menu.details, menu);
//	}
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch(item.getItemId()){
//		case R.id.menu_item_timer: 
//			if(!timerOn){
//				MenuItem menuitem = menu.getItem(1);
//				timer = new Timer(15, TimeUnit.MINUTES, menuitem);
//				timer.start();
//				timerOn = true;
//			} else {
//				timer.kill();
//			}
//			break;
//		}
//		return true;
//	}
//
//	public void displayValues(){
//		Log.d(TAG, "displayValues");
//		if(Data.getCurrentAudiobook() == null){
//			System.out.println("displayValues: "+"no current audiobook");
//			//Cover
//			if(cover_iv != null) cover_iv.setImageDrawable(noCover);
//			
//			//Author
//			if(author_tv != null) author_tv.setText("Author");
//
//			//Album
//			if(album_tv != null) album_tv.setText("Album");
//			
//			return;
//		}
//		if(Data.getCurrentTrack() == null) return;
//		System.out.println("displayValues: "+"current track exists\n"+Data.getCurrentAudiobook());
//		if(cover_iv == null) System.out.println("cover_iv == null");
//		//Cover
//		String cover = Data.getCurrentTrack().getCover();
//		if(cover == null) cover = Data.getCurrentAudiobook().getCover();
//		if(cover != null) {
//			Bitmap bitmap = BitmapFactory.decodeFile(cover);
//			if(cover_iv != null) cover_iv.setImageBitmap(bitmap);
//		} else {
//			if(cover_iv != null) cover_iv.setImageDrawable(noCover);
//		}
//
//		//Author
//		if(author_tv == null) System.out.println("author_tv == null");
//		if(author_tv != null) author_tv.setText(Data.getCurrentAudiobook().getAuthor());
//
//		//Album
//		if(album_tv == null) System.out.println("audiobook_basics_album_tv == null");
//		if(album_tv != null) album_tv.setText(Data.getCurrentAudiobook().getAlbum());
//	}
//	public void displayTracks(){
//		if(Data.getCurrentAudiobook() == null){
//			if(title_tv != null) title_tv.setText("Title");
//			if(tracks_gv != null) tracks_gv.removeAllViews();
//			return;
//		}
//		if(Data.getCurrentPosition() == -1)return;
//		if(Data.getCurrentTrack() == null) return;
//		Activity activity = getActivity();
//		if(activity == null) return;
//		//Title
//		if(title_tv != null) title_tv.setText(Data.getCurrentTrack().getTitle());
//
//		//Tracks
//		if(tracks_gv != null){
//			tracks_gv.removeAllViews();
//			final int COLUMNS = 8;
//			LinearLayout row = null;
//			int m = LayoutParams.MATCH_PARENT;
//			int w = LayoutParams.WRAP_CONTENT;
//			LayoutParams row_p = new LinearLayout.LayoutParams(m, w);
//			LayoutParams p = new LinearLayout.LayoutParams(0, 80, 1);
//			for(int i = 0; i < Data.getCurrentAudiobook().getPlaylist().size(); i++){
//				if(i % COLUMNS == 0){
//					row = new LinearLayout(activity);
//					row.setOrientation(LinearLayout.HORIZONTAL);
//					tracks_gv.addView(row, row_p);
//				}
//				TextView cell = new TextView(activity);
//				cell.setTextColor(getResources().getColor(R.color.white));
//				cell.setGravity(Gravity.CENTER);
//				cell.setText(String.format("%02d", i+1));
//				if(i == Data.getCurrentPosition()){
//					cell.setBackground(getResources().getDrawable(R.drawable.circle));
//				}
//				cell.setId(CELL);
//				cell.setTag(i); //Autoboxing
//				cell.setOnClickListener(this);
//				row.addView(cell, p);
//			}
//			if(Data.getCurrentAudiobook().getPlaylist().size() % COLUMNS > 0){
//				Space space = new Space(activity);
//				int weight = COLUMNS - (Data.getCurrentAudiobook().getPlaylist().size() % COLUMNS);
//				LinearLayout.LayoutParams space_p = new LinearLayout.LayoutParams(0, 75, weight);
//				row.addView(space, space_p);
//			}
//		}
//	}
//	public void displayProgress(){
//		if(Data.getCurrentAudiobook() == null){
//			if(progress_tv != null) {
//				progress_tv.setText(Time.toString(0));
//			}
//			return;
//		}
//		if(player == null) return;
//		int progress = player.getCurrentProgress();
//		String _progress = Time.toString(progress);
//		long duration = Data.getCurrentTrack().getDuration();
//		if(duration > 0) _progress += " / " + Time.toString(duration);
//		progress_tv.setText(_progress);
//	}
//
//	@Override
//	public void onClick(View v) {
//		Log.d(TAG, "onClick");
//		String str = "";
//		long duration = -1;
//		switch(v.getId()){
//		//Cases for audiobook basics
//		case R.id.audiobook_basics_cover_btn:
//			//Fix view
//			cover_btn.setImageDrawable(!player.isPlaying() ? drw_pause_on_cover : drw_play_on_cover);
//			//Toggle play/pause
//			player.toggle();
//			break;
//		case R.id.audiobook_basics_info_layout:
//			if(Data.getCurrentAudiobook() == null) return;
//			Intent intent = new Intent(getActivity(), AudiobookActivity.class);
//			intent.putExtra("state", STATE_EDIT);
//			intent.putExtra("audiobook", Data.getCurrentAudiobook());
//			startActivityForResult(intent, REQUEST_FRAGMENT_BASICS_EDIT);
//			break;
//
//			//Cases for Tracks
//		case R.id.track_next:
//			if(player == null) return;
//			//if currently playing the last track - do nothing
//			if(Data.getCurrentAudiobook().getPlaylist().getLast().equals(Data.getCurrentTrack())) return;
//
//			int nextPosition = Data.getCurrentPosition()+1;
//			Track nextTrack = Data.getCurrentAudiobook().getPlaylist().get(nextPosition);
//			Data.setCurrentPosition(nextPosition);
//			Data.setCurrentTrack(nextTrack);
//			//Fix view
//			displayTracks();
//			cover_btn.setImageDrawable(drw_play_on_cover);
//
//			player.reload();
//			break;
//
//		case R.id.track_previous:
//			if(player == null) return;
//			//if currently playing the first track - do nothing
//			if(Data.getCurrentAudiobook().getPlaylist().getFirst().equals(Data.getCurrentTrack())) return;
//
//			int previousPosition = Data.getCurrentPosition()-1;
//			Track previousTrack = Data.getCurrentAudiobook().getPlaylist().get(previousPosition);
//			Data.setCurrentPosition(previousPosition);
//			Data.setCurrentTrack(previousTrack);
//			//Fix view
//			displayTracks();
//			cover_btn.setImageDrawable(drw_play_on_cover);
//
//			player.reload();
//			break;
//
//		case CELL:
//			if(player == null) return;
//			if(v.getTag() == null) return;
//			try{
//				int i = ((Integer)v.getTag()).intValue();
//				if(i >= 0 && i < Data.getCurrentAudiobook().getPlaylist().size()){
//					Data.setCurrentPosition(i);
//					Data.setCurrentTrack(Data.getCurrentAudiobook().getPlaylist().get(Data.getCurrentPosition()));
//					//Fix view
//					displayTracks();
//					cover_btn.setImageDrawable(drw_play_on_cover);
//
//					player.reload();
//					break;
//				}
//			} catch (Exception e) { break; }
//			break;
//
//			//Cases for seeker
//		case R.id.seeker_fast_forward:
//			if(player == null) return;
//			if(Data.getCurrentAudiobook() == null) return;
//			if(Data.getCurrentTrack() == null) return;
//			int ff_position = player.getCurrentProgress();
//			int ff_duration = player.getDuration();
//			int ff_newPos = 0;
//
//			ff_newPos = Math.min(ff_position + (60 * 1000), ff_duration);
//			if(ff_position == -1 || ff_duration == -1) return; 
//			player.seekTo(ff_newPos);
//			
//			str = Time.toString(ff_newPos);
//			duration = Data.getCurrentTrack().getDuration();
//			if(duration > 0) str += " / " + Time.toString(duration);
//			progress_tv.setText(str);
//			break;
//
//		case R.id.seeker_rewind:
//			if(player == null) return;
//			if(Data.getCurrentAudiobook() == null) return;
//			if(Data.getCurrentTrack() == null) return;
//			int rew_position = player.getCurrentProgress();
//			int rew_duration = player.getDuration();
//			int rew_newPos = 0;
//
//			rew_newPos = Math.max(rew_position - (60 * 1000), 0);
//			if(rew_position == -1 || rew_duration == -1) return; 
//			player.seekTo(rew_newPos);
//			
//			str = Time.toString(rew_newPos);
//			duration = Data.getCurrentTrack().getDuration();
//			if(duration > 0) str += " / " + Time.toString(duration);
//			progress_tv.setText(str);
//			break;
//
//		case R.id.seeker_progress_tv:
//			break;
//		}
//	}
//
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		switch (requestCode) {
//		case REQUEST_FRAGMENT_BASICS_EDIT:
//			Log.d(TAG, "onActivityResult - REQUEST_FRAGMENT_BASICS_EDIT");
//			if(resultCode == Activity.RESULT_OK){
//				Audiobook result = (Audiobook) data.getSerializableExtra("result");
//				Data.setCurrentAudiobook(result);
//				displayValues();
//			}
//		}
//	}
//
//	@Override
//	public void onStart(){
//		Log.d(TAG, "onStart");
//		super.onStart();
//		//Bind to PlayerService
//		Intent intent = new Intent(getActivity(), PlayerService.class);
//		getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);
//	}
//	@Override
//	public void onStop(){
//		Log.d(TAG, "onStop");
//		super.onStop();
//		//Unbind from PlayerService
//		if(bound){
//			getActivity().unbindService(this);
//			bound = false;
//		}
//	}
//
//	@Override
//	public void onServiceConnected(ComponentName name, IBinder service) {
//		Log.d(TAG, "onServiceConnected");
//		DAPBinder binder = (DAPBinder) service;
//		player = binder.getPlayerService();
//		bound = true;
//	}
//	@Override
//	public void onServiceDisconnected(ComponentName name) {
//		Log.d(TAG, "onServiceDisconnected");
//		bound = false;
//	}
//
//	class ControllerMonitor extends Monitor {
//		public ControllerMonitor(int delay, TimeUnit unit) {
//			super(delay, unit);
//		}
//
//		//		private static final String TAG = "ControllerActivity.Monitor";
//		private boolean isPlaying = false;
//
//		@Override
//		public void execute() {
//			isPlaying = player != null && player.isPlaying();
//
//			if(activity == null) return;
//			activity.runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					if(bound && Data.getCurrentAudiobook() != null){
//						cover_btn.setImageDrawable(isPlaying ? drw_pause_on_cover : drw_play_on_cover);
//					} else {
//						if(cover_btn != null) cover_btn.setImageDrawable(null);
//						if(cover_iv != null) cover_iv.setImageDrawable(noCover);
//					}
//					displayValues();
//					displayTracks();
//					displayProgress();
//				}
//			});
//		}
//	}
//	class Timer extends Monitor {
//		private long endTime;
//		private MenuItem item;
//		private String _delay;
//		
//		public Timer(int delay, TimeUnit unit, final MenuItem item) {
//			super(1, TimeUnit.SECONDS);
//			
//			switch(unit){
//			case MILLISECONDS: /*Do nothing*/ break;
//			case SECONDS: delay = delay * SEC; break;
//			case MINUTES: delay = delay * MIN; break;
//			case HOURS: delay = delay * HOUR; break;
//			case DAYS: delay = delay * DAY; break;
//			case MICROSECONDS: //fall through
//			case NANOSECONDS: throw new RuntimeException("Countdown must be miliseconds or more");
//			}
//			
//			endTime = System.currentTimeMillis() + delay;
//			
//			this.item = item;
//			_delay = Time.toString(delay);
//			getActivity().runOnUiThread(new Runnable() {
//				
//				@Override
//				public void run() {
//					item.setTitle(_delay);
//				}
//			});
//		}
//
//		@Override
//		public void execute() {
//			final long timeleft = endTime - System.currentTimeMillis();
//			getActivity().runOnUiThread(new Runnable() {
//				
//				@Override
//				public void run() {
//					item.setTitle(Time.toString(timeleft));
//				}
//			});
//			if(timeleft <= 0){
//				player.pause();
//				kill();
//			}
//		}
//		
//		@Override
//		public void kill(){
//			getActivity().runOnUiThread(new Runnable() {
//				
//				@Override
//				public void run() {
//					item.setTitle(_delay);
//				}
//			});
//			timerOn = false;
//			super.kill();
//		}
//	}
//
//}
