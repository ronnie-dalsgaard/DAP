package rd.dap.activities;

import static rd.dap.activities.AudiobookActivity.STATE_EDIT;
import static rd.dap.activities.FileBrowserActivity.TYPE_FOLDER;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import rd.dap.R;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AudiobooksFragment extends Fragment implements OnClickListener, OnLongClickListener {
	private static final String TAG = "AudioboosFragment";
	private LinearLayout layout;
	private static final int REQUEST_EDIT_AUDIOBOOK = 9002;
	private static final int REQUEST_SET_HOME_FOLDER = 9003;
	private OnAudiobookSelectedListener audiobookSelectedListener;
	
	public static interface OnAudiobookSelectedListener {
		public void onAudiobookSelected(Audiobook audiobook);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	audiobookSelectedListener = (OnAudiobookSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnAudiobookSelectedListener");
        }
    }


	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);

		ScrollView scroller = new ScrollView(getActivity());
		scroller.addView(layout);

//		int width = (int) getResources().getDimension(R.dimen.mini_player_width);
		int width = LayoutParams.WRAP_CONTENT;
		int height = LayoutParams.WRAP_CONTENT;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);

		RelativeLayout base = new RelativeLayout(getActivity());
		base.addView(scroller, params);
		
		//////////////Delete all audiobooks ///////////////
