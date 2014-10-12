package rd.dap.model;

public class Data {
	private static Audiobook audiobook;
	private static int position;
//TODO Move track from PlayerService to here
	
	
	
	public static Audiobook getAudiobook() { return audiobook; }
	public static void setAudiobook(Audiobook audiobook) { Data.audiobook = audiobook; }
	
	public static int getPosition() { return position; }
	public static void setPosition(int position) { Data.position = position; }
	
	
	
}
