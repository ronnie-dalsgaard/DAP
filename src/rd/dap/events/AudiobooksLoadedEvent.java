package rd.dap.events;

import java.util.ArrayList;

import rd.dap.model.Audiobook;

public class AudiobooksLoadedEvent extends Event {
	private ArrayList<Audiobook> audiobooks;

	public AudiobooksLoadedEvent(String sourceName, ArrayList<Audiobook> audiobooks) {
		super(sourceName, Event.AUDIOBOOKS_LOADED_EVENT);
		this.audiobooks = audiobooks;
	}

	public ArrayList<Audiobook> getAudiobooks(){
		return audiobooks;
	}
}
