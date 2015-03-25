package rd.dap.fragments;

import static rd.dap.activities.FileBrowserActivity.TYPE_FOLDER;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import rd.dap.R;
import rd.dap.activities.AudiobookActivity;
import rd.dap.activities.AudiobooksActivity;
import rd.dap.activities.FileBrowserActivity;
import rd.dap.events.Event;
import rd.dap.events.Event.Type;
import rd.dap.events.EventBus;
import rd.dap.events.Subscriber;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.AudiobookManager.ProgressCallback;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AudiobooksFragment extends Fragment implements Subscriber, OnItemClickListener, OnItemLongClickListener {
	private static final int REQUEST_EDIT_AUDIOBOOK = 9002;
	private static final int REQUEST_SET_HOME_FOLDER = 9003;
	public static final int STATE_NEW = 501;
	public static final int STATE_EDIT = 502;
	private ViewGroup layout;
	private GridView gridview;
	private View lbl_saving;
	private ArrayList<Audiobook> audiobooks;
	private GridViewAdapter adapter;
	private Activity activity;
	private static boolean saving = false;

	public AudiobooksFragment(){
		AudiobookManager am =  AudiobookManager.getInstance();
		audiobooks = am.getAudiobooks();
		Collections.sort(audiobooks);
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		EventBus.addSubsciber(this);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		layout = (ViewGroup) inflater.inflate(R.layout.fragment_audiobooks_grid, container, false);
		gridview = (GridView) layout.findViewById(R.id.fragment_audiobooks_grid_grid);
		lbl_saving = layout.findViewById(R.id.fragment_audiobooks_grid_saving);

		if(savedInstanceState != null){
			saving = savedInstanceState.getBoolean("saving");
		}
		lbl_saving.setVisibility(saving ? View.VISIBLE : View.GONE);
		
		adapter = new GridViewAdapter(activity, audiobooks);
		gridview.setAdapter(adapter);

		gridview.setOnItemClickListener(this);
		gridview.setOnItemLongClickListener(this);

		return layout;
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("saving", saving);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Audiobook audiobook = audiobooks.get(position);
		EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.AUDIOBOOKS_SELECTED_EVENT).setAudiobook(audiobook));
		if(activity.getClass().getSimpleName().equals(AudiobooksActivity.class.getSimpleName())) {
			activity.finish();
		}
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position,	long id) {
		Audiobook clicked_audiobook = audiobooks.get(position);
		changeAudiobookDialog(clicked_audiobook);
		return true;
	}

	//Menu
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.audiobooks, menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_audiobooks_set_home:
			AudiobookManager.getInstance().removeAllAudiobooks(activity);
			Intent intent = new Intent(activity, FileBrowserActivity.class);
			intent.putExtra("type", TYPE_FOLDER);
			intent.putExtra("message", "Select Home folder");
			intent.putExtra("requestcode", REQUEST_SET_HOME_FOLDER);
			startActivityForResult(intent, REQUEST_SET_HOME_FOLDER);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		switch(requestCode){
		case REQUEST_EDIT_AUDIOBOOK: adapter.notifyDataSetChanged(); break;
		case REQUEST_SET_HOME_FOLDER:
			System.out.println("RESULT");
			if(data == null) return;
			String folder_path = data.getStringExtra("result");
			File folder = new File(folder_path);
			SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
			pref.edit().putString("homefolder", folder.getAbsolutePath()).commit();

			loadAudiobooksDialog(folder);
			break;
		}
	}
	private void loadAudiobooksDialog(File folder){
		final String TAG = "show_audibooks_loading_dialog";
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag(TAG);
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		final LoadAudiobooksDialogFragment newFragment = LoadAudiobooksDialogFragment.newInstance();
		newFragment.show(ft, TAG);


		//Do stuff
		final AudiobookManager am = AudiobookManager.getInstance();
		AsyncTask<File, String, Void> autoDetectAudiobooksTask = new AsyncTask<File, String, Void>() {
			@Override
			protected Void doInBackground(File... params) {
				File folder = params[0];
				ArrayList<Audiobook> list = new ArrayList<Audiobook>();
				ProgressCallback pcallback = new ProgressCallback() {
					@Override
					public void onProgressChanged(String _progress) {
						publishProgress(_progress);
					}
				};
				list.addAll(am.autodetect(folder, pcallback));
				am.addAllAudiobooks(activity, list, pcallback);
				return null;
			}
			@Override
			protected void onProgressUpdate(String... values) {
				super.onProgressUpdate(values);
				String _progress = values[0];
				EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.AUDIOBOOKS_DISCOVER_ELEMENT_EVENT).setString(_progress));
			}
			@Override
			protected void onPostExecute(Void result){
				EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.AUDIOBOOKS_DISCOVERED_EVENT));
			}
		};
		autoDetectAudiobooksTask.execute(folder);
	}
	
	
	@Override
	public void onEvent(Event event) {
		switch(event.getType()){
		case AUDIOBOOKS_DISCOVERED_EVENT:
			final AudiobookManager am = AudiobookManager.getInstance();
			AsyncTask<Void, Void, Void> saveAudiobooksTask = new AsyncTask<Void, Void, Void>() {
				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					lbl_saving.setVisibility(View.VISIBLE);
					saving = true;
				}
				@Override
				protected Void doInBackground(Void... params) {
					am.saveAudiobooks(activity);
					return null;
				}
				@Override
				protected void onPostExecute(Void result) {
					super.onPostExecute(result);
					String src = getClass().getSimpleName();
					EventBus.fireEvent(new Event(src, Type.AUDIOBOOKS_SAVED_EVENT));
				}
			};
			adapter.notifyDataSetChanged();
			saveAudiobooksTask.execute();
			break;
		case AUDIOBOOKS_SAVED_EVENT:
			saving = false;
			lbl_saving.setVisibility(View.GONE);
			String text = activity.getResources().getString(R.string.saving_complete);
			Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	}

	private void changeAudiobookDialog(final Audiobook audiobook){
		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dv = inflater.inflate(R.layout.dialog_text_2btn, gridview, false);

		//Title
		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
		title_tv.setText("Change audiobook");

		//Message
		TextView msg_tv = (TextView) dv.findViewById(R.id.dialog_msg_tv);
		msg_tv.setText(audiobook.getAuthor() + "\n" + audiobook.getAlbum());

		//Exit button
		ImageButton exit_btn = (ImageButton) dv.findViewById(R.id.dialog_exit_btn);
		exit_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		//Left button
		Button left_btn = (Button) dv.findViewById(R.id.dialog_left_btn);
		left_btn.setText("Delete");
		left_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				deleteAudiobookDialog(audiobook);
			}
		});

		//Right button
		Button right_btn = (Button) dv.findViewById(R.id.dialog_right_btn);
		right_btn.setText("Edit");
		right_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();

				Intent intent = new Intent(activity, AudiobookActivity.class);
				intent.putExtra("state", STATE_EDIT);
				intent.putExtra("audiobook", audiobook);
				startActivityForResult(intent, REQUEST_EDIT_AUDIOBOOK); 
			}
		});

		dialog.setContentView(dv);
		dialog.show();
	}
	private void deleteAudiobookDialog(final Audiobook audiobook){
		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dv = inflater.inflate(R.layout.dialog_text_2btn, gridview, false);

		//Title
		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
		title_tv.setText("Delete audiobook");

		//Message
		TextView msg_tv = (TextView) dv.findViewById(R.id.dialog_msg_tv);
		msg_tv.setText(audiobook.getAuthor() + "\n" + audiobook.getAlbum());

		//Exit button
		ImageButton exit_btn = (ImageButton) dv.findViewById(R.id.dialog_exit_btn);
		exit_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		//Left button
		Button left_btn = (Button) dv.findViewById(R.id.dialog_left_btn);
		left_btn.setText("Cancel");
		left_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		//Right button
		Button right_btn = (Button) dv.findViewById(R.id.dialog_right_btn);
		right_btn.setText("Confirm");
		right_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();

				audiobooks.remove(audiobook);
				adapter.notifyDataSetChanged();

				//Remove the audiobook
				new AsyncTask<Audiobook, Void, Void>(){
					@Override
					protected Void doInBackground(Audiobook... params) {
						Audiobook audiobook = params[0];
						AudiobookManager am = AudiobookManager.getInstance();
						audiobooks = am.removeAudiobook(activity, audiobook);
						am.saveAudiobooks(activity);
						BookmarkManager bm = BookmarkManager.getInstance();
						Bookmark bookmark = bm.getBookmark(audiobook);
						BookmarkManager.getInstance().removeBookmark(activity, bookmark);

						activity.runOnUiThread(new Runnable() {
							@Override public void run() {
								adapter.notifyDataSetChanged();
							}
						});
						return null;
					}
				}.execute(audiobook);
			}
		});

		dialog.setContentView(dv);
		dialog.show();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	public class GridViewAdapter extends ArrayAdapter<Audiobook> {
		private Context context;
		private ArrayList<Audiobook> list;

		public GridViewAdapter(Context context, ArrayList<Audiobook> list) {
			super(context, R.layout.fragment_audiobooks_grid_item, list);
			this.context = context;
			this.list = list;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) context).getLayoutInflater();
				convertView = inflater.inflate(R.layout.fragment_audiobooks_grid_item, parent, false);
				holder = new ViewHolder();
				holder.author_tv = (TextView) convertView.findViewById(R.id.fragment_audiobooks_grid_item_author_tv);
				holder.album_tv = (TextView) convertView.findViewById(R.id.fragment_audiobooks_grid_item_album_tv);
				holder.image = (ImageView) convertView.findViewById(R.id.fragment_audiobooks_grid_item_iv);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			Audiobook audiobook = list.get(position);
			holder.author_tv.setText(audiobook.getAuthor());
			holder.album_tv.setText(audiobook.getAlbum());
			holder.image.setImageBitmap(audiobook.getThumbnail());
			return convertView;
		}
	}

	static class ViewHolder {
		TextView author_tv, album_tv;
		ImageView image;
	}

	public void displayAudiobooks() {
		if(adapter != null) adapter.notifyDataSetChanged();
	}

}
