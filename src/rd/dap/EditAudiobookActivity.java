package rd.dap;

import rd.dap.model.Audiobook;
import rd.dap.support.AudiobookDetailsEditAdapter;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;

public class EditAudiobookActivity extends Activity {
	private AudiobookDetailsEditAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(EditorInfo.IME_ACTION_DONE);
		
		final Audiobook audiobook = (Audiobook) getIntent().getExtras().getSerializable("audiobook");
		if(audiobook == null) return;

		//detailslist
		ListView list = new ListView(EditAudiobookActivity.this);
		list.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.light_gray));
		list.setDivider(colorDrawable);
		list.setDividerHeight(1);
		setContentView(list);
		adapter = new AudiobookDetailsEditAdapter(this, R.layout.audiobook_item_track, audiobook.getPlaylist());
		adapter.setAudiobook(audiobook);
		list.setAdapter(adapter);
//		list.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				if(position == TYPE_AUTHOR){
//					Toast.makeText(EditAudiobookActivity.this, "Author", Toast.LENGTH_SHORT).show();
//				} else if(position == TYPE_ALBUM) {
//					Toast.makeText(EditAudiobookActivity.this, "Album", Toast.LENGTH_SHORT).show();
//				} else if(position == TYPE_COVER) {
//					Toast.makeText(EditAudiobookActivity.this, "Cover", Toast.LENGTH_SHORT).show();
//				} else {
//					Toast.makeText(EditAudiobookActivity.this, "Track "+(position-NUMBER_OF_ELEMENTS_NOT_OF_TYPE_TRACK), Toast.LENGTH_SHORT).show();
//				}
//			}
//		});
	}
}

