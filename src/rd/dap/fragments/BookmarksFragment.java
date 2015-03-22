package rd.dap.fragments;

import java.io.File;
import java.util.ArrayList;

import rd.dap.R;
import rd.dap.dialogs.Dialog_bookmark_details;
import rd.dap.dialogs.Dialog_disabled_bookmarks;
import rd.dap.events.BookmarksLoadedEvent;
import rd.dap.events.Event;
import rd.dap.events.Event.Type;
import rd.dap.events.EventBus;
import rd.dap.events.HasAudiobookEvent;
import rd.dap.events.HasBookmarkEvent;
import rd.dap.events.Subscriber;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkEvent;
import rd.dap.model.BookmarkManager;
import rd.dap.support.FlowLayout;
import rd.dap.support.Time;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BookmarksFragment extends Fragment implements Subscriber, OnClickListener, OnLongClickListener {
	private static Drawable noCover, drw_play, drw_pause, drw_play_on_cover, drw_pause_on_cover;
	private LinearLayout layout;
	private FlowLayout flowview;
	private ArrayList<Bookmark> bookmarks;
	private View loading;
	private Activity activity = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//No cover
		if(noCover ==null) noCover = getResources().getDrawable(R.drawable.ic_action_help);
		if(drw_play == null) drw_play = getResources().getDrawable(R.drawable.ic_action_play);
		if(drw_pause == null) drw_pause = getResources().getDrawable(R.drawable.ic_action_pause);
		if(drw_play_on_cover == null) drw_play_on_cover = getResources().getDrawable(R.drawable.ic_action_play_over_video);
		if(drw_pause_on_cover == null) drw_pause_on_cover = getResources().getDrawable(R.drawable.ic_action_pause_over_video);

		layout = (LinearLayout) inflater.inflate(R.layout.fragment_bookmarks_flow, container, false);
		flowview = (FlowLayout) layout.findViewById(R.id.fragment_bookmarks_flow);
		loading = layout.findViewById(R.id.fragment_bookmarks_progress);
		
		EventBus.addSubsciber(this);
		
		if(savedInstanceState != null){
			BookmarkManager bm = BookmarkManager.getInstance();
			bookmarks = bm.getBookmarks();
			updateFlow();
			loading.setVisibility(View.GONE);
		}
		return layout;
	}
	
	@Override
	public void onEvent(Event event) {
		System.out.println(getClass().getSimpleName()+":\n"+event);
		BookmarkManager bm;
		switch(event.getType()){
		case BOOKMARKS_LOADED_EVENT:
			BookmarksLoadedEvent bookmarks_event = (BookmarksLoadedEvent)event;
			bookmarks = bookmarks_event.getBookmarks();
			if(bookmarks == null || bookmarks.isEmpty()) return;
			updateFlow();
			if(activity == null) return;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					loading.setVisibility(View.GONE);
				}
			});
			break;
		case NO_AUDIOBOOKS_FOUND_EVENT:
		case NO_BOOKMARKS_FOUND_EVENT:
			if(activity == null) return;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					loading.setVisibility(View.GONE);
				}
			});
			break;
		case AUDIOBOOKS_SELECTED_EVENT:
			Audiobook audiobook = ((HasAudiobookEvent)event).getAudiobook();
			bm = BookmarkManager.getInstance();
			File filesDir = activity.getFilesDir();
			String author = audiobook.getAuthor();
			String album = audiobook.getAlbum();
			int trackno = 0;
			int progress = 0;
			ArrayList<BookmarkEvent> events = new ArrayList<BookmarkEvent>();
			boolean force = false;
			bm.createOrUpdateBookmark(filesDir, author, album, trackno, progress, events, force);
			updateFlow();
			break;
		case BOOKMARK_DELETED_EVENT:
		case AUDIOBOOKS_LOADED_EVENT:
			bm = BookmarkManager.getInstance();
			bookmarks = bm.getBookmarks();
			updateFlow();
			break;
		default:
			break;
		}
	}

	private void updateFlow(){
		final LayoutInflater inflater = LayoutInflater.from(activity);
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				flowview.removeAllViews();
				ArrayList<Bookmark> disabledBookmarks = new ArrayList<Bookmark>();
				for(Bookmark bookmark : bookmarks){
					View v = inflater.inflate(R.layout.fragment_bookmark_flow_item, flowview, false);
					v.setTag(bookmark);
					v.setOnClickListener(BookmarksFragment.this);
					v.setOnLongClickListener(BookmarksFragment.this);

					//Cover
					Audiobook audiobook = AudiobookManager.getInstance().getAudiobook(bookmark);
					if(audiobook == null) { disabledBookmarks.add(bookmark); continue; }

					ImageView cover_iv = (ImageView) v.findViewById(R.id.bookmark_flow_item_cover_iv);
					if(cover_iv != null){
						Bitmap bm = audiobook.getThumbnail();
						if(bm == null) cover_iv.setImageDrawable(noCover);
						else cover_iv.setImageBitmap(bm);
					}

					//Track no
					TextView track_tv = (TextView) v.findViewById(R.id.bookmark_flow_item_track_tv);
					track_tv.setText(String.format("%02d", bookmark.getTrackno()+1));

					//Progress
					TextView progress_tv = (TextView) v.findViewById(R.id.bookmark_flow_item_progress_tv);
					progress_tv.setText(Time.toString(bookmark.getProgress()));

					flowview.addView(v);
				}
				if(!disabledBookmarks.isEmpty()){
					new Dialog_disabled_bookmarks(getActivity(), layout, disabledBookmarks).show();
				}
			}
		});
	}

	@Override
	public boolean onLongClick(View view) {
		final Bookmark bookmark = (Bookmark) view.getTag();
		new Dialog_bookmark_details(activity, (ViewGroup)layout, bookmark).show();
		return true;
	}

	@Override
	public void onClick(View view) {
		Bookmark bookmark = (Bookmark) view.getTag();
		Event event = new HasBookmarkEvent(getClass().getSimpleName(), Type.BOOKMARK_SELECTED_EVENT, bookmark);
		EventBus.fireEvent(event);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}
}
