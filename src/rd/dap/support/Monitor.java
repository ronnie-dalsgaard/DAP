package rd.dap.support;

import java.util.concurrent.TimeUnit;

import android.util.Log;

public abstract class Monitor extends Thread {
	private static final String TAG = "Monitor";
	public static final int DEFAULT_DELAY = 1000;
	public static int delay = DEFAULT_DELAY;
	private boolean alive = true;
	
	private static final int SEC = 1000;
	private static final int MIN = 60*SEC;
	private static final int HOUR = 60*MIN;
	private static final int DAY = 24*HOUR;
	
	public Monitor(int delay, TimeUnit unit){
		switch(unit){
		case MILLISECONDS: Monitor.delay = delay; break;
		case SECONDS: Monitor.delay = delay * SEC; break;
		case MINUTES: Monitor.delay = delay * MIN; break;
		case HOURS: Monitor.delay = delay * HOUR; break;
		case DAYS: Monitor.delay = delay * DAY; break;
		case MICROSECONDS: //fall through
		case NANOSECONDS: Monitor.delay = DEFAULT_DELAY;
		}
	}

	public void kill(){
		this.alive = false;
	}
	
	public abstract void execute();

	@Override
	public void run() {
		int insomnicEpisodes = 0;
		long t0 = System.currentTimeMillis();
		double errorFrequency = 0.0;
		while(alive){
			execute();
			

			//Delay with error handling
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				//Ignored - just insomnia
				insomnicEpisodes++;
				long t = System.currentTimeMillis();
				//error / sec.
				errorFrequency = insomnicEpisodes / ((t - t0) / 1000);
				Log.d(TAG, "insomnia frequency="+errorFrequency);
				if(errorFrequency > 1.0){
					System.err.println("Too insomnic! - system exited");
					System.exit(-1);
				}
			}
		}
	}

}
