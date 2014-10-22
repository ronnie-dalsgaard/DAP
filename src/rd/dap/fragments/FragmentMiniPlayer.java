package rd.dap.fragments;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rd.dap.R;
import rd.dap.model.Data;
import rd.dap.services.PlayerService;
import rd.dap.services.PlayerService.DAPBinder;
import rd.dap.support.Monitor;
import rd.dap.support.Time;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FragmentMiniPlayer extends Fragment implements OnClickListener, OnLongClickListener, ServiceConnection {
	private static final String TAG = "MiniPlayer";
	private boolean bound = false;
	private PlayerService player;
	private TextView author_tv, album_tv, track_tv, progress_tv;
	private RelativeLayout info, bookmark_layout;
	private ImageView iv;
	private ImageButton btn;
	private static Drawable noCover = null, drw_play = null, drw_pause = null;
	private MiniPlayerMonitor monitor;
	private static final int COVER_BTN_ID = 22223;
	
	//Observer pattern - Miniplayer is observable
	private ArrayList<MiniPlayerObserver> observers = new ArrayList<MiniPlayerObserver>();
	public interface MiniPlayerObserver{
		public void miniplayer_play();
		public void miniplayer_pause();
		public void miniplayer_click();
		public void miniplayer_longClick();
		public void miniplayer_seekTo(int progress);
	}
	public void addObserver(MiniPlayerObserver observer) { observers.add(observer); }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		if(noCover == null || drw_play == null || drw_pause == null){
			Resources res = getActivity().getResources();
			noCover = res.getDrawable(R.drawable.ic_action_help);
			drw_play = res.getDrawable(R.drawable.ic_action_play_over_video);
			drw_pause = res.getDrawable(R.drawable.ic_action_pause_over_video);
		}
		monitor = new MiniPlayerMonitor(1, TimeUnit.SECONDS);
		monitor.start();
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(monitor != null){
			monitor.kill();
			monitor = null;
		}
	}

	@Override
	public void onStart(){
		Log.d(TAG, "onStart");
		super.onStart();
		//Bind to PlayerService
		Intent intent = new Intent(getActivity(), PlayerService.class);
		getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);	
	}
	@Override
	public void onStop(){
		Log.d(TAG, "onStop");
		super.onStop();
		//Unbind from PlayerService
		if(bound){
			getActivity().unbindService(this);
			bound = false;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View v = (ViewGroup) inflater.inflate(R.layout.bookmark_item, container, false);
//		View v = (ViewGroup) inflater.inflate(R.layout.mini_player, container, false);

		author_tv = ((TextView) v.findViewById(R.id.bookmark_author_tv));
		album_tv = ((TextView) v.findViewById(R.id.bookmark_album_tv));
		track_tv = ((TextView) v.findViewById(R.id.bookmark_track_tv));
		iv = (ImageView) v.findViewById(R.id.bookmark_cover_iv); 
		info = (RelativeLayout) v.findViewById(R.id.bookmark_info_layout);
		progress_tv = (TextView) v.findViewById(R.id.bookmark_progress_tv);
		bookmark_layout = (RelativeLayout) v.findViewById(R.id.bookmark_layout);
		RelativeLayout cover_layout = (RelativeLayout) v.findViewById(R.id.bookmark_cover_layout);
		
		bookmark_layout.setBackground(getActivity().getResources().getDrawable(R.drawable.miniplayer_bg));
		
		int width = (int) getActivity().getResources().getDimension(R.dimen.cover_width);
		int height = (int) getActivity().getResources().getDimension(R.dimen.cover_height);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		btn = new ImageButton(getActivity());
		btn.setId(COVER_BTN_ID);
		btn.setBackgroundColor(getActivity().getResources().getColor(R.color.transparent));
		cover_layout.addView(btn, params);
		
		btn.setOnClickListener(this);
		btn.setOnLongClickListener(this);

		info.setOnClickListener(this);
		info.setOnLongClickListener(this);

		updateView();

		Log.d(TAG, "View created");
		return v;
	}
	
	public void updateView(){
		if(bookmark_layout != null){
			bookmark_layout.setVisibility(Data.getCurrentAudiobook() == null ? View.GONE : View.VISIBLE);
		}
		if(Data.getCurrentAudiobook() == null || Data.getCurrentTrack() == null){
			Log.d(TAG, "Unable to update view - no audiobook selected!");
			return;
		}
		author_tv.setText(Data.getCurrentAudiobook().getAuthor());
		album_tv.setText(Data.getCurrentAudiobook().getAlbum());
		String title = Data.getCurrentTrack().getTitle();
		if(title.length() > 28) title = title.substring(0, 25) + "...";
		track_tv.setText(String.format("%02d", Data.getCurentPosition()+1) + " " + title);
		String cover = Data.getCurrentTrack().getCover();
		if(cover == null) cover = Data.getCurrentAudiobook().getCover();
		if(cover != null) {
			Bitmap bitmap = BitmapFactory.decodeFile(cover);
			iv.setImageBitmap(bitmap);
		} else {
			iv.setImageDrawable(noCover);
		}
		
		//Handle state (pause if playing)
		boolean isPlaying = false;
		if(player != null) {
			isPlaying = player.isPlaying();
			btn.setImageDrawable(isPlaying ? drw_pause : drw_play);
		} else {
			Log.d(TAG, "player is null");
			btn.setImageDrawable(null);
		}
	}
	public void reload(){
		if(player == null) return;
		player.reload();
	}
	public void seekTo(int progress){
		if(player == null) return;
		player.seekTo(progress);
		for(MiniPlayerObserver observer : observers){
			observer.miniplayer_seekTo(progress);
		}
	}
	public void setVisibility(int visibility){
		bookmark_layout.setVisibility(visibility);
	}
	public PlayerService getPlayer(){ return player; } //Convenience method
	
	@Override
 	public void onClick(View v) {
		switch(v.getId()){
		case R.id.bookmark_info_layout:
		case COVER_BTN_ID: //id is set programmativcally
			//Toggle button icon
			/* Note that the result of isPlaying is the state BEFORE the click,
			 * thus the result must be negated.
			 * The intent is send, but the service won't get processor time, 
			 * before this method is finished. So the result will always be wrong.
			 */
			boolean isPlaying = false;
			if(player != null) isPlaying = player.isPlaying();
			btn.setImageDrawable(!isPlaying ? drw_pause : drw_play);
			player.toggle();
			
			for(MiniPlayerObserver observer : observers){
				if(!isPlaying) observer.miniplayer_play();
				else observer.miniplayer_pause();
			}
			break;

		}

	}
	@Override
	public boolean onLongClick(View v) {
		switch(v.getId()){
		
		}

		return true;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		DAPBinder binder = (DAPBinder) service;
		player = binder.getPlayerService();
		bound = true;
	}
	@Override
	public void onServiceDisconnected(ComponentName name) {
		bound = false;
	}

	class MiniPlayerMonitor extends Monitor {
		private static final String TAG = "MiniPlayer.Monitor";
		private boolean isPlaying = false;
		private int progress = 0;

		public MiniPlayerMonitor(int delay, TimeUnit unit) {
			super(delay, unit);
		}
		
		@Override
		public void execute() {
			if(player != null) {
				isPlaying = player.isPlaying();
				progress = player.getCurrentProgress();
			}
			else Log.d(TAG, "player is null");
			
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(bound && Data.getCurrentAudiobook() != null){
						btn.setImageDrawable(isPlaying ? drw_pause : drw_play);
					} else {
						btn.setImageDrawable(null);
					}
					progress_tv.setText(Time.toString(progress));
				}
			});
		}
	}
}