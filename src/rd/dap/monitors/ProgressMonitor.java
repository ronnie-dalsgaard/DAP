package rd.dap.monitors;

import java.util.concurrent.TimeUnit;

import rd.dap.events.Event;
import rd.dap.events.Event.Type;
import rd.dap.events.EventBus;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.services.PlayerService;

public class ProgressMonitor extends Monitor {
	private PlayerService player;
	private int lifetime;

	public ProgressMonitor(PlayerService player) {
		super(333, TimeUnit.MILLISECONDS);
		this.player = player;
	}

	@Override
	public void execute() {
		if(player == null) kill();
		if(player.isPlaying()) lifetime = 2;
		else lifetime--;
		if(lifetime <= 0) return;

		Bookmark bookmark = player.getBookmark();
		int progress = player.getProgress();
		bookmark.setProgress(progress);
		int trackno = bookmark.getTrackno();
		String title = AudiobookManager.getTitle(bookmark);
		
		EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.ON_TRACK_CHANGED).setInteger(trackno).setString(title));
		EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.ON_PROGRESS_CHANGED).setInteger(progress));
	}
}
