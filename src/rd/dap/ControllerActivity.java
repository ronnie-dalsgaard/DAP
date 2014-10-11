package rd.dap;

import static rd.dap.PlayerService.audiobook;

import java.util.ArrayList;

import rd.dap.PlayerService.DAPBinder;
import rd.dap.fragments.FragmentAudiobookBasics;
import rd.dap.fragments.FragmentAudiobookBasics.Fragment_Audiobooks_Basics_Observer;
import rd.dap.fragments.FragmentSeeker;
import rd.dap.fragments.FragmentSeeker.Seeker_Fragment_Observer;
import rd.dap.fragments.FragmentTrack;
import rd.dap.fragments.FragmentTrack.Fragment_Track_Observer;
import rd.dap.support.DriveHandler;
import rd.dap.support.Monitor;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.Metadata;
import com.google.gson.Gson;

public class ControllerActivity extends DriveHandler implements ServiceConnection, OnClickListener, 
	Fragment_Track_Observer, Seeker_Fragment_Observer, Fragment_Audiobooks_Basics_Observer {
	
	private final String TAG = "ControllerActivity";
	private boolean bound = false;
	private PlayerService player;
	private static Drawable noCover = null, drw_play = null, drw_pause = null,
			drw_play_on_cover = null, drw_pause_on_cover;
	private ControllerMonitor monitor;
	private FragmentAudiobookBasics audiobook_basics_frag;
	private FragmentTrack track_frag;
	private FragmentSeeker seeker_frag;
	private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
	private ImageButton play_btn, upload_btn, download_btn;
	
	private static final int REQUEST_CODE_UPLOAD = 13001;
	private static final int REQUEST_CODE_DOWNLOAD = 13002;
	private static final int REQUEST_CODE_GET_CONTENTS = 13003;
	private static final int REQUEST_CODE_QUERY = 13004;
	private static final int REQUEST_CODE_UPDATE = 13005;
	
	private DriveFile currentBookmarkFile = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controller);

		if(noCover == null || drw_play == null || drw_pause == null
				|| drw_play_on_cover == null || drw_pause_on_cover == null){
			noCover = getResources().getDrawable(R.drawable.ic_action_help);
			drw_play = getResources().getDrawable(R.drawable.ic_action_play);
			drw_pause = getResources().getDrawable(R.drawable.ic_action_pause);
			drw_play_on_cover = getResources().getDrawable(R.drawable.ic_action_play_over_video);
			drw_pause_on_cover = getResources().getDrawable(R.drawable.ic_action_pause_over_video);
		}

		play_btn = (ImageButton) findViewById(R.id.controller_play);
		play_btn.setImageDrawable(null);
		play_btn.setOnClickListener(this);
		
		upload_btn = (ImageButton) findViewById(R.id.controller_upload);
		upload_btn.setOnClickListener(this);
		
		download_btn = (ImageButton) findViewById(R.id.controller_download);
		download_btn.setOnClickListener(this);
		
		FragmentManager fm = getFragmentManager();
		
		audiobook_basics_frag = (FragmentAudiobookBasics) fm.findFragmentById(R.id.controller_fragment_audiobooks_basics);
		audiobook_basics_frag.addObserver(this);
		
		track_frag = (FragmentTrack) fm.findFragmentById(R.id.controller_track_fragment);
		track_frag.addObserver(this);
		
		seeker_frag = (FragmentSeeker) fm.findFragmentById(R.id.controller_seeker_fragment);
		seeker_frag.addObserver(this);
		
		fragments.clear();
		fragments.add(audiobook_basics_frag);
		fragments.add(track_frag);
		fragments.add(seeker_frag);
		
		///////////////////////////////////////////////////////////////////////////////////////////////////
		// Just for test
		ImageButton t1 = (ImageButton) findViewById(R.id.controller_test1);
		t1.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				Gson gson = new Gson();
				System.out.println(gson.toJson(audiobook));
				
			}
		});
		
		///////////////////////////////////////////////////////////////////////////////////////////////////

		monitor = new ControllerMonitor();
		monitor.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_controller, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Intent intent;
		switch(id){
		case R.id.menu_item_audiobook_list:
			intent = new Intent(this, AudiobookListActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onClick(View v) {
		Log.d(TAG, "onClick");
		switch(v.getId()){
		case R.id.controller_play:
			if(audiobook == null) break;
			boolean isPlaying = false;
			if(player != null) isPlaying = player.isPlaying();
			//Fix view
			play_btn.setImageDrawable(!isPlaying ? drw_pause : drw_play);
			audiobook_basics_frag.setActionDrawabel(!isPlaying ? drw_pause_on_cover : drw_play_on_cover);
			//Toggle play/pause
			player.toggle();
			break;
			
		case R.id.controller_upload:
			if(this.currentBookmarkFile == null){
				upload(REQUEST_CODE_UPLOAD, "THIS IS A TEST!!!");
			} else {
				update(REQUEST_CODE_UPDATE, currentBookmarkFile, "New test");
			}
			break;
		case R.id.controller_download:
			super.download(REQUEST_CODE_DOWNLOAD);
//			super.query(REQUEST_CODE_QUERY, ".dap");
			break;
		}
	}
	public void onDriveResult(int requestCode, int result, Object... data){
		if(result != DriveHandler.SUCCESS) return;
		switch(requestCode){
		case REQUEST_CODE_UPLOAD: 
			Toast.makeText(this, "Upload successfull", Toast.LENGTH_SHORT).show();
			break;
		case REQUEST_CODE_DOWNLOAD: 
			//This is a passthrough
			currentBookmarkFile = (DriveFile) data[0];
			getContents(REQUEST_CODE_GET_CONTENTS, currentBookmarkFile);
			break;
		case REQUEST_CODE_GET_CONTENTS:
			String str = (String) data[0];
			Toast.makeText(this, ":::"+str, Toast.LENGTH_SHORT).show();
			break;
		case REQUEST_CODE_QUERY:
			@SuppressWarnings("unchecked")
			ArrayList<Metadata> list = (ArrayList<Metadata>) data[0];
			for(Metadata m : list){
				System.out.println("--->"+m.getTitle());
			}
		case REQUEST_CODE_UPDATE:
			Toast.makeText(this, "Update successfull", Toast.LENGTH_SHORT).show();
			break;
		}
	}

	@Override
	public void onStart(){
		Log.d(TAG, "onStart");
		super.onStart();
		//Bind to PlayerService
		Intent intent = new Intent(this, PlayerService.class);
		bindService(intent, this, Context.BIND_AUTO_CREATE);

		//connect to Google Drive
		super.connect();
	}
	@Override
	public void onStop(){
		Log.d(TAG, "onStop");
		super.onStop();
		//Unbind from PlayerService
		if(bound){
			unbindService(this);
			bound = false;
		}
		
		//disconnet from Google Drive
		super.disconnect();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected");
		DAPBinder binder = (DAPBinder) service;
		player = binder.getPlayerService();
		bound = true;
	}
	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "onServiceDisconnected");
		bound = false;
	}

	class ControllerMonitor extends Monitor {
//		private static final String TAG = "ControllerActivity.Monitor";
		private boolean isPlaying = false;

		@Override
		public void execute() {
			isPlaying = player != null && player.isPlaying();

			ControllerActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(bound && audiobook != null){
						play_btn.setImageDrawable(isPlaying ? drw_pause : drw_play);
						audiobook_basics_frag.setActionDrawabel(isPlaying ? drw_pause_on_cover : drw_play_on_cover);
					} else {
						play_btn.setImageDrawable(null);
						audiobook_basics_frag.setActionDrawabel(null);
					}
				}
			});
		}
	}

	@Override
	public void fragment_track_next() {
		play_btn.setImageDrawable(drw_play);
		audiobook_basics_frag.setActionDrawabel(drw_play_on_cover);
	}
	@Override
	public void fragment_track_previous() {
		play_btn.setImageDrawable(drw_play);
		audiobook_basics_frag.setActionDrawabel(drw_play_on_cover);
	}
	@Override
	public void fragment_track_click() {
		Toast.makeText(this, "Click on track_fragment", Toast.LENGTH_SHORT).show();
	}
	@Override
	public void fragment_track_select(int position) {
		play_btn.setImageDrawable(drw_play);
		audiobook_basics_frag.setActionDrawabel(drw_play_on_cover);
	}

	@Override public void seeker_fragment_forward() {
		Toast.makeText(this, "Seeker forward", Toast.LENGTH_SHORT).show();
	}

	@Override public void seeker_fragment_rewind() {
		
	}

	@Override public void seeker_fragment_click() {
		
	}

	@Override
	public void fragment_audiobooks_basics_click() {
		if(audiobook == null) return;;
		boolean isPlaying = false;
		if(player != null) isPlaying = player.isPlaying();
		//Fix view
		play_btn.setImageDrawable(!isPlaying ? drw_pause : drw_play);
		audiobook_basics_frag.setActionDrawabel(!isPlaying ? drw_pause_on_cover : drw_play_on_cover);
		//Toggle play/pause
		player.toggle();
	}


	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult :: forwarding to super and all fragments!");
		super.onActivityResult(requestCode, resultCode, data);
	    for(Fragment frag : fragments){
	    	frag.onActivityResult(requestCode, resultCode, data);
	    }
	}

	
	
}
