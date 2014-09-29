package rd.dap;

import static rd.dap.support.AudiobookDetailsAdapter.NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK;
import static rd.dap.support.AudiobookDetailsAdapter.TYPE_ALBUM;
import static rd.dap.support.AudiobookDetailsAdapter.TYPE_AUTHOR;
import static rd.dap.support.AudiobookDetailsAdapter.TYPE_COVER;

import java.util.ArrayList;

import rd.dap.model.Audiobook;
import rd.dap.support.AudiobookDetailsAdapter;
import rd.dap.support.AuthorAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class AudiobookActivity extends Activity {
	private boolean edit_mode = false;
	private AudiobookDetailsAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Audiobook audiobook = (Audiobook) getIntent().getExtras().getSerializable("audiobook");
		if(audiobook == null) return;

		//detailslist
		ListView list = new ListView(AudiobookActivity.this);
		ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.light_gray));
		list.setDivider(colorDrawable);
		list.setDividerHeight(1);
		setContentView(list);
		adapter = new AudiobookDetailsAdapter(this, R.layout.track_item, audiobook.getPlaylist());
		adapter.setAudiobook(audiobook);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(!edit_mode) return;
				if(position == TYPE_AUTHOR){
					final ArrayList<String> authors = new ArrayList<String>();
					authors.add("Dennis Jürgensen"); authors.add("John G. Hemry"); authors.add("Rick Riordan");
					
					ListView list = new ListView(AudiobookActivity.this);
					list.setBackground(AudiobookActivity.this.getResources().getDrawable(R.drawable.miniplayer_bg));
					AlertDialog.Builder builder = new AlertDialog.Builder(AudiobookActivity.this);
					final ArrayAdapter<String> adapter = new AuthorAdapter(AudiobookActivity.this, R.layout.input_item, authors);
					list.setAdapter(adapter);
					builder.setView(list);
					Dialog dialog = builder.create();
					dialog.show();
				
				} else if(position == TYPE_ALBUM) {
					Toast.makeText(AudiobookActivity.this, "Album", Toast.LENGTH_SHORT).show();
				} else if(position == TYPE_COVER) {
					Toast.makeText(AudiobookActivity.this, "Cover", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(AudiobookActivity.this, "Track "+(position-NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK), Toast.LENGTH_SHORT).show();
				}
			}
		});
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
			edit_mode = !edit_mode;
			int string_id = edit_mode ? R.string.edit_mode_exit : R.string.edit_mode_enter;
			String item_title = getResources().getString(string_id) ;
			item.setTitle(item_title);
			adapter.setEditMode(edit_mode);
			adapter.notifyDataSetChanged();
		}
		return super.onOptionsItemSelected(item);
	}
}
