package rd.dap.events;

import rd.dap.model.Audiobook;

public class HasAudiobookEvent extends Event {
	private Audiobook audiobook;
	public HasAudiobookEvent(String sourceName, Type type, Audiobook audiobook) {
		super(sourceName, type);
		this.audiobook = audiobook;
	}
	public Audiobook getAudiobook() {
		return audiobook;
	}
}
