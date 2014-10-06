package rd.dap.fragments;

import static rd.dap.PlayerService.audiobook;
import static rd.dap.PlayerService.position;
import static rd.dap.PlayerService.track;

import java.util.ArrayList;

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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

public class TrackFragment extends Fragment implements OnClickListener, ServiceConnection{
	private final String TAG = "Track_Fragment";
	private static final int CELL = 1111;
	private boolean bound = false;
	private PlayerService player;
	private Monitor monitor;
	private ImageButton next_btn, prev_btn;
	private TextView position_tv, title_tv;
	private LinearLayout tracks_gv;
	
	private ArrayList<Track_Fragment_Observer> observers = new ArrayList<Track_Fragment_Observer>();
	public interface Track_Fragment_Observer{
		public void track_fragment_next();
		public void track_fragment_previous();
		public void track_fragment_click();
		public void track_fragment_select(int position);
	}
	public void addObserver(Track_Fragment_Observer observer) { observers.add(observer); }

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//		monitor = new ProgressMonitor();
		//		monitor.start();
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
		View v = (ViewGroup) inflater.inflate(R.layout.track_fragment, container, false);

		next_btn = (ImageButton) v.findViewById(R.id.track_next);
		next_btn.setOnClickListener(this);

		prev_btn = (ImageButton) v.findViewById(R.id.track_previous);
		prev_btn.setOnClickListener(this);

		position_tv = (TextView) v.findViewById(R.id.track_position);
		if(position != -1){
			position_tv.setText(String.format("%02d", position+1));
		}

		title_tv = (TextView) v.findViewById(R.id.track_title);
		if(track != null){
			title_tv.setText(track.getTitle());
		}

		tracks_gv = (LinearLayout) v.findViewById(R.id.tracks_grid);

		displayTracks();

		return v;
	}

	private void displayTracks(){
		if(position == -1 || track == null || audiobook == null) return;
		//Position
		position_tv.setText(String.format("%02d", position+1));

		//Track
		title_tv.setText(track.getTitle());

		//Tracks
		tracks_gv.removeAllViews();
		final int COLUMNS = 8;
		LinearLayout row = null;
		int m = LinearLayout.LayoutParams.MATCH_PARENT;
		int w = LinearLayout.LayoutParams.WRAP_CONTENT;
		LinearLayout.LayoutParams row_p = new LinearLayout.LayoutParams(m, w);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 85, 1);
		for(int i = 0; i < audiobook.getPlaylist().size(); i++){
			if(i % COLUMNS == 0){
				row = new LinearLayout(getActivity());
				row.setOrientation(LinearLayout.HORIZONTAL);
				tracks_gv.addView(row, row_p);
			}
			TextView cell = new TextView(getActivity());
			cell.setTextColor(getResources().getColor(R.color.white));
			cell.setGravity(Gravity.CENTER);
			cell.setText(String.format("%02d", i+1));
			if(i == position){
				cell.setBackground(getResources().getDrawable(R.drawable.circle));
			}
			cell.setId(CELL);
			cell.setTag(i); //Autoboxing
			cell.setOnClickListener(this);
			row.addView(cell, p);
		}
		if(audiobook.getPlaylist().size() % COLUMNS > 0){
			Space space = new Space(getActivity());
			int weight = COLUMNS - (audiobook.getPlaylist().size() % COLUMNS);
			LinearLayout.LayoutParams space_p = new LinearLayout.LayoutParams(0, 75, weight);
			row.addView(space, space_p);
		}
	}


	@Override
	public void onClick(View v) {
		if(audiobook == null || track == null || position == -1) return;
		switch(v.getId()){
		case R.id.track_next:
			if(audiobook.getPlaylist().getLast().equals(track)) return;
			position++;
			track = audiobook.getPlaylist().get(position);
			//Fix view
			displayTracks();

			for(Track_Fragment_Observer observer : observers){
				observer.track_fragment_next();
			}
			
			player.reload();
			
			break;

		case R.id.track_previous:
			if(audiobook.getPlaylist().getFirst().equals(track)) return;
			position--;
			track = audiobook.getPlaylist().get(position);
			//Fix view
			displayTracks();

			for(Track_Fragment_Observer observer : observers){
				observer.track_fragment_previous();
			}
			
			player.reload();
			
			break;

		case CELL:
			if(v.getTag() == null) break;
			try{
				int i = ((Integer)v.getTag()).intValue();
				if(i >= 0 && i < audiobook.getPlaylist().size()){
					position = i;
					track = audiobook.getPlaylist().get(position);
					//Fix view
					displayTracks();

					for(Track_Fragment_Observer observer : observers){
						observer.track_fragment_select(i);
					}
					
					player.reload();
				}
			} catch (Exception e) { break; }
			break;
		}
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
	}
	@Override
	public void onServiceDisconnected(ComponentName name) {
		bound = false;
	}
}
