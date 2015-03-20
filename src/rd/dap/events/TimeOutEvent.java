package rd.dap.events;

public class TimeOutEvent extends Event {

	public TimeOutEvent(String sourceID) {
		super(sourceID, Event.TIME_OUT_EVENT);
	}

}
