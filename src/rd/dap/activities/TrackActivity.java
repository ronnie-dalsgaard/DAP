package rd.dap.activities;

import static rd.dap.activities.InputActivity.REQUEST_EDIT_TRACK_FILE;
import static rd.dap.activities.InputActivity.REQUEST_EDIT_TRACK_TITLE;

import java.util.ArrayList;

import rd.dap.R;
import rd.dap.model.Audiobook;
import rd.dap.model.Track;
import rd.dap.support.Time;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class TrackActivity extends Activity {
	private Audiobook audiobook;
	private int position;
	private TextView position_tv, title_tv, duration_tv, file_tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track);

		//Verify input
		audiobook = (Audiobook) getIntent().getSerializableExtra("audiobook");
		if(audiobook == null) throw new RuntimeException("No audiobook supplied");

		position = getIntent().getIntExtra("position", -1);
		if(position == -1) throw new RuntimeException("No position supplied");

		//Track
		final Track track = audiobook.getPlaylist().get(position);

		//Position
		position_tv = (TextView) findViewById(R.id.details_item_track_position);
		position_tv.setText(String.format("%02d", position+1));

		//Title
		title_tv = (TextView) findViewById(R.id.details_item_track_title);
		title_tv.setText(track.getTitle());
		title_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<String> list = new ArrayList<String>();
//				list.add("Dennis Jürgensen");
//				list.add("Rick Riordan");
//				list.add("John G. Hemry");
				Intent intent = new Intent(TrackActivity.this, InputActivity.class);
				intent.putExtra("list", list);
				intent.putExtra("value", track.getTitle());
				intent.putExtra("requestcode", REQUEST_EDIT_TRACK_TITLE);
				startActivityForResult(intent, REQUEST_EDIT_TRACK_TITLE);
			}
		});

		//Duration
		duration_tv = (TextView) findViewById(R.id.track_duration);
		if(track.getDuration() >= 0){
			String _duration = Time.toShortString(track.getDuration());
			duration_tv.setText(_duration);
			duration_tv.setVisibility(View.VISIBLE);
		} else {
			duration_tv.setVisibility(View.GONE);
		}

		//File
		file_tv = (TextView) findViewById(R.id.details_item_file_tv);
		file_tv.setText(track.getPath());
		file_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TrackActivity.this, FileBrowserActivity.class);
				intent.putExtra("type", "audio");
				intent.putExtra("message", "Select audio file");
				intent.putExtra("requestcode", REQUEST_EDIT_TRACK_FILE);
				startActivityForResult(intent, REQUEST_EDIT_TRACK_FILE);
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(data == null) return;
		String result;
		switch(requestCode){
		case REQUEST_EDIT_TRACK_TITLE:
			result = data.getStringExtra("result");
			audiobook.getPlaylist().get(position).setTitle(result);
			title_tv.setText(result);
			break;
		case REQUEST_EDIT_TRACK_FILE:
			result = data.getStringExtra("result");
			audiobook.getPlaylist().get(position).setPath(result);
			file_tv.setText(result);
			break;
		}
	}
}
