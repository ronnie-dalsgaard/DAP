package rd.dap.model;

import rd.dap.support.Time;
import rd.dap.support.Time.TimeStamp;

public class BookmarkEvent {
	public static enum Function {
		CREATE, PLAY, NEXT, PREV, FORWARD, 
		REWIND, SEEK_PROGRESS, SEEK_TRACK, 
		SELECT, UNDO, DOWNLOAD, END
	};
	
	private Function function;
	private int trackno;
	private int progress;
	private TimeStamp timestamp;

	public BookmarkEvent(Function function, int trackno, int progress) {
		this.function = function;
		this.trackno = trackno;
		this.progress = progress;
		this.timestamp = Time.getTimestamp();
	}
	
	public BookmarkEvent(){} //Must be bean

	public BookmarkEvent(BookmarkEvent be) {
		this.function = be.getFunction();
		this.trackno = be.getTrackno();
		this.progress = be.getProgress();
		this.timestamp = Time.getTimeStamp(be.getTimestamp());
	}

	//Getters and Setters
	public Function getFunction() { return function; }
	public void setFunction(Function function) { this.function = function; }
	public int getTrackno() { return trackno; }
	public void setTrackno(int trackno) { this.trackno = trackno; }
	public int getProgress() { return progress; }
	public void setProgress(int progress) { this.progress = progress; }
	public TimeStamp getTimestamp() { return timestamp; }
	public void setTimestamp(TimeStamp timestamp) { this.timestamp = timestamp; }

	public String toString(){
		String _function = function.toString();
		String _progress = Time.toString(progress);
		String _timestamp = timestamp.toString(Time.TimeStamp.TIME);
		return _function + " -> " + _progress + " @ " + _timestamp;
	}
}
