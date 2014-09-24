package rd.dap.support;

import java.io.File;
import java.util.List;

import rd.dap.R;
import rd.dap.model.Audiobook;
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

public class AudiobookAdapter extends ArrayAdapter<Audiobook> {
	private Context context;
	private List<Audiobook> audiobooks;
	
	public AudiobookAdapter(Context context, int resource, List<Audiobook> audiobooks) {
		super(context, resource, audiobooks);
		this.context = context;
		this.audiobooks = audiobooks;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.audiobook_item, parent, false);
			//in an arrayAdapter 'attach' should always be false, as the view is attaced later on by the system.
			
			holder = new ViewHolder();
			holder.author_tv = (TextView) convertView.findViewById(R.id.audiobook_item_author_tv);
			holder.album_tv = (TextView) convertView.findViewById(R.id.audiobook_item_title_tv);
			holder.track_tv = (TextView) convertView.findViewById(R.id.audiobook_item_track_tv);
			holder.cover_iv = (ImageView) convertView.findViewById(R.id.audiobook_item_cover_iv);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Audiobook audiobook = audiobooks.get(position);
		holder.author_tv.setText(audiobook.getAuthor());
		holder.album_tv.setText(audiobook.getAlbum());
		if(audiobook.getCover() != null){
			File cover = audiobook.getCover();
			Bitmap bm = BitmapFactory.decodeFile(cover.getAbsolutePath());
			holder.cover_iv.setImageBitmap(bm);
		} else {
			Drawable drw = context.getResources().getDrawable(R.drawable.ic_action_help);
			holder.cover_iv.setImageDrawable(drw);
		}
		
		
		return convertView;
	}
	
	static class ViewHolder {
		public TextView author_tv, album_tv, track_tv;
		public ImageView cover_iv;
	}
}
