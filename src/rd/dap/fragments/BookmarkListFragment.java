package rd.dap.fragments;

import static rd.dap.MainActivity.miniplayer;

import java.util.List;

import rd.dap.MainActivity.ChangeBookmarkDialogFragment;
import rd.dap.PlayerService;
import rd.dap.PlayerService.DAPBinder;
import rd.dap.PlayerService.PlayerObserver;
import rd.dap.R;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.Data;
import rd.dap.model.Track;
import rd.dap.support.Time;
import android.app.Activity;
import android.app.Fragment;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BookmarkListFragment extends Fragment implements 
	OnItemClickListener, OnItemLongClickListener, ServiceConnection, PlayerObserver {
	public static final String TAG = "BookmarkListActivity";
	public BookmarkAdapter adapter;
	private PlayerService player;
	private boolean bound = false;
	private ListView list;
	
	//TODO up-/download bookmarks
	//TODO Helper texts
	
	//TODO constant class (final class + private constructor)
	//TODO enable delete track
	//TODO mnually set bookmark
	
	//TODO Home folder
	//TODO when auto-detecting auidobook include subfolders 
	//TODO auto-detect all audiobooks
	//TODO Texts as resource
	//TODO sleeptimer
	//TODO pregress as progressbar
	//TODO author heading for audiobooks

	//Fragment must-haves
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		adapter = new BookmarkAdapter(getActivity(), R.layout.bookmark_item, Data.getBookmarks());
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View v = (ViewGroup) inflater.inflate(R.layout.activity_list_with_miniplayer, container, false);

		list = (ListView) v.findViewById(R.id.list_layout_lv);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
		
		System.out.println(Data.getBookmarks());
		
		return v;
	}
	
	//Helper method
	public void updateBookmark(Bookmark bookmark){
		Log.d(TAG, "updateBookmark");
		Activity activity = getActivity();
		if(activity == null) return;
		activity.runOnUiThread(new Runnable() {
			@Override public void run() {
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	//Listener
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		Log.d(TAG, "onItemClick");
		Bookmark bookmark = Data.getBookmarks().get(index);
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
		Log.d(TAG, "onItemLongClick");
		ChangeBookmarkDialogFragment frag = ChangeBookmarkDialogFragment.newInstance(position);
		frag.show(getFragmentManager(), "ChagenBookmarkDialog");
		return true; //Consume click
	}	

	//Adapter
	public BookmarkAdapter getAdapter() { return adapter; }
	public class BookmarkAdapter extends ArrayAdapter<Bookmark> {
		private List<Bookmark> bookmarks;

		public BookmarkAdapter(Context context, int resource, List<Bookmark> bookmarks) {
			super(context, resource, bookmarks);
			this.bookmarks = bookmarks;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			Log.d(TAG, "getView");
			ViewHolder holder;
			if(convertView == null){
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.bookmark_item, parent, false);
				//in an arrayAdapter 'attach' should always be false, as the view is attaced later on by the system.

				holder = new ViewHolder();
				holder.author_tv = (TextView) convertView.findViewById(R.id.bookmark_author_tv);
				holder.album_tv = (TextView) convertView.findViewById(R.id.bookmark_album_tv);
				holder.cover_iv = (ImageView) convertView.findViewById(R.id.bookmark_cover_iv);
				holder.info = (RelativeLayout) convertView.findViewById(R.id.bookmark_info_layout);
				holder.track_tv = (TextView) convertView.findViewById(R.id.bookmark_track_tv);
				holder.progress_tv = (TextView) convertView.findViewById(R.id.bookmark_progress_tv);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Bookmark bookmark = bookmarks.get(position);
			AudiobookManager am = AudiobookManager.getInstance();
			Audiobook audiobook = am.getAudiobook(bookmark);
			Track track = null;
			if(audiobook != null){ //In case the audiobook have been deleted
				track = audiobook.getPlaylist().get(bookmark.getTrackno());
				if(audiobook.getCover() != null){
					String cover = audiobook.getCover();
					Bitmap bm = BitmapFactory.decodeFile(cover);
					holder.cover_iv.setImageBitmap(bm);
				} else {
					Drawable drw = getResources().getDrawable(R.drawable.ic_action_help);
					holder.cover_iv.setImageDrawable(drw);
				}
			}
			holder.author_tv.setText(bookmark.getAuthor());
			holder.album_tv.setText(bookmark.getAlbum());
			if(track != null){ //In case the audiobook have been deleted
				String title = track.getTitle();
				if(title.length() > 28) title = title.substring(0, 25) + "...";
				holder.track_tv.setText(String.format("%02d", bookmark.getTrackno()+1) + " " + title);
			} else {
				holder.track_tv.setText(String.format("%02d", bookmark.getTrackno()+1));
			}
			holder.progress_tv.setText(Time.toShortString(bookmark.getProgress()));
			
			return convertView;
		}
	}
	static class ViewHolder {
		public TextView author_tv, album_tv, track_tv, progress_tv;
		public ImageView cover_iv;
		public RelativeLayout info;
	}

	//Connection must-haves
	@Override
	public void onStart(){
		Log.d(TAG, "onStart");
		super.onStart();
		//Bind to PlayerService
		Intent intent = new Intent(getActivity(), PlayerService.class);
		getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);	
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
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		DAPBinder binder = (DAPBinder) service;
		player = binder.getPlayerService();
		player.addObserver(this);
		bound = true;
	}
	@Override
	public void onServiceDisconnected(ComponentName name) {
		bound = false;
	}
}
