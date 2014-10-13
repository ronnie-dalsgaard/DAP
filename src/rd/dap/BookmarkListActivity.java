package rd.dap;

import java.util.ArrayList;
import java.util.List;

import rd.dap.fragments.FragmentMiniPlayer;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.model.Data;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BookmarkListActivity extends Fragment implements OnItemClickListener, OnItemLongClickListener {
	public static final String TAG = "BookmarkListActivity";
	public static ArrayAdapter<Bookmark> adapter;
	public static ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
	public static FragmentMiniPlayer miniplayer;
	
	
	//TODO Bookmark activity
	//TODO up-/download bookmarks
	//TODO remove all activity titles
	
	//TODO use tabs/swipe to navigate
	//TODO constant class (final class + private constructor)
	//TODO enable delete track
	
	//TODO Home folder
	//TODO auto-detect all audiobooks
	//TODO Texts as resource
	//TODO sleeptimer
	//TODO pregress as progressbar
	//TODO Helper texts

	public BookmarkListActivity(FragmentMiniPlayer miniplayer) {
		BookmarkListActivity.miniplayer = miniplayer;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_list_with_miniplayer);
		
		adapter = new BookmarkAdapter(getActivity(), R.layout.audiobook_item, bookmarks);
		
		new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				BookmarkManager bm = BookmarkManager.getInstance();
				ArrayList<Bookmark> loadedList = bm.loadBookmarks(getActivity().getFilesDir()); 
				bookmarks.clear();
				bookmarks.addAll(loadedList);
				return null;
			}
			@Override 
			protected void onPostExecute(Void result){
				getActivity().runOnUiThread(new Runnable() {
					@Override public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			}
		}.execute();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View v = (ViewGroup) inflater.inflate(R.layout.activity_list_with_miniplayer, container, false);

		ListView list = (ListView) v.findViewById(R.id.list_layout_lv);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
		
		return v;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		Log.d(TAG, "onItemClick");
		Bookmark bookmark = bookmarks.get(index);
		AudiobookManager manager = AudiobookManager.getInstance();
		Audiobook audiobook = manager.getAudiobook(bookmark);
		Data.setAudiobook(audiobook);
		Data.setPosition(bookmark.getTrackno());
		Data.setTrack(audiobook.getPlaylist().get(bookmark.getTrackno()));
		miniplayer.setVisibility(Data.getAudiobook() == null ? View.GONE : View.VISIBLE);
		miniplayer.reload();
		miniplayer.seekTo(bookmark.getProgress());
		miniplayer.updateView();
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		return true; //Consume click
	}	

	class BookmarkAdapter extends ArrayAdapter<Bookmark> {
		private List<Bookmark> bookmarks;

		public BookmarkAdapter(Context context, int resource, List<Bookmark> bookmarks) {
			super(context, resource, bookmarks);
			this.bookmarks = bookmarks;
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
				holder.cover_iv = (ImageView) convertView.findViewById(R.id.audiobook_item_cover_iv);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Bookmark bookmark = bookmarks.get(position);
			AudiobookManager am = AudiobookManager.getInstance();
			Audiobook audiobook = am.getAudiobook(bookmark);
			holder.author_tv.setText(bookmark.getAuthor());
			holder.album_tv.setText(bookmark.getAlbum());
			if(audiobook.getCover() != null){
				String cover = audiobook.getCover();
				Bitmap bm = BitmapFactory.decodeFile(cover);
				holder.cover_iv.setImageBitmap(bm);
			} else {
				Drawable drw = getResources().getDrawable(R.drawable.ic_action_help);
				holder.cover_iv.setImageDrawable(drw);
			}

			return convertView;
		}
	}
	static class ViewHolder {
		public TextView author_tv, album_tv;
		public ImageView cover_iv;
	}
}
