package rd.dap.services;

import java.io.File;
import java.util.ArrayList;

import rd.dap.model.Audiobook;
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
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PlayerService extends Service implements OnErrorListener, OnCompletionListener {
	private static final String TAG = "PlayerService";
	private static MediaPlayer mp = null;
	private final IBinder binder = new DAPBinder();
	private long laststart = 0;
	private Audiobook audiobook;
	private int trackno;
	private PhoneStateListener phoneStateListener;

	//Observer pattern - Miniplayer is observable
	private ArrayList<PlayerObserver> observers = new ArrayList<PlayerObserver>();
	public interface PlayerObserver{
		public void onSetAudiobook(Audiobook audiobook);
		public void onSetBookmark(Audiobook audiobook, int trackno, int progress);
		public void onPlayAudiobook();
		public void onPauseAudiobook();
		public void onNext(Audiobook audiobook, int new_trackno);
		public void onPrev(Audiobook audiobook, int new_trackno);
		public void onForward(Audiobook audiobook, int trackno, int new_progress);
		public void onRewind(Audiobook audiobook, int trackno, int new_progress);
		public void onSelectTrack(Audiobook audiobook, int new_trackno);
		public void onSeekProgress(Audiobook audiobook, int trackno, int new_progress);
		public void onSeekTrack(Audiobook audiobook, int new_trackno);
		public void onUndo(Audiobook audiobook, int new_trackno, int new_progress);
		public void onComplete(Audiobook audiobook, int new_trackno);
		public void onUpdateBookmark(String author, String album, int trackno, int progress);
	}
	public void addObserver(PlayerObserver observer) { observers.add(observer); }

	@Override
	public void onCreate(){
		Log.d(TAG, "onCreate");
		phoneStateListener = new PhoneStateListener() {
		    @Override
		    public void onCallStateChanged(int state, String incomingNumber) {
		    	boolean wasPlaying = false;
		    	switch(state){
		    	case TelephonyManager.CALL_STATE_RINGING: 
		            PlayerService.this.pause();
		            wasPlaying = true;
		    		break;
		    	case TelephonyManager.CALL_STATE_IDLE:
		    		if(wasPlaying) PlayerService.this.play();
		    		break;
		        }
		        super.onCallStateChanged(state, incomingNumber);
		    }
		};
		TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if(mgr != null) {
		    mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		}
	}
	@Override
	public void onDestroy(){
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if(mgr != null) {
		    mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
		kill();
	}

	private void set(Audiobook audiobook, int trackno, int progress){
		Log.d(TAG, "set");
		if(audiobook == null) return;
		this.audiobook = audiobook;
		this.trackno = trackno;
		Track track = audiobook.getPlaylist().get(trackno);
		
		if(mp != null){ mp.release(); mp = null; }
		mp = MediaPlayer.create(this, Uri.fromFile(new File(track.getPath())));
		track.setDuration(mp.getDuration());
		mp.seekTo(progress);
		mp.setOnCompletionListener(this);
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
	
	public void setAudiobook(Audiobook audiobook){
		set(audiobook, 0, 0);
		for(PlayerObserver obs : observers) { obs.onSetAudiobook(audiobook); }
	}
	public void setAudiobook(Audiobook audiobook, int trackno, int progress){
		set(audiobook, trackno, progress);
		for(PlayerObserver obs : observers) { obs.onSetBookmark(audiobook, trackno, progress); }
	}
	public void toggle(){
		if(audiobook == null) return;
		if(mp == null) return;
		
		if(mp.isPlaying()) {
			mp.pause();
			for(PlayerObserver obs : observers) { obs.onPauseAudiobook(); }
		} else {
			mp.start();
			for(PlayerObserver obs : observers) { obs.onPlayAudiobook(); }
		}
		
	}
	public void play(){
		if(audiobook == null) return;
		if(mp == null) return;
		if(!mp.isPlaying()) {
			mp.start();
			for(PlayerObserver obs : observers) { obs.onPlayAudiobook(); }
		}
	}
	public void pause(){
		if(mp != null){
			if(mp.isPlaying()) mp.pause();
			for(PlayerObserver obs : observers) { obs.onPauseAudiobook(); }
		}
	}
	public void next(){
		if(audiobook == null) return;
		Track track = audiobook.getPlaylist().get(trackno);
		if(track == null) return;		
		if(audiobook.getPlaylist().getLast().equals(track)) return;
		
		trackno++;
		int progress = 0;
		set(audiobook, trackno, progress);
		for(PlayerObserver obs : observers) { obs.onNext(audiobook, trackno); }
	}
	public void prev(){
		if(audiobook == null) return;
		Track track = audiobook.getPlaylist().get(trackno);
		if(track == null) return;		
		if(audiobook.getPlaylist().getFirst().equals(track)) return;
		
		trackno--;
		int progress = 0;
		set(audiobook, trackno, progress);
		for(PlayerObserver obs : observers) { obs.onPrev(audiobook, trackno); }
	}
	public void forward(int millis) {
		if(mp == null) return;
		int progress = mp.getCurrentPosition() + millis;
		int duration = mp.getDuration();
		int new_progress = Math.min(progress, duration);
		mp.seekTo(new_progress);
		for(PlayerObserver obs : observers) { obs.onForward(audiobook, trackno, new_progress); }
	}
	public void rewind(int millis) {
		if(mp == null) return;
		int progress = mp.getCurrentPosition() - millis;
		int new_progress = Math.max(progress, 0);
		mp.seekTo(new_progress);
		for(PlayerObserver obs : observers) { obs.onRewind(audiobook, trackno, new_progress); }
	}
	public void selectTrack(int new_trackno){
		if(mp == null) return;
		set(audiobook, new_trackno, 0);
		for(PlayerObserver obs : observers) { obs.onSelectTrack(audiobook, new_trackno); }
	}
	public void seekProgressTo(int new_progress){
		if(mp == null) return;
		mp.seekTo(new_progress);
		for(PlayerObserver obs : observers) { obs.onSeekProgress(audiobook, trackno, new_progress); }
	}
	public void undoTo(int new_trackno, int new_progress){
		if(mp == null) return;
		set(audiobook, new_trackno, new_progress);
		for(PlayerObserver obs : observers) { obs.onUndo(audiobook, new_trackno, new_progress); }
	}
	public void seekTrackTo(int trackno){
		if(audiobook == null) return;
		Track track = audiobook.getPlaylist().get(trackno);
		if(track == null) return;		
		
		int progress = 0;
		set(audiobook, trackno, progress);
		for(PlayerObserver obs : observers) { obs.onNext(audiobook, trackno); }
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		if((trackno+1) >= audiobook.getPlaylist().size()){
			trackno = 0;
			set(audiobook, trackno, 0);
			for(PlayerObserver obs : observers) { obs.onComplete(audiobook, 0); }
		} else {
			trackno++;
			set(audiobook, trackno, 0);
			PlayerService.mp.start();
			for(PlayerObserver obs : observers) { obs.onComplete(audiobook, trackno); }
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
	public void reset(){
		if (mp != null) {
			mp.release();
			mp = null;
		}
		
		audiobook = null;
		trackno = 0;
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
