package rd.dap.model;

import java.util.ArrayList;

public class Data {
	protected static ArrayList<Audiobook> audiobooks = new ArrayList<Audiobook>();
	protected static ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
	private static Audiobook audiobook;
	private static int position;
	private static Track track;

	public static ArrayList<Audiobook> getAudiobooks() { return audiobooks; }
	
	public static ArrayList<Bookmark> getBookmarks() { return bookmarks; }
	
	public static Audiobook getAudiobook() { return audiobook; }
	public static void setAudiobook(Audiobook audiobook) { Data.audiobook = audiobook; }
	
	public static int getPosition() { return position; }
	public static void setPosition(int position) { Data.position = position; }
	
	public static Track getTrack() { return track; }
	public static void setTrack(Track track) { Data.track = track; }
	
}
