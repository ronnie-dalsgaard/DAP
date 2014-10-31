package rd.dap.model;

import java.util.ArrayList;
import java.util.HashSet;

public class Data {
	protected static ArrayList<Audiobook> audiobooks = new ArrayList<Audiobook>();
	protected static HashSet<String> authors = new HashSet<String>();
	protected static ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
	private static Audiobook currentAudiobook;
	private static int currentPosition;
	private static Track currentTrack;

	public static ArrayList<Audiobook> getAudiobooks() { return audiobooks; }
	
	public static ArrayList<Bookmark> getBookmarks() { return bookmarks; }
	
	public static Audiobook getCurrentAudiobook() { return currentAudiobook; }
	public static void setCurrentAudiobook(Audiobook audiobook) { Data.currentAudiobook = audiobook; }
	
	public static int getCurrentPosition() { return currentPosition; }
	public static void setCurrentPosition(int position) { Data.currentPosition = position; }
	
	public static Track getCurrentTrack() { return currentTrack; }
	public static void setCurrentTrack(Track track) { Data.currentTrack = track; }
	
	public static void addAuthor(String name) { authors.add(name); }
	public static void removeAuthor(String name) { authors.remove(name); }
	public static HashSet<String> getAuthors() { return authors; }
}
