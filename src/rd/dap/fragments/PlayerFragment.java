package rd.dap.fragments;

import rd.dap.R;
import rd.dap.events.Event;
import rd.dap.events.EventBus;
import rd.dap.events.PlayPauseEvent;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class PlayerFragment extends Fragment implements OnClickListener {
	private static final double ANIMATION_SPEED = 1.5;
	private View timer_layout, timer_thumb_iv, timer_thumb_back_iv;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.controller_audiobook_basics, container, false);
		
		//Buttons
		ImageButton play_btn = (ImageButton) layout.findViewById(R.id.audiobook_basics_btn_cover);
		play_btn.setOnClickListener(this);
		
		//Timer
		timer_layout = layout.findViewById(R.id.timer_layout);
		timer_layout.setVisibility(View.GONE);

		timer_thumb_iv = layout.findViewById(R.id.timer_thumb_iv);
		timer_thumb_iv.setOnClickListener(this);

		timer_thumb_back_iv = layout.findViewById(R.id.timer_thumb_back_iv);
		timer_thumb_back_iv.setOnClickListener(this);
		
		return layout;
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.audiobook_basics_btn_cover:
		String className = this.getClass().getSimpleName();
		Event event = new PlayPauseEvent(className, true);
		EventBus.fireEvent(event);
		break;
		case R.id.timer_thumb_iv: click_timer_thumb(); break;
		case R.id.timer_thumb_back_iv: click_timer_thumb_back(); break;
		}
		
		
	}

	private void click_timer_thumb() {
		timer_layout.setVisibility(View.VISIBLE);
		timer_thumb_iv.setVisibility(View.GONE);

		float fromXDelta = timer_layout.getWidth(); 
		if(fromXDelta < 1) fromXDelta = 500;
		Animation show_timer = new TranslateAnimation(fromXDelta, 0, 0, 0);
		show_timer.setDuration((int)(250*ANIMATION_SPEED));
		show_timer.setInterpolator(getActivity(), android.R.anim.linear_interpolator);
		show_timer.setAnimationListener(new AnimationListener() {
			@Override public void onAnimationStart(Animation animation) { }
			@Override public void onAnimationRepeat(Animation animation) { }
			@Override public void onAnimationEnd(Animation animation) {
				timer_thumb_back_iv.setVisibility(View.VISIBLE);
				Animation show_back = new AlphaAnimation(0f, 1f);
				show_back.setDuration((int)(250*ANIMATION_SPEED));
				show_back.setInterpolator(getActivity(), android.R.anim.linear_interpolator);
				timer_thumb_back_iv.startAnimation(show_back);
			}
		});

		timer_layout.startAnimation(show_timer);
	}
	private void click_timer_thumb_back() {
		timer_thumb_back_iv.setVisibility(View.GONE);

		float toXDelta = timer_layout.getWidth();
		Animation hide_timer = new TranslateAnimation(0, toXDelta, 0, 0);
		hide_timer.setDuration(250);
		hide_timer.setInterpolator(getActivity(), android.R.anim.linear_interpolator);
		hide_timer.setAnimationListener(new AnimationListener() {
			@Override public void onAnimationStart(Animation animation) { }
			@Override public void onAnimationRepeat(Animation animation) { }
			@Override public void onAnimationEnd(Animation animation) {
				timer_layout.setVisibility(View.GONE);
				timer_thumb_iv.setVisibility(View.VISIBLE);
				Animation show_thumb = new AlphaAnimation(0f, 1f);
				show_thumb.setDuration((int)(250*ANIMATION_SPEED));
				show_thumb.setInterpolator(getActivity(), android.R.anim.linear_interpolator);
				timer_thumb_iv.startAnimation(show_thumb);
			}
		});

		timer_layout.startAnimation(hide_timer);
	}

}
