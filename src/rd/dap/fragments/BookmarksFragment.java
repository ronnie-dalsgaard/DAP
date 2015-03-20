package rd.dap.fragments;

import java.util.ArrayList;

import rd.dap.R;
import rd.dap.dialogs.Dialog_disabled_bookmarks;
import rd.dap.events.BookmarksLoadedEvent;
import rd.dap.events.Event;
import rd.dap.events.EventBus;
import rd.dap.events.Subscriber;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
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
	private LinearLayout layout, bookmark_list;
	private View loading;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//No cover
		if(noCover ==null) noCover = getResources().getDrawable(R.drawable.ic_action_help);
		if(drw_play == null) drw_play = getResources().getDrawable(R.drawable.ic_action_play);
		if(drw_pause == null) drw_pause = getResources().getDrawable(R.drawable.ic_action_pause);
		if(drw_play_on_cover == null) drw_play_on_cover = getResources().getDrawable(R.drawable.ic_action_play_over_video);
		if(drw_pause_on_cover == null) drw_pause_on_cover = getResources().getDrawable(R.drawable.ic_action_pause_over_video);


		layout = (LinearLayout) inflater.inflate(R.layout.fragment_bookmarks, container, false);
		bookmark_list = (LinearLayout) layout.findViewById(R.id.fragment_bookmarks_list);
		loading = layout.findViewById(R.id.fragment_bookmarks_progress);
		EventBus.addSubsciber(this);
		
		if(savedInstanceState != null){
			BookmarkManager bm = BookmarkManager.getInstance();
			ArrayList<Bookmark> bookmarks = bm.getBookmarks();
			updateList(bookmarks);
			loading.setVisibility(View.GONE);
		}
		return layout;
	}
	@Override
	public void onEvent(Event event) {
		if(event.getEventID() == Event.BOOKMARKS_LOADED_EVENT){
			BookmarksLoadedEvent bookmarks_event = (BookmarksLoadedEvent)event;
			ArrayList<Bookmark> bookmarks = bookmarks_event.getBookmarks();
			if(bookmarks == null || bookmarks.isEmpty()) return;
			updateList(bookmarks);
			Activity activity = getActivity();
			if(activity == null) return;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					loading.setVisibility(View.GONE);
				}
			});
		}
	}

	private void updateList(final ArrayList<Bookmark> bookmarks){
		Activity activity = getActivity();
		if(activity == null) return;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				bookmark_list.removeAllViews();
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				ArrayList<Bookmark> disabledBookmarks = new ArrayList<Bookmark>();
				for(Bookmark bookmark : bookmarks){
					View v = inflater.inflate(R.layout.bookmark_item, bookmark_list, false);
					v.setTag(bookmark);
					v.setOnClickListener(BookmarksFragment.this);
					v.setOnLongClickListener(BookmarksFragment.this);

					//Cover
					Audiobook audiobook = AudiobookManager.getInstance().getAudiobook(bookmark);
					if(audiobook == null) { disabledBookmarks.add(bookmark); continue; }

					ImageView cover_iv = (ImageView) v.findViewById(R.id.bookmark_cover_iv);
					if(cover_iv != null){
						Bitmap bm = audiobook.getThumbnail();
						if(bm == null) cover_iv.setImageDrawable(noCover);
						else cover_iv.setImageBitmap(bm);
					}

					//Track no
					TextView track_tv = (TextView) v.findViewById(R.id.bookmark_track_tv);
					track_tv.setText(String.format("%02d", bookmark.getTrackno()+1));

					//Progress
					TextView progress_tv = (TextView) v.findViewById(R.id.bookmark_progress_tv);
					progress_tv.setText(Time.toString(bookmark.getProgress()));

					bookmark_list.addView(v);

					View div = inflater.inflate(R.layout.divider_vertical, bookmark_list, false);
					bookmark_list.addView(div);
				}
				if(!disabledBookmarks.isEmpty()){
					new Dialog_disabled_bookmarks(getActivity(), layout, disabledBookmarks, new Dialog_disabled_bookmarks.Callback() {
						@Override
						public void onBookmarkDeleted() {
							updateList(bookmarks);
						}
					}).show();
				}
			}
		});
	}

	@Override
	public boolean onLongClick(View arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}



}
