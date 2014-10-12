package rd.dap;

import static rd.dap.AudiobookActivity.STATE_EDIT;
import static rd.dap.AudiobookActivity.STATE_NEW;
import static rd.dap.FileBrowserActivity.TYPE_FOLDER;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rd.dap.fragments.FragmentMiniPlayer;
import rd.dap.fragments.FragmentMiniPlayer.MiniPlayerObserver;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Data;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AudiobookListActivity extends Activity implements MiniPlayerObserver, OnItemClickListener, OnItemLongClickListener {
	public static final String TAG = "AudiobookListActivity";
	public static ArrayAdapter<Audiobook> adapter;
	public static ArrayList<Audiobook> audiobooks = new ArrayList<Audiobook>();
	public static FragmentMiniPlayer miniplayer = null;
	private static final int REQUEST_NEW_AUDIOBOOK = 9001;
	private static final int REQUEST_EDIT_AUDIOBOOK = 9002;
	
	//TODO autosave bookmarks
	//TODO up-/download bookmarks
	//TODO remove all activity titles
	
	//TODO use tabs/swipe to navigate
	//TODO constant class (final class + private constructor)
	
	//TODO Home folder
	//TODO auto-detect all audiobooks
	//TODO Texts as resource
	//TODO sleeptimer
	//TODO pregress as progressbar
	//TODO Helper texts

	@Override
 	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_with_miniplayer);
		
		FragmentManager fm = getFragmentManager();
		miniplayer = (FragmentMiniPlayer) fm.findFragmentById(R.id.main_mini_player);
		miniplayer.addObserver(this);
		miniplayer.setVisibility(Data.getAudiobook() == null ? View.GONE : View.VISIBLE);
		
		adapter = new AudiobookAdapter(this, R.layout.audiobook_item, audiobooks);
		
		ListView list = (ListView) findViewById(R.id.main_list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);

		
		new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				AudiobookManager am = AudiobookManager.getInstance();
				ArrayList<Audiobook> loadedList = am.loadAudiobooks(AudiobookListActivity.this); 
				audiobooks.clear();
				audiobooks.addAll(loadedList);
				return null;
			}
			@Override 
			protected void onPostExecute(Void result){
				runOnUiThread(new Runnable() {
					@Override public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			}
		}.execute();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		Log.d(TAG, "onItemClick");
		Data.setAudiobook(audiobooks.get(index));
		Data.setPosition(0);
		Data.setTrack(Data.getAudiobook().getPlaylist().get(Data.getPosition()));
		miniplayer.setVisibility(Data.getAudiobook() == null ? View.GONE : View.VISIBLE);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_audiobook_list, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Intent intent;
		switch(id){
		case R.id.menu_item_controller:
			Log.d(TAG, "menu_item_controller");
			intent = new Intent(this, ControllerActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_item_new_audiobook:
			Log.d(TAG, "menu_item_new_audiobook");
			intent = new Intent(this, FileBrowserActivity.class);
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
			Intent intent = new Intent(AudiobookListActivity.this, AudiobookActivity.class);
			intent.putExtra("state", STATE_NEW);
			intent.putExtra("audiobook", audiobook);
			startActivityForResult(intent, REQUEST_EDIT_AUDIOBOOK);
			break;
		case REQUEST_EDIT_AUDIOBOOK:
			Log.d(TAG, "onActivityResult - REQUEST_EDIT_AUDIOBOOK");
			audiobooks.clear();
			audiobooks.addAll(AudiobookManager.getInstance().getAudiobooks(this));
			adapter.notifyDataSetChanged();
		}
	}
	
	class AudiobookAdapter extends ArrayAdapter<Audiobook> {
		private List<Audiobook> audiobooks;

		public AudiobookAdapter(Context context, int resource, List<Audiobook> audiobooks) {
			super(context, resource, audiobooks);
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
				holder.cover_iv = (ImageView) convertView.findViewById(R.id.audiobook_item_cover_iv);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Audiobook audiobook = audiobooks.get(position);
			holder.author_tv.setText(audiobook.getAuthor());
			holder.album_tv.setText(audiobook.getAlbum());
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
	
	@Override public void miniplayer_play() {
//		Toast.makeText(AudiobookListActivity.this, "Play on miniplayer", Toast.LENGTH_SHORT).show();
	}
	@Override public void miniplayer_pause() {
//		Toast.makeText(AudiobookListActivity.this, "Pause on miniplayer", Toast.LENGTH_SHORT).show();
	}
	@Override public void miniplayer_longClick() {
//		Toast.makeText(AudiobookListActivity.this, "Long click on miniplayer", Toast.LENGTH_SHORT).show();
	}
	@Override public void miniplayer_click() {
//		Toast.makeText(AudiobookListActivity.this, "Click on miniplayer", Toast.LENGTH_SHORT).show();
	}

	public static class ChangeAudiobookDialogFragment extends DialogFragment {
		public static final ChangeAudiobookDialogFragment newInstance(int position){
			ChangeAudiobookDialogFragment fragment = new ChangeAudiobookDialogFragment();
		    Bundle bundle = new Bundle();
		    bundle.putInt("position", position);
		    fragment.setArguments(bundle);
		    return fragment ;
		}
		
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
			final int position = getArguments().getInt("position");
			final Audiobook audiobook = audiobooks.get(position);
	        
			return new AlertDialog.Builder(getActivity())
			.setMessage("Change audiobook")
			.setPositiveButton("Edit audiobook", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(getActivity(), AudiobookActivity.class);
					intent.putExtra("state", STATE_EDIT);
					intent.putExtra("audiobook", audiobook);
					startActivity(intent);
				}
			})
			.setNegativeButton("Delete audiobook", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					ConfirmDeleteDialogFragment frag = ConfirmDeleteDialogFragment.newInstance(position);
					frag.show(getFragmentManager(), "ConfirmDeleteDialogFragment");
				}
			})
			.create();
	    }
	}
	public static class ConfirmDeleteDialogFragment extends DialogFragment {
		public static final ConfirmDeleteDialogFragment newInstance(int position){
			ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
		    Bundle bundle = new Bundle();
		    bundle.putInt("position", position);
		    fragment.setArguments(bundle);
		    return fragment ;
		}
		
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
			int position = getArguments().getInt("position");
			final Audiobook audiobook = audiobooks.get(position);
	        return new AlertDialog.Builder(getActivity())
			.setMessage("Confirm delete "+audiobook.getAuthor() + " - " + audiobook.getAlbum())
			.setPositiveButton("Cancel", null) //Do nothing
			.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					//stop and un-set as current
					miniplayer.getPlayer().pause();
					Data.setAudiobook(null);
					Data.setTrack(null);
					Data.setPosition(-1);
					
					//update the miniplayers view
					miniplayer.updateView();

					//Remove the audiobook
					AudiobookManager.getInstance().removeAudiobook(getActivity(), audiobook);
					
					//update the list
					audiobooks.remove(audiobook);
					adapter.notifyDataSetChanged();
				}
			})
			.create();
	    }
	}
}
