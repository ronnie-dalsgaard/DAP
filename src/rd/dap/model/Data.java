package rd.dap.model;

public class Data {
	private static Audiobook audiobook;
	private static int position;
	private static Track track;
	
	public static Audiobook getAudiobook() { return audiobook; }
	public static void setAudiobook(Audiobook audiobook) { Data.audiobook = audiobook; }
	
	public static int getPosition() { return position; }
	public static void setPosition(int position) { Data.position = position; }
	
	public static Track getTrack() { return track; }
	public static void setTrack(Track track) { Data.track = track; }
	
}
