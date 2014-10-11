package rd.dap;

import static rd.dap.AudiobookActivity.STATE_EDIT;
import static rd.dap.AudiobookActivity.STATE_NEW;
import static rd.dap.FileBrowserActivity.TYPE_FOLDER;
import static rd.dap.PlayerService.audiobook;
import static rd.dap.PlayerService.position;
import static rd.dap.PlayerService.track;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rd.dap.fragments.FragmentMiniPlayer;
import rd.dap.fragments.FragmentMiniPlayer.MiniPlayerObserver;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Toast;

public class AudiobookListActivity extends Activity implements MiniPlayerObserver, OnItemClickListener, OnItemLongClickListener {
	public static final String TAG = "AudiobookListActivity";
	public static ArrayAdapter<Audiobook> adapter;
	public static ArrayList<Audiobook> audiobooks = new ArrayList<Audiobook>();
	public static FragmentMiniPlayer miniplayer = null;
	private static final int REQUEST_NEW_AUDIOBOOK = 9001;
	private static final int REQUEST_EDIT_AUDIOBOOK = 9002;

	@Override
 	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_with_miniplayer);
		
		FragmentManager fm = getFragmentManager();
		miniplayer = (FragmentMiniPlayer) fm.findFragmentById(R.id.main_mini_player);
		miniplayer.addObserver(this);
		
		adapter = new AudiobookAdapter(this, R.layout.audiobook_item, audiobooks);
		
		ListView list = (ListView) findViewById(R.id.main_list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);

		
		new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				AudiobookManager am = AudiobookManager.getInstance();
				audiobooks.clear();
//				audiobooks.addAll(am.autodetect());
				audiobooks.addAll(am.getAudiobooks());
				return null;
			}
			@Override 
			protected void onPostExecute(Void result){
				runOnUiThread(new Runnable() {
					@Override public void run() {
						adapter.notifyDataSetChanged();
						for(Audiobook a : audiobooks){
							System.out.println("#"+a);
						}
					}
				});
			}
		}.execute();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		Log.d(TAG, "onItemClick");
		audiobook = audiobooks.get(index);
		position = 0;
		track = audiobook.getPlaylist().get(position);
		miniplayer.updateView();
		miniplayer.reload();
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "onItemLongClick");
		final Audiobook audiobook = audiobooks.get(position);
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setMessage("Change audiobook")
		.setPositiveButton("Edit audiobook", new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(AudiobookListActivity.this, AudiobookActivity.class);
				intent.putExtra("state", STATE_EDIT);
				intent.putExtra("audiobook", audiobook);
				startActivity(intent);
			}
		})
		.setNegativeButton("Delete audiobook", new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				confirmDelete(audiobook);
			}
		})
		.create();
		dialog.show();
		return true; //consume click
	}
	private void confirmDelete(final Audiobook audiobook){
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setMessage("Confirm delete "+audiobook.getAuthor() + " - " + audiobook.getAlbum())
		.setPositiveButton("Cancel", null) //Do nothing
		.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				AudiobookManager.getInstance().removeAudiobook(audiobook);
				audiobooks.remove(audiobook);
				adapter.notifyDataSetChanged();
			}
		})
		.create();
		dialog.show();
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
			if(data == null) throw new RuntimeException("No data provided");
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
			audiobooks.addAll(AudiobookManager.getInstance().getAudiobooks());
			for(Audiobook a : audiobooks) System.out.println("! "+a);
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

	
}
