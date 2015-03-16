package rd.dap.monitors;

import java.util.concurrent.TimeUnit;

import rd.dap.model.Audiobook;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.services.PlayerService;
import android.app.Activity;

public class DisplayMonitor extends Monitor {
	private Activity activity;
	private DisplayMoniterListener display_monitor_listener;
	
	public interface DisplayMoniterListener {
		public PlayerService getPlayer();
		public void displayTime(Audiobook audiobook, int trackno);
		public void displayNoTime();
	}

	public DisplayMonitor(Activity activity, DisplayMoniterListener display_monitor_listener) {
		super(1, TimeUnit.SECONDS);
		this.activity = activity;
		this.display_monitor_listener = display_monitor_listener;
	}

	@Override
	public void execute() {
		if(activity == null) return;
		final PlayerService player = display_monitor_listener.getPlayer();
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(player == null) return;
				Audiobook audiobook = player.getAudiobook();
				if(audiobook == null) return;
				int trackno = player.getTrackno();
				Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
				if(bookmark != null){
					display_monitor_listener.displayTime(audiobook, trackno);
				} else {
					display_monitor_listener.displayNoTime();
				}
			}
		});
	}

}
