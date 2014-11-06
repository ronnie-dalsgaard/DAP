package rd.dap.activities;

import static rd.dap.activities.AudiobookActivity.STATE_EDIT;
import static rd.dap.activities.FileBrowserActivity.TYPE_FOLDER;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import rd.dap.R;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.model.Data;
import android.app.Activity;
import android.app.Dialog;
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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AudiobooksActivity extends Activity implements OnClickListener, OnLongClickListener {
	private static final String TAG = "AudiobookGridActivity";
	private LinearLayout layout;
	private static final int REQUEST_EDIT_AUDIOBOOK = 9002;
	private static final int REQUEST_SET_HOME_FOLDER = 9003;
	public static final int REQUEST_AUDIOBOOK = 8801;
	private static final int COLUMNS = 3;

	//Fragment must-haves
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		int width = (int) getResources().getDimension(R.dimen.mini_player_width);
		int height = LayoutParams.WRAP_CONTENT;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);

		ScrollView scroller = new ScrollView(this);
		scroller.addView(layout);

		RelativeLayout base = new RelativeLayout(this);
		base.addView(scroller, params);
		
		//////////////Delete all audiobooks ///////////////
//		AudiobookManager.getInstance().removeAllAudiobooks(this);
		////////////////////////////////////////////////////

		if(AudiobookManager.getAudiobooks().isEmpty()){
			askToLoadDialog();
		} else {
			displayAudiobooks();
		}
		
		setContentView(base);
	}

	public void displayAudiobooks(){
		Log.d(TAG, "displayAudiobooks");
		if(layout != null){
			layout.removeAllViews();
			HashSet<String> authors_set = new HashSet<String>();
			for(Audiobook audiobook : Data.getAudiobooks()){
				authors_set.add(audiobook.getAuthor());
			}
			ArrayList<String> authors = new ArrayList<String>();
			authors.addAll(authors_set);


			//LayoutParams
			int width = LayoutParams.MATCH_PARENT;
			int height = LayoutParams.WRAP_CONTENT;
			LayoutParams table_params = new LayoutParams(width, height);
			int buttomMargin = (int) getResources().getDimension(R.dimen.margin_big);
			table_params.setMargins(0, 0, 0, buttomMargin);

			LayoutParams row_params = new LayoutParams(width, height);

			width = (int) getResources().getDimension(R.dimen.cover_width_big);
			height = (int) getResources().getDimension(R.dimen.cover_height_big);
			LayoutParams cover_params = new LayoutParams(width, height);

			LayoutParams element_params = new LayoutParams(0, height, 1);

			for(String author : authors){
				TextView author_tv = new TextView(this);
				author_tv.setTextAppearance(this, android.R.style.TextAppearance_Large);
				author_tv.setTextColor(getResources().getColor(R.color.white));
				author_tv.setText(author);
				layout.addView(author_tv);

				LinearLayout table = new LinearLayout(this);
				table.setOrientation(LinearLayout.VERTICAL);

				ArrayList<Audiobook> books_by_author = new ArrayList<Audiobook>();

				for(Audiobook audiobook : Data.getAudiobooks()){
					if(author.equals(audiobook.getAuthor())){
						books_by_author.add(audiobook);
					}
				}		

				LinearLayout row = null;

				for(int i = 0; i < books_by_author.size(); i++){
					Audiobook audiobook = books_by_author.get(i);

					if(i % COLUMNS == 0){
						row = new LinearLayout(this);
						row.setOrientation(LinearLayout.HORIZONTAL);
						table.addView(row, row_params);
					}

					ImageView cover_iv = new ImageView(this);
					LinearLayout element = new LinearLayout(this);
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
				}

				int missing = COLUMNS - (books_by_author.size() % COLUMNS);
				if(missing < COLUMNS){
					for(int i = 0; i < missing; i++){
						View dummy = new View(this);
						row.addView(dummy, element_params);
					}
				}

				layout.addView(table, table_params);
			}
		}
	}

	//Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.audiobooks, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_audiobooks_reload:
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
			SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
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
		Intent intent = new Intent();
		intent.putExtra("result", audiobook);
		setResult(REQUEST_AUDIOBOOK, intent);
		finish();
	}
	@Override
	public boolean onLongClick(View view) {
		Log.d(TAG, "onItemLongClick");
		Audiobook clicked = (Audiobook) view.getTag();
		changeAudiobookDialog(clicked);

		return true; //consume click
	}

	private void setHomeFolder(){
		AudiobookManager.getInstance().removeAllAudiobooks(this);
		displayAudiobooks();
		Intent intent = new Intent(this, FileBrowserActivity.class);
		intent.putExtra("type", TYPE_FOLDER);
		intent.putExtra("message", "Select Home folder");
		intent.putExtra("requestcode", REQUEST_SET_HOME_FOLDER);
		startActivityForResult(intent, REQUEST_SET_HOME_FOLDER);
	}
	private void loadAudibooks(){
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
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
		dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(this);
		View dv = inflater.inflate(R.layout.dialog, layout, false);

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

				Intent intent = new Intent(AudiobooksActivity.this, AudiobookActivity.class);
				intent.putExtra("state", STATE_EDIT);
				intent.putExtra("audiobook", audiobook);
				startActivityForResult(intent, REQUEST_EDIT_AUDIOBOOK); 
			}
		});


		dialog.setContentView(dv);
		dialog.show();
	}
	private void deleteAudiobookDialog(final Audiobook audiobook){
		dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(this);
		View dv = inflater.inflate(R.layout.dialog, layout, false);

		//Title
		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
		title_tv.setText("Delete audiobooke");

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
				AudiobookManager.getInstance().removeAudiobook(AudiobooksActivity.this, audiobook);
				Bookmark bookmark = BookmarkManager.getInstance().getBookmark(audiobook);
				BookmarkManager.getInstance().removeBookmark(AudiobooksActivity.this, bookmark);
				Log.d(TAG, "Deleting Audiobook:\n"+audiobook);
				Log.d(TAG, "Deleting Bookmark:\n"+bookmark);
				
				displayAudiobooks();
			}
		});

		dialog.setContentView(dv);
		dialog.show();
	}
	private void askToLoadDialog(){
		dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(this);
		View dv = inflater.inflate(R.layout.dialog, layout, false);

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
		dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(this);
		View dv = inflater.inflate(R.layout.loading, layout, false);
		dialog.setContentView(dv);
		dialog.show();
		new AsyncTask<File, Void, Void>() {

			@Override
			protected Void doInBackground(File... params) {
				File folder = params[0];
				AudiobookManager am = AudiobookManager.getInstance();
				ArrayList<Audiobook> list = am.autodetect(folder);
				am.addAllAudiobooks(AudiobooksActivity.this, list);
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result){
				dialog.dismiss();
				if(AudiobookManager.getAudiobooks().isEmpty()){
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
