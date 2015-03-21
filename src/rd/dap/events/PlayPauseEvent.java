package rd.dap.events;

public class PlayPauseEvent extends Event {
	private boolean isPlay;
	
	public PlayPauseEvent(String sourceID, boolean isPlay) {
		super(sourceID, Type.PLAY_PAUSE_EVENT);
		this.isPlay = isPlay;
	}

	public boolean isPlay(){
		return isPlay;
	}
}
