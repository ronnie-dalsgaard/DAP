package rd.dap.fragments;

import java.util.concurrent.TimeUnit;

import rd.dap.R;
import rd.dap.events.Event;
import rd.dap.events.Event.Type;
import rd.dap.events.EventBus;
import rd.dap.events.HasBookmarkEvent;
import rd.dap.events.ProgressUpdatedEvent;
import rd.dap.events.Subscriber;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.model.Track;
import rd.dap.support.Time;
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

public class ProgressFragment extends Fragment implements Subscriber, OnClickListener, OnSeekBarChangeListener {
	private TextView progress_tv;
	private SeekBar seeker;
	private Bookmark bookmark = null;
	private int progress;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_progress, container, false);
		
		ImageButton rewind_btn = (ImageButton) layout.findViewById(R.id.fragment_progress_btn_rewind);
		rewind_btn.setOnClickListener(this);

		ImageButton forward_btn = (ImageButton) layout.findViewById(R.id.fragment_progress_btn_forward);
		forward_btn.setOnClickListener(this);
		
		seeker = (SeekBar) layout.findViewById(R.id.fragment_progress_seeker);
		seeker.setOnSeekBarChangeListener(this);
		
		progress_tv = (TextView) layout.findViewById(R.id.fragment_progress_progress_tv);
		
		if(savedInstanceState != null){
			String author = savedInstanceState.getString("author");
			String album = savedInstanceState.getString("album");
			progress = savedInstanceState.getInt("progress");
			BookmarkManager bm = BookmarkManager.getInstance();
			bookmark = bm.getBookmark(author, album);
			if(bookmark != null){
				AudiobookManager am = AudiobookManager.getInstance();
				Audiobook audiobook = am.getAudiobook(bookmark);
				if(audiobook != null){
					int trackno = bookmark.getTrackno();
					TrackList tracks = audiobook.getPlaylist();
					Track track = tracks.get(trackno);
					int duration = track.getDuration();
					seeker.setMax(duration);
					seeker.setProgress(progress);
					String _progress = Time.toString(progress);
					progress_tv.setText(_progress);
				}
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
			outState.putInt("progress", progress);
		}
	}
	



	@Override
	public void onEvent(Event event) {
		String _progress;
		switch(event.getType()){
		case BOOKMARK_SELECTED_EVENT:
		case BOOKMARK_UPDATED_EVENT:
			if(event.getSourceName().equals(getClass().getSimpleName())){
				System.out.println("Ignoring event - thrown by this class");
				break;
			}
			bookmark = ((HasBookmarkEvent)event).getBookmark();
			AudiobookManager am = AudiobookManager.getInstance();
			Audiobook audiobook = am.getAudiobook(bookmark);
			int trackno = bookmark.getTrackno();
			TrackList tracks = audiobook.getPlaylist(); 
			Track track = tracks.get(trackno);
			progress = bookmark.getProgress();
			_progress = Time.toString(progress);
			progress_tv.setText(_progress);
			seeker.setMax(track.getDuration());
			seeker.setProgress(progress);
			break;
		case PROGRESS_UPDATED_EVENT:
			progress = ((ProgressUpdatedEvent)event).getProgress();
			_progress = Time.toString(progress);
			progress_tv.setText(_progress);
			seeker.setProgress(progress);
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if(bookmark == null) return;
//		progress = bookmark.getProgress();
		int tentative = progress;
		switch(v.getId()){
		case R.id.fragment_progress_btn_rewind: tentative -= Time.toMillis(1, TimeUnit.MINUTES); break;
		case R.id.fragment_progress_btn_forward: tentative += Time.toMillis(1, TimeUnit.MINUTES); break;
		}
		System.out.println("&& "+Time.toString(progress));
		AudiobookManager am = AudiobookManager.getInstance();
		Audiobook audiobook = am.getAudiobook(bookmark);
		TrackList tracks = audiobook.getPlaylist();
		int trackno =  bookmark.getTrackno();
		Track track = tracks.get(trackno);
		
		int duration = track.getDuration();
		
		if(duration > 0 && (tentative < 0 || tentative > duration)) return;
		progress = tentative;
		bookmark.setProgress(progress);

		String _progress = Time.toString(progress);
		progress_tv.setText(_progress);
		seeker.setProgress(progress);
		
		Event event = new HasBookmarkEvent(getClass().getSimpleName(), Type.BOOKMARK_UPDATED_EVENT, bookmark);
		EventBus.fireEvent(event);
		
		event = new ProgressUpdatedEvent(getClass().getSimpleName(), progress);
		EventBus.fireEvent(event);
	}

	@Override public void onStopTrackingTouch(SeekBar seekBar) { } //Never used
	@Override public void onStartTrackingTouch(SeekBar seekBar) { } //Never used
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(!fromUser) return;
		this.progress = progress;
		
		String _progress = Time.toString(progress);
		progress_tv.setText(_progress);
		/* Seeker obviously set */
		
		bookmark.setProgress(progress);
		
		Event event = new HasBookmarkEvent(getClass().getSimpleName(), Type.BOOKMARK_UPDATED_EVENT, bookmark);
		EventBus.fireEvent(event);
		
		event = new ProgressUpdatedEvent(getClass().getSimpleName(), progress);
		EventBus.fireEvent(event);
	}
}
