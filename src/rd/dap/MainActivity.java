package rd.dap;

import java.util.Locale;

import rd.dap.fragments.FragmentMiniPlayer;
import rd.dap.fragments.FragmentMiniPlayer.MiniPlayerObserver;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity implements ActionBar.TabListener, MiniPlayerObserver {
	public static FragmentMiniPlayer miniplayer = null;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter sectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
//		miniplayer.setVisibility(Data.getAudiobook() == null ? View.GONE : View.VISIBLE);
		miniplayer.setVisibility(View.VISIBLE);
	}

	//Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//Tabs
	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		viewPager.setCurrentItem(tab.getPosition());
	}
	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
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
			case 0: return new AudiobookGridFragment(miniplayer);
			case 1: return new BookmarkListActivity(miniplayer);
			}
			return null;
		}

		@Override public int getCount() { return 2; }

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
