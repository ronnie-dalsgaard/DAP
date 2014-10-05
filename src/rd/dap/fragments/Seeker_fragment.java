package rd.dap.fragments;

import rd.dap.PlayerService;
import rd.dap.PlayerService.DAPBinder;
import rd.dap.R;
import rd.dap.support.Monitor;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import rd.dap.support.Time;

public class Seeker_fragment extends Fragment implements OnClickListener, ServiceConnection{
	private final String TAG = "Seeker";
	private boolean bound = false;
	private PlayerService player;
	private Monitor monitor;
	private ImageButton forward_btn, rewind_btn;
	private TextView progress_tv;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		monitor = new ProgressMonitor();
		monitor.start();
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(monitor != null){
			monitor.kill();
			monitor = null;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View v = (ViewGroup) inflater.inflate(R.layout.seeker_fragment, container, false);
		
		forward_btn = (ImageButton) v.findViewById(R.id.seeker_fast_forward);
		forward_btn.setOnClickListener(this);

		rewind_btn = (ImageButton) v.findViewById(R.id.seeker_rewind);
		rewind_btn.setOnClickListener(this);
		
		progress_tv = (TextView) v.findViewById(R.id.seeker_progress_tv);
		progress_tv.setText(Time.toString(0));
		
		return v;
	}
	
	@Override
	public void onClick(View v) {
		int position = player.getCurrentProgress();
		int duration = player.getDuration();
		int newPos = 0;
		switch(v.getId()){
		case R.id.seeker_fast_forward:
			newPos = Math.min(position + (60 * 1000), duration);
			break;
		case R.id.seeker_rewind:
			newPos = Math.max(position - (60 * 1000), 0);
			break;
		}
		if(position == -1 || duration == -1) return; 
		player.seekTo(newPos);
		progress_tv.setText(Time.toString(newPos));
	}

	@Override
	public void onStart(){
		Log.d(TAG, "onStart");
		super.onStart();
		//Bind to PlayerService
		Intent intent = new Intent(getActivity(), PlayerService.class);
		getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);	
	}
	@Override
	public void onStop(){
		Log.d(TAG, "onStop");
		super.onStop();
		//Unbind from PlayerService
		if(bound){
			getActivity().unbindService(this);
			bound = false;
		}
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		DAPBinder binder = (DAPBinder) service;
		player = binder.getPlayerService();
		bound = true;
		progress_tv.setText(Time.toString(player.getCurrentProgress()));
	}
	@Override
	public void onServiceDisconnected(ComponentName name) {
		bound = false;
	}

	
	class ProgressMonitor extends Monitor{

		@Override
		public void execute() {
			if(progress_tv == null) return;
			if(player == null) return;
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					progress_tv.setText(Time.toString(player.getCurrentProgress()));
				}
			});
		}
	}
}
