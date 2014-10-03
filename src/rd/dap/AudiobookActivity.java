package rd.dap;

import static rd.dap.InputActivity.REQUEST_EDIT_ALBUM;
import static rd.dap.InputActivity.REQUEST_EDIT_AUTHOR;
import static rd.dap.InputActivity.REQUEST_EDIT_COVER;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rd.dap.model.Audiobook;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AudiobookActivity extends Activity {
	public static final int TYPE_COUNT = 4;
	public static final int NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK = 3; 
	public static final int TYPE_AUTHOR = 0;
	public static final int TYPE_ALBUM = 1;
	public static final int TYPE_COVER = 2;
	public static final int TYPE_TRACK = 3;
	
	private AudiobookDetailsAdapter adapter;
	private Audiobook audiobook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		audiobook = (Audiobook) getIntent().getExtras().getSerializable("audiobook");
		if(audiobook == null) throw new RuntimeException("No audiobook supplied");

		//detailslist
//		ListView list = new ListView(this);
//		Drawable divider = getResources().getDrawable(R.drawable.horizontal_divider);
//		list.setDivider(divider);
//		list.setDividerHeight(1);
//		setContentView(list);
		
		ListView list = (ListView) findViewById(R.id.main_list);
		adapter = new AudiobookDetailsAdapter(this, R.layout.details_item_track, audiobook.getPlaylist());
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
					intent.putExtra("value", audiobook.getAuthor());
					intent.putExtra("requestcode", REQUEST_EDIT_AUTHOR);
					startActivityForResult(intent, REQUEST_EDIT_AUTHOR);
				} else if(position == TYPE_ALBUM) {
					ArrayList<String> list = new ArrayList<String>();
//					list.add("Dennis Jürgensen");
//					list.add("Rick Riordan");
//					list.add("John G. Hemry");
					Intent intent = new Intent(AudiobookActivity.this, InputActivity.class);
					intent.putExtra("list", list);
					intent.putExtra("value", audiobook.getAlbum());
					intent.putExtra("requestcode", REQUEST_EDIT_ALBUM);
					startActivityForResult(intent, REQUEST_EDIT_ALBUM);
				} else if(position == TYPE_COVER) {
					Intent intent = new Intent(AudiobookActivity.this, FileBrowserActivity.class);
					intent.putExtra("type", "image");
					intent.putExtra("requestcode", REQUEST_EDIT_COVER);
					startActivityForResult(intent, REQUEST_EDIT_COVER);
				} else {
					Intent intent = new Intent(AudiobookActivity.this, TrackActivity.class);
					intent.putExtra("audiobook", audiobook);
					intent.putExtra("position", position-NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK);
					startActivity(intent);
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
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			switch(type){
			case TYPE_AUTHOR:
				AuthorViewHolder authorHolder;
				if(convertView == null){
					authorHolder = new AuthorViewHolder();
					convertView = inflater.inflate(R.layout.details_item_author, parent, false);
					authorHolder.author_item_tv = (TextView) convertView.findViewById(R.id.detail_item_author_tv);
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
				File cover = audiobook.getCover();
				if(cover != null){
					Bitmap bm = BitmapFactory.decodeFile(cover.getAbsolutePath());
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
					convertView = inflater.inflate(R.layout.details_item_track, parent, false);
					//in an arrayAdapter 'attach' should always be false, as the view is attaced later on by the system.

					trackHolder.track_item_title_tv = (TextView) convertView.findViewById(R.id.details_item_audiobook_track_title);
					trackHolder.track_item_duration_tv = (TextView) convertView.findViewById(R.id.details_item_audiobook_track_duration);
					trackHolder.track_item_cover_iv = (ImageView) convertView.findViewById(R.id.details_item_audiobook_track_cover);
					convertView.setTag(trackHolder);
				} else {
					trackHolder = (TrackViewHolder) convertView.getTag();
				}
				Track track = tracks.get(position-3);

				//Title
				trackHolder.track_item_title_tv.setText(track.getTitle());

				//Cover
				File track_cover = track.getCover();
				if(track_cover != null){
					Bitmap bm = BitmapFactory.decodeFile(track_cover.getAbsolutePath());
					trackHolder.track_item_cover_iv.setImageBitmap(bm);
					trackHolder.track_item_cover_iv.setVisibility(View.VISIBLE);
				} else {
					trackHolder.track_item_cover_iv.setVisibility(View.GONE);
				}

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
		public TextView track_item_title_tv, track_item_duration_tv;
		public ImageView track_item_cover_iv;
	}
}