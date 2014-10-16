package rd.dap.fragments;

import static rd.dap.AudiobookActivity.STATE_NEW;
import static rd.dap.FileBrowserActivity.TYPE_FOLDER;
import static rd.dap.MainActivity.miniplayer;

import java.io.File;
import java.util.List;

import rd.dap.AudiobookActivity;
import rd.dap.FileBrowserActivity;
import rd.dap.MainActivity;
import rd.dap.MainActivity.ChangeAudiobookDialogFragment;
import rd.dap.R;
import rd.dap.fragments.BookmarkListFragment.BookmarkAdapter;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Data;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class AudiobookGridFragment extends Fragment implements OnItemClickListener, OnItemLongClickListener{
	private static final String TAG = "AudiobookGridActivity";
	private static AudiobookAdapter adapter;
	private GridView grid;
	private static final int REQUEST_NEW_AUDIOBOOK = 9001;
	private static final int REQUEST_EDIT_AUDIOBOOK = 9002;
	
	public AudiobookGridFragment(){
		super();
	}
	public AudiobookGridFragment(Activity activity){
		super();
		adapter = new AudiobookAdapter(activity, R.layout.cover_view, Data.getAudiobooks());
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
//		adapter = new AudiobookAdapter(getActivity(), R.layout.cover_view, Data.getAudiobooks());

//		new AsyncTask<Void, Void, Void>(){
//			@Override
//			protected Void doInBackground(Void... params) {
//				AudiobookManager.getInstance().loadAudiobooks(getActivity()); 
//				return null;
//			}
//			@Override 
//			protected void onPostExecute(Void result){
//				Log.d(TAG, "onPostExecute - audiobooks loaded");
//				getActivity().runOnUiThread(new Runnable() {
//					@Override public void run() {
//						adapter.notifyDataSetChanged();
//					}
//				});
//			}
//		}.execute();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View v = (ViewGroup) inflater.inflate(R.layout.activity_grid_with_miniplayer, container, false);
		
		grid = (GridView) v.findViewById(R.id.grid_layout_gv);
		grid.setAdapter(adapter);
		grid.setOnItemClickListener(this);
		grid.setOnItemLongClickListener(this);
		
		return v;
	}

	//Menu
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.main, menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_new_audiobook:
			Log.d(TAG, "menu_item_new_audiobook");
			Intent intent = new Intent(getActivity(), FileBrowserActivity.class);
			intent.putExtra("type", TYPE_FOLDER);
			intent.putExtra("message", "Select folder");
			intent.putExtra("requestcode", REQUEST_NEW_AUDIOBOOK);
			startActivityForResult(intent, REQUEST_NEW_AUDIOBOOK);
			break;

		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		switch(requestCode){
		case REQUEST_NEW_AUDIOBOOK:
			Log.d(TAG, "onActivityResult - REQUEST_NEW_AUDIOBOOK");
			if(data == null) return;
			String folder_path = data.getStringExtra("result");
			File folder = new File(folder_path);
			AudiobookManager manager = AudiobookManager.getInstance();
			Audiobook audiobook = manager.autoCreateAudiobook(folder, true);
			Intent intent = new Intent(getActivity(), AudiobookActivity.class);
			intent.putExtra("state", STATE_NEW);
			intent.putExtra("audiobook", audiobook);
			startActivityForResult(intent, REQUEST_EDIT_AUDIOBOOK);
			break;
		case REQUEST_EDIT_AUDIOBOOK:
			Log.d(TAG, "onActivityResult - REQUEST_EDIT_AUDIOBOOK");
			
			//update the lists
			AudiobookGridFragment audiobookGridFragment = MainActivity.getAudiobookGridFragment();
			if(audiobookGridFragment != null ) {
				AudiobookAdapter audiobookAdapter = audiobookGridFragment.getAdapter();
				if(audiobookAdapter != null) audiobookAdapter.notifyDataSetChanged();
			}
			
			BookmarkListFragment bookmarkListFragment = MainActivity.getBookmarkListFragment();
			if(bookmarkListFragment != null) {
				BookmarkAdapter bookmarkAdapter = bookmarkListFragment.getAdapter();
				if(bookmarkAdapter != null) bookmarkAdapter.notifyDataSetChanged();
				//just in case the bookmark was there before the audiobook
			}
			
			//update the controller
			ControllerFragment controllerFragment = MainActivity.getControllerFragment();
			if(controllerFragment != null) {
				controllerFragment.displayValues();
				controllerFragment.displayTracks();
				controllerFragment.displayProgress();
			}
			
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		Log.d(TAG, "onItemClick");
		Data.setAudiobook(Data.getAudiobooks().get(index));
		Data.setPosition(0);
		Data.setTrack(Data.getAudiobook().getPlaylist().get(Data.getPosition()));
		
//		miniplayer.setVisibility(Data.getAudiobook() == null ? View.GONE : View.VISIBLE);
		miniplayer.reload();
		miniplayer.updateView();
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
		Log.d(TAG, "onItemLongClick");
		ChangeAudiobookDialogFragment frag = ChangeAudiobookDialogFragment.newInstance(position);
		frag.show(getFragmentManager(), "ChagenAudiobookDialogFragment");
		
		return true; //consume click
	}

	public AudiobookAdapter getAdapter() { return adapter; }
	public class AudiobookAdapter extends ArrayAdapter<Audiobook> {
		private List<Audiobook> audiobooks;

		public AudiobookAdapter(Context context, int resource, List<Audiobook> audiobooks) {
			super(context, resource, audiobooks);
			this.audiobooks = audiobooks;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null){
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.cover_view, parent, false);
				//in an arrayAdapter 'attach' should always be false, as the view is attaced later on by the system.

				holder = new ViewHolder();
				holder.cover_iv = (ImageView) convertView.findViewById(R.id.cover_view_cover_iv);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Audiobook audiobook = audiobooks.get(position);
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
		public ImageView cover_iv;
	}

	

}
