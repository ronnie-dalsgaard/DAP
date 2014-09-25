package rd.dap;

import java.io.File;

import rd.dap.PlayerService.DAPBinder;
import rd.dap.model.Audiobook;
import rd.dap.model.Track;
import android.app.Activity;
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
import android.widget.Toast;

public class MiniPlayer extends Fragment implements OnClickListener, OnLongClickListener, ServiceConnection {
	private static final String TAG = "MiniPlayer";
	private boolean bound = false;
	private PlayerService player;
	private TextView author_tv, album_tv, track_tv;
	private LinearLayout info;
	private ImageView iv;
	private ImageButton btn;
	private static Drawable noCover = null, drw_play = null, drw_pause = null;
	private static Audiobook audiobook;
	private static Track track;
	private Monitor monitor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		if(noCover == null){
			Resources res = getActivity().getResources();
			noCover = res.getDrawable(R.drawable.ic_action_help);
			drw_play = res.getDrawable(R.drawable.ic_action_play_over_video);
			drw_pause = res.getDrawable(R.drawable.ic_action_pause_over_video);
		}
		monitor = new Monitor();
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
		container = (ViewGroup) inflater.inflate(R.layout.mini_player, container, false);

		author_tv = ((TextView) container.findViewById(R.id.miniplayer_author_tv));
		album_tv = ((TextView) container.findViewById(R.id.miniplayer_album_tv));
		track_tv = ((TextView) container.findViewById(R.id.miniplayer_track_tv));
		iv = (ImageView) container.findViewById(R.id.miniplayer_cover_iv); 
		btn = (ImageButton) container.findViewById(R.id.miniplayer_play_btn);
		info = (LinearLayout) container.findViewById(R.id.miniplayer_info);

		btn.setOnClickListener(this);
		btn.setOnLongClickListener(this);

		info.setOnClickListener(this);

		updateView();

		Log.d(TAG, "View created");
		return container;
	}
	private void updateView(){
		if(audiobook == null || track == null){
			Log.d(TAG, "Unable to update view - no audiobook selected!");
			return;
		}
		author_tv.setText(audiobook.getAuthor());
		album_tv.setText(audiobook.getAlbum());
		track_tv.setText(track.getTitle());
		File cover = track.getCover();
		if(cover == null) cover = audiobook.getCover();
		if(cover != null) {
			Bitmap bitmap = BitmapFactory.decodeFile(cover.getPath());
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
	public void setAudiobook(Audiobook audiobook, Track track){
		//Instantiate data
		MiniPlayer.audiobook = audiobook;
		MiniPlayer.track = track;
		//Display data
		updateView(); //helper method
		//Handle state (pause if playing)
		boolean isPlaying = false;
		if(player != null) isPlaying = player.isPlaying();
		if(isPlaying){
			//Toggle play/pause
			Intent intent = new Intent(getActivity(), PlayerService.class);
			intent.setAction(PlayerService.PLAY_PAUSE);
			getActivity().startService(intent);

		}
		//Set data
		Intent intent = new Intent(getActivity(), PlayerService.class);
		intent.setAction(PlayerService.SET);
		intent.putExtra("audiobook", audiobook);
		getActivity().startService(intent);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.miniplayer_play_btn:
			Log.d(TAG, "-->Play/Pause clicked");
			if(audiobook == null) return;
			//Toggle button icon
			/* Note that the result of isPlaying is the state BEFORE the click,
			 * thus the result must be negated.
			 * The intent is send, but the service won't get processor time, 
			 * before this method is finished. So the result will always be wrong.
			 */
			boolean isPlaying = false;
			if(player != null) isPlaying = player.isPlaying();
			btn.setImageDrawable(!isPlaying ? drw_pause : drw_play);
			//Toggle play/pause
			Intent intent = new Intent(getActivity(), PlayerService.class);
			intent.setAction(PlayerService.PLAY_PAUSE);
			getActivity().startService(intent);
			break;
		case R.id.miniplayer_info:
			//FIXME remove - this is just for testing
			Log.d(TAG, "Info clicked");
			if(player != null){
				String txt = player.isPlaying() ? "is playing" : "is NOT playing";
				Toast.makeText(getActivity(), txt, Toast.LENGTH_SHORT).show();
				Log.d(TAG, txt);
			} else {
				Log.d(TAG, "PLAYER IS NULL");
			}
		}

	}
	@Override
	public boolean onLongClick(View v) {
		switch(v.getId()){
		case R.id.miniplayer_play_btn:
			Log.d(TAG, "Play/Pause long pressed (Kill mp)");
			Intent intent = new Intent(getActivity(), PlayerService.class);
			intent.setAction(PlayerService.KILL);
			getActivity().startService(intent);
			break;
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

	class Monitor extends Thread {
		private static final String TAG = "MiniPlayer.Monitor";
		private boolean alive = true;
		private boolean isPlaying = false;
		
		public void kill(){
			this.alive = false;
		}

		@Override
		public void run() {
			int insomnicEpisodes = 0;
			long t0 = System.currentTimeMillis();
			double errorFrequency = 0.0;
			while(alive){
				if(player != null) isPlaying = player.isPlaying();
				else Log.d(TAG, "player is null");
				
				Activity a = MiniPlayer.this.getActivity();
				a.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(bound && audiobook != null){
							btn.setImageDrawable(isPlaying ? drw_pause : drw_play);
						} else {
							btn.setImageDrawable(null);
						}
					}
				});
				
				//Delay with error handling
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					//Ignored - just insomnia
					insomnicEpisodes++;
					long t = System.currentTimeMillis();
					//error / sec.
					errorFrequency = insomnicEpisodes / ((t - t0) / 1000);
					Log.d(TAG, "insomnia frequency="+errorFrequency);
					if(errorFrequency > 1.0){
						System.err.println("Too insomnic! - system exited");
						System.exit(-1);
					}
				}
			}
		}
	}
}