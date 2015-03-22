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
import rd.dap.support.FlowLayout;
import rd.dap.support.TrackList;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class TracksFragment extends Fragment implements Subscriber, OnClickListener {
	private FlowLayout flowview;
	private Bookmark bookmark = null;
	private TrackList tracks = new TrackList();
	private int currentTrackno = 0;
	private Activity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_tracks_flow, container, false);
		flowview = (FlowLayout) view.findViewById(R.id.fragment_tracks_flow);
		
		if(savedInstanceState != null){
			String author = savedInstanceState.getString("author");
			String album = savedInstanceState.getString("album");
			BookmarkManager bm = BookmarkManager.getInstance();
			bookmark = bm.getBookmark(author, album);
			
			if(bookmark != null){
				currentTrackno = bookmark.getTrackno();
				AudiobookManager am = AudiobookManager.getInstance();
				Audiobook audiobook = am.getAudiobook(bookmark);
				if(audiobook != null){
					tracks = audiobook.getPlaylist();
					updateFlow();
				}
			}
		}
		
		EventBus.addSubsciber(this);
		
		return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(bookmark != null){
			outState.putString("author", bookmark.getAuthor());
			outState.putString("album", bookmark.getAlbum());
			outState.putInt("currentTrackno", currentTrackno);
		}
	}

	private void updateFlow(){
		final LayoutInflater inflater = LayoutInflater.from(activity);
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				flowview.removeAllViews();
				for(int i = 0; i < tracks.size(); i++){
					View v = inflater.inflate(R.layout.fragment_tracks_flow_item, flowview, false);
					v.setTag(Integer.valueOf(i));
					v.setOnClickListener(TracksFragment.this);

					//Trackno
					TextView trackno_tv = (TextView) v.findViewById(R.id.fragment_tracks_flow_item_trackno_tv);
					trackno_tv.setText(String.format("%02d", i+1));
					
					//Circle
					View circle_iv = v.findViewById(R.id.fragment_tracks_flow_item_circle_iv);
					circle_iv.setVisibility(i == currentTrackno ? View.VISIBLE : View.GONE);
					flowview.addView(v);
				}
			}
		});
	}

	@Override
	public void onClick(View view) {
		int trackno = (int)view.getTag();
		bookmark.setTrackno(trackno); 

		//Remove cicle from previous
		View v_prev = flowview.getChildAt(currentTrackno);
		View circle_iv_prev = v_prev.findViewById(R.id.fragment_tracks_flow_item_circle_iv);
		circle_iv_prev.setVisibility(View.GONE);
		
		//Add circle to current
		View v = flowview.getChildAt(trackno);
		View circle_iv = v.findViewById(R.id.fragment_tracks_flow_item_circle_iv);
		circle_iv.setVisibility(View.VISIBLE);
		
//		updateFlow(); //Also good, but a bit heavy
		
		currentTrackno = trackno;
		Event event = new HasBookmarkEvent(getClass().getSimpleName(), Type.BOOKMARK_UPDATED_EVENT, bookmark);
		EventBus.fireEvent(event);
	}

	@Override
	public void onEvent(Event event) {
		System.out.println(getClass().getSimpleName()+":\n"+event);
		switch(event.getType()){
		case BOOKMARK_SELECTED_EVENT:
		case BOOKMARK_UPDATED_EVENT:
			bookmark = ((HasBookmarkEvent)event).getBookmark();
			AudiobookManager am = AudiobookManager.getInstance();
			Audiobook audiobook = am.getAudiobook(bookmark);
			tracks.clear();
			tracks.addAll(audiobook.getPlaylist());
			currentTrackno = bookmark.getTrackno();
			updateFlow();
			break;
		default:
			break;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}
}