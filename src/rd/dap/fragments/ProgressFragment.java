package rd.dap.fragments;

import rd.dap.R;
import rd.dap.events.Event;
import rd.dap.events.Event.Type;
import rd.dap.events.EventBus;
import rd.dap.events.Subscriber;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import rd.dap.support.Time;
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

public class ProgressFragment extends Fragment implements Subscriber, OnClickListener, OnSeekBarChangeListener {
	private Activity activity;
	private TextView progress_tv;
	private SeekBar seeker;
	private Bookmark bookmark = null;
	private int progress;
	private int duration;
	
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
			duration = savedInstanceState.getInt("duration");
			BookmarkManager bm = BookmarkManager.getInstance();
			bookmark = bm.getBookmark(author, album);
			if(bookmark != null){
				seeker.setMax(duration);
				seeker.setProgress(progress);
				String _progress = Time.toString(progress);
				progress_tv.setText(_progress);
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
			outState.putInt("progress", progress);
			outState.putInt("duration", duration);
		}
	}
	
	@Override
	public void onEvent(Event event) {
		switch(event.getType()){
		case BOOKMARK_SELECTED_EVENT:
			bookmark = event.getBookmark();
			progress = bookmark.getProgress();
			String _progress = Time.toString(progress);
			progress_tv.setText(_progress);
			seeker.setProgress(progress);
			break;
		case DURATION_SET_EVENT:
			duration = event.getInteger();
			seeker.setMax(duration);
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String _progress = ""+progress_tv.getText();
					if(duration > 0) {
						String _duration = Time.toString(duration);
						_progress += " / " + _duration;
					}
					progress_tv.setText(_progress);
				}
			});
			break;
		case ON_PROGRESS_CHANGED:
			progress = event.getInteger();
			activity.runOnUiThread(new Runnable() {
				@Override public void run() {
					String _progress = Time.toString(progress);
					if(duration > 0) {
						String _duration = Time.toString(duration);
						_progress += " / " + _duration;
					}
					progress_tv.setText(_progress);
					seeker.setProgress(progress);
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
		case R.id.fragment_progress_btn_rewind:
			EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.REQUEST_REWIND));
			break;
		case R.id.fragment_progress_btn_forward: 
			EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.REQUEST_FORWARD));
			break;
		}
	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(!fromUser) return;
		EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.REQUEST_SEEK_TO).setInteger(progress));
	}
	@Override public void onStopTrackingTouch(SeekBar seekBar) { } //Never used
	@Override public void onStartTrackingTouch(SeekBar seekBar) { } //Never used
}
