package rd.dap.events;

public class Event {
	public enum Type {
		PLAY_PAUSE_EVENT,
		TIME_OUT_EVENT,
		NO_AUDIOBOOKS_FOUND_EVENT,
		NO_BOOKMARKS_FOUND_EVENT,
		FILE_FOUND_EVENT,
		AUDIOBOOKS_LOADED_EVENT,
		AUDIOBOOKS_SELECTED_EVENT,
		BOOKMARKS_LOADED_EVENT,
		BOOKMARK_SELECTED_EVENT,
		BOOKMARK_DELETED_EVENT, 
		BOOKMARK_UPDATED_EVENT,
		PROGRESS_UPDATED_EVENT
	}
	
	private String sourceName;
	private Type type;

	public Event(String sourceName, Type type){
		this.sourceName = sourceName;
		this.type = type;
	}
	
	public String getSourceName(){
		return sourceName;
	}
	
	public Type getType(){
		return type;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("========================================================")
				.append("\nEvent sourcenName = ").append(sourceName)
				.append("\n\teventID = ").append(type)
				.append("\n\tevent class=").append(getClass().getSimpleName())
				.append("\n========================================================");
		return builder.toString();
	}
}
