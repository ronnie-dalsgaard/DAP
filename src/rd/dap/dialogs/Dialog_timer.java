package rd.dap.dialogs;

import java.util.concurrent.TimeUnit;

import rd.dap.R;
import rd.dap.activities.MainActivity;
import rd.dap.support.Time;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Dialog_timer {
	private MainActivity activity;
	private RelativeLayout base;
	private Menu menu;
	private int delay;
	private final int HOUR_STEP = 1;
	private final int MIN_STEP = 5;
	private final int SEC_STEP = 1;
	
	public Dialog_timer(MainActivity activity) {
		this.activity = activity;
		this.base = activity.getBase();
		this.menu = activity.getMenu();
		this.delay = activity.getDelay();
	}
	
	public void show(){
		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dv = inflater.inflate(R.layout.dialog_timer, base, false);

		//Title
		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
		title_tv.setText("Adjust sleep timer");

		//Custom numberpicker hour
		final TextView hour_tv = (TextView) dv.findViewById(R.id.dialog_timer_hour);
		final TextView min_tv = (TextView) dv.findViewById(R.id.dialog_timer_min);
		final TextView sec_tv = (TextView) dv.findViewById(R.id.dialog_timer_sec);
		hour_tv.setText(String.format("%02d", Time.hoursPart(delay)));
		min_tv.setText(String.format("%02d", Time.minutesPart(delay)));
		sec_tv.setText(String.format("%02d", Time.secondsPart(delay)));
		
		ImageButton hour_inc = (ImageButton) dv.findViewById(R.id.dialog_timer_hour_inc);
		hour_inc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int new_delay = delay + Time.toMillis(HOUR_STEP, TimeUnit.HOURS);
				if(new_delay > Time.toMillis(1, TimeUnit.DAYS)) return;
				delay = new_delay;
				hour_tv.setText(String.format("%02d", Time.hoursPart(delay)));
				min_tv.setText(String.format("%02d", Time.minutesPart(delay)));
				sec_tv.setText(String.format("%02d", Time.secondsPart(delay)));
			}
		});

		ImageButton hour_dec = (ImageButton) dv.findViewById(R.id.dialog_timer_hour_dec);
		hour_dec.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int new_delay = delay - Time.toMillis(HOUR_STEP, TimeUnit.HOURS);
				if(new_delay < 0) return;
				delay = new_delay;
				hour_tv.setText(String.format("%02d", Time.hoursPart(delay)));
				min_tv.setText(String.format("%02d", Time.minutesPart(delay)));
				sec_tv.setText(String.format("%02d", Time.secondsPart(delay)));
			}
		});

		//Custom numberpicker min
		ImageButton min_inc = (ImageButton) dv.findViewById(R.id.dialog_timer_min_inc);
		min_inc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int new_delay = delay + Time.toMillis(MIN_STEP, TimeUnit.MINUTES);
				if(new_delay > Time.toMillis(1, TimeUnit.DAYS)) return;
				delay = new_delay;
				hour_tv.setText(String.format("%02d", Time.hoursPart(delay)));
				min_tv.setText(String.format("%02d", Time.minutesPart(delay)));
				sec_tv.setText(String.format("%02d", Time.secondsPart(delay)));
			}
		});

		ImageButton min_dec = (ImageButton) dv.findViewById(R.id.dialog_timer_min_dec);
		min_dec.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int new_delay = delay - Time.toMillis(MIN_STEP, TimeUnit.MINUTES);
				if(new_delay < 0) return;
				delay = new_delay;
				hour_tv.setText(String.format("%02d", Time.hoursPart(delay)));
				min_tv.setText(String.format("%02d", Time.minutesPart(delay)));
				sec_tv.setText(String.format("%02d", Time.secondsPart(delay)));
			}
		});

		//Custom numberpicker sec
		ImageButton sec_inc = (ImageButton) dv.findViewById(R.id.dialog_timer_sec_inc);
		sec_inc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int new_delay = delay + Time.toMillis(SEC_STEP, TimeUnit.SECONDS);
				if(new_delay > Time.toMillis(1, TimeUnit.DAYS)) return;
				delay = new_delay;
				hour_tv.setText(String.format("%02d", Time.hoursPart(delay)));
				min_tv.setText(String.format("%02d", Time.minutesPart(delay)));
				sec_tv.setText(String.format("%02d", Time.secondsPart(delay)));
			}
		});

		ImageButton sec_dec = (ImageButton) dv.findViewById(R.id.dialog_timer_sec_dec);
		sec_dec.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int new_delay = delay - Time.toMillis(SEC_STEP, TimeUnit.SECONDS);
				if(new_delay < 0) return;
				delay = new_delay;
				hour_tv.setText(String.format("%02d", Time.hoursPart(delay)));
				min_tv.setText(String.format("%02d", Time.minutesPart(delay)));
				sec_tv.setText(String.format("%02d", Time.secondsPart(delay)));
			}
		});

		//Exit button
		ImageButton exit_btn = (ImageButton) dv.findViewById(R.id.dialog_exit_btn);
		exit_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		//Left button
		Button left_btn = (Button) dv.findViewById(R.id.dialog_left_btn);
		left_btn.setText("Cancel");
		left_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		//Right button
		Button right_btn = (Button) dv.findViewById(R.id.dialog_right_btn);
		right_btn.setText("Confirm");
		right_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				MenuItem item = menu.findItem(R.id.menu_item_countdown);
				item.setTitle(Time.toString(delay));
				activity.setTimerDelay(delay);
			}
		});

		dialog.setContentView(dv);
		dialog.show();
	}
}
