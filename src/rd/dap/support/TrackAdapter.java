package rd.dap.support;

import java.io.File;
import java.util.List;

import rd.dap.R;
import rd.dap.model.Track;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TrackAdapter extends ArrayAdapter<Track>{
	private List<Track> tracks;

	public TrackAdapter(Context context, int resource, List<Track> tracks) {
		super(context, resource, tracks);
		this.tracks = tracks;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.track_item, parent, false);
			//in an arrayAdapter 'attach' should always be false, as the view is attaced later on by the system.
			
			holder = new ViewHolder();
			holder.title_tv = (TextView) convertView.findViewById(R.id.track_item_title);
			holder.duration_tv = (TextView) convertView.findViewById(R.id.track_item_duration);
			holder.cover_iv = (ImageView) convertView.findViewById(R.id.track_item_cover);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Track track = tracks.get(position);
		
		//Title
		holder.title_tv.setText(track.getTitle());
		
		//Cover
		File cover = track.getCover();
		if(cover != null){
			Bitmap bm = BitmapFactory.decodeFile(cover.getAbsolutePath());
			holder.cover_iv.setImageBitmap(bm);
			holder.cover_iv.setVisibility(View.VISIBLE);
		} else {
			holder.cover_iv.setVisibility(View.GONE);
		}
		
		//Duration
		if(track.getDuration() >= 0){
			String _duration = Time.toShortString(track.getDuration());
			holder.duration_tv.setText(_duration);
			holder.duration_tv.setVisibility(View.VISIBLE);
		} else {
			holder.duration_tv.setVisibility(View.GONE);
		}
		
		return convertView;
	}

	static class ViewHolder {
		public TextView title_tv, duration_tv;
		public ImageView cover_iv;
	}
}
