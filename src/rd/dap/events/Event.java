package rd.dap.events;

public abstract class Event {
	public static final int EVENT = 5000;
	public static final int PLAY_PAUSE_EVENT = 5001;
	public static final int TIME_OUT_EVENT = 5002;
	public static final int AUDIOBOOKS_LOADED_EVENT = 5003;
	public static final int BOOKMARKS_LOADED_EVENT = 5004;
	
	private String sourceName;
	private int eventID;

	public Event(String sourceName, int eventID){
		this.sourceName = sourceName;
		this.eventID = eventID;
	}
	
	public String getSourceName(){
		return sourceName;
	}
	
	public int getEventID(){
		return eventID;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Event [sourcenName=").append(sourceName)
				.append("\n\teventID=").append(eventID).append("]")
				.append("\n\tevent class=").append(getClass().getSimpleName());
		return builder.toString();
	}
	
	
}
