package rd.dap.events;

import java.util.ArrayList;

import rd.dap.model.Audiobook;

public class HasAudiobooksEvent extends Event {
	private ArrayList<Audiobook> audiobooks;
	public HasAudiobooksEvent(String sourceName, Type type, ArrayList<Audiobook> audiobooks) {
		super(sourceName, type);
		this.audiobooks = audiobooks;
	}
	public ArrayList<Audiobook> getAudiobooks() {
		return audiobooks;
	}
}
