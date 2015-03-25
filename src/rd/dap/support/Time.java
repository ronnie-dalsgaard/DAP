package rd.dap.support;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Time {
	public static final int SEC = 1000;
	public static final int MIN = 60*SEC;
	public static final int HOUR = 60*MIN;
	public static final int DAY = 24*HOUR;
	
	private static NumberFormat f2d = new DecimalFormat("00");
	private static NumberFormat f2or3d = new DecimalFormat("#00");

	/**
	 * Generates a timestamp with the format hh:mm:ss (d)d/(m)m-yyyy
	 * @return time and date as string
	 */
	public static TimeStamp getTimestamp(){
		Calendar cal = Calendar.getInstance();
		int milis = cal.get(Calendar.MILLISECOND);
		int sec = cal.get(Calendar.SECOND);
		int min = cal.get(Calendar.MINUTE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH) +1;
		int year = cal.get(Calendar.YEAR);
		TimeStamp time = new Time().new TimeStamp(milis, sec, min, hour, day, month, year);
		return time;
	}
	public static TimeStamp getTimeStamp(TimeStamp ts){
		TimeStamp time = new Time().new TimeStamp(ts);
		return time;
	}
	
	public class TimeStamp{
		public static final int 
			TIME = 0, 
			TIME_EXACT = 1, 
			DAY = 2, 
			DAY_EXACT = 3, 
			DAY_TIME = 4, 
			DAY_TIME_EXACT = 5, 
			DAY_TIME_VERY_EXACT = 6;
		private DecimalFormat df = new DecimalFormat("00");
		private DecimalFormat df3 = new DecimalFormat("000");
		public int milis, sec, min, hour, day, month, year;
		public String _milis, _sec, _min, _hour, _day, _month, _year;
		public TimeStamp(int milis, int sec, int min, int hour, int day, int month, int year){
			this.milis = milis; this.sec = sec; this.min = min; this.hour = hour;
			this.day = day; this.month = month; this.year = year;
			this._milis = this.df3.format(milis);
			this._sec   =  this.df.format(sec);
			this._min   =  this.df.format(min);
			this._hour  =  this.df.format(hour);
			this._day   =  this.df.format(day);
			this._month =  this.df.format(month);
			this._year  =  this.df.format(year);
		}
		public TimeStamp(TimeStamp ts){
			this(ts.milis, ts.sec, ts.min, ts.hour, ts.day, ts.month, ts.year);
		}
		public String toString(int format){
			switch (format){
				case TIME: return this._hour + ":" + this._min + ":" + this._sec;
				case TIME_EXACT: return this._hour + ":" + this._min + ":" + this._sec + ":" + this._milis;
				case DAY: return this._day + "/" + this._month;
				case DAY_EXACT: return this._day + "/" + this._month + "-" + this._year;
				case DAY_TIME: return this._hour + ":" + this._min + " " + this._day + "/" + this._month;
				case DAY_TIME_EXACT: return this._hour + ":" + this._min + ":" + this._sec + " " + this._day + "/" + this._month + "-" + this._year;
				case DAY_TIME_VERY_EXACT: return this._hour + ":" + this._min + ":" + this._sec + ":" + this._milis 
						+ " " + this._day + "/" + this._month + "-" + this._year;
				default: return "Bad format";
			}
			
		}
		@Override public String toString(){
			return this._hour +":"+ this._min +":"+ this._sec +" "+ this._day +"/"+ this._month +"-"+ this._year;
		}
	}
	
	/**
	 * Converts time formatted as (hh:)mm:ss:lll to milis 
	 * @param time as formatted string
	 * @return milis
	 */
	public static int toInt(String time) {
		int hour, min, sec;
		String[] s = time.split(":");
		sec    = Integer.parseInt(s[s.length - 1]);
		min    = Integer.parseInt(s[s.length - 2]);
		hour = s.length > 2 ? Integer.parseInt(s[s.length - 3]) : 0;

		int result = 0;
		result += sec * 1000;
		result += min * 60 * 1000;
		result += hour * 60 * 60 * 1000;
		return result;
	}
	
	/**
	 * Converts time as milisecs to (hh:)mm:ss
	 * @param progress time as milis
	 * @return the formatted time
	 */
	public static String toString(long progress){
		long hours = TimeUnit.MILLISECONDS.toHours(progress);
		progress -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(progress);
		progress -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(progress);
		progress -= TimeUnit.SECONDS.toMillis(seconds);
		//        long millis = progress;

		String output = "";
		output += hours > 0? f2d.format(hours) + ":" : "";
		output += f2d.format(minutes) + ":"; 
		output += f2d.format(seconds);
		//        output += ":" + f3d.format(millis);

		return output;
	}
	
	/**
	 * Converts time as milisecs to (m)mm:ss
	 * @param progress time as milis
	 * @return the formatted time
	 */
	public static String toShortString(long progress){
		long minutes = TimeUnit.MILLISECONDS.toMinutes(progress);
		progress -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(progress);

		return f2or3d.format(minutes) + ":" + f2d.format(seconds);
	}

	public static int toMillis(int value, TimeUnit unit){
		switch(unit){
		case DAYS: value *= 24;
		case HOURS: value *= 60;
		case MINUTES: value *= 60;
		case SECONDS: value *= 1000;
		case MILLISECONDS: return value;
		default: return -1;
		}
	}
	public static double toUnits(int value, TimeUnit fromUnit, TimeUnit toUnit){
		double millis = toMillis(value, fromUnit);
		switch(toUnit){
		case DAYS: return millis / DAY; 
		case HOURS: return millis / HOUR;
		case MINUTES: return millis / MIN;
		case SECONDS: return millis / SEC; 
		default: System.out.println("Bad toUnit"); 
			throw new IllegalArgumentException("toUnit : "+toUnit);
		}
	}
	public static int hoursPart(int millis){
		if(millis < HOUR) return 0;
		millis = millis % DAY;
		return (int)(millis / HOUR);
	}
	public static int minutesPart(int millis){
		if(millis < MIN) return 0;
		millis = millis % HOUR;
		return (int)(millis / MIN);
	}
	public static int secondsPart(int millis){
		if(millis < SEC) return 0;
		millis = millis %  MIN;
		return (int)(millis / SEC);
	}
}
