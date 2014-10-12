package rd.dap;

import java.io.File;

import rd.dap.model.Audiobook;
import rd.dap.model.Track;
import rd.dap.support.Monitor;
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
	private static MediaPlayer mp = null;
	public static Audiobook audiobook;
	public static int position;
	public static Track track;

	private final IBinder binder = new DAPBinder();
	private long laststart = 0;
		
	public void toggle(){
		if(audiobook == null) return;
		if(track == null) return;
		if(position < 0) return;
		if(mp == null){
			mp = MediaPlayer.create(this, Uri.fromFile(new File(track.getPath())));
		}
		if(mp.isPlaying()) mp.pause();
		else mp.start();
	}
	public void play(){
		if(audiobook == null) return;
		if(track == null) return;
		if(position < 0) return;
		if(mp == null){
			mp = MediaPlayer.create(this, Uri.fromFile(new File(track.getPath())));
		}
		if(!mp.isPlaying()) mp.start();
	}
	public void pause(){
		if(mp != null){
			mp.pause();
		}
	}
	public int getDuration(){
		if(mp == null) return -1;
		try{
			return mp.getDuration();
		} catch (IllegalStateException e){
			return -1;
		}
	}
	public int getCurrentProgress(){
		if(mp == null) return -1;
		try{
			return mp.getCurrentPosition();			
		}catch (IllegalStateException e){
			return -1;
		}
	}
	public void seekTo(int position){
		if(mp == null) return;
		mp.seekTo(position);
	}
	public void reset(){
		if (mp != null) {
			mp.release();
			mp = null;
		}
		audiobook = null;
		track = null;
		position = -1;
	}
	public void reload(){
		if(mp != null){
			mp.release();
		} 
		if(track == null) return;
		mp = MediaPlayer.create(this, Uri.fromFile(new File(track.getPath())));		
	}
	public boolean isPlaying(){ 
		if(mp == null) {
			return false;
		}
		if(System.currentTimeMillis() - laststart < Monitor.DELAY * 1.5){
			return true;
		}
		try{
			return mp.isPlaying();
		} catch(IllegalStateException e){
			Log.d(TAG, "exception occured -> isPlaying is false");
			return false;
		}
	}
	public void kill(){
		Log.d(TAG, "DIE");
		mp.release();
		mp = null;
		audiobook = null;
		stopSelf();
	}

	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return binder;
	}
	public class DAPBinder extends Binder{
		public PlayerService getPlayerService(){
			return PlayerService.this;
		}
	}


	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		System.out.println("An error occured in the MediaPlayer and" +
				" was cought by PlayerSerice: what="+what+", extra="+extra);
		return true; //Keep going
	}
}
