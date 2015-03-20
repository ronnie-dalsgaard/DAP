package rd.dap.events;

import java.util.ArrayList;

public class EventBus {
	private static ArrayList<Subscriber> subscribers = new ArrayList<Subscriber>(); 

	public static void fireEvent(Event event){
		for(Subscriber subscriber : subscribers){
			subscriber.onEvent(event);
		}
	}
	
	public static void addSubsciber(Subscriber subscriber){
		subscribers.add(subscriber);
	}
}
