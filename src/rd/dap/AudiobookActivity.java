package rd.dap;

import static rd.dap.support.AudiobookDetailsAdapter.NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK;
import static rd.dap.support.AudiobookDetailsAdapter.TYPE_ALBUM;
import static rd.dap.support.AudiobookDetailsAdapter.TYPE_AUTHOR;
import static rd.dap.support.AudiobookDetailsAdapter.TYPE_COVER;

import java.util.ArrayList;

import rd.dap.model.Audiobook;
import rd.dap.model.Track;
import rd.dap.support.AudiobookDetailsAdapter;
import rd.dap.support.TextInputDialog;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class AudiobookActivity extends Activity {
	private AudiobookDetailsAdapter adapter;
	private Audiobook audiobook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		audiobook = (Audiobook) getIntent().getExtras().getSerializable("audiobook");
		if(audiobook == null) return;

		//detailslist
		ListView list = new ListView(AudiobookActivity.this);
		ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.light_gray));
		list.setDivider(colorDrawable);
		list.setDividerHeight(1);
		setContentView(list);
		adapter = new AudiobookDetailsAdapter(this, R.layout.audiobook_item_track, audiobook.getPlaylist());
		adapter.setAudiobook(audiobook);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(position == TYPE_AUTHOR){
					FragmentManager manager = getFragmentManager();
					ArrayList<String> list = new ArrayList<String>();
					list.add("Dennis Jürgensen");
					list.add("Rick Riordan");
					list.add("John G. Hemry");
					String title = "Author";
					String message = "Enter an author name or select from list";
					String value = audiobook.getAuthor();
					DialogFragment dialog = new TextInputDialog(title, message, value, list) {
						@Override 
						public void setResult(String result) {
							audiobook.setAuthor(result);
							adapter.notifyDataSetChanged();
						}
					};
					dialog.show(manager, "TextInputDialog");
				} else if(position == TYPE_ALBUM) {
					FragmentManager manager = getFragmentManager();
					ArrayList<String> list = new ArrayList<String>();
					String title = "Album";
					String message = "Enter an album name";
					String value = audiobook.getAlbum();
					DialogFragment dialog = new TextInputDialog(title, message, value, list) {
						@Override 
						public void setResult(String result) {
							audiobook.setAlbum(result);
							adapter.notifyDataSetChanged();
						}
					};
					dialog.show(manager, "TextInputDialog");
				} else if(position == TYPE_COVER) {
					Toast.makeText(AudiobookActivity.this, "Cover", Toast.LENGTH_SHORT).show();
				} else {
					final Track track = audiobook.getPlaylist().get(position-NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK);					
					FragmentManager manager = getFragmentManager();
					ArrayList<String> list = new ArrayList<String>();
					String title = "Track";
					String message = "Enter a track name";
					String value = track.getTitle();
					DialogFragment dialog = new TextInputDialog(title, message, value, list) {
						@Override 
						public void setResult(String result) {
							track.setTitle(result);
							adapter.notifyDataSetChanged();
						}
					};
					dialog.show(manager, "TextInputDialog");
				}
			}
		});
		
//		list.setOnItemLongClickListener(new OnItemLongClickListener() {
//			@Override
//			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//				if(position == TYPE_AUTHOR){
//					// Do nothing
//				} else if(position == TYPE_ALBUM) {
//					// Do nothing
//				} else if(position == TYPE_COVER) {
//					// Do nothing
//				} else {
//					final Track track = audiobook.getPlaylist().get(position-NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK);					
//					FragmentManager manager = getFragmentManager();
//					ArrayList<String> list = new ArrayList<String>();
//					String title = "File name";
//					String message = "Enter a file name";
//					String value = track.getFile().getName();
//					DialogFragment dialog = new TextInputDialog(title, message, value, list) {
//						@Override 
//						public void setResult(String result) {
//							String path = track.getFile().getParentFile().getAbsolutePath();
//							track.getFile().renameTo(new File(path, result));
//							adapter.notifyDataSetChanged();
//						}
//					};
//					dialog.show(manager, "TextInputDialog");
//				}
//				return true;
//			}
//		});

	}

	//Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.audiobook, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.menu_item_edit_mode) {
			Intent intent = new Intent(this, EditAudiobookActivity.class);
			intent.putExtra("audiobook", audiobook);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
}
