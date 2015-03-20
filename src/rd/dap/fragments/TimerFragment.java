package rd.dap.fragments;

import java.util.concurrent.TimeUnit;

import rd.dap.R;
import rd.dap.events.Event;
import rd.dap.events.EventBus;
import rd.dap.events.TimeOutEvent;
import rd.dap.monitors.Monitor;
import rd.dap.support.Time;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AnalogClock;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimerFragment extends Fragment implements OnClickListener {
	private static final int STEP_VALUE = 5;
	private static final TimeUnit STEP_UNIT = TimeUnit.MINUTES;
	private static final int MAX_VALUE = 12;
	private static final TimeUnit MAX_UNIT = TimeUnit.HOURS;
	private TextView digi, timer_value_tv;
	private ImageView min_hand, sec_hand;
	private View inc_btn, dec_btn, timer_value_layout;
	private AnalogClock clock;
	private Timer timer;
	private static boolean timerOn = false;
	private int delay;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
		delay = pref.getInt("timer_delay", Time.toMillis(15, TimeUnit.MINUTES));

		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_timer, container, false);
		timer_value_tv = (TextView) view.findViewById(R.id.timer_value);
		min_hand = (ImageView) view.findViewById(R.id.min_hand);
		sec_hand = (ImageView) view.findViewById(R.id.sec_hand);
		digi = (TextView) view.findViewById(R.id.digitalClock);
		clock = (AnalogClock) view.findViewById(R.id.analogClock);
		inc_btn = view.findViewById(R.id.timer_value_inc);
		dec_btn = view.findViewById(R.id.timer_value_dec);
		timer_value_layout = view.findViewById(R.id.timer_value_layout); 

		sec_hand.setPivotX(0);
		sec_hand.setPivotY(1);
		min_hand.setPivotX(0);
		min_hand.setPivotY(1);

		if(timerOn){
			sec_hand.setRotation(0);
			min_hand.setRotation(0);
			timer.setClock();
		} else {
			display(delay);
		}

		clock.setOnClickListener(this);
		inc_btn.setOnClickListener(this);
		dec_btn.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View v) {
		SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
		switch(v.getId()){
		case R.id.analogClock:
			if(!timerOn){
				timer = new Timer();
				timer.start();
				timer_value_layout.setVisibility(View.GONE);
				timerOn = true;
			} else {
				timer.kill();
			}
			break;
		case R.id.timer_value_inc:
			int delay_inc = delay + Time.toMillis(STEP_VALUE, STEP_UNIT);
			if(delay_inc > Time.toMillis(MAX_VALUE, MAX_UNIT)) break;
			delay = delay_inc;
			pref.edit().putInt("timer_delay", delay).commit();
			/* if(listener != null) */display(delay);
			break;
		case R.id.timer_value_dec:
			int delay_dec = delay - Time.toMillis(STEP_VALUE, STEP_UNIT);
			if(delay_dec < Time.toMillis(1, TimeUnit.MINUTES)) break;
			delay = delay_dec;
			pref.edit().putInt("timer_delay", delay).commit();
			/* if(listener != null) */display(delay);
			break;
		}
		timer_value_tv.setText(Time.toString(delay));
	}

	private void display(int delay){
		//Settings
		timer_value_tv.setText(Time.toString(delay));

		//Analog clock
		int min = (int)Time.toUnits(delay, TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
		int angle = ((int)(min / 60.0 * 360.0)-90);
		while(angle >= 360) angle -= 360;

		min_hand.setRotation(angle);
		sec_hand.setRotation(-90);

		//Digital clock
		digi.setText(Time.toString(delay));
	}

	private class Timer extends Monitor {
		private long endTime;
		private int timeleft;

		public Timer() {
			super(1, TimeUnit.SECONDS);
			endTime = System.currentTimeMillis() + TimerFragment.this.delay;
			timeleft = delay;
			
			getActivity().runOnUiThread(new Runnable() { 
				@Override public void run() {
					TimerFragment.this.min_hand.setRotation(0);
					TimerFragment.this.sec_hand.setRotation(0);
				} 
			});

			Animation min_hand_anim = createMin_hand_anim(TimerFragment.this.delay);
			Animation sec_hand_anim = createSec_hand_anim(TimerFragment.this.delay);
			
			sec_hand.startAnimation(sec_hand_anim);
			min_hand.startAnimation(min_hand_anim);
		}

		@Override
		public void execute() {
			timeleft = (int)(endTime - System.currentTimeMillis());

			getActivity().runOnUiThread(new Runnable() { 
				@Override public void run() { 
					digi.setText(Time.toString(timeleft));
				} 
			});

			if(timeleft <= 0){
				String className = this.getClass().getSimpleName();
				Event event = new TimeOutEvent(className);
				EventBus.fireEvent(event);
//				listener.onTimerTerminate();
				kill();
			}
		}
		public void setClock(){
			Animation min_hand_anim = createMin_hand_anim(timeleft);
			Animation sec_hand_anim = createSec_hand_anim(timeleft);
			
			sec_hand.startAnimation(sec_hand_anim);
			min_hand.startAnimation(min_hand_anim);
		}
		
		@Override
		public void kill(){
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					resetClock();
				}
			});
			timerOn = false;
			super.kill();
		}
		
		public void resetClock(){
			digi.setText(Time.toString(TimerFragment.this.delay));
			
			min_hand.clearAnimation();
			sec_hand.clearAnimation();

			int min = (int)Time.toUnits(TimerFragment.this.delay, TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
			int angle = ((int)(min / 60.0 * 360.0)-90);
			while(angle >= 360) angle -= 360;
			
			min_hand.setRotation(angle);
			sec_hand.setRotation(-90);
			
			timer_value_layout.setVisibility(View.VISIBLE); 
		}
	
		public Animation createMin_hand_anim(int delay){
			double mins = Time.toUnits(delay, TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
			int min = (int)mins;
			int diffDegrees = (int)(min / 60.0 * 360.0);
			
			int toDegrees = -90;
			int fromDegrees = diffDegrees + toDegrees;
			Animation min_hand_anim = new RotateAnimation(fromDegrees, toDegrees, 0, 1);
			min_hand_anim.setInterpolator(getActivity(), android.R.anim.linear_interpolator);
			min_hand_anim.setDuration(delay+5);
			return min_hand_anim;
		}
		public Animation createSec_hand_anim(int delay){
			double d_sec = Time.toUnits(delay, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
			int sec = (int)d_sec;
			int diffDegrees = (int)(sec / 60.0 * 360.0);
			
			int toDegrees = -90;
			int fromDegrees = diffDegrees + toDegrees;
			Animation sec_hand_anim = new RotateAnimation(fromDegrees, toDegrees, 0, 1);
			sec_hand_anim.setInterpolator(getActivity(), android.R.anim.linear_interpolator);
			sec_hand_anim.setDuration(delay+5);
			return sec_hand_anim;
		}
	}
}
