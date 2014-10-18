package rd.dap.fragments;

import static rd.dap.AudiobookActivity.STATE_NEW;
import static rd.dap.FileBrowserActivity.TYPE_FOLDER;
import static rd.dap.MainActivity.miniplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import rd.dap.AudiobookActivity;
import rd.dap.FileBrowserActivity;
import rd.dap.R;
import rd.dap.dialogs.ChangeAudiobookDialogFragment;
import rd.dap.dialogs.Changer;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Data;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AudiobookGridFragment extends Fragment implements OnClickListener, OnLongClickListener {
	private static final String TAG = "AudiobookGridActivity";
	private LinearLayout layout;
	private static final int REQUEST_NEW_AUDIOBOOK = 9001;
	private static final int REQUEST_EDIT_AUDIOBOOK = 9002;
	private Changer changer;
	private static final int COLUMNS = 3;
	
	//Fragment must-haves
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		
		
//		adapter = new AudiobookAdapter(getActivity(), R.layout.cover_view, authors);
		
		setHasOptionsMenu(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");

		layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);
		
		int width = (int) getResources().getDimension(R.dimen.mini_player_width);
		int height = LayoutParams.WRAP_CONTENT;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		
		ScrollView scroller = new ScrollView(getActivity());
		scroller.addView(layout);
		
		RelativeLayout base = new RelativeLayout(getActivity());
		base.addView(scroller, params);
		
		displayAudiobooks();

		return base;
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

			width = (int)getResources().getDimension(R.dimen.cover_width_big);
			height = (int)getResources().getDimension(R.dimen.cover_height_big);
			LayoutParams cover_params = new LayoutParams(width, height);

			LayoutParams element_params = new LayoutParams(0, height, 1);

			for(String author : authors){
				TextView author_tv = new TextView(getActivity());
				author_tv.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
				author_tv.setTextColor(getResources().getColor(R.color.white));
				author_tv.setText(author);
				layout.addView(author_tv);

				LinearLayout table = new LinearLayout(getActivity());
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
						row = new LinearLayout(getActivity());
						row.setOrientation(LinearLayout.HORIZONTAL);
						table.addView(row, row_params);
					}

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
				}

				int missing = COLUMNS - (books_by_author.size() % COLUMNS);
				if(missing < COLUMNS){
					for(int i = 0; i < missing; i++){
						View dummy = new View(getActivity());
						row.addView(dummy, element_params);
					}
				}

				layout.addView(table, table_params);
			}
		}
	}
	
	//Callback
	@Override 
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try {
            changer = (Changer) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callback");
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
		case R.id.menu_item_new_audiobook:
			Log.d(TAG, "menu_item_new_audiobook");
			Intent intent = new Intent(getActivity(), FileBrowserActivity.class);
			intent.putExtra("type", TYPE_FOLDER);
			intent.putExtra("message", "Select folder");
			intent.putExtra("requestcode", REQUEST_NEW_AUDIOBOOK);
			startActivityForResult(intent, REQUEST_NEW_AUDIOBOOK);
			break;

		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		Log.d(TAG, "onActivityResult");
		switch(requestCode){
		case REQUEST_NEW_AUDIOBOOK:
			Log.d(TAG, "onActivityResult - REQUEST_NEW_AUDIOBOOK");
			if(data == null) return;
			String folder_path = data.getStringExtra("result");
			File folder = new File(folder_path);
			AudiobookManager manager = AudiobookManager.getInstance();
			Audiobook audiobook = manager.autoCreateAudiobook(folder, true);
			Intent intent = new Intent(getActivity(), AudiobookActivity.class);
			intent.putExtra("state", STATE_NEW);
			intent.putExtra("audiobook", audiobook);
			startActivityForResult(intent, REQUEST_EDIT_AUDIOBOOK);
			break;
		case REQUEST_EDIT_AUDIOBOOK:
			Log.d(TAG, "onActivityResult - REQUEST_EDIT_AUDIOBOOK");
			
			//update the lists
			changer.updateAudiobooks();
			changer.updateBookmarks();
			changer.updateController();
		}
	}
	
	//Listeners

	@Override
	public void onClick(View view) {
		Log.d(TAG, "onItemClick");
		Audiobook audiobook = (Audiobook) view.getTag();
		Data.setCurrentAudiobook(audiobook);
		Data.setCurrentPosition(0);
		Data.setCurrentTrack(Data.getCurrentAudiobook().getPlaylist().get(Data.getCurentPosition()));
		
		changer.updateController();
		
		miniplayer.reload();
		miniplayer.updateView();
	}
	@Override
	public boolean onLongClick(View view) {
		Log.d(TAG, "onItemLongClick");
		Audiobook clicked = (Audiobook) view.getTag();
		int position = -1;
		for(int i = 0; i < Data.getAudiobooks().size(); i++){
			if(Data.getAudiobooks().get(i).equals(clicked)){
				position = i;
			}
		}
		
		ChangeAudiobookDialogFragment frag = ChangeAudiobookDialogFragment.newInstance(position);
		frag.setTargetFragment(this, REQUEST_EDIT_AUDIOBOOK);
		frag.show(getFragmentManager(), "ChagenAudiobookDialog");
		
		return true; //consume click
	}

}
