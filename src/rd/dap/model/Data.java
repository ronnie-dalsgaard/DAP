package rd.dap.model;

import java.util.ArrayList;

public class Data {
	protected static ArrayList<Audiobook> audiobooks = new ArrayList<Audiobook>();
	protected static ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
	private static Audiobook currentAudiobook;
	private static int currentPosition;
	private static Track currentTrack;

	public static ArrayList<Audiobook> getAudiobooks() { return audiobooks; }
	
	public static ArrayList<Bookmark> getBookmarks() { return bookmarks; }
	
	public static Audiobook getCurrentAudiobook() { return currentAudiobook; }
	public static void setCurrentAudiobook(Audiobook audiobook) { Data.currentAudiobook = audiobook; }
	
	public static int getCurentPosition() { return currentPosition; }
	public static void setCurrentPosition(int position) { Data.currentPosition = position; }
	
	public static Track getCurrentTrack() { return currentTrack; }
	public static void setCurrentTrack(Track track) { Data.currentTrack = track; }
	
}
