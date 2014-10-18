package rd.dap;

import java.util.Locale;

import rd.dap.dialogs.Changer;
import rd.dap.fragments.AudiobookGridFragment;
import rd.dap.fragments.BookmarkListFragment;
import rd.dap.fragments.BookmarkListFragment.BookmarkAdapter;
import rd.dap.fragments.ControllerFragment;
import rd.dap.fragments.FragmentMiniPlayer;
import rd.dap.fragments.FragmentMiniPlayer.MiniPlayerObserver;
import rd.dap.model.AudiobookManager;
import rd.dap.model.BookmarkManager;
import rd.dap.model.Data;
import rd.dap.support.MainDriveHandler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

public class MainActivity extends MainDriveHandler implements ActionBar.TabListener, MiniPlayerObserver, Changer {
	private static final String TAG = "MainActivity";
	public static FragmentMiniPlayer miniplayer = null;
	private SectionsPagerAdapter sectionsPagerAdapter;
	private ViewPager viewPager;
	private AudiobookGridFragment audiobookGridFragment;
	private BookmarkListFragment bookmarkListFragment;
	private static ControllerFragment controllerFragment;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent serviceIntent = new Intent(this, PlayerService.class);
		startService(serviceIntent);
		
		audiobookGridFragment = new AudiobookGridFragment();
		bookmarkListFragment = new BookmarkListFragment();
		controllerFragment = new ControllerFragment();
		
		//Load Bookmarks
//		new  
		
		
		//Load Audiobooks
		new AsyncTask<Activity, Void, Void>(){
			Activity activity;
			@Override
			protected Void doInBackground(Activity... params) {
				activity = params[0];
				AudiobookManager.getInstance().loadAudiobooks(activity);
				BookmarkManager.getInstance().loadBookmarks(activity.getFilesDir()); 
				return null;
			}
			@Override 
			protected void onPostExecute(Void result){
				Log.d(TAG, "onPostExecute - audiobooks loaded");
				runOnUiThread(new Runnable() {
					
					@Override 
					public void run() {
						try{
							Changer changer = (Changer)activity;
							changer.updateAudiobooks();
							changer.updateBookmarks();
						} catch (ClassCastException e) {
				            throw new ClassCastException(activity.toString()+ " must implement Changer");
				        }
					}
				});
			}
		}.execute(this);
		
		
		

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		sectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(sectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
				
				miniplayer.setVisibility(Data.getCurrentAudiobook() == null ? View.GONE : View.VISIBLE);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(sectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		
		FragmentManager fm = getFragmentManager();
		miniplayer = (FragmentMiniPlayer) fm.findFragmentById(R.id.main_miniplayer);
		miniplayer.addObserver(this);
	}

	//CALLBACK
	public FragmentMiniPlayer getMiniplayer() { return miniplayer; }
	public void updateAudiobooks() { 
		if(audiobookGridFragment == null) return;
		audiobookGridFragment.displayAudiobooks();
	}
	public void updateBookmarks() { 
		if(bookmarkListFragment == null) return;
		BookmarkAdapter adapter = bookmarkListFragment.getAdapter();
		if(adapter == null) return;
		adapter.notifyDataSetChanged(); 
	}
	public void updateController() {
		if(controllerFragment == null) return;
		controllerFragment.displayValues();
		controllerFragment.displayTracks();
		controllerFragment.displayProgress();
	}
	
	
//	public static BookmarkListFragment getBookmarkListFragment() { return bookmarkListFragment; }
	public static ControllerFragment getControllerFragment() { return controllerFragment; }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		Log.d(TAG, "onActivityResult");
	}
	
	//Tabs
	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in the ViewPager.
		viewPager.setCurrentItem(tab.getPosition());
	}
	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
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
	@Override public void miniplayer_seekTo(int progress){
		controllerFragment.displayValues();
		controllerFragment.displayTracks();
		controllerFragment.displayProgress();
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch(position){
			case 0: return bookmarkListFragment;
			case 1: return controllerFragment;
			case 2: return audiobookGridFragment;
			}
			return null;
		}

		@Override public int getCount() { return 3; }

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0: return getString(R.string.tab1).toUpperCase(l);
			case 1: return getString(R.string.tab2).toUpperCase(l);
			case 2: return getString(R.string.tab3).toUpperCase(l);
			default: return "tab";
			}
		}
	}

}
