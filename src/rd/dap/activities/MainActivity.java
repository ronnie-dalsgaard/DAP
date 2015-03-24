package rd.dap.activities;

import java.util.ArrayList;

import rd.dap.R;
import rd.dap.dialogs.Dialog_import_export;
import rd.dap.dialogs.Dialog_import_export.Callback;
import rd.dap.events.Event;
import rd.dap.events.Event.Type;
import rd.dap.events.EventBus;
import rd.dap.events.Subscriber;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.services.PlayerService;
import rd.dap.services.PlayerService.DAPBinder;
import rd.dap.support.MainDriveHandler;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

public class MainActivity extends MainDriveHandler implements Subscriber, Callback, ServiceConnection {
	private static PlayerService player;
	private boolean bound = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if(player == null){
			new AsyncTask<Void, Void, Void>() {
				@Override protected Void doInBackground(Void... params) {
					Intent serviceIntent = new Intent(MainActivity.this, PlayerService.class);
					startService(serviceIntent);
					return null;
				}
			}.execute();
		}
		
		if(savedInstanceState == null){
			new LoadAudiobooksTask().execute(this);
		}
		
		EventBus.addSubsciber(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_audiobooks:
			Intent intent = new Intent(this, AudiobooksActivity.class);
			startActivityForResult(intent, AudiobooksActivity.REQUEST_AUDIOBOOK);
			break;
		case R.id.menu_item_import_export:
			ViewGroup base = (ViewGroup) findViewById(R.id.base);
			new Dialog_import_export(this, base, this).show();
			break;
		}
		return false;
	}
	@Override
	public void onStart(){
		super.onStart();
		//Bind to PlayerService
		if(player == null){
			Intent intent = new Intent(this, PlayerService.class);
			bindService(intent, this, Context.BIND_AUTO_CREATE);
		}
	}
	@Override
	public void onStop(){
		super.onStop();
		//Unbind from PlayerService
		if(bound){
			unbindService(this);
			bound = false;
		}
	}
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		DAPBinder binder = (DAPBinder) service;
		player = binder.getPlayerService();
		bound = true;
	}
	@Override
	public void onServiceDisconnected(ComponentName name) {
		bound = false;
	}
	
	@Override
	public void onEvent(Event event) {
		switch(event.getType()){
		case AUDIOBOOKS_LOADED_EVENT: 
			new LoadBookmarksTask().execute(this); break;
		case BOOKMARK_SELECTED_EVENT: 
			Bookmark bookmark = event.getBookmark();
			System.out.println("bookmark selected: "+bookmark);
			break;
		case BOOKMARK_DELETED_EVENT: 
			Bookmark bookmark1 = event.getBookmark();
			System.out.println("bookmark deleted: "+bookmark1);
			break;
		default:
			break;
		}
	}
	
	private class LoadAudiobooksTask extends AsyncTask<Activity, Void, Void>{
		@Override
		protected Void doInBackground(Activity... params) {
			Activity activity = params[0];
			AudiobookManager.getInstance().loadAudiobooks(activity);
			return null;
		}
	}
	private class LoadBookmarksTask extends AsyncTask<Activity, Void, Void>{
		@Override
		protected Void doInBackground(Activity... params) {
			Activity activity = params[0];
			BookmarkManager.getInstance().loadBookmarks(activity);
			return null;
		}
	}
	
	@Override
	public void displayBookmarks(ArrayList<Bookmark> bookmarks) {
		EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.BOOKMARKS_UPDATED_EVENT).setBookmarks(bookmarks));
	}

	
	
	
}
