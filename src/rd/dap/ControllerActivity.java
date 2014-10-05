package rd.dap;

import static rd.dap.PlayerService.audiobook;
import static rd.dap.PlayerService.track;

import java.io.File;

import rd.dap.PlayerService.DAPBinder;
import rd.dap.support.Monitor;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ControllerActivity extends Activity implements ServiceConnection, OnClickListener {
	private boolean bound = false;
	private PlayerService player;
	private static Drawable noCover = null, drw_play = null, drw_pause = null,
			drw_play_on_cover = null, drw_pause_on_cover;
	private ControllerMonitor monitor;
	private ImageView cover_iv;
	private TextView author_tv, album_tv;
	private ImageButton cover_btn, play_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controller);

		if(noCover == null || drw_play == null || drw_pause == null){
			noCover = getResources().getDrawable(R.drawable.ic_action_help);
			drw_play = getResources().getDrawable(R.drawable.ic_action_play);
			drw_pause = getResources().getDrawable(R.drawable.ic_action_pause);
			drw_play_on_cover = getResources().getDrawable(R.drawable.ic_action_play_over_video);
			drw_pause_on_cover = getResources().getDrawable(R.drawable.ic_action_pause_over_video);
		}

		cover_iv = (ImageView) findViewById(R.id.controller_cover_iv);
		author_tv = (TextView) findViewById(R.id.controller_author_tv);
		album_tv = (TextView) findViewById(R.id.controller_album_tv);

		if(audiobook != null){
			//Cover
			File cover = track.getCover();
			if(cover == null) cover = audiobook.getCover();
			if(cover != null) {
				Bitmap bitmap = BitmapFactory.decodeFile(cover.getPath());
				cover_iv.setImageBitmap(bitmap);
			} else {
				cover_iv.setImageDrawable(noCover);
			}

			//Author
			author_tv.setText(audiobook.getAuthor());

			//Album
			album_tv.setText(audiobook.getAlbum());
		}
		cover_btn = (ImageButton) findViewById(R.id.controller_cover_btn);
		cover_btn.setOnClickListener(this);

		play_btn = (ImageButton) findViewById(R.id.controller_play);
		play_btn.setImageDrawable(null);
		play_btn.setOnClickListener(this);

		monitor = new ControllerMonitor();
		monitor.start();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.controller_cover_btn:
		case R.id.controller_play:
			if(audiobook == null) break;
			boolean isPlaying = false;
			if(player != null) isPlaying = player.isPlaying();
			//Fix view
			play_btn.setImageDrawable(!isPlaying ? drw_pause : drw_play);
			cover_btn.setImageDrawable(!isPlaying ? drw_pause_on_cover : drw_play_on_cover);
			//Toggle play/pause
			player.toggle();
			break;
		}
	}

	@Override
	public void onStart(){
		super.onStart();
		//Bind to PlayerService
		Intent intent = new Intent(this, PlayerService.class);
		bindService(intent, this, Context.BIND_AUTO_CREATE);	
	}
	@Override
	public void onStop(){
		super.onStop();
		//Unbind from PlayerService
		if(bound){
			this.unbindService(this);
			bound = false;
		}
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

	class ControllerMonitor extends Monitor {
		private static final String TAG = "ControllerActivity.Monitor";
		private boolean isPlaying = false;

		@Override
		public void execute() {
			if(player != null) isPlaying = player.isPlaying();
			else Log.d(TAG, "player is null");

			ControllerActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(bound && audiobook != null){
						play_btn.setImageDrawable(isPlaying ? drw_pause : drw_play);
						cover_btn.setImageDrawable(isPlaying ? drw_pause_on_cover : drw_play_on_cover);
					} else {
						play_btn.setImageDrawable(null);
						cover_btn.setImageDrawable(null);
					}
				}
			});
		}
	}
}
