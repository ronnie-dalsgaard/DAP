//package rd.dap.fragments;
//
//import java.util.List;
//
//import rd.dap.R;
//import rd.dap.model.Audiobook;
//import rd.dap.model.AudiobookManager;
//import rd.dap.model.Bookmark;
//import rd.dap.model.BookmarkManager;
//import rd.dap.model.Callback;
//import rd.dap.model.Data;
//import rd.dap.model.DriveHandler;
//import rd.dap.model.Track;
//import rd.dap.services.PlayerService;
//import rd.dap.services.PlayerService.DAPBinder;
//import rd.dap.services.PlayerService.PlayerObserver;
//import rd.dap.support.Time;
//import android.app.Activity;
//import android.app.Dialog;
//import android.app.Fragment;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.drawable.Drawable;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.AdapterView.OnItemLongClickListener;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.gson.Gson;
//
//public class BookmarkListFragment extends Fragment implements 
//	OnItemClickListener, OnItemLongClickListener, ServiceConnection, PlayerObserver {
//	public static final String TAG = "BookmarkListActivity";
//	public BookmarkAdapter adapter;
//	private PlayerService player;
//	private boolean bound = false;
//	private ListView list;
//	private Activity activity;
//	private Changer changer; //Needed when bookmarks can be added or changed manually
//	private DriveHandler drivehandler;
//	private static final String END = "/END";
//	
//	//TODO Helper texts
//	//TODO show progress when down-/uploading
//	
//	//TODO constant class (final class + private constructor)
//	//TODO enable delete track
//	//TODO position instead of cover on tracks
//	//TODO manually set bookmark
//	
//	//TODO Home folder
//	//TODO when auto-detecting auidobook include subfolders 
//	//TODO auto-detect all audiobooks
//	//TODO Texts as resource
//	//TODO sleeptimer
//	//TODO pregress as progressbar
//	//TODO author heading for audiobooks
//	
//	@Override
//	public void onAttach(Activity activity){
//		super.onAttach(activity);
//		this.activity = activity;
//		try {
//            changer = (Changer) activity;
//            drivehandler = (DriveHandler) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement Callback and DriveHandler");
//        }
//	}
//	
//	//Fragment must-haves
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		Log.d(TAG, "onCreate");
//		super.onCreate(savedInstanceState);
//		
//		adapter = new BookmarkAdapter(getActivity(), R.layout.bookmark_item, Data.getBookmarks());
//		
//		setHasOptionsMenu(true);
//	}
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		Log.d(TAG, "onCreateView");
//		View v = (ViewGroup) inflater.inflate(R.layout.activity_list_with_miniplayer, container, false);
//
//		list = (ListView) v.findViewById(R.id.list_layout_lv);
//		list.setAdapter(adapter);
//		list.setOnItemClickListener(this);
//		list.setOnItemLongClickListener(this);
//		
//		return v;
//	}
//	
//	//Helper method
//	public void updateBookmark(Bookmark bookmark){
//		Log.d(TAG, "updateBookmark");
//		if(activity == null) return;
//		activity.runOnUiThread(new Runnable() {
//			@Override public void run() {
//				adapter.notifyDataSetChanged();
//			}
//		});
//	}
//
//	//Menu
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		inflater.inflate(R.menu.bookmarks, menu);
//	}
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch(item.getItemId()){
//		case R.id.menu_item_upload:
//			Gson gson = new Gson();
//			String json = "";
//			for(Bookmark bookmark : Data.getBookmarks()){
//				json += gson.toJson(bookmark) + END + "\n";
//			}
//			Log.d(TAG, "onClick - upload: "+json);
//			drivehandler.upload(json); 
//			break;
//
//		case R.id.menu_item_download:
//			drivehandler.download(new Callback<String>() { 
//				@Override public void onResult(String result) {
//					Log.d(TAG, "onClick - download: "+result);
//					if(result == null || result.isEmpty()) return;
//					BookmarkManager bm = BookmarkManager.getInstance();
//					AudiobookManager am = AudiobookManager.getInstance();
//					Gson gson = new Gson();
//					boolean changesHappened = false;
//					for(String line : result.split(END)){
//						System.out.println("Line = "+line);
//						Bookmark fetched = gson.fromJson(line, Bookmark.class);
//						Audiobook fetchedAudiobook = am.getAudiobook(fetched);
//						if(bm.hasBookmark(fetched)){
//							Bookmark exisisting = bm.getBookmark(fetched.getAuthor(), fetched.getAlbum());
//							System.out.println("Bookmark:\n"+exisisting);
//							System.out.println("Fetched:\n"+fetched);
//							System.out.println("fetched.compareTo(existing) = "+fetched.compareTo(exisisting));
//
//							if(fetched.compareTo(exisisting) > 0){
//								exisisting.setTrackno(fetched.getTrackno());
//								exisisting.setProgress(fetched.getProgress());
//								changesHappened = true;
//
//								//fetched is current
//								Audiobook audiobook = am.getAudiobook(fetched);
//								Data.setCurrentAudiobook(audiobook);
//								Data.setCurrentPosition(fetched.getTrackno());
//								Data.setCurrentTrack(audiobook.getPlaylist().get(fetched.getTrackno()));
//
//								//reload using miniplayer as pass-through
//								miniplayer.reload();
//								miniplayer.seekTo(fetched.getProgress());
//							}
//						} else if(fetchedAudiobook != null){
//							bm.createOrUpdateBookmark(getActivity().getFilesDir(), fetched, false);
//							changesHappened = true;
//						}
//					}
//					if(changesHappened){
//						changer.updateBookmarks();
//						changer.updateController();
//					}
//				}
//			});
//			break;
//
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	//Listener
//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
//		Log.d(TAG, "onItemClick");
//		Bookmark bookmark = Data.getBookmarks().get(index);
//		AudiobookManager manager = AudiobookManager.getInstance();
//		Audiobook audiobook = manager.getAudiobook(bookmark);
//		Data.setCurrentAudiobook(audiobook);
//		Data.setCurrentPosition(bookmark.getTrackno());
//		Data.setCurrentTrack(audiobook.getPlaylist().get(bookmark.getTrackno()));
//
//		SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
//		Editor editor = pref.edit();
//		editor.putString("author", audiobook.getAuthor());
//		editor.putString("album", audiobook.getAlbum());
//		editor.commit();
//		
//		miniplayer.setVisibility(Data.getCurrentAudiobook() == null ? View.GONE : View.VISIBLE);
//		miniplayer.reload();
//		miniplayer.seekTo(bookmark.getProgress());
//		miniplayer.updateView();
//		
//		changer.updateAudiobooks();
//		changer.updateBookmarks();
//		changer.updateController();
//	}
//	@Override
//	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//		Log.d(TAG, "onItemLongClick");
//		
//		Bookmark bookmark = Data.getBookmarks().get(position);
//		changeBookmarkDialog(bookmark);
//		return true; //Consume click
//	}	
//
//	//Adapter
//	public BookmarkAdapter getAdapter() { return adapter; }
//	public class BookmarkAdapter extends ArrayAdapter<Bookmark> {
//		private List<Bookmark> bookmarks;
//
//		public BookmarkAdapter(Context context, int resource, List<Bookmark> bookmarks) {
//			super(context, resource, bookmarks);
//			this.bookmarks = bookmarks;
//		}
//		
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent){
//			ViewHolder holder;
//			if(convertView == null){
//				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				convertView = inflater.inflate(R.layout.bookmark_item, parent, false);
//				//in an arrayAdapter 'attach' should always be false, as the view is attaced later on by the system.
//
//				holder = new ViewHolder();
//				holder.author_tv = (TextView) convertView.findViewById(R.id.bookmark_author_tv);
//				holder.album_tv = (TextView) convertView.findViewById(R.id.bookmark_album_tv);
//				holder.cover_iv = (ImageView) convertView.findViewById(R.id.bookmark_cover_iv);
//				holder.info = (RelativeLayout) convertView.findViewById(R.id.bookmark_info_layout);
//				holder.track_tv = (TextView) convertView.findViewById(R.id.bookmark_track_tv);
//				holder.progress_tv = (TextView) convertView.findViewById(R.id.bookmark_progress_tv);
//
//				convertView.setTag(holder);
//			} else {
//				holder = (ViewHolder) convertView.getTag();
//			}
//
//			Bookmark bookmark = bookmarks.get(position);
//			AudiobookManager am = AudiobookManager.getInstance();
//			Audiobook audiobook = am.getAudiobook(bookmark);
//			Track track = null;
//			if(audiobook != null){ //In case the audiobook have been deleted
//				track = audiobook.getPlaylist().get(bookmark.getTrackno());
//				if(audiobook.getCover() != null){
//					String cover = audiobook.getCover();
//					Bitmap bm = BitmapFactory.decodeFile(cover);
//					holder.cover_iv.setImageBitmap(bm);
//				} else {
//					Drawable drw = getResources().getDrawable(R.drawable.ic_action_help);
//					holder.cover_iv.setImageDrawable(drw);
//				}
//			}
//			holder.author_tv.setText(bookmark.getAuthor());
//			holder.album_tv.setText(bookmark.getAlbum());
//			if(track != null){ //In case the audiobook have been deleted
//				String title = track.getTitle();
//				if(title.length() > 28) title = title.substring(0, 25) + "...";
//				holder.track_tv.setText(String.format("%02d", bookmark.getTrackno()+1) + " " + title);
//			} else {
//				holder.track_tv.setText(String.format("%02d", bookmark.getTrackno()+1));
//			}
//			holder.progress_tv.setText(Time.toShortString(bookmark.getProgress()));
//			
//			return convertView;
//		}
//	}
//	static class ViewHolder {
//		public TextView author_tv, album_tv, track_tv, progress_tv;
//		public ImageView cover_iv;
//		public RelativeLayout info;
//	}
//
//	//Connection must-haves
//	@Override
//	public void onStart(){
//		Log.d(TAG, "onStart");
//		super.onStart();
//		//Bind to PlayerService
//		Intent intent = new Intent(getActivity(), PlayerService.class);
//		getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);	
//	}
//	@Override
//	public void onStop(){
//		Log.d(TAG, "onStop");
//		super.onStop();
//		//Unbind from PlayerService
//		if(bound){
//			getActivity().unbindService(this);
//			bound = false;
//		}
//	}
//	
//	@Override
//	public void onServiceConnected(ComponentName name, IBinder service) {
//		DAPBinder binder = (DAPBinder) service;
//		player = binder.getPlayerService();
//		player.addObserver(this);
//		bound = true;
//	}
//	@Override
//	public void onServiceDisconnected(ComponentName name) {
//		bound = false;
//	}
//	
//	
//	private void changeBookmarkDialog(final Bookmark bookmark){
//		final Dialog dialog = new Dialog(getActivity());
//		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View dv = inflater.inflate(R.layout.dialog, list, false);
//
//		//Title
//		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
//		title_tv.setText("Change bookmark");
//
//		//Message
//		TextView msg_tv = (TextView) dv.findViewById(R.id.dialog_msg_tv);
//		msg_tv.setText(bookmark.getAuthor() + "\n" + bookmark.getAlbum());
//
//		//Exit button
//		ImageButton exit_btn = (ImageButton) dv.findViewById(R.id.dialog_exit_btn);
//		exit_btn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				dialog.dismiss();
//			}
//		});
//
//		//Left button
//		Button left_btn = (Button) dv.findViewById(R.id.dialog_left_btn);
//		left_btn.setText("Delete");
//		left_btn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				dialog.dismiss();
//				deleteBookmarkDialog(bookmark);
//			}
//		});
//
//		//Right button
//		Button right_btn = (Button) dv.findViewById(R.id.dialog_right_btn);
//		right_btn.setText("Edit");
//		right_btn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				dialog.dismiss();
//
//				Toast.makeText(getActivity(), "Not implemented yet!", Toast.LENGTH_SHORT).show();
////				Intent intent = new Intent(getActivity(), AudiobookActivity.class);
////				intent.putExtra("state", STATE_EDIT);
////				intent.putExtra("audiobook", bookmark);
////				startActivityForResult(intent, REQUEST_EDIT_AUDIOBOOK); 
//			}
//		});
//
//
//		dialog.setContentView(dv);
//		dialog.show();
//	}
//	private void deleteBookmarkDialog(final Bookmark bookmark){
//		final Dialog dialog = new Dialog(getActivity());
//		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View dv = inflater.inflate(R.layout.dialog, list, false);
//
//		//Title
//		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
//		title_tv.setText("Delete audiobooke");
//
//		//Message
//		TextView msg_tv = (TextView) dv.findViewById(R.id.dialog_msg_tv);
//		msg_tv.setText(bookmark.getAuthor() + "\n" + bookmark.getAlbum());
//
//		//Exit button
//		ImageButton exit_btn = (ImageButton) dv.findViewById(R.id.dialog_exit_btn);
//		exit_btn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				dialog.dismiss();
//			}
//		});
//
//		//Left button
//		Button left_btn = (Button) dv.findViewById(R.id.dialog_left_btn);
//		left_btn.setText("Cancel");
//		left_btn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				dialog.dismiss();
//			}
//		});
//
//		//Right button
//		Button right_btn = (Button) dv.findViewById(R.id.dialog_right_btn);
//		right_btn.setText("Confirm");
//		right_btn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				dialog.dismiss();
//				
//				System.out.println("Confirm delete bookmark: "+bookmark);
//				Audiobook current = Data.getCurrentAudiobook();
//				if(current != null){
//					String author = current.getAuthor();
//					String album = current.getAlbum();
//				
//					if(bookmark.isSame(author, album)){
//						System.out.println("bookmark is current");
//						if(miniplayer != null){
//							System.out.println("miniplayer exists");
//							//stop and un-set as current
//							miniplayer.getPlayer().pause();
//							Data.setCurrentAudiobook(null);
//							Data.setCurrentTrack(null);
//							Data.setCurrentPosition(-1);
//
//							//update the miniplayers view
//							miniplayer.updateView();
//							System.out.println("current audiobook: "+Data.getCurrentAudiobook());
//							miniplayer.setVisibility(Data.getCurrentAudiobook() == null ? View.GONE : View.VISIBLE);
//						}
//						if(changer != null) changer.updateController();
//					}
//				}
//				//Remove the audiobook
//				BookmarkManager.getInstance().removeBookmark(getActivity(), bookmark);
//				Log.d(TAG, "Deleting Bookmark:\n"+bookmark);
//				
//				changer.updateBookmarks();
//				changer.updateController();
//			}
//		});
//
//		dialog.setContentView(dv);
//		dialog.show();
//	}
//}
