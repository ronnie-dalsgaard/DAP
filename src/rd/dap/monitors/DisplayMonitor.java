package rd.dap.monitors;

import java.util.concurrent.TimeUnit;

import rd.dap.model.Audiobook;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.model.Track;
import rd.dap.services.PlayerService;
import android.app.Activity;

public class DisplayMonitor extends Monitor {
	private Activity activity;
	private PlayerService player;
	private DisplayMoniterListener listener;
	
	public interface DisplayMoniterListener {
		public void displayTime(Track track);
		public void displayNoTime();
	}

	public DisplayMonitor(Activity activity, PlayerService player, DisplayMoniterListener listener) {
		super(1, TimeUnit.SECONDS);
		this.activity = activity;
		this.player = player;
		this.listener = listener;
	}

	@Override
	public void execute() {
		if(activity == null) return;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(player == null) return;
				Audiobook audiobook = player.getAudiobook();
				if(audiobook == null) return;
				int trackno = player.getTrackno();
				Track track = audiobook.getPlaylist().get(trackno);
				Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
				if(bookmark != null){
					listener.displayTime(track);
				} else {
					listener.displayNoTime();
				}
			}
		});
	}

}
