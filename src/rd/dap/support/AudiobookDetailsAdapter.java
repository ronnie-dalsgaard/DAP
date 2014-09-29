package rd.dap.support;

import java.io.File;
import java.util.List;

import rd.dap.R;
import rd.dap.model.Audiobook;
import rd.dap.model.Track;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AudiobookDetailsAdapter extends ArrayAdapter<Track>{
	public static final int TYPE_COUNT = 4, NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK = 3, 
			TYPE_AUTHOR = 0, TYPE_ALBUM = 1, TYPE_COVER = 2, TYPE_TRACK = 3;
	
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
		int type = getItemViewType(position);
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		switch(type){
		case TYPE_AUTHOR:
			AuthorViewHolder authorHolder;
			if(convertView == null){
				authorHolder = new AuthorViewHolder();
				convertView = inflater.inflate(R.layout.audiobook_item_author, parent, false);
				authorHolder.author_item_tv = (TextView) convertView.findViewById(R.id.author_item_tv);
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
				convertView = inflater.inflate(R.layout.audiobook_item_album, parent, false);
				albumHolder.album_item_tv = (TextView) convertView.findViewById(R.id.album_item_tv);
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
				convertView = inflater.inflate(R.layout.audiobook_item_cover, parent, false);
				coverHolder.cover_item_iv = (ImageView) convertView.findViewById(R.id.cover_item_iv);
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
				convertView = inflater.inflate(R.layout.audiobook_item_track, parent, false);
				//in an arrayAdapter 'attach' should always be false, as the view is attaced later on by the system.

				trackHolder.track_item_title_tv = (TextView) convertView.findViewById(R.id.track_item_title);
				trackHolder.track_item_duration_tv = (TextView) convertView.findViewById(R.id.track_item_duration);
				trackHolder.track_item_cover_iv = (ImageView) convertView.findViewById(R.id.track_item_cover);
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
