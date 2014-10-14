//package rd.dap.discontinued;
//
//import java.util.ArrayList;
//import java.util.concurrent.TimeUnit;
//
//import rd.dap.PlayerService;
//import rd.dap.PlayerService.DAPBinder;
//import rd.dap.R;
//import rd.dap.support.Monitor;
//import android.app.Fragment;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.TextView;
//import rd.dap.support.Time;
//
//public class FragmentSeeker extends Fragment implements OnClickListener, ServiceConnection{
//	private final String TAG = "Seeker";
//	private boolean bound = false;
//	private PlayerService player;
//	private Monitor monitor;
//	private ImageButton forward_btn, rewind_btn;
//	private TextView progress_tv;
//	
//	/*
//	 *	<fragment
//	 *	    android:id="@+id/controller_seeker_fragment"
//	 *	    android:name="rd.dap.fragments.FragmentSeeker"
//	 *	    android:layout_width="@dimen/mini_player_width"
//	 *	    android:layout_height="wrap_content"
//	 *	    android:layout_below="@+id/controller_divider_2"
//	 *	    android:layout_centerHorizontal="true"
//	 *	    tools:layout="@layout/fragment_seeker" />
//	 *	
//	 */
//	
//	private ArrayList<Seeker_Fragment_Observer> observers = new ArrayList<Seeker_Fragment_Observer>();
//	public interface Seeker_Fragment_Observer{
//		public void seeker_fragment_forward();
//		public void seeker_fragment_rewind();
//		public void seeker_fragment_click();
//	}
//	public void addObserver(Seeker_Fragment_Observer observer) { observers.add(observer); }
//
//	@Override
//	public void onCreate(Bundle savedInstanceState){
//		super.onCreate(savedInstanceState);
//		monitor = new ProgressMonitor(1, TimeUnit.SECONDS);
//		monitor.start();
//	}
//	@Override
//	public void onDestroy(){
//		super.onDestroy();
//		if(monitor != null){
//			monitor.kill();
//			monitor = null;
//		}
//	}
//	
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		Log.d(TAG, "onCreateView");
//		View v = (ViewGroup) inflater.inflate(R.layout.fragment_seeker, container, false);
//		
//		forward_btn = (ImageButton) v.findViewById(R.id.seeker_fast_forward);
//		forward_btn.setOnClickListener(this);
//
//		rewind_btn = (ImageButton) v.findViewById(R.id.seeker_rewind);
//		rewind_btn.setOnClickListener(this);
//		
//		progress_tv = (TextView) v.findViewById(R.id.seeker_progress_tv);
//		progress_tv.setText(Time.toString(0));
//		progress_tv.setOnClickListener(this);
//		
//		return v;
//	}
//	
//	@Override
//	public void onClick(View v) {
//		
//		switch(v.getId()){
//		case R.id.seeker_fast_forward:
//			int ff_position = player.getCurrentProgress();
//			int ff_duration = player.getDuration();
//			int ff_newPos = 0;
//			
//			ff_newPos = Math.min(ff_position + (60 * 1000), ff_duration);
//			if(ff_position == -1 || ff_duration == -1) return; 
//			player.seekTo(ff_newPos);
//			progress_tv.setText(Time.toString(ff_newPos));
//			
//			for(Seeker_Fragment_Observer observer : observers){
//				observer.seeker_fragment_forward();
//			}
//			break;
//
//		case R.id.seeker_rewind:
//			int rew_position = player.getCurrentProgress();
//			int rew_duration = player.getDuration();
//			int rew_newPos = 0;
//
//			rew_newPos = Math.max(rew_position - (60 * 1000), 0);
//			if(rew_position == -1 || rew_duration == -1) return; 
//			player.seekTo(rew_newPos);
//			progress_tv.setText(Time.toString(rew_newPos));
//			
//			for(Seeker_Fragment_Observer observer : observers){
//				observer.seeker_fragment_rewind();
//			}
//			break;
//			
//		case R.id.seeker_progress_tv:
//			for(Seeker_Fragment_Observer observer : observers){
//				observer.seeker_fragment_click();
//			}
//			break;
//		}
//	}
//
//	@Override
//	public void onStart(){
//		Log.d(TAG, "onStart");
//		super.onStart();
//		//Bind to PlayerService
//		Intent intent = new Intent(getActivity(), PlayerService.class);
//		getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);	
//	}
//	@Override
//	public void onStop(){
//		Log.d(TAG, "onStop");
//		super.onStop();
//		//Unbind from PlayerService
//		if(bound){
//			getActivity().unbindService(this);
//			bound = false;
//		}
//	}
//	
//	@Override
//	public void onServiceConnected(ComponentName name, IBinder service) {
//		DAPBinder binder = (DAPBinder) service;
//		player = binder.getPlayerService();
//		bound = true;
//		progress_tv.setText(Time.toString(player.getCurrentProgress()));
//	}
//	@Override
//	public void onServiceDisconnected(ComponentName name) {
//		bound = false;
//	}
//
//	
//	class ProgressMonitor extends Monitor{
//		public ProgressMonitor(int delay, TimeUnit unit) {
//			super(delay, unit);
//		}
//
//		@Override
//		public void execute() {
//			if(progress_tv == null) return;
//			if(player == null) return;
//			getActivity().runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					if(player.isPlaying()){
//						progress_tv.setText(Time.toString(player.getCurrentProgress()));
//					}
//				}
//			});
//		}
//	}
//}
