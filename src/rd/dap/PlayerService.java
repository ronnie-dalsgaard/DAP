package rd.dap;

import java.io.File;

import rd.dap.model.Audiobook;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class PlayerService extends Service implements OnErrorListener {
	private static final String TAG = "PlayerService";
	public static final String PLAY_PAUSE = "play_pause", KILL = "kill", SET = "set", GET = "get";
	public static final int NO_AUDIOBOOK = 1;
	
	private final IBinder binder = new DAPBinder();
	private MediaPlayer mp = null;
	private Audiobook audiobook = null;
	private long laststart = 0;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "onStartCommand");

		String action = intent.getAction();

		switch (action) {
		case PLAY_PAUSE:
			if (audiobook == null) {
				stopSelf();
				return NO_AUDIOBOOK;
			}
			if (mp == null) {
				File file = audiobook.getPlaylist().get(0).getFile();
				mp = MediaPlayer.create(this, Uri.fromFile(file));
				mp.setOnErrorListener(this);
				mp.start();
				laststart = System.currentTimeMillis();
			} else {
				laststart = 0;
				try {
					if (mp.isPlaying()){
						mp.pause();						
					} else {
						mp.start();
					}
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case SET:
			if (intent.hasExtra("audiobook")) {
				if (mp != null) {
					mp.release();
					mp = null;
				}
				audiobook = (Audiobook) intent.getSerializableExtra("audiobook");
			}
			break;
		case KILL:
		default:
			Log.d(TAG, "DIE");
			mp.release();
			mp = null;
			audiobook = null;
			stopSelf();
			break;
		}
		return 0;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return binder;
	}
	
	class DAPBinder extends Binder{
		public PlayerService getPlayerService(){
			return PlayerService.this;
		}
	}

	public boolean isPlaying(){ 
		if(mp == null) return false;
		if(System.currentTimeMillis() - laststart < 2000){ //FIXME should be monitor delay * 1.5
			return true;
		}
		return mp.isPlaying(); 
	}

	
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		System.out.println("An error occured in the MediaPlayer and" +
				" was cought by PlayerSerice: what="+what+", extra="+extra);
		return true; //Keep going
	}
}
