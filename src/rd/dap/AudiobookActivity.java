package rd.dap;

import static rd.dap.support.AudiobookDetailsAdapter.NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK;
import static rd.dap.support.AudiobookDetailsAdapter.TYPE_ALBUM;
import static rd.dap.support.AudiobookDetailsAdapter.TYPE_AUTHOR;
import static rd.dap.support.AudiobookDetailsAdapter.TYPE_COVER;
import rd.dap.model.Audiobook;
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
//					EditText et = new EditText(AudiobookActivity.this);
////					et.setBackground(AudiobookActivity.this.getResources().getDrawable(R.drawable.miniplayer_bg));
////					et.setBackgroundColor(AudiobookActivity.this.getResources().getColor(R.color.black));
////					et.setTextColor(AudiobookActivity.this.getResources().getColor(R.color.white));
//					AlertDialog.Builder builder = new AlertDialog.Builder(AudiobookActivity.this);
//					builder.setView(et);
//					builder.setPositiveButton("OK", null);
//					builder.setNegativeButton("Cancel", null);
//					Dialog d = builder.create();
//					d.getWindow().set
//					d.getWindow().setBackgroundDrawable(AudiobookActivity.this.getResources().getDrawable(R.drawable.miniplayer_bg));
//					d.show();
					FragmentManager manager = getFragmentManager();
					DialogFragment dialog = new TextInputDialog();
					dialog.show(manager, "TextInputDialog");
					
//					Toast.makeText(AudiobookActivity.this, "Author", Toast.LENGTH_SHORT).show();
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
			Intent intent = new Intent(this, EditAudiobookActivity.class);
			intent.putExtra("audiobook", audiobook);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
}
