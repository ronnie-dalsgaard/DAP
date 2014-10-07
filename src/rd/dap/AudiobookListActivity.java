package rd.dap;

import static rd.dap.PlayerService.audiobook;
import static rd.dap.PlayerService.position;
import static rd.dap.PlayerService.track;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rd.dap.fragments.MiniPlayer;
import rd.dap.fragments.MiniPlayer.MiniPlayerObserver;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class AudiobookListActivity extends Activity implements MiniPlayerObserver {
	public static ArrayAdapter<Audiobook> adapter;
	public static ArrayList<Audiobook> audiobooks = new ArrayList<Audiobook>();
	public static MiniPlayer miniplayer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_with_miniplayer);
		
		FragmentManager fm = getFragmentManager();
		miniplayer = (MiniPlayer) fm.findFragmentById(R.id.main_mini_player);
		miniplayer.addObserver(this);
		
		adapter = new AudiobookAdapter(this, R.layout.audiobook_item, audiobooks);
		
		ListView list = (ListView) findViewById(R.id.main_list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
				//In this case position and id is the same
				audiobook = audiobooks.get(index);
				position = 0;
				track = audiobook.getPlaylist().get(position);
				miniplayer.updateView();
				miniplayer.reload();
			}
		});
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Audiobook audiobook = audiobooks.get(position);
				Intent intent = new Intent(AudiobookListActivity.this, AudiobookActivity.class);
				intent.putExtra("audiobook", audiobook);
				startActivity(intent);
				return true; //consume click
			}
		});

		
		new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				AudiobookManager am = new AudiobookManager();
				audiobooks.clear();
				audiobooks.addAll(am.autodetect());
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
			intent = new Intent(this, ControllerActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
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
				File cover = audiobook.getCover();
				Bitmap bm = BitmapFactory.decodeFile(cover.getAbsolutePath());
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
