package rd.dap.fragments;

import static rd.dap.AudiobookActivity.STATE_EDIT;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rd.dap.AudiobookActivity;
import rd.dap.PlayerService;
import rd.dap.PlayerService.DAPBinder;
import rd.dap.R;
import rd.dap.model.Audiobook;
import rd.dap.model.Data;
import rd.dap.model.Track;
import rd.dap.support.DriveHandler;
import rd.dap.support.Monitor;
import rd.dap.support.Time;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.Metadata;

public class ControllerFragment extends DriveHandler implements ServiceConnection, OnClickListener {

	private final String TAG = "ControllerActivity";
	private boolean bound = false;
	private PlayerService player;
	private Monitor controllerMonitor, progressMonitor;
	private static Drawable noCover, drw_play, drw_pause, drw_play_on_cover, drw_pause_on_cover;
	private LinearLayout info_layout, tracks_gv;
	private TextView author_tv, audiobook_basics_album_tv, position_tv, title_tv, progress_tv;
	private ImageButton cover_btn, play_btn, upload_btn, download_btn, next_btn, prev_btn, forward_btn, rewind_btn;
	private ImageView cover_iv;

	private static final int REQUEST_FRAGMENT_BASICS_EDIT = 1701;
	private static final int REQUEST_CODE_UPLOAD = 13001;
	private static final int REQUEST_CODE_DOWNLOAD = 13002;
	private static final int REQUEST_CODE_GET_CONTENTS = 13003;
	private static final int REQUEST_CODE_QUERY = 13004;
	private static final int REQUEST_CODE_UPDATE = 13005;
	
	private static final int CELL = 1111;

	private DriveFile currentBookmarkFile = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		if(noCover == null || drw_play == null || drw_pause == null
				|| drw_play_on_cover == null || drw_pause_on_cover == null){
			noCover = getResources().getDrawable(R.drawable.ic_action_help);
			drw_play = getResources().getDrawable(R.drawable.ic_action_play);
			drw_pause = getResources().getDrawable(R.drawable.ic_action_pause);
			drw_play_on_cover = getResources().getDrawable(R.drawable.ic_action_play_over_video);
			drw_pause_on_cover = getResources().getDrawable(R.drawable.ic_action_pause_over_video);
		}

		controllerMonitor = new ControllerMonitor(1, TimeUnit.SECONDS);
		controllerMonitor.start();
		
		progressMonitor = new ProgressMonitor(1, TimeUnit.SECONDS);
		progressMonitor.start();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View v = (ViewGroup) inflater.inflate(R.layout.controller, container, false);
		
		cover_iv = (ImageView) v.findViewById(R.id.audiobook_basics_cover_iv);
		author_tv = (TextView) v.findViewById(R.id.audiobook_basics_author_tv);
		audiobook_basics_album_tv = (TextView) v.findViewById(R.id.audiobook_basics_album_tv);
		if(Data.getAudiobook() != null){
			displayValues();
		}
		
		//Audiobook basics
		cover_btn = (ImageButton) v.findViewById(R.id.audiobook_basics_cover_btn);
		cover_btn.setImageDrawable(null);
		cover_btn.setOnClickListener(this);
		
		info_layout = (LinearLayout) v.findViewById(R.id.audiobook_basics_info_layout);
		info_layout.setOnClickListener(this);
		
		//Button bar
		play_btn = (ImageButton) v.findViewById(R.id.controller_play);
		play_btn.setImageDrawable(null);
		play_btn.setOnClickListener(this);

		upload_btn = (ImageButton) v.findViewById(R.id.controller_upload);
		upload_btn.setOnClickListener(this);

		download_btn = (ImageButton) v.findViewById(R.id.controller_download);
		download_btn.setOnClickListener(this);

		//Tracks
		next_btn = (ImageButton) v.findViewById(R.id.track_next);
		next_btn.setOnClickListener(this);

		prev_btn = (ImageButton) v.findViewById(R.id.track_previous);
		prev_btn.setOnClickListener(this);

