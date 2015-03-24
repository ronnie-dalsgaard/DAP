package rd.dap.fragments;

import rd.dap.R;
import rd.dap.events.Event;
import rd.dap.events.Event.Type;
import rd.dap.events.EventBus;
import rd.dap.events.Subscriber;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


public class TrackFragment extends Fragment implements Subscriber, OnClickListener, OnSeekBarChangeListener {
	private Activity activity;
	private TextView title_tv;
	private SeekBar seeker;
	private Bookmark bookmark = null;
	private int trackno;
	private int trackcount;
	private String title;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_track, container, false);
		
		ImageButton prev_btn = (ImageButton) layout.findViewById(R.id.fragment_track_btn_previous);
		prev_btn.setOnClickListener(this);

		ImageButton next_btn = (ImageButton) layout.findViewById(R.id.fragment_track_btn_next);
		next_btn.setOnClickListener(this);
		
		seeker = (SeekBar) layout.findViewById(R.id.fragment_track_seeker);
		seeker.setOnSeekBarChangeListener(this);
		
		title_tv = (TextView) layout.findViewById(R.id.fragment_track_title);
		
		if(savedInstanceState != null){
			String author = savedInstanceState.getString("author");
			String album = savedInstanceState.getString("album");
			trackcount = savedInstanceState.getInt("trackcount");
			trackno = savedInstanceState.getInt("trackno");
			title = savedInstanceState.getString("title");
			BookmarkManager bm = BookmarkManager.getInstance();
			bookmark = bm.getBookmark(author, album);
			if(bookmark != null){
				seeker.setMax(trackcount);
				seeker.setProgress(trackno);
				title_tv.setText(title);
			}
		}
		
		EventBus.addSubsciber(this);
		
		return layout;
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(bookmark != null){
			outState.putString("author", bookmark.getAuthor());
			outState.putString("album", bookmark.getAlbum());
			outState.putInt("trackno", trackno);
			outState.putInt("trackcount", trackcount);
			outState.putString("title", title);
		}
	}
	
	@Override
	public void onEvent(Event event) {
		switch(event.getType()){
		case BOOKMARK_SELECTED_EVENT:
			Bookmark bookmark = event.getBookmark();
			trackno = bookmark.getTrackno();
			seeker.setProgress(trackno);
			title = event.getString();
			title_tv.setText(title);
			break;
		case TRACKCOUNT_SET_EVENT:
			trackcount = event.getInteger();
			seeker.setMax(trackcount);
			break;
		case ON_TRACK_CHANGED:
			trackno = event.getInteger();
			title = event.getString();
			activity.runOnUiThread(new Runnable() {
				@Override public void run() {
					title_tv.setText(title);
					seeker.setProgress(trackno);
				}
			});
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.fragment_track_btn_previous:
			EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.REQUEST_PREV));
			break;
		case R.id.fragment_track_btn_next:
			EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.REQUEST_NEXT));
			break;
		}
	}

	@Override public void onStopTrackingTouch(SeekBar seekBar) { } //Never used
	@Override public void onStartTrackingTouch(SeekBar seekBar) { } //Never used
	@Override
	public void onProgressChanged(SeekBar seekBar, int trackno, boolean fromUser) {
		if(!fromUser) return;
		EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.REQUEST_SEEK_TO_TRACK).setInteger(trackno));
	}

}
