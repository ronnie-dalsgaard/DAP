package rd.dap.events;

import java.util.HashSet;

public class EventBus {
	private static HashSet<Subscriber> subscribers = new HashSet<Subscriber>(); 

	public static void fireEvent(Event event){
		for(Subscriber subscriber : subscribers){
			subscriber.onEvent(event);
		}
	}
	
	public static void addSubsciber(Subscriber subscriber){
		System.out.println("Subscriber added: "+subscriber.getClass().getSimpleName());
		subscribers.add(subscriber);
	}
}
