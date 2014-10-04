package rd.dap;

import rd.dap.PlayerService.DAPBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class ControllerActivity extends Activity implements ServiceConnection, OnClickListener {
	private boolean bound = false;
	private PlayerService player;
	private static Drawable noCover = null, drw_play = null, drw_pause = null;
	private ImageButton play_btn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controller);
		
		if(noCover == null || drw_play == null || drw_pause == null){
			noCover = getResources().getDrawable(R.drawable.ic_action_help);
			drw_play = getResources().getDrawable(R.drawable.ic_action_play);
			drw_pause = getResources().getDrawable(R.drawable.ic_action_pause);
		}

		play_btn = (ImageButton) findViewById(R.id.controller_play);
		play_btn.setImageDrawable(null);
		play_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.controller_play:
			if(MiniPlayer.audiobook != null){
				boolean isPlaying = false;
				if(player != null) isPlaying = player.isPlaying();
				play_btn.setImageDrawable(!isPlaying ? drw_pause : drw_play);
				//Toggle play/pause
				Intent intent = new Intent(this, PlayerService.class);
				intent.setAction(PlayerService.PLAY_PAUSE);
				startService(intent);
			} else
				Toast.makeText(this, "No audiobook", Toast.LENGTH_SHORT).show();
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


	class Monitor extends Thread {
		private static final String TAG = "ControllerActivity.Monitor";
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
				
				ControllerActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(bound && MiniPlayer.audiobook != null){
							play_btn.setImageDrawable(isPlaying ? drw_pause : drw_play);
						} else {
							play_btn.setImageDrawable(null);
						}
					}
				});
				
				//Delay with error handling
				try {
					Thread.sleep(MiniPlayer.DELAY);
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