		position_tv = (TextView) v.findViewById(R.id.track_position);
		if(Data.getPosition() != -1){
			position_tv.setText(String.format("%02d", Data.getPosition()+1));
		}

		title_tv = (TextView) v.findViewById(R.id.track_title);
		if(Data.getTrack() != null){
			title_tv.setText(Data.getTrack().getTitle());
		}

		tracks_gv = (LinearLayout) v.findViewById(R.id.tracks_grid);

		displayTracks();
		
		//Seeker
		forward_btn = (ImageButton) v.findViewById(R.id.seeker_fast_forward);
		forward_btn.setOnClickListener(this);

		rewind_btn = (ImageButton) v.findViewById(R.id.seeker_rewind);
		rewind_btn.setOnClickListener(this);
		
		progress_tv = (TextView) v.findViewById(R.id.seeker_progress_tv);
		progress_tv.setText(Time.toString(0));
		progress_tv.setOnClickListener(this);
		
		displayProgress();
		
		return v;
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////
		// Just for test
//		final Audiobook audiobook = Data.getAudiobook();
//		ImageButton t1 = (ImageButton) findViewById(R.id.controller_test1);
//		t1.setOnClickListener(new OnClickListener() {
//			@Override public void onClick(View v) {
//				Gson gson = new Gson();
//				Toast.makeText(ControllerActivity.this, gson.toJson(audiobook), Toast.LENGTH_SHORT).show();
//			}
//		});
//
//		ImageButton t2 = (ImageButton) findViewById(R.id.controller_test2);
//		t2.setOnClickListener(new OnClickListener() {
//			@Override public void onClick(View v) {
//				ArrayList<Bookmark> list = BookmarkManager.getInstance().loadBookmarks(getFilesDir());
//				String str = "";
//				for(Bookmark b : list) str += b.toString() + "\n";
//				Toast.makeText(ControllerActivity.this, str, Toast.LENGTH_LONG).show();
//			}
//		});
//
//		ImageButton t3 = (ImageButton) findViewById(R.id.controller_test3);
//		t3.setOnClickListener(new OnClickListener() {
//			@Override public void onClick(View arg0) {
//				Intent intent = new Intent(ControllerActivity.this, AudiobookGridFragment.class);
//				ControllerActivity.this.startActivity(intent);
//			}
//		});
		///////////////////////////////////////////////////////////////////////////////////////////////////
	}
	public void displayValues(){
		if(Data.getAudiobook() == null) return;
		if(Data.getTrack() == null) return;
		//Cover
		String cover = Data.getTrack().getCover();
		if(cover == null) cover = Data.getAudiobook().getCover();
		if(cover != null) {
			Bitmap bitmap = BitmapFactory.decodeFile(cover);
			cover_iv.setImageBitmap(bitmap);
		} else {
			cover_iv.setImageDrawable(noCover);
		}

		//Author
		author_tv.setText(Data.getAudiobook().getAuthor());

		//Album
		audiobook_basics_album_tv.setText(Data.getAudiobook().getAlbum());
	}
	public void displayTracks(){
		if(Data.getPosition() == -1 || Data.getTrack() == null || Data.getAudiobook() == null) return;
		//Position
		position_tv.setText(String.format("%02d", Data.getPosition()+1));

		//Track
		title_tv.setText(Data.getTrack().getTitle());

		//Tracks
		tracks_gv.removeAllViews();
		final int COLUMNS = 8;
		LinearLayout row = null;
		int m = LinearLayout.LayoutParams.MATCH_PARENT;
		int w = LinearLayout.LayoutParams.WRAP_CONTENT;
		LinearLayout.LayoutParams row_p = new LinearLayout.LayoutParams(m, w);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 85, 1);
		for(int i = 0; i < Data.getAudiobook().getPlaylist().size(); i++){
			if(i % COLUMNS == 0){
				row = new LinearLayout(getActivity());
				row.setOrientation(LinearLayout.HORIZONTAL);
				tracks_gv.addView(row, row_p);
			}
			TextView cell = new TextView(getActivity());
			cell.setTextColor(getResources().getColor(R.color.white));
			cell.setGravity(Gravity.CENTER);
			cell.setText(String.format("%02d", i+1));
			if(i == Data.getPosition()){
				cell.setBackground(getResources().getDrawable(R.drawable.circle));
			}
			cell.setId(CELL);
			cell.setTag(i); //Autoboxing
			cell.setOnClickListener(this);
			row.addView(cell, p);
		}
		if(Data.getAudiobook().getPlaylist().size() % COLUMNS > 0){
			Space space = new Space(getActivity());
			int weight = COLUMNS - (Data.getAudiobook().getPlaylist().size() % COLUMNS);
			LinearLayout.LayoutParams space_p = new LinearLayout.LayoutParams(0, 75, weight);
			row.addView(space, space_p);
		}
	}
	public void displayProgress(){
		if(player == null) return;
		int progress = player.getCurrentProgress();
		progress_tv.setText(Time.toString(progress));
	}
	
	@Override
	public void onClick(View v) {
		Log.d(TAG, "onClick");
		switch(v.getId()){
		
		//Cases for buttom button bar
		case R.id.controller_play:
			play_pause();
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
			
		//Cases for audiobook basics
		case R.id.audiobook_basics_cover_btn:
			play_pause();
			break;
		case R.id.audiobook_basics_info_layout:
			if(Data.getAudiobook() == null) return;
			Intent intent = new Intent(getActivity(), AudiobookActivity.class);
			intent.putExtra("state", STATE_EDIT);
			intent.putExtra("audiobook", Data.getAudiobook());
			System.out.println("Start edit audiobook");
			startActivityForResult(intent, REQUEST_FRAGMENT_BASICS_EDIT);
			break;
			
			
		//Cases for Tracks
		case R.id.track_next:
			if(player == null) return;
			//if currently playing the last track - do nothing
			if(Data.getAudiobook().getPlaylist().getLast().equals(Data.getTrack())) return;
			
			int nextPosition = Data.getPosition()+1;
			Track nextTrack = Data.getAudiobook().getPlaylist().get(nextPosition);
			Data.setPosition(nextPosition);
			Data.setTrack(nextTrack);
			//Fix view
			displayTracks();
			play_btn.setImageDrawable(drw_play);
			cover_btn.setImageDrawable(drw_play_on_cover);

			player.reload();
			break;

		case R.id.track_previous:
			if(player == null) return;
			//if currently playing the first track - do nothing
			if(Data.getAudiobook().getPlaylist().getFirst().equals(Data.getTrack())) return;
			
			int previousPosition = Data.getPosition()-1;
			Track previousTrack = Data.getAudiobook().getPlaylist().get(previousPosition);
			Data.setPosition(previousPosition);
			Data.setTrack(previousTrack);
			//Fix view
			displayTracks();
			play_btn.setImageDrawable(drw_play);
			cover_btn.setImageDrawable(drw_play_on_cover);
			
			player.reload();
			break;

		case CELL:
			if(player == null) return;
			if(v.getTag() == null) return;
			try{
				int i = ((Integer)v.getTag()).intValue();
				if(i >= 0 && i < Data.getAudiobook().getPlaylist().size()){
					Data.setPosition(i);
					Data.setTrack(Data.getAudiobook().getPlaylist().get(Data.getPosition()));
					//Fix view
					displayTracks();
					play_btn.setImageDrawable(drw_play);
					cover_btn.setImageDrawable(drw_play_on_cover);
					
					player.reload();
					break;
				}
			} catch (Exception e) { break; }
			break;
			
		//Cases for seeker
		case R.id.seeker_fast_forward:
			if(player == null) return;
			if(Data.getAudiobook() == null) return;
			if(Data.getTrack() == null) return;
			int ff_position = player.getCurrentProgress();
			int ff_duration = player.getDuration();
			int ff_newPos = 0;
			
			ff_newPos = Math.min(ff_position + (60 * 1000), ff_duration);
			if(ff_position == -1 || ff_duration == -1) return; 
			player.seekTo(ff_newPos);
			progress_tv.setText(Time.toString(ff_newPos));
			break;

		case R.id.seeker_rewind:
			if(player == null) return;
			if(Data.getAudiobook() == null) return;
			if(Data.getTrack() == null) return;
			int rew_position = player.getCurrentProgress();
			int rew_duration = player.getDuration();
			int rew_newPos = 0;

			rew_newPos = Math.max(rew_position - (60 * 1000), 0);
			if(rew_position == -1 || rew_duration == -1) return; 
			player.seekTo(rew_newPos);
			progress_tv.setText(Time.toString(rew_newPos));
			break;
			
		case R.id.seeker_progress_tv:
			break;
		}
	}
	public void onDriveResult(int requestCode, int result, Object... data){
		if(result != DriveHandler.SUCCESS) return;
		switch(requestCode){
		case REQUEST_CODE_UPLOAD: 
			Toast.makeText(getActivity(), "Upload successfull", Toast.LENGTH_SHORT).show();
			break;
		case REQUEST_CODE_DOWNLOAD: 
			//This is a passthrough
			currentBookmarkFile = (DriveFile) data[0];
			getContents(REQUEST_CODE_GET_CONTENTS, currentBookmarkFile);
			break;
		case REQUEST_CODE_GET_CONTENTS:
			String str = (String) data[0];
			Toast.makeText(getActivity(), ":::"+str, Toast.LENGTH_SHORT).show();
			break;
		case REQUEST_CODE_QUERY:
			@SuppressWarnings("unchecked")
			ArrayList<Metadata> list = (ArrayList<Metadata>) data[0];
			for(Metadata m : list){
				System.out.println("--->"+m.getTitle());
			}
		case REQUEST_CODE_UPDATE:
			Toast.makeText(getActivity(), "Update successfull", Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private void play_pause(){
		//Fix view
		play_btn.setImageDrawable(!player.isPlaying() ? drw_pause : drw_play);
		cover_btn.setImageDrawable(!player.isPlaying() ? drw_pause_on_cover : drw_play_on_cover);
		//Toggle play/pause
		player.toggle();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult :: forwarding to super and all fragments!");
		super.onActivityResult(requestCode, resultCode, data);
//FIXME		for(Fragment frag : fragments){
//			frag.onActivityResult(requestCode, resultCode, data);
//		}
		switch (requestCode) {
		case REQUEST_FRAGMENT_BASICS_EDIT:
			Log.d(TAG, "onActivityResult - REQUEST_FRAGMENT_BASICS_EDIT");
			if(resultCode == Activity.RESULT_OK){
				Audiobook result = (Audiobook) data.getSerializableExtra("result");
				Data.setAudiobook(result);
				displayValues();
			}
		}
	}
	
	@Override
	public void onStart(){
		Log.d(TAG, "onStart");
		super.onStart();
		//Bind to PlayerService
		Intent intent = new Intent(getActivity(), PlayerService.class);
		getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);

		//connect to Google Drive
		super.connect();
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
		public ControllerMonitor(int delay, TimeUnit unit) {
			super(delay, unit);
		}

		//		private static final String TAG = "ControllerActivity.Monitor";
		private boolean isPlaying = false;

		@Override
		public void execute() {
			isPlaying = player != null && player.isPlaying();

			if(getActivity() == null) return;
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(bound && Data.getAudiobook() != null){
						play_btn.setImageDrawable(isPlaying ? drw_pause : drw_play);
						cover_btn.setImageDrawable(isPlaying ? drw_pause_on_cover : drw_play_on_cover);
					} else {
						play_btn.setImageDrawable(null);
						cover_btn.setImageDrawable(null);
						cover_iv.setImageDrawable(noCover);
					}
					displayValues();
				}
			});
		}
	}
	class ProgressMonitor extends Monitor{
		public ProgressMonitor(int delay, TimeUnit unit) {
			super(delay, unit);
		}

		@Override
		public void execute() {
			if(progress_tv == null) return;
			if(player == null) return;
			if(getActivity() == null) return;
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					progress_tv.setText(Time.toString(player.getCurrentProgress()));
				}
			});
		}
	}
}
