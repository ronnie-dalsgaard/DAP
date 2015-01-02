package rd.dap.activities;

import static rd.dap.activities.InputActivity.REQUEST_EDIT_ALBUM;
import static rd.dap.activities.InputActivity.REQUEST_EDIT_AUTHOR;
import static rd.dap.activities.InputActivity.REQUEST_EDIT_COVER;
import static rd.dap.activities.InputActivity.REQUEST_EDIT_TRACK;

import java.util.ArrayList;
import java.util.List;

import rd.dap.R;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Track;
import rd.dap.support.Time;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class AudiobookActivity extends Activity implements OnItemClickListener, OnClickListener{
	private static final String TAG = "AudiobookActivity";
	public static final int TYPE_COUNT = 4;
	public static final int NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK = 3; 
	public static final int TYPE_AUTHOR = 0;
	public static final int TYPE_ALBUM = 1;
	public static final int TYPE_COVER = 2;
	public static final int TYPE_TRACK = 3;
	
	public static final int STATE_NEW = 501;
	public static final int STATE_EDIT = 502;
	
	private AudiobookDetailsAdapter adapter;
	private Audiobook audiobook, original_audiobook;
	private int state = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		ListView list = new ListView(this);
		setContentView(list);

		original_audiobook = (Audiobook) getIntent().getExtras().getSerializable("audiobook");
		if(original_audiobook == null) throw new RuntimeException("No audiobook supplied");
		audiobook = new Audiobook(original_audiobook); //defensive copy
		
		state = getIntent().getIntExtra("state", 0);
		if(state == 0) throw new RuntimeException("State not provided");
		
		//List
		list.setDivider(getResources().getDrawable(R.drawable.horizontal_divider));
		adapter = new AudiobookDetailsAdapter(this, R.layout.item_track, audiobook.getPlaylist());
		adapter.setAudiobook(audiobook);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		
		//Header
		LayoutInflater inflater = LayoutInflater.from(this);
		LinearLayout header = (LinearLayout) inflater.inflate(R.layout.audiobook_header_buttons, list, false);
		list.addHeaderView(header);

		//Save button
		ImageButton save_btn = (ImageButton) header.findViewById(R.id.audiobook_header_save_btn);
		save_btn.setOnClickListener(this);
		
		//Cancel button
		ImageButton cancel_btn = (ImageButton) header.findViewById(R.id.audiobook_header_cancel_btn);
		cancel_btn.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.audiobook_header_save_btn:
			if(audiobook != null){
				AudiobookManager manager = AudiobookManager.getInstance();
				if(state == STATE_NEW) { manager.addAudiobook(this, audiobook); }
				else { manager.updateAudiobook(this, audiobook, original_audiobook); }
			}
			break;
		case R.id.audiobook_header_cancel_btn:
			audiobook.setAudiobook(original_audiobook);
			break;
		}
		Log.d(TAG, "Returning result: "+audiobook);
		Intent data = new Intent();
		data.putExtra("result", audiobook);
		setResult(Activity.RESULT_OK, data);
		finish();
		
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position--;
		if(position == TYPE_AUTHOR){
			ArrayList<String> list = new ArrayList<String>();
			list.addAll(AudiobookManager.getInstance().getAuthors());
			Intent intent = new Intent(AudiobookActivity.this, InputActivity.class);
			intent.putExtra("list", list);
			intent.putExtra("value", audiobook.getAuthor());
			intent.putExtra("requestcode", REQUEST_EDIT_AUTHOR);
			startActivityForResult(intent, REQUEST_EDIT_AUTHOR);
		} else if(position == TYPE_ALBUM) {
			ArrayList<String> list = new ArrayList<String>();
			list.addAll(AudiobookManager.getInstance().getAlbums());
			Intent intent = new Intent(AudiobookActivity.this, InputActivity.class);
			intent.putExtra("list", list);
			intent.putExtra("value", audiobook.getAlbum());
			intent.putExtra("requestcode", REQUEST_EDIT_ALBUM);
			startActivityForResult(intent, REQUEST_EDIT_ALBUM);
		} else if(position == TYPE_COVER) {
			Intent intent = new Intent(AudiobookActivity.this, FileBrowserActivity.class);
			intent.putExtra("type", "image");
			intent.putExtra("message", "Select image file");
			intent.putExtra("requestcode", REQUEST_EDIT_COVER);
			startActivityForResult(intent, REQUEST_EDIT_COVER);
		} else {
			Intent intent = new Intent(AudiobookActivity.this, TrackActivity.class);
			intent.putExtra("audiobook", audiobook);
			intent.putExtra("position", position-NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK);
			startActivityForResult(intent, REQUEST_EDIT_TRACK);
		}
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(data == null){
			Log.d(TAG, "onActivityResult - NO DATA");
			return;
		}
		String result;
		switch(requestCode){
		case REQUEST_EDIT_AUTHOR:
			result = data.getStringExtra("result");
			Log.d(TAG, "onActivityResult - REQUEST_EDIT_AUTHOR : "+result);
			audiobook.setAuthor(result);
			adapter.notifyDataSetChanged();
			break;
		case REQUEST_EDIT_ALBUM:
			result = data.getStringExtra("result");
			Log.d(TAG, "onActivityResult - REQUEST_EDIT_ALBUM : "+result);
			audiobook.setAlbum(result);
			adapter.notifyDataSetChanged();
			break;
		case REQUEST_EDIT_COVER:
			result = data.getStringExtra("result");
			Log.d(TAG, "onActivityResult - REQUEST_EDIT_COVER : "+result);
			audiobook.setCover(result);
			adapter.notifyDataSetChanged();
			break;
		}
	}
	
	class AudiobookDetailsAdapter extends ArrayAdapter<Track>{
		private final String TAG = "AudiobookDetailsAdapter";
		private Context context;
		private Audiobook audiobook;
		private List<Track> tracks;

		public AudiobookDetailsAdapter(Context context, int resource, List<Track> tracks) {
			super(context, resource, tracks);
			this.context = context;
			this.tracks = tracks;
		}

		public void setAudiobook(Audiobook audiobook) {
			this.audiobook = audiobook;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "getView");
			int type = getItemViewType(position);
			LayoutInflater inflater = LayoutInflater.from(getContext());
			switch(type){
			case TYPE_AUTHOR:
				AuthorViewHolder authorHolder;
				if(convertView == null){
					authorHolder = new AuthorViewHolder();
					convertView = inflater.inflate(R.layout.details_item_author, parent, false);
					authorHolder.author_item_tv = (TextView) convertView.findViewById(R.id.details_item_author_tv);
					convertView.setTag(authorHolder);
				} else {
					authorHolder = (AuthorViewHolder) convertView.getTag();
				}
				authorHolder.author_item_tv.setText(audiobook.getAuthor());
				break;

			case TYPE_ALBUM:
				AlbumViewHolder albumHolder;
				if(convertView == null){
					albumHolder = new AlbumViewHolder();
					convertView = inflater.inflate(R.layout.details_item_album, parent, false);
					albumHolder.album_item_tv = (TextView) convertView.findViewById(R.id.details_item_album_tv);
					convertView.setTag(albumHolder);
				} else {
					albumHolder = (AlbumViewHolder) convertView.getTag();
				}
				albumHolder.album_item_tv.setText(audiobook.getAlbum());
				break;

			case TYPE_COVER:
				CoverViewHolder coverHolder;
				if(convertView == null){
					coverHolder = new CoverViewHolder();
					convertView = inflater.inflate(R.layout.details_item_cover_large, parent, false);
					coverHolder.cover_item_iv = (ImageView) convertView.findViewById(R.id.details_item_cover_iv);
					convertView.setTag(coverHolder);
				} else {
					coverHolder = (CoverViewHolder) convertView.getTag();
				}
				String cover = audiobook.getCover();
				if(cover != null){
					Bitmap bm = BitmapFactory.decodeFile(cover);
					coverHolder.cover_item_iv.setImageBitmap(bm);
				} else {
					Drawable drw = context.getResources().getDrawable(R.drawable.ic_action_help);
					coverHolder.cover_item_iv.setImageDrawable(drw);
				}
				break;

			case TYPE_TRACK:
				TrackViewHolder trackHolder;
				if(convertView == null){
					trackHolder = new TrackViewHolder();
					convertView = inflater.inflate(R.layout.item_track, parent, false);
					//in an arrayAdapter 'attach' should always be false, as the view is attaced later on by the system.

					trackHolder.track_item_title_tv = (TextView) convertView.findViewById(R.id.details_item_audiobook_track_title);
					trackHolder.track_item_duration_tv = (TextView) convertView.findViewById(R.id.details_item_audiobook_track_duration);
					trackHolder.track_item_position_tv = (TextView) convertView.findViewById(R.id.details_item_audiobook_track_position);  
					convertView.setTag(trackHolder);
				} else {
					trackHolder = (TrackViewHolder) convertView.getTag();
				}
				int trackno = position-3;
				
				Track track = tracks.get(trackno);
				
				//trackno
				trackHolder.track_item_position_tv.setText(String.format("%02d", trackno+1));
				
				//Title
				trackHolder.track_item_title_tv.setText(track.getTitle());

				//Duration
				if(track.getDuration() >= 0){
					String _duration = Time.toShortString(track.getDuration());
					trackHolder.track_item_duration_tv.setText(_duration);
					trackHolder.track_item_duration_tv.setVisibility(View.VISIBLE);
				} else {
					trackHolder.track_item_duration_tv.setVisibility(View.GONE);
				}
				break;
			}
			return convertView;
		}

		@Override
		public int getItemViewType(int position) {
			if(position >= TYPE_TRACK) return TYPE_TRACK;
			else return position;
		}

		@Override
		public int getViewTypeCount() { return TYPE_COUNT; }
		
		@Override
	    public int getCount() { return tracks.size() + NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK; }
	}
	static class AuthorViewHolder {
		public TextView author_item_tv;
	}
	static class AlbumViewHolder {
		public TextView album_item_tv;
	}
	static class CoverViewHolder {
		public ImageView cover_item_iv;
	}
	static class TrackViewHolder {
		public TextView track_item_position_tv, track_item_title_tv, track_item_duration_tv;
	}

}