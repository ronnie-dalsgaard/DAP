package rd.dap;

import java.io.File;

import rd.dap.model.Audiobook;
import rd.dap.support.TrackAdapter;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AudiobookActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audiobook);
		
		Audiobook audiobook = (Audiobook) getIntent().getExtras().getSerializable("audiobook");
		if(audiobook == null) return;

		//Author
		TextView author_tv = (TextView) findViewById(R.id.audiobook_author_tv);
		author_tv.setText(audiobook.getAuthor());

		//Album
		TextView album_tv = (TextView) findViewById(R.id.audiobook_album_tv);
		album_tv.setText(audiobook.getAlbum());

		//Cover
		ImageView cover_iv = (ImageView) findViewById(R.id.audiobook_cover_iv);
		File cover = audiobook.getCover();
		if(cover != null){
			Bitmap bm = BitmapFactory.decodeFile(cover.getAbsolutePath());
			cover_iv.setImageBitmap(bm);
		} else {
			Drawable drw = getResources().getDrawable(R.drawable.ic_action_help);
			cover_iv.setImageDrawable(drw);
		}
		
		//Tracklist
		ListView list = (ListView) findViewById(R.id.audiobook_tracklist_lv);
		TrackAdapter adapter = new TrackAdapter(this, R.layout.track_item, audiobook.getPlaylist());
		list.setAdapter(adapter);
	}
}
