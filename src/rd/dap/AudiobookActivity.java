package rd.dap;

import static rd.dap.support.AudiobookDetailsAdapter.NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK;
import static rd.dap.support.AudiobookDetailsAdapter.TYPE_ALBUM;
import static rd.dap.support.AudiobookDetailsAdapter.TYPE_AUTHOR;
import static rd.dap.support.AudiobookDetailsAdapter.TYPE_COVER;

import java.io.File;
import java.util.ArrayList;

import rd.dap.model.Audiobook;
import rd.dap.model.Track;
import rd.dap.support.AudiobookDetailsAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class AudiobookActivity extends Activity {
	private static final int REQUEST_EDIT_AUTHOR = 1000;
	private static final int REQUEST_EDIT_ALBUM = 1001;
	private static final int REQUEST_EDIT_COVER = 1002;
	private static final int REQUEST_EDIT_TRACK = 1003;
	private AudiobookDetailsAdapter adapter;
	private Audiobook audiobook;
	private int selectedPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		audiobook = (Audiobook) getIntent().getExtras().getSerializable("audiobook");
		if(audiobook == null) return;

		//detailslist
		ListView list = new ListView(AudiobookActivity.this);
		ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.light_gray));
		list.setDivider(colorDrawable);
		list.setDividerHeight(1);
		setContentView(list);
		adapter = new AudiobookDetailsAdapter(this, R.layout.audiobook_details_item_track, audiobook.getPlaylist());
		adapter.setAudiobook(audiobook);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(position == TYPE_AUTHOR){
					ArrayList<String> list = new ArrayList<String>();
					list.add("Dennis Jürgensen");
					list.add("Rick Riordan");
					list.add("John G. Hemry");
					Intent intent = new Intent(AudiobookActivity.this, InputActivity.class);
					intent.putExtra("list", list);
					intent.putExtra("requestcode", REQUEST_EDIT_AUTHOR);
					startActivityForResult(intent, REQUEST_EDIT_AUTHOR);
				} else if(position == TYPE_ALBUM) {
					ArrayList<String> list = new ArrayList<String>();
//					list.add("Dennis Jürgensen");
//					list.add("Rick Riordan");
//					list.add("John G. Hemry");
					Intent intent = new Intent(AudiobookActivity.this, InputActivity.class);
					intent.putExtra("list", list);
					intent.putExtra("requestcode", REQUEST_EDIT_ALBUM);
					startActivityForResult(intent, REQUEST_EDIT_ALBUM);
				} else if(position == TYPE_COVER) {
					Intent intent = new Intent(AudiobookActivity.this, FileBrowserActivity.class);
					intent.putExtra("requestcode", REQUEST_EDIT_COVER);
					startActivityForResult(intent, REQUEST_EDIT_COVER);
				} else {
					ArrayList<String> list = new ArrayList<String>();
//					list.add("Dennis Jürgensen");
//					list.add("Rick Riordan");
//					list.add("John G. Hemry");
					Intent intent = new Intent(AudiobookActivity.this, InputActivity.class);
					intent.putExtra("list", list);
					selectedPosition = position-NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK;
					intent.putExtra("requestcode", REQUEST_EDIT_TRACK);
					startActivityForResult(intent, REQUEST_EDIT_TRACK);
				}
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(data == null) return;
		String result;
		switch(requestCode){
		case REQUEST_EDIT_AUTHOR:
			result = data.getStringExtra("result");
			audiobook.setAuthor(result);
			adapter.notifyDataSetChanged();
			break;
		case REQUEST_EDIT_ALBUM:
			result = data.getStringExtra("result");
			audiobook.setAlbum(result);
			adapter.notifyDataSetChanged();
			break;
		case REQUEST_EDIT_COVER:
			result = data.getStringExtra("result");
			File cover = new File(result);
			audiobook.setCover(cover);
			adapter.notifyDataSetChanged();
			break;
		case REQUEST_EDIT_TRACK:
			result = data.getStringExtra("result");
			Track track = audiobook.getPlaylist().get(selectedPosition);
			track.setTitle(result);
			adapter.notifyDataSetChanged();
			break;
		}
	}
}