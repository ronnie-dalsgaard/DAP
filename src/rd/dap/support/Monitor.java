package rd.dap.support;

import android.util.Log;

public abstract class Monitor extends Thread {
	private static final String TAG = "Monitor";
	public static final int DELAY = 1000;
	private boolean alive = true;

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
				Thread.sleep(DELAY);
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
