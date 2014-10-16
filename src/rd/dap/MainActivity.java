package rd.dap;

import static rd.dap.AudiobookActivity.STATE_EDIT;

import java.util.Locale;

import rd.dap.fragments.AudiobookGridFragment;
import rd.dap.fragments.AudiobookGridFragment.AudiobookAdapter;
import rd.dap.fragments.BookmarkListFragment;
import rd.dap.fragments.BookmarkListFragment.BookmarkAdapter;
import rd.dap.fragments.ControllerFragment;
import rd.dap.fragments.FragmentMiniPlayer;
import rd.dap.fragments.FragmentMiniPlayer.MiniPlayerObserver;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.model.Data;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity implements ActionBar.TabListener, MiniPlayerObserver {
	private static final String TAG = "MainActivity";
	public static FragmentMiniPlayer miniplayer = null;
	private SectionsPagerAdapter sectionsPagerAdapter;
	private ViewPager viewPager;
	private static AudiobookGridFragment audiobookGridFragment;
	private static BookmarkListFragment bookmarkListFragment;
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
		new AsyncTask<Activity, Void, Void>(){
			@Override
			protected Void doInBackground(Activity... params) {
				Activity activity = params[0];
				BookmarkManager.getInstance().loadBookmarks(activity.getFilesDir()); 
				return null;
			}
			@Override 
			protected void onPostExecute(Void result){
				runOnUiThread(new Runnable() {
					@Override public void run() {
						BookmarkListFragment frag = MainActivity.getBookmarkListFragment();
						if(frag != null){
							BookmarkAdapter adapter = frag.getAdapter();
							if(adapter != null) adapter.notifyDataSetChanged();
						}
					}
				});
			}
		}.execute(this);
		
		
		//Load Audiobooks
		new AsyncTask<Activity, Void, Void>(){
			@Override
			protected Void doInBackground(Activity... params) {
				try { Thread.sleep(3000); } catch(Exception e){/*Ignore*/} //FIXME remove - just for testing
				Activity activity = params[0];
				AudiobookManager.getInstance().loadAudiobooks(activity); 
				return null;
			}
			@Override 
			protected void onPostExecute(Void result){
				Log.d(TAG, "onPostExecute - audiobooks loaded");
				runOnUiThread(new Runnable() {
					@Override public void run() {
						AudiobookGridFragment a_frag = MainActivity.getAudiobookGridFragment();
						if(a_frag != null){
							AudiobookAdapter a_adapter = a_frag.getAdapter();
							if(a_adapter != null) {
								a_adapter.notifyDataSetChanged();
							} else System.err.println("No audiobook adapter");
						} else System.err.println("No audiobook fragment");

						BookmarkListFragment b_frag = MainActivity.getBookmarkListFragment();
						if(b_frag != null){
							BookmarkAdapter b_adapter = b_frag.getAdapter();
							if(b_adapter != null) {
								b_adapter.notifyDataSetChanged();
							} else System.err.println("No bookmark adapter");
						} else System.err.println("No bookmark fragment");
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
				
				if(position == 2){
					miniplayer.setVisibility(View.GONE);
				} else {
					miniplayer.setVisibility(Data.getAudiobook() == null ? View.GONE : View.VISIBLE);
				}
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
//		miniplayer.setVisibility(View.VISIBLE);
	}

	public static AudiobookGridFragment getAudiobookGridFragment() { return audiobookGridFragment; }
	public static BookmarkListFragment getBookmarkListFragment() { return bookmarkListFragment; }
	public static ControllerFragment getControllerFragment() { return controllerFragment; }
	
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
			case 0: return audiobookGridFragment;
			case 1: return bookmarkListFragment;
			case 2: return controllerFragment;
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

	//Edit audiobook dialogs
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
			final Audiobook audiobook = Data.getAudiobooks().get(position);

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
					ConfirmDeleteaAudiobookDialog frag = ConfirmDeleteaAudiobookDialog.newInstance(position);
					frag.show(getFragmentManager(), "ConfirmDeleteAudiobookDialog");
				}
			})
			.create();
		}
	}
	public static class ConfirmDeleteaAudiobookDialog extends DialogFragment {
		public static final ConfirmDeleteaAudiobookDialog newInstance(int position){
			ConfirmDeleteaAudiobookDialog fragment = new ConfirmDeleteaAudiobookDialog();
			Bundle bundle = new Bundle();
			bundle.putInt("position", position);
			fragment.setArguments(bundle);
			return fragment ;
		}
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int position = getArguments().getInt("position");
			final Audiobook audiobook = Data.getAudiobooks().get(position);
			return new AlertDialog.Builder(getActivity())
			.setIcon(getActivity().getResources().getDrawable(R.drawable.ic_action_warning))
			.setTitle("Confirm delete")
			.setMessage(audiobook.getAuthor() + "\n" + audiobook.getAlbum())
			.setNegativeButton("Cancel", null) //Do nothing
			.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					if(audiobook.equals(Data.getAudiobook()) && miniplayer != null){
						//stop and un-set as current
						miniplayer.getPlayer().pause();
						Data.setAudiobook(null);
						Data.setTrack(null);
						Data.setPosition(-1);
						
						//update the miniplayers view
						miniplayer.updateView();
					}

					//Remove the audiobook
					AudiobookManager.getInstance().removeAudiobook(getActivity(), audiobook);
					Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
					BookmarkManager.getInstance().removeBookmark(getActivity(), bookmark);
					Log.d(TAG, "Deleting Audiobook:\n"+audiobook);
					Log.d(TAG, "Deleting Bookmark:\n"+bookmark);

					//update the lists
					AudiobookAdapter audiobookAdapter = audiobookGridFragment.getAdapter();
					if(audiobookAdapter != null) audiobookAdapter.notifyDataSetChanged();
					
					BookmarkAdapter bookmarkAdapter = bookmarkListFragment.getAdapter();
					if(bookmarkAdapter != null) bookmarkAdapter.notifyDataSetChanged();
					
					//update the controller
					if(controllerFragment != null){
						controllerFragment.displayValues();
						controllerFragment.displayTracks();
						controllerFragment.displayProgress();
					}
				}
			})
			.create();
		}
	}
	
	//Edit bookmark dialogs
	public static class ChangeBookmarkDialogFragment extends DialogFragment {
		public static final ChangeBookmarkDialogFragment newInstance(int position){
			ChangeBookmarkDialogFragment fragment = new ChangeBookmarkDialogFragment();
			Bundle bundle = new Bundle();
			bundle.putInt("position", position);
			fragment.setArguments(bundle);
			return fragment ;
		}
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final int position = getArguments().getInt("position");
			final Bookmark bookmark = Data.getBookmarks().get(position);

			return new AlertDialog.Builder(getActivity())
			.setMessage("Change bookmark")
			.setPositiveButton("Edit bookmark", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
//					Intent intent = new Intent(getActivity(), AudiobookActivity.class);
//					intent.putExtra("state", STATE_EDIT);
//					intent.putExtra("bookmark", bookmark);
//					startActivity(intent);
					Toast.makeText(getActivity(), "Not implemented yet!", Toast.LENGTH_LONG).show();
				}
			})
			.setNegativeButton("Delete bookmark", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					ConfirmDeleteaBookmarkDialog frag = ConfirmDeleteaBookmarkDialog.newInstance(position);
					frag.show(getFragmentManager(), "ConfirmDeleteBookmarkDialog");
				}
			})
			.create();
		}
	}
	public static class ConfirmDeleteaBookmarkDialog extends DialogFragment {
		public static final ConfirmDeleteaBookmarkDialog newInstance(int position){
			ConfirmDeleteaBookmarkDialog fragment = new ConfirmDeleteaBookmarkDialog();
			Bundle bundle = new Bundle();
			bundle.putInt("position", position);
			fragment.setArguments(bundle);
			return fragment ;
		}
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int position = getArguments().getInt("position");
			final Bookmark bookmark = Data.getBookmarks().get(position);
			
			return new AlertDialog.Builder(getActivity(), android.R.style.Theme_Holo_Dialog)
			.setIcon(getActivity().getResources().getDrawable(R.drawable.ic_action_warning))
			.setTitle("Confirm delete bookmark")
			.setMessage(bookmark.getAuthor() + "\n" + bookmark.getAlbum())
			.setNegativeButton("Cancel", null) //Do nothing
			.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					Audiobook audiobook = AudiobookManager.getInstance().getAudiobook(bookmark);
					if(audiobook != null && audiobook.equals(Data.getAudiobook()) && miniplayer != null){
						//stop and un-set as current
						miniplayer.getPlayer().pause();
						Data.setAudiobook(null);
						Data.setTrack(null);
						Data.setPosition(-1);
						
						//update the miniplayers view
						miniplayer.updateView();
					}

					//Remove the audiobook
					BookmarkManager.getInstance().removeBookmark(getActivity(), bookmark);
					Log.d(TAG, "Deleting Bookmark:\n"+bookmark);

					//update the lists
//					AudiobookAdapter audiobookAdapter = audiobookGridFragment.getAdapter();
//					if(audiobookAdapter != null) audiobookAdapter.notifyDataSetChanged();
					
					BookmarkAdapter bookmarkAdapter = bookmarkListFragment.getAdapter();
					if(bookmarkAdapter != null) bookmarkAdapter.notifyDataSetChanged();
					
					//update the controller
					if(controllerFragment != null){
						controllerFragment.displayValues();
						controllerFragment.displayTracks();
						controllerFragment.displayProgress();
					}
				}
			})
			.create();
		}
	}
}
