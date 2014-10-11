package rd.dap.fragments;

import static rd.dap.AudiobookActivity.STATE_EDIT;
import static rd.dap.PlayerService.audiobook;
import static rd.dap.PlayerService.track;

import java.io.File;
import java.util.ArrayList;

import rd.dap.AudiobookActivity;
import rd.dap.R;
import rd.dap.model.Audiobook;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FragmentAudiobookBasics extends Fragment implements OnClickListener {
	private final String TAG = "Audiobook_Basics_Fragment";
	private static Drawable noCover;
	private ImageView cover_iv;
	private TextView author_tv, audiobook_basics_album_tv;
	private ImageButton cover_btn;
	private LinearLayout info_layout;
	
	private static final int REQUEST_FRAGMENT_BASICS_EDIT = 1701;
	
	//Observer pattern
	private ArrayList<Fragment_Audiobooks_Basics_Observer> observers = new ArrayList<Fragment_Audiobooks_Basics_Observer>();
	public interface Fragment_Audiobooks_Basics_Observer{
		public void fragment_audiobooks_basics_click();
	}
	public void addObserver(Fragment_Audiobooks_Basics_Observer observer) { observers.add(observer); }
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		if(noCover == null){
			noCover = getResources().getDrawable(R.drawable.ic_action_help);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View v = (ViewGroup) inflater.inflate(R.layout.fragment_audiobook_basics, container, false);

		cover_iv = (ImageView) v.findViewById(R.id.audiobook_basics_cover_iv);
		author_tv = (TextView) v.findViewById(R.id.audiobook_basics_author_tv);
		audiobook_basics_album_tv = (TextView) v.findViewById(R.id.audiobook_basics_album_tv);
		if(audiobook != null){
			displayValues();
		}
		
		cover_btn = (ImageButton) v.findViewById(R.id.audiobook_basics_cover_btn);
		cover_btn.setImageDrawable(null);
		cover_btn.setOnClickListener(this);
		
		info_layout = (LinearLayout) v.findViewById(R.id.audiobook_basics_info_layout);
		info_layout.setOnClickListener(this);
		
		return v;
	}
	private void displayValues(){
		//Cover
		File cover = track.getCover();
		if(cover == null) cover = audiobook.getCover();
		if(cover != null) {
			Bitmap bitmap = BitmapFactory.decodeFile(cover.getPath());
			cover_iv.setImageBitmap(bitmap);
		} else {
			cover_iv.setImageDrawable(noCover);
		}

		//Author
		author_tv.setText(audiobook.getAuthor());

		//Album
		audiobook_basics_album_tv.setText(audiobook.getAlbum());
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.audiobook_basics_cover_btn:
			for(Fragment_Audiobooks_Basics_Observer observer : observers){
				observer.fragment_audiobooks_basics_click();
			}
			break;
		case R.id.audiobook_basics_info_layout:
			Intent intent = new Intent(getActivity(), AudiobookActivity.class);
			intent.putExtra("state", STATE_EDIT);
			intent.putExtra("audiobook", audiobook);
			System.out.println("Start edit audiobook");
			startActivityForResult(intent, REQUEST_FRAGMENT_BASICS_EDIT);
			break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_FRAGMENT_BASICS_EDIT:
			Log.d(TAG, "onActivityResult - REQUEST_FRAGMENT_BASICS_EDIT");
			if(resultCode == Activity.RESULT_OK){
				Audiobook result = (Audiobook) data.getSerializableExtra("result");
				audiobook.setAudiobook(result);
				displayValues();
			}
		}
	}

	public void setActionDrawabel(Drawable drw){
		cover_btn.setImageDrawable(drw);
	}
}
