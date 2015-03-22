package rd.dap.events;

public class ProgressUpdatedEvent extends Event {
	private int progress;

	public ProgressUpdatedEvent(String sourceName, int progress) {
		super(sourceName, Type.PROGRESS_UPDATED_EVENT);
		this.progress = progress;
	}

	public int getProgress() {
		return progress;
	}
}
