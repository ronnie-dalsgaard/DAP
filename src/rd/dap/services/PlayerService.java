package rd.dap.services;

import java.io.File;
import java.util.concurrent.TimeUnit;

import rd.dap.events.Event;
import rd.dap.events.Event.Type;
import rd.dap.events.EventBus;
import rd.dap.events.Subscriber;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkEvent;
import rd.dap.model.BookmarkEvent.Function;
import rd.dap.model.Track;
import rd.dap.monitors.BookmarkMonitor;
import rd.dap.monitors.Monitor;
import rd.dap.monitors.ProgressMonitor;
import rd.dap.support.Time;
import rd.dap.support.TrackList;
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

public class PlayerService extends Service implements Subscriber, OnErrorListener, OnCompletionListener {
	private static final String TAG = "PlayerService";
	private static MediaPlayer mp = null;
	private final IBinder binder = new DAPBinder();
	private long laststart = 0;
	private Bookmark bookmark;
	private DAPPhoneStateListener phoneStateListener;
	private static final String src = "PlayerService";
	private ProgressMonitor pmonitor;
	private BookmarkMonitor bmonitor;
	
	
	@Override
	public void onCreate(){
		EventBus.addSubsciber(this);
		phoneStateListener = new DAPPhoneStateListener();
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
	
	@Override
	public void onEvent(Event event) {
		switch(event.getType()){
		case TIME_OUT_EVENT: pause(); break;
		case BOOKMARK_SELECTED_EVENT:
			Bookmark bookmark = event.getBookmark();
			set(bookmark);
			(pmonitor = new ProgressMonitor(this)).start();
			(bmonitor = new BookmarkMonitor(this)).start();
			break;
		case REQUEST_TOGGLE: System.err.println("request received -->"+Time.getTimestamp().toString(Time.TimeStamp.TIME_EXACT)); 
			toggle(); break;
		case REQUEST_PLAY: play(); break;
		case REQUEST_PAUSE: pause(); break;
		case REQUEST_PREV: prev(); break;
		case REQUEST_NEXT: next(); break;
		case REQUEST_SEEK_TO_TRACK: 
			int new_trackno = event.getInteger();
			selectTrack(new_trackno); 
			break;
		case REQUEST_REWIND: rewind(Time.toMillis(1, TimeUnit.MINUTES)); break;
		case REQUEST_FORWARD: forward(Time.toMillis(1, TimeUnit.MINUTES)); break;
		case REQUEST_SEEK_TO: 
			int new_progress = event.getInteger();
			seekProgressTo(new_progress);
			break;
		default:
			break;
		}
	}
	
	private void set(Bookmark bookmark){
		this.bookmark = bookmark;
		//	private void set(Audiobook audiobook, int trackno, int progress){
		Log.d(TAG, "set");
		AudiobookManager am = AudiobookManager.getInstance();
		Audiobook audiobook = am.getAudiobook(bookmark);
		if(audiobook == null) return;
		int trackno = bookmark.getTrackno();
		int progress = bookmark.getProgress();
		TrackList tracks = audiobook.getPlaylist();
		int trackcount = tracks.size();
		Track track = tracks.get(trackno);
		if(track == null) return;
		
		if(mp != null){ mp.release(); mp = null; }
		mp = MediaPlayer.create(this, Uri.fromFile(new File(track.getPath())));
		if(mp == null) return;
		int duration = mp.getDuration();
		track.setDuration(duration);
		mp.seekTo(progress);
		mp.setOnCompletionListener(this);
		EventBus.fireEvent(new Event(src, Type.TRACKCOUNT_SET_EVENT).setInteger(trackcount));
		EventBus.fireEvent(new Event(src, Type.DURATION_SET_EVENT).setInteger(duration));
	}
	public void toggle(){
		if(bookmark == null) return;
		if(mp == null) return;
		
		if(mp.isPlaying()) {
			EventBus.fireEvent(new Event(src, Type.ON_PAUSE));
			mp.pause();
		} else {
			System.err.println("start -->"+Time.getTimestamp().toString(Time.TimeStamp.TIME_EXACT));
			EventBus.fireEvent(new Event(src, Type.ON_PLAY));
			mp.start();
		}
	}
	public void play(){
		if(bookmark == null) return;
		if(mp == null) return;
		if(!mp.isPlaying()) {
			mp.start();
		}
	}
	public void pause(){
		if(mp != null){
			if(mp.isPlaying()) mp.pause();
		}
	}
	public void next(){
		Audiobook audiobook = getAudiobook();
		int trackno = bookmark.getTrackno();
		if(trackno +1 >= audiobook.getPlaylist().size()) return;
		bookmark.setTrackno(++trackno);
		bookmark.setProgress(0);
		bookmark.addEvent(new BookmarkEvent(Function.NEXT, trackno, 0));
		String title = AudiobookManager.getTitle(bookmark);
		set(bookmark);
		EventBus.fireEvent(new Event(src, Type.ON_TRACK_CHANGED).setInteger(trackno).setString(title));
		EventBus.fireEvent(new Event(src, Type.ON_PROGRESS_CHANGED).setInteger(0));
	}
	public void prev(){
		int trackno = bookmark.getTrackno();
		if(trackno -1 < 0) return;
		bookmark.setTrackno(--trackno);
		bookmark.setProgress(0);
		bookmark.addEvent(new BookmarkEvent(Function.PREV, trackno, 0));
		set(bookmark);
		String title = AudiobookManager.getTitle(bookmark);
		EventBus.fireEvent(new Event(src, Type.ON_TRACK_CHANGED).setInteger(trackno).setString(title));
		EventBus.fireEvent(new Event(src, Type.ON_PROGRESS_CHANGED).setInteger(0));
	}
	public void selectTrack(int new_trackno){
		bookmark.setTrackno(new_trackno);
		bookmark.setProgress(0);
		bookmark.addEvent(new BookmarkEvent(Function.SEEK_TRACK, new_trackno, 0));
		set(bookmark);
		String title = AudiobookManager.getTitle(bookmark);
		EventBus.fireEvent(new Event(src, Type.ON_TRACK_CHANGED).setInteger(new_trackno).setString(title));
		EventBus.fireEvent(new Event(src, Type.ON_PROGRESS_CHANGED).setInteger(0));
	}
	public void forward(int millis) {
		if(bookmark == null) return;
		int progress = mp.getCurrentPosition() + millis;
		int duration = mp.getDuration();
		int new_progress = Math.min(progress, duration);
		int trackno = bookmark.getTrackno();
		bookmark.addEvent(new BookmarkEvent(Function.FORWARD, trackno, new_progress));
		bookmark.setProgress(new_progress);
		boolean isPlaying = mp.isPlaying();
		set(bookmark);
		if(isPlaying) mp.start();
		EventBus.fireEvent(new Event(src, Type.ON_PROGRESS_CHANGED).setInteger(new_progress));
	}
	public void rewind(int millis) {
		if(bookmark == null) return;
		int progress = mp.getCurrentPosition() - millis;
		int new_progress = Math.max(progress, 0);
		int trackno = bookmark.getTrackno();
		bookmark.addEvent(new BookmarkEvent(Function.REWIND, trackno, new_progress));
		bookmark.setProgress(new_progress);
		boolean isPlaying = mp.isPlaying();
		set(bookmark);
		if(isPlaying) mp.start();
		EventBus.fireEvent(new Event(src, Type.ON_PROGRESS_CHANGED).setInteger(new_progress));
	}
	public void seekProgressTo(int new_progress){
		if(bookmark == null) return;
		int trackno = bookmark.getTrackno();
		bookmark.addEvent(new BookmarkEvent(Function.SEEK_PROGRESS, trackno, new_progress));
		bookmark.setProgress(new_progress);
		boolean isPlaying = mp.isPlaying();
		set(bookmark);
		if(isPlaying) mp.start();
		EventBus.fireEvent(new Event(src, Type.ON_PROGRESS_CHANGED).setInteger(new_progress));
	}
	public void undoTo(int new_trackno, int new_progress){
		bookmark.setTrackno(new_trackno);
		bookmark.setProgress(new_progress);
		set(bookmark);
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		Audiobook audiobook = getAudiobook();
		int trackno = bookmark.getTrackno();
		if((trackno +1) >= audiobook.getPlaylist().size()){
			int end_progress = audiobook.getPlaylist().get(trackno).getDuration();
			bookmark.setTrackno(0);
			bookmark.setProgress(0);
			bookmark.addEvent(new BookmarkEvent(Function.END, trackno, end_progress));
			set(bookmark);
		} else {
			bookmark.setTrackno(trackno++);
			bookmark.setProgress(0);
			bookmark.addEvent(new BookmarkEvent(Function.PLAY, trackno, 0));
			set(bookmark);
			PlayerService.mp.start();
		}
	}
	
	private Audiobook getAudiobook(){
		AudiobookManager am = AudiobookManager.getInstance();
		Audiobook audiobook = am.getAudiobook(bookmark);
		return audiobook;
	}
	public Bookmark getBookmark() { return bookmark; }
	public int getDuration(){
		if(mp == null) return -1;
		try{
			return mp.getDuration();
		} catch (IllegalStateException e){
			return -1;
		}
	}
	public int getProgress(){
		if(mp == null) return -1;
		try{
			return mp.getCurrentPosition();
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
	public void kill(){
		Log.d(TAG, "kill");
		if(mp != null){
			mp.release();
			mp = null;
		}
		if(pmonitor != null) pmonitor.kill();
		if(bmonitor != null) bmonitor.kill();
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

	private class DAPPhoneStateListener extends PhoneStateListener {
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
	}
	
}
