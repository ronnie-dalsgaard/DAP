package rd.dap.fragments;

import static rd.dap.PlayerService.audiobook;
import static rd.dap.PlayerService.position;
import static rd.dap.PlayerService.track;

import java.io.File;

import rd.dap.PlayerService;
import rd.dap.PlayerService.DAPBinder;
import rd.dap.R;
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
import android.widget.Toast;

public class MiniPlayer extends Fragment implements OnClickListener, OnLongClickListener, ServiceConnection {
	private static final String TAG = "MiniPlayer";
	private boolean bound = false;
	private PlayerService player;
	private TextView author_tv, album_tv, track_tv, progress_tv;
	private LinearLayout info;
	private ImageView iv;
	private ImageButton btn;
	private static Drawable noCover = null, drw_play = null, drw_pause = null;
	private MiniPlayerMonitor monitor;

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

		btn.setOnClickListener(this);
		btn.setOnLongClickListener(this);

		info.setOnClickListener(this);

		updateView();

		Log.d(TAG, "View created");
		return v;
	}
	public void updateView(){
		if(audiobook == null || track == null){
			Log.d(TAG, "Unable to update view - no audiobook selected!");
			return;
		}
		author_tv.setText(audiobook.getAuthor());
		album_tv.setText(audiobook.getAlbum());
		track_tv.setText(String.format("%02d", position) + " " + track.getTitle());
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
	
	public void reload(){
		player.reload();
	}

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
			player.kill();
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
					if(bound && audiobook != null){
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