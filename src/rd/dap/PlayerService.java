package rd.dap;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.model.Data;
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
	private final IBinder binder = new DAPBinder();
	private long laststart = 0;
	private static Monitor_Bookmarks monitor = null;
	
	//Observer pattern - Miniplayer is observable
		private ArrayList<PlayerObserver> observers = new ArrayList<PlayerObserver>();
		public interface PlayerObserver{
			public void updateBookmark(Bookmark bookmark);
		}
		public void addObserver(PlayerObserver observer) { observers.add(observer); }
	
	@Override
	public void onCreate(){
		Log.d(TAG, "onCreate");
		super.onCreate();
		if(monitor == null){
			monitor = new Monitor_Bookmarks(10, TimeUnit.SECONDS, getFilesDir());
			monitor.start();
		}
	}
	@Override
	public void onDestroy(){
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		kill();
	}
	
	public void toggle(){
		if(Data.getAudiobook() == null) return;
		if(Data.getTrack() == null) return;
		if(Data.getPosition() < 0) return;
		if(mp == null){
			mp = MediaPlayer.create(this, Uri.fromFile(new File(Data.getTrack().getPath())));
		}
		if(mp.isPlaying()) mp.pause();
		else mp.start();
	}
	public void play(){
		if(Data.getAudiobook() == null) return;
		if(Data.getTrack() == null) return;
		if(Data.getPosition() < 0) return;
		if(mp == null){
			mp = MediaPlayer.create(this, Uri.fromFile(new File(Data.getTrack().getPath())));
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
//		boolean wasPlaying = mp.isPlaying();
//		mp.pause();
		mp.seekTo(position);
//		if(wasPlaying) mp.start();
	}
	public void reset(){
		if (mp != null) {
			mp.release();
			mp = null;
		}
		Data.setAudiobook(null);
		Data.setTrack(null);
		Data.setPosition(-1);
	}
	public void reload(){
		if(mp != null){
			mp.release();
		} 
		if(Data.getTrack() == null) return;
		mp = MediaPlayer.create(this, Uri.fromFile(new File(Data.getTrack().getPath())));		
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
//			Log.d(TAG, "exception occured -> isPlaying is false");
			return false;
		}
	}
	public void kill(){
		Log.d(TAG, "kill");
		if(mp != null){
			mp.release();
			mp = null;
		}
		Data.setAudiobook(null);
		if(monitor != null){
			monitor.kill();
			monitor = null;
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
		System.out.println("An error occured in the MediaPlayer and" +
				" was cought by PlayerSerice: what="+what+", extra="+extra);
		return true; //Keep going
	}

	class Monitor_Bookmarks extends Monitor {
		private static final String TAG = "Monitor_bookmarks";
		private File filesDir;
		private boolean go_again = true;

		public Monitor_Bookmarks(int delay, TimeUnit unit, File filesDir) {
			super(delay, unit);
			this.filesDir = filesDir;
		}

		@Override
		public void execute() {
			if(mp == null) {
				Log.d(TAG, "Unable to update bookmarks, since mediaplayer is unset");
				return;
			}
			if(!go_again && !mp.isPlaying()){
				return;
			}
			if(Data.getAudiobook() == null) {
				Log.d(TAG, "Unable to update bookmarks, since audiobook is missing");
				return;
			}
			if(Data.getTrack() == null) {
				Log.d(TAG, "Unable to update bookmarks, since track is missing");
				return;
			}
			if(Data.getPosition() < 0) {
				Log.d(TAG, "Unable to update bookmarks, since position is missing");
				return;
			}
			
			BookmarkManager manager = BookmarkManager.getInstance();
			String author = Data.getAudiobook().getAuthor();
			String album = Data.getAudiobook().getAlbum();
			int trackno = Data.getPosition();
			int progress = mp.getCurrentPosition();
			boolean force = false; //only update bookmark if progress is greater than previously recorded
			Bookmark bookmark = manager.createOrUpdateBookmark(filesDir, author, album, trackno, progress, force);
			Log.d(TAG, "Bookmark created or updated\n"+bookmark);
			
			for(PlayerObserver observer : observers){
				observer.updateBookmark(bookmark);
			}
			
			go_again = mp.isPlaying();
		}
	}
}
