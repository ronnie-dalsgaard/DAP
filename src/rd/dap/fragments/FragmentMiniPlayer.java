package rd.dap.fragments;

import java.util.ArrayList;

import rd.dap.ControllerActivity;
import rd.dap.PlayerService;
import rd.dap.PlayerService.DAPBinder;
import rd.dap.R;
import rd.dap.model.Data;
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
import android.widget.LinearLayout;
import android.widget.TextView;

public class FragmentMiniPlayer extends Fragment implements OnClickListener, OnLongClickListener, ServiceConnection {
	private static final String TAG = "MiniPlayer";
	private boolean bound = false;
	private PlayerService player;
	private TextView author_tv, album_tv, track_tv, progress_tv;
	private LinearLayout info, miniplayer_layout;
	private ImageView iv;
	private ImageButton btn;
	private static Drawable noCover = null, drw_play = null, drw_pause = null;
	private MiniPlayerMonitor monitor;
	
	//Observer pattern - Miniplayer is observable
	private ArrayList<MiniPlayerObserver> observers = new ArrayList<MiniPlayerObserver>();
	public interface MiniPlayerObserver{
		public void miniplayer_play();
		public void miniplayer_pause();
		public void miniplayer_click();
		public void miniplayer_longClick();
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
		monitor = new MiniPlayerMonitor();
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
		View v = (ViewGroup) inflater.inflate(R.layout.mini_player, container, false);

		author_tv = ((TextView) v.findViewById(R.id.miniplayer_author_tv));
		album_tv = ((TextView) v.findViewById(R.id.miniplayer_album_tv));
		track_tv = ((TextView) v.findViewById(R.id.miniplayer_track_tv));
		iv = (ImageView) v.findViewById(R.id.miniplayer_cover_iv); 
		btn = (ImageButton) v.findViewById(R.id.miniplayer_play_btn);
		info = (LinearLayout) v.findViewById(R.id.miniplayer_info);
		progress_tv = (TextView) v.findViewById(R.id.miniplayer_progress_tv);
		miniplayer_layout = (LinearLayout) v.findViewById(R.id.miniplayer_layout);

		btn.setOnClickListener(this);
		btn.setOnLongClickListener(this);

		info.setOnClickListener(this);
		info.setOnLongClickListener(this);

		updateView();

		Log.d(TAG, "View created");
		return v;
	}
	
	public void updateView(){
		if(miniplayer_layout != null){
			miniplayer_layout.setVisibility(Data.getAudiobook() == null ? View.GONE : View.VISIBLE);
		}
		if(Data.getAudiobook() == null || Data.getTrack() == null){
			Log.d(TAG, "Unable to update view - no audiobook selected!");
			return;
		}
		author_tv.setText(Data.getAudiobook().getAuthor());
		album_tv.setText(Data.getAudiobook().getAlbum());
		track_tv.setText(String.format("%02d", Data.getPosition()+1) + " " + Data.getTrack().getTitle());
		String cover = Data.getTrack().getCover();
		if(cover == null) cover = Data.getAudiobook().getCover();
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
		player.reload();
	}
	public void setVisibility(int visibility){
		miniplayer_layout.setVisibility(visibility);
	}
	public PlayerService getPlayer(){ return player; } //Convenience method
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.miniplayer_play_btn:
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

		case R.id.miniplayer_info:
			Log.d(TAG, "Info clicked");
			if(player != null){
				for(MiniPlayerObserver observer : observers){
					observer.miniplayer_click();
				}
				
				Intent intent = new Intent(getActivity(), ControllerActivity.class);
				startActivity(intent);
			} else {
				Log.d(TAG, "PLAYER IS NULL");
			}
		}

	}
	@Override
	public boolean onLongClick(View v) {
		switch(v.getId()){
		case R.id.miniplayer_play_btn:
			player.kill();
			Log.d(TAG, "Cover long clicked - Play/Pause long pressed (Kill mp)");

			for(MiniPlayerObserver observer : observers){
				observer.miniplayer_longClick();
			}
			break;
		case R.id.miniplayer_info:
			Log.d(TAG, "Info long clicked - Audiobook un-selected");
			Data.setAudiobook(null);
			Data.setTrack(null);
			reload();
			updateView();

//			for(MiniPlayerObserver observer : observers){
//				observer.miniplayer_longClick();
//			}
			return true;
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
					if(bound && Data.getAudiobook() != null){
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