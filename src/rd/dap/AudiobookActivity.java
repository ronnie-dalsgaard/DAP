package rd.dap;

import rd.dap.model.Audiobook;
import rd.dap.support.AudiobookDetailsAdapter;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import static rd.dap.support.AudiobookDetailsAdapter.*;

public class AudiobookActivity extends Activity {
	private boolean edit_mode = false;
	private AudiobookDetailsAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Audiobook audiobook = (Audiobook) getIntent().getExtras().getSerializable("audiobook");
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
				if(position == TYPE_AUTHOR){
					Toast.makeText(AudiobookActivity.this, "Author", Toast.LENGTH_SHORT).show();
				} else if(position == TYPE_ALBUM) {
					Toast.makeText(AudiobookActivity.this, "Album", Toast.LENGTH_SHORT).show();
				} else if(position == TYPE_COVER) {
					Toast.makeText(AudiobookActivity.this, "Cover", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(AudiobookActivity.this, "Track "+(position-NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK), Toast.LENGTH_SHORT).show();
				}
				
//				ListView list = new ListView(AudiobookActivity.this);
			}
		});

		//edit mode
		editMode();
	}

	private void editMode(){
//		ImageView[] edits = new ImageView[3];
//		edits[0] = (ImageView) findViewById(R.id.audiobook_album_edit_btn);
//		edits[1] = (ImageView) findViewById(R.id.audiobook_author_edit_btn);
//		edits[2] = (ImageView) findViewById(R.id.audiobook_cover_edit_btn);
//		for(ImageView edit : edits){
//			if(edit_mode) edit.setVisibility(View.VISIBLE);
//			else edit.setVisibility(View.GONE);
//		}
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
			editMode();
			adapter.setEditMode(edit_mode);
			adapter.notifyDataSetChanged();
		}
		return super.onOptionsItemSelected(item);
	}
}
