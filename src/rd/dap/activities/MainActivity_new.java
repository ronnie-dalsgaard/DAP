package rd.dap.activities;

import rd.dap.R;
import rd.dap.events.Event;
import rd.dap.events.EventBus;
import rd.dap.events.Subscriber;
import rd.dap.model.AudiobookManager;
import rd.dap.model.BookmarkManager;
import rd.dap.support.MainDriveHandler;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity_new extends MainDriveHandler implements Subscriber {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_activity_new);
		
		if(savedInstanceState == null){
			new LoadAudiobooksTask().execute(this);
		}
		
		EventBus.addSubsciber(this);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_activity_new, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onEvent(Event event) {
		switch(event.getEventID()){
		case Event.AUDIOBOOKS_LOADED_EVENT: new LoadBookmarksTask().execute(this); break;
		}
		System.out.println("=====================================");
		System.out.println(event);
		System.out.println("=====================================");
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
			BookmarkManager.getInstance().loadBookmarks(activity.getFilesDir());
			return null;
		}
	}
}
