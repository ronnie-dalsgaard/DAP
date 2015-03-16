package rd.dap.dialogs;

import java.util.ArrayList;

import rd.dap.R;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.support.Time;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class Dialog_disabled_bookmarks extends CustomDialog {
	private ArrayList<Bookmark> bookmarks;
	private Callback callback;
	
	public interface Callback {
		public void onBookmarkDeleted();
	}
	
	public Dialog_disabled_bookmarks(Activity activity, ViewGroup parent, ArrayList<Bookmark> bookmarks, Callback callback) {
		super(activity, parent);
		this.bookmarks = bookmarks;
		this.callback = callback;
	}

	public void show(){
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dv = inflater.inflate(R.layout.dialog_item_list, parent, false);

		//Title
		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
		title_tv.setText("Disabled bookmarks");

		//Exit button
		ImageButton exit_btn = (ImageButton) dv.findViewById(R.id.dialog_exit_btn);
		exit_btn.setOnClickListener(new ExitListener());

		//Cancel button
		Button left_btn = (Button) dv.findViewById(R.id.dialog_history_btn);
		left_btn.setText("Done");
		left_btn.setOnClickListener(new ExitListener());

		//List
		ListView lv = (ListView) dv.findViewById(R.id.dialog_history_list);
		lv.setDivider(activity.getResources().getDrawable(R.drawable.horizontal_divider));
		final DisabledBookmarkAdapter adapter = new DisabledBookmarkAdapter(activity, bookmarks);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Bookmark bookmark = bookmarks.get(position);
				
				new Dialog_delete_bookmark(activity, parent, bookmark, new Dialog_delete_bookmark.Callback() {
					@Override
					public void onDeleteBookmarkConfirmed() {
						//Remove the bookmark
						BookmarkManager.getInstance().removeBookmark(activity, bookmark);
						BookmarkManager bm = BookmarkManager.getInstance();
						bm.removeBookmark(activity, bookmark);
						adapter.notifyDataSetChanged();
						callback.onBookmarkDeleted();
					}
				}).show();
			}
		});

		dialog.setContentView(dv);
		dialog.show();
	}

	private class DisabledBookmarkAdapter extends ArrayAdapter<Bookmark> {
		private ArrayList<Bookmark> list;

		public DisabledBookmarkAdapter(Context context, ArrayList<Bookmark> list) {
			super(context, R.layout.audiobook_item, list);
			this.list = list;
		}

		@Override
		public int getCount(){
			return list.size();
		}

		@Override @SuppressLint("InflateParams")
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				LayoutInflater inflater;
				inflater = LayoutInflater.from(getContext());
				convertView = inflater.inflate(R.layout.bookmark_item_full, null);
				holder = new ViewHolder();
				holder.author_tv = (TextView) convertView.findViewById(R.id.audiobook_item_author_tv);
				holder.album_tv = (TextView) convertView.findViewById(R.id.audiobook_item_album_tv);
				holder.track_tv = (TextView) convertView.findViewById(R.id.audiobook_item_track_tv);
				holder.progress_tv = (TextView) convertView.findViewById(R.id.audiobook_item_progress_tv);
				convertView.setTag(holder);		        
			} else { holder = (ViewHolder) convertView.getTag(); }

			Bookmark bookmark = list.get(position);

			holder.author_tv.setText(bookmark.getAuthor());
			holder.album_tv.setText(bookmark.getAlbum());
			holder.track_tv.setText(bookmark.getTrackno()+1);
			holder.progress_tv.setText(Time.toString(bookmark.getProgress()));
			

			return convertView;
		}

		class ViewHolder {
			public TextView author_tv, album_tv, track_tv, progress_tv;
		}
	}

}
