package rd.dap.fragments;

import rd.dap.R;
import rd.dap.events.Event;
import rd.dap.events.Event.Type;
import rd.dap.events.EventBus;
import rd.dap.events.HasBookmarkEvent;
import rd.dap.events.Subscriber;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.model.Track;
import rd.dap.support.TrackList;
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
	private TextView title_tv;
	private SeekBar seeker;
	private Bookmark bookmark = null;
	
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
			BookmarkManager bm = BookmarkManager.getInstance();
			bookmark = bm.getBookmark(author, album);
			
			if(bookmark != null){
				displayBookmark(bookmark);
			}
		}
		
		EventBus.addSubsciber(this);
		
		return layout;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(bookmark != null){
			outState.putString("author", bookmark.getAuthor());
			outState.putString("album", bookmark.getAlbum());
		}
	}
	
	@Override
	public void onEvent(Event event) {
		switch(event.getType()){
		case BOOKMARK_SELECTED_EVENT:
		case BOOKMARK_UPDATED_EVENT:
			if(event.getSourceName().equals(getClass().getSimpleName())){
				System.out.println("Ignoring event - thrown by this class");
				break;
			}
			Bookmark bookmark = ((HasBookmarkEvent)event).getBookmark();
			displayBookmark(bookmark);
			break;
		default:
			break;
		}
	}

	private void displayBookmark(Bookmark bookmark) {
		this.bookmark = bookmark;
		AudiobookManager am = AudiobookManager.getInstance();
		Audiobook audiobook = am.getAudiobook(bookmark);
		int trackno = bookmark.getTrackno();
		TrackList tracks = audiobook.getPlaylist(); 
		Track track = tracks.get(trackno);
		String title = track.getTitle();
		title_tv.setText(title);
		seeker.setMax(tracks.size());
		seeker.setProgress(trackno);
	}

	@Override
	public void onClick(View v) {
		if(bookmark == null) return;
		int trackno = bookmark.getTrackno();
		switch(v.getId()){
		case R.id.fragment_track_btn_previous: trackno--; break;
		case R.id.fragment_track_btn_next: trackno++; break;
		}
		AudiobookManager am = AudiobookManager.getInstance();
		Audiobook audiobook = am.getAudiobook(bookmark);
		TrackList tracks = audiobook.getPlaylist();
		if(trackno < 0 || trackno >= tracks.size()) return;
		bookmark.setTrackno(trackno);

		displayBookmark(bookmark);
		
		Event event = new HasBookmarkEvent(getClass().getSimpleName(), Type.BOOKMARK_UPDATED_EVENT, bookmark);
		EventBus.fireEvent(event);
	}



	@Override public void onStopTrackingTouch(SeekBar seekBar) { } //Never used
	@Override public void onStartTrackingTouch(SeekBar seekBar) { } //Never used
	@Override
	public void onProgressChanged(SeekBar seekBar, int trackno, boolean fromUser) {
		if(!fromUser) return;
		bookmark.setTrackno(trackno);
		
		displayBookmark(bookmark);
		
		Event event = new HasBookmarkEvent(getClass().getSimpleName(), Type.BOOKMARK_UPDATED_EVENT, bookmark);
		EventBus.fireEvent(event);
	}

}