//		AudiobookManager.getInstance().removeAllAudiobooks(this);
		////////////////////////////////////////////////////

		if(AudiobookManager.getInstance().getAudiobooks().isEmpty()){
			askToLoadDialog();
		} else {
			displayAudiobooks();
		}
		
		return base;
	}
	
	public void displayAudiobooks(){
		Log.d(TAG, "displayAudiobooks");
		if(layout != null){
			layout.removeAllViews();
			HashSet<String> authors_set = new HashSet<String>();
			ArrayList<Audiobook> list = AudiobookManager.getInstance().getAudiobooks();
			Collections.sort(list);
			for(Audiobook audiobook : list){
				authors_set.add(audiobook.getAuthor());
			}
			ArrayList<String> authors = new ArrayList<String>();
			authors.addAll(authors_set);

			Collections.sort(authors);

			//LayoutParams
			int width = LayoutParams.WRAP_CONTENT;
			int height = LayoutParams.WRAP_CONTENT;
			LayoutParams table_params = new LayoutParams(width, height);
			int buttomMargin = (int) getResources().getDimension(R.dimen.margin_big);
			table_params.setMargins(0, 0, 0, buttomMargin);

			LayoutParams row_params = new LayoutParams(width, height);

			width = (int) getResources().getDimension(R.dimen.cover_width_big);
			height = (int) getResources().getDimension(R.dimen.cover_height_big);
			LayoutParams cover_params = new LayoutParams(width, height);

			LayoutParams element_params = new LayoutParams(width, height, 1);

			for(String author : authors){
				TextView author_tv = new TextView(getActivity());
				author_tv.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
				author_tv.setTextColor(getResources().getColor(R.color.white));
				author_tv.setText(author);
				layout.addView(author_tv);

				LinearLayout table = new LinearLayout(getActivity());
				table.setOrientation(LinearLayout.VERTICAL);

				LinkedList<Audiobook> albums_by_author = new LinkedList<Audiobook>();
				for(Audiobook audiobook : AudiobookManager.getInstance().getAudiobooks()){
					if(author.equals(audiobook.getAuthor())){
						albums_by_author.add(audiobook);
					}
				}
				if(albums_by_author.isEmpty()) return; //Just a precaution
				Collections.sort(albums_by_author);
				
				HashMap<String, LinkedList<Audiobook>> albummap = new HashMap<String, LinkedList<Audiobook>>();
				for(Audiobook album : albums_by_author){
					String series = album.getSeries();
					if(series == null) series = "null";
					LinkedList<Audiobook> serieslist;
					if(!albummap.containsKey(series)){
						serieslist = new LinkedList<Audiobook>();
						albummap.put(series, serieslist);
					} else {
						serieslist = albummap.get(series);
					}
					serieslist.addLast(album);
				}
				
				Set<String> keyset = albummap.keySet();
				ArrayList<String> keys = new ArrayList<String>();
				for(String key : keyset) keys.add(key);
				Collections.sort(keys);
				for(String series : keys){
					LinkedList<Audiobook> serieslist = albummap.get(series);
					LinearLayout row = null;
					int c = 0;
					while(!serieslist.isEmpty()){
						Audiobook audiobook = serieslist.removeFirst();
						if(c == 0){
							row = new LinearLayout(getActivity());
							row.setOrientation(LinearLayout.HORIZONTAL);
							table.addView(row, row_params);
						}
						//////////////////////////////
						ImageView cover_iv = new ImageView(getActivity());
						LinearLayout element = new LinearLayout(getActivity());
						element.setGravity(Gravity.CENTER_HORIZONTAL);
						element.setTag(audiobook);
						element.setOnClickListener(this);
						element.setOnLongClickListener(this);

						if(audiobook.getCover() != null){
							String cover = audiobook.getCover();
							Bitmap bm = BitmapFactory.decodeFile(cover);
							cover_iv.setImageBitmap(bm);
						} else {
							Drawable drw = getResources().getDrawable(R.drawable.ic_action_help);
							cover_iv.setImageDrawable(drw);
						}

						element.addView(cover_iv, cover_params);
						row.addView(element, element_params);
						c++;
						if(c == 3) c = 0;
						//////////////////////////
					}
					
				}
				layout.addView(table, table_params);
				
///////////////////////////////////////////////////////////////////////////////////////////

//				LinearLayout row = null;
//
//				for(int i = 0; i < albums_by_author.size(); i++){
//					Audiobook audiobook = albums_by_author.get(i);
//					
//
//					if(i % COLUMNS == 0){
//						row = new LinearLayout(getActivity());
//						row.setOrientation(LinearLayout.HORIZONTAL);
//						table.addView(row, row_params);
//					}
//
//					ImageView cover_iv = new ImageView(getActivity());
//					LinearLayout element = new LinearLayout(getActivity());
//					element.setGravity(Gravity.CENTER_HORIZONTAL);
//					element.setTag(audiobook);
//					element.setOnClickListener(this);
//					element.setOnLongClickListener(this);
//
//					if(audiobook.getCover() != null){
//						String cover = audiobook.getCover();
//						Bitmap bm = BitmapFactory.decodeFile(cover);
//						cover_iv.setImageBitmap(bm);
//					} else {
//						Drawable drw = getResources().getDrawable(R.drawable.ic_action_help);
//						cover_iv.setImageDrawable(drw);
//					}
//
//					element.addView(cover_iv, cover_params);
//					row.addView(element, element_params);
//				}
//
//				int missing = COLUMNS - (albums_by_author.size() % COLUMNS);
//				if(missing < COLUMNS){
//					for(int i = 0; i < missing; i++){
//						View dummy = new View(getActivity());
//						row.addView(dummy, element_params);
//					}
//				}
//
//				layout.addView(table, table_params);
			}
		}
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
		case R.id.menu_item_audiobooks:
			Log.d(TAG, "menu_item_reload_audiobooks");
			loadAudibooks();
			break;
		case R.id.menu_item_audiobooks_set_home:
			setHomeFolder();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		Log.d(TAG, "onActivityResult");
		switch(requestCode){
		case REQUEST_EDIT_AUDIOBOOK:
			Log.d(TAG, "onActivityResult - REQUEST_EDIT_AUDIOBOOK");
			displayAudiobooks();
			break;
		
		case REQUEST_SET_HOME_FOLDER:
			Log.d(TAG, "onActivityResult - REQUEST_LOAD_AUDIOBOOKS");
			if(data == null) return;
			String folder_path = data.getStringExtra("result");
			File folder = new File(folder_path);
			SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
			pref.edit().putString("homefolder", folder.getAbsolutePath()).commit();
			
			loadAudiobooksDialog(folder);
			break;
		}
	}
	

	//Listeners
	@Override
	public void onClick(View view) {
		Log.d(TAG, "onItemClick");
		Audiobook audiobook = (Audiobook) view.getTag();
		audiobookSelectedListener.onAudiobookSelected(audiobook);
	}
	@Override
	public boolean onLongClick(View view) {
		Log.d(TAG, "onItemLongClick");
		Audiobook clicked = (Audiobook) view.getTag();
		changeAudiobookDialog(clicked);

		return true; //consume click
	}

	private void setHomeFolder(){
		AudiobookManager.getInstance().removeAllAudiobooks(getActivity());
		displayAudiobooks();
		Intent intent = new Intent(getActivity(), FileBrowserActivity.class);
		intent.putExtra("type", TYPE_FOLDER);
		intent.putExtra("message", "Select Home folder");
		intent.putExtra("requestcode", REQUEST_SET_HOME_FOLDER);
		startActivityForResult(intent, REQUEST_SET_HOME_FOLDER);
	}
	private void loadAudibooks(){
		SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
		String path = pref.getString("homefolder", null);
		if(path == null){
			setHomeFolder();
		} else {
			File folder = new File(path);
			loadAudiobooksDialog(folder);
		}
	}
	
	Dialog dialog;
	private void changeAudiobookDialog(final Audiobook audiobook){
		dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View dv = inflater.inflate(R.layout.dialog_text_2btn, layout, false);

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
		dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View dv = inflater.inflate(R.layout.dialog_text_2btn, layout, false);

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

				//Remove the audiobook
				AudiobookManager.getInstance().removeAudiobook(getActivity(), audiobook);
				Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
				BookmarkManager.getInstance().removeBookmark(getActivity(), bookmark);
				Log.d(TAG, "Deleting Audiobook:\n"+audiobook);
				Log.d(TAG, "Deleting Bookmark:\n"+bookmark);
				
				displayAudiobooks();
			}
		});

		dialog.setContentView(dv);
		dialog.show();
	}
	private void askToLoadDialog(){
		dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View dv = inflater.inflate(R.layout.dialog_text_2btn, layout, false);

		//Title
		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
		title_tv.setText("No audiobooks");

		//Message
		TextView msg_tv = (TextView) dv.findViewById(R.id.dialog_msg_tv);
		msg_tv.setText("Do you whish to load audioooks?");

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
		right_btn.setText("OK");
		right_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				loadAudibooks();
			}
		});


		dialog.setContentView(dv);
		dialog.show();
	}
	private void loadAudiobooksDialog(File folder){
		dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View dv = inflater.inflate(R.layout.loading, layout, false);
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
				if(AudiobookManager.getInstance().getAudiobooks().isEmpty()){
					setHomeFolder();
				} else {
					displayAudiobooks();					
				}
			}
			
		}.execute(folder);
	}
	@Override
	public void onPause(){
		Log.d(TAG, "onPause");
		super.onPause();
		if(dialog == null) return;
		dialog.dismiss();
	}
}
