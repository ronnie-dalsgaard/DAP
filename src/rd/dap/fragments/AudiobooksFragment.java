package rd.dap.fragments;

import static rd.dap.activities.FileBrowserActivity.TYPE_FOLDER;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import rd.dap.R;
import rd.dap.activities.AudiobookActivity;
import rd.dap.activities.FileBrowserActivity;
import rd.dap.events.Event;
import rd.dap.events.Event.Type;
import rd.dap.events.EventBus;
import rd.dap.events.HasAudiobookEvent;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
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

public class AudiobooksFragment extends Fragment implements OnItemClickListener, OnItemLongClickListener {
	private static final int REQUEST_EDIT_AUDIOBOOK = 9002;
	private static final int REQUEST_SET_HOME_FOLDER = 9003;
	public static final int STATE_NEW = 501;
	public static final int STATE_EDIT = 502;
	private GridView gridview;
	private ArrayList<Audiobook> audiobooks;
	private GridViewAdapter adapter;

	public AudiobooksFragment(){
		AudiobookManager am =  AudiobookManager.getInstance();
		audiobooks = am.getAudiobooks();
		Collections.sort(audiobooks);
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		gridview = (GridView) inflater.inflate(R.layout.fragment_audiobooks_grid, container, false);

		adapter = new GridViewAdapter(getActivity(), audiobooks);
		gridview.setAdapter(adapter);

		gridview.setOnItemClickListener(this);
		gridview.setOnItemLongClickListener(this);

		return gridview;
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Audiobook audiobook = audiobooks.get(position);
		Event event = new HasAudiobookEvent(getClass().getSimpleName(), Type.AUDIOBOOKS_SELECTED_EVENT, audiobook);
		EventBus.fireEvent(event);
		getActivity().finish();
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
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.audiobooks, menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_audiobooks_set_home:
			AudiobookManager.getInstance().removeAllAudiobooks(getActivity());
			Intent intent = new Intent(getActivity(), FileBrowserActivity.class);
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
			SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
			pref.edit().putString("homefolder", folder.getAbsolutePath()).commit();

			loadAudiobooksDialog(folder);
			break;
		}
	}
	private void loadAudiobooksDialog(File folder){
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View dv = inflater.inflate(R.layout.loading, gridview, false);
		dialog.setContentView(dv);
		dialog.show();
		new AsyncTask<File, Void, Void>() {
			@Override
			protected Void doInBackground(File... params) {
				File folder = params[0];
				AudiobookManager am = AudiobookManager.getInstance();
				ArrayList<Audiobook> list = new ArrayList<Audiobook>();
				list.addAll(am.autodetect(folder));
				am.addAllAudiobooks(getActivity(), list);
				return null;
			}
			@Override
			protected void onPostExecute(Void result){
				dialog.dismiss();
				adapter.notifyDataSetChanged();
			}
		}.execute(folder);
	}
	private void changeAudiobookDialog(final Audiobook audiobook){
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(getActivity());
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

				Intent intent = new Intent(getActivity(), AudiobookActivity.class);
				intent.putExtra("state", STATE_EDIT);
				intent.putExtra("audiobook", audiobook);
				startActivityForResult(intent, REQUEST_EDIT_AUDIOBOOK); 
			}
		});

		dialog.setContentView(dv);
		dialog.show();
	}
	private void deleteAudiobookDialog(final Audiobook audiobook){
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(getActivity());
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
				System.out.println("Notifying adapter once");

				//Remove the audiobook
				new AsyncTask<Audiobook, Void, Void>(){
					@Override
					protected Void doInBackground(Audiobook... params) {
						Audiobook audiobook = params[0];
						AudiobookManager am = AudiobookManager.getInstance();
						audiobooks = am.removeAudiobook(getActivity(), audiobook);
						BookmarkManager bm = BookmarkManager.getInstance();
						Bookmark bookmark = bm.getBookmark(audiobook);
						BookmarkManager.getInstance().removeBookmark(getActivity(), bookmark);

						getActivity().runOnUiThread(new Runnable() {
							@Override public void run() {
								adapter.notifyDataSetChanged();
							}
						});
						System.out.println("Notifying adapter twice");
						return null;
					}
				}.execute(audiobook);
			}
		});

		dialog.setContentView(dv);
		dialog.show();
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
