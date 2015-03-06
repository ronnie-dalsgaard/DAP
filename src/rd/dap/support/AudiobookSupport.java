package rd.dap.support;

public class AudiobookSupport {
	public static final long SEC = 1000l; //MUST be long
	public static final long MIN = SEC * 60; 
	public static final long HOUR = MIN * 60;
	public static final long DAY = HOUR * 24;
	public static final long WEEK = DAY * 7;

	public static String prettyDuration(long duration){
		if(duration == -1) return "Unknown";
		
		long remainder = duration;
		int hours = (int)(remainder / HOUR);
		remainder %= HOUR;
		int min = (int)(remainder / MIN);
		remainder %= MIN;
		int sec = (int)(remainder / SEC);
		
		return hours > 0 ? hours+":" : "" + min + ":" + sec;
	}

}
