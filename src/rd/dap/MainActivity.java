package rd.dap;

import java.util.ArrayList;

import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.support.AudiobookAdapter;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {
	private final static String TAG = "MainActivity"; 
	public static ArrayAdapter<Audiobook> adapter;
	public static ArrayList<Audiobook> audiobooks = new ArrayList<Audiobook>();
	public static MiniPlayer player = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.d(TAG, "onCreate");
		
		FragmentManager fm = getFragmentManager();
		player = (MiniPlayer) fm.findFragmentById(R.id.main_mini_player);
		
		ListView list = (ListView) findViewById(R.id.main_audiobook_list);
		adapter = new AudiobookAdapter(this, R.layout.audiobook_item, audiobooks);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//In this case position and id is the same
				Audiobook audiobook = audiobooks.get(position);
				player.setAudiobook(audiobook, audiobook.getPlaylist().get(0));
			}
		});
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Audiobook audiobook = audiobooks.get(position);
				System.out.println("LONG CLICK...");
				System.out.println(audiobook);
				Intent intent = new Intent(MainActivity.this, AudiobookActivity.class);
				intent.putExtra("audiobook", audiobook);
				startActivity(intent);
				return true; //consume click
			}
		});
		Log.d(TAG, "Detect audiobooks");
		new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				AudiobookManager am = new AudiobookManager();
				audiobooks.addAll(am.autodetect());
				Log.d(TAG, "Audiobooks detected");
				return null;
			}
			@Override 
			protected void onPostExecute(Void result){
				runOnUiThread(new Runnable() {
					@Override public void run() {
						adapter.notifyDataSetChanged();
						Log.d(TAG, "adapter notified");
					}
				});
				Log.d(TAG, "Done detecting audiobooks");
			}
		}.execute();
	}

	
	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}
	
	@Override
	public void onStart(){
		super.onStart();
		Log.d(TAG, "onStart");
	}

	//Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.menu_item_file_browser) {
			Intent intent = new Intent(this, FileBrowserActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
}
