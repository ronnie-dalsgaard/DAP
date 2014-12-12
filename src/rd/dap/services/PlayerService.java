package rd.dap.services;

import java.io.File;
import java.util.ArrayList;

import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Track;
import rd.dap.support.Monitor;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class PlayerService extends Service implements OnErrorListener, OnCompletionListener {
	private static final String TAG = "PlayerService";
	private static MediaPlayer mp = null;
	private final IBinder binder = new DAPBinder();
	private long laststart = 0;
	private Audiobook audiobook;
	private int trackno;

	//Observer pattern - Miniplayer is observable
	private ArrayList<PlayerObserver> observers = new ArrayList<PlayerObserver>();
	public interface PlayerObserver{
		public void set(Audiobook audiobook, int trackno, int progress);
		public void play();
		public void pause();
		public void next(Audiobook audiobook, Track track, int trackno);
		public void prev(Audiobook audiobook, Track track, int trackno);
		public void seek(Track track);
		public void complete(Audiobook audiobook, int new_trackno);
		public void updateBookmark(String author, String album, int trackno, int progress);
	}
	public void addObserver(PlayerObserver observer) { observers.add(observer); }

	@Override
	public void onCreate(){
		Log.d(TAG, "onCreate");
	}
	@Override
	public void onDestroy(){
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		kill();
	}

	public void set(Audiobook audiobook, int trackno, int progress){
		Log.d(TAG, "set");
		if(audiobook == null) return;
		this.audiobook = audiobook;
		this.trackno = trackno;
		Track track = audiobook.getPlaylist().get(trackno);
		
		System.out.println("--- Track paht: "+track.getPath());
		
		if(mp != null){
			mp.release();
			mp = null;
		}

		mp = MediaPlayer.create(this, Uri.fromFile(new File(track.getPath())));
		track.setDuration(mp.getDuration());
		AudiobookManager.getInstance().saveAudiobooks(this);
		mp.seekTo(progress);
		mp.setOnCompletionListener(this);
		for(PlayerObserver obs : observers) { obs.set(audiobook, trackno, progress); }
	}
	public Audiobook getAudiobook() { return audiobook; }
	public int getTrackno() { return trackno; }
	public int getCurrentProgress(){
		if(mp == null) return -1;
		try{
			return mp.getCurrentPosition();			
		}catch (IllegalStateException e){
			return -1;
		}
	}
	public void toggle(){
		if(audiobook == null) return;
		if(mp == null) return;
		
		if(mp.isPlaying()) {
			mp.pause();
			for(PlayerObserver obs : observers) { obs.pause(); }
		} else {
			mp.start();
			for(PlayerObserver obs : observers) { obs.play(); }
		}
		
	}
	public void play(){
		if(audiobook == null) return;
		if(mp == null) return;
		if(!mp.isPlaying()) {
			mp.start();
			for(PlayerObserver obs : observers) { obs.play(); }
		}
	}
	public void pause(){
		if(mp != null){
			if(mp.isPlaying()) mp.pause();
			for(PlayerObserver obs : observers) { obs.pause(); }
		}
	}
	public int next(){
		if(audiobook == null) return -1;
		Track track = audiobook.getPlaylist().get(trackno);
		if(track == null) return -1;		
		
		if(audiobook.getPlaylist().getLast().equals(track)) return 0;
		
		trackno++;
		Track new_track = audiobook.getPlaylist().get(trackno);
		int progress = 0;
		set(audiobook, trackno, progress);
		for(PlayerObserver obs : observers) { obs.next(audiobook, new_track, trackno); }
		return trackno;
	}
	public void prev(){
		if(audiobook == null) return;
		Track track = audiobook.getPlaylist().get(trackno);
		if(track == null) return;		
		
		if(audiobook.getPlaylist().getFirst().equals(track)) return;
		
		trackno--;
		Track new_track = audiobook.getPlaylist().get(trackno);
		int progress = 0;
		set(audiobook, trackno, progress);
		for(PlayerObserver obs : observers) { obs.next(audiobook, new_track, trackno); }
	}

	public int getDuration(){
		if(mp == null) return -1;
		try{
			return mp.getDuration();
		} catch (IllegalStateException e){
			return -1;
		}
	}
	public void seekTo(int progress){
		if(mp == null) return;
		mp.seekTo(progress);
		Track track = audiobook.getPlaylist().get(trackno);
		for(PlayerObserver obs : observers) { obs.seek(track); }
	}
	public void reset(){
		if (mp != null) {
			mp.release();
			mp = null;
		}
		
		audiobook = null;
		trackno = 0;
	}
	public boolean isPlaying(){ 
		if(mp == null) {
			return false;
		}
		if(System.currentTimeMillis() - laststart < Monitor.DEFAULT_DELAY * 1.5){
			return true;
		}
		try{
			return mp.isPlaying();
		} catch(IllegalStateException e){
			return false;
		}
	}
	public void kill(){
		Log.d(TAG, "kill");
		if(mp != null){
			mp.release();
			mp = null;
		}
		stopSelf();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		int new_track = next();
		play();
		
		for(PlayerObserver obs : observers) { obs.complete(audiobook, new_track); }
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
		Log.e(TAG, "An error occured in the MediaPlayer and" +
				" was cought by PlayerSerice: what="+what+", extra="+extra);
		return true; //Keep going
	}
	
}
