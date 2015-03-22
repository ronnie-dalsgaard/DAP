package rd.dap.fragments;

import rd.dap.R;
import rd.dap.events.Event;
import rd.dap.events.EventBus;
import rd.dap.events.HasBookmarkEvent;
import rd.dap.events.PlayPauseEvent;
import rd.dap.events.Subscriber;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PlayerFragment extends Fragment implements OnClickListener, Subscriber {
	private static Drawable noCover, drw_play, drw_pause, drw_play_on_cover, drw_pause_on_cover;
	private ImageView cover_iv;
	private TextView author_tv, album_tv;
	private Bookmark bookmark;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_player, container, false);
		
		if(noCover == null)	noCover = getResources().getDrawable(R.drawable.ic_action_picture);
		if(drw_play == null) drw_play = getResources().getDrawable(R.drawable.ic_action_play);
		if(drw_pause == null) drw_pause = getResources().getDrawable(R.drawable.ic_action_pause);
		if(drw_play_on_cover == null) drw_play_on_cover = getResources().getDrawable(R.drawable.ic_action_play_over_video);
		if(drw_pause_on_cover == null) drw_pause_on_cover = getResources().getDrawable(R.drawable.ic_action_pause_over_video);
		
		//Buttons
		ImageButton play_btn = (ImageButton) layout.findViewById(R.id.fragment_player_btn_cover);
		play_btn.setOnClickListener(this);

		cover_iv = (ImageView) layout.findViewById(R.id.fragment_player_cover_iv);
		author_tv = (TextView) layout.findViewById(R.id.fragment_player_author_tv);
		album_tv = (TextView) layout.findViewById(R.id.fragment_player_album_tv);
		
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
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.fragment_player_btn_cover:
		String className = this.getClass().getSimpleName();
		Event event = new PlayPauseEvent(className, true);
		EventBus.fireEvent(event);
		break;
		}
	}


	private void displayBookmark(Bookmark bookmark){
		this.bookmark = bookmark;
		AudiobookManager am = AudiobookManager.getInstance(); 
		Audiobook audiobook = am.getAudiobook(bookmark);
		Bitmap bm = audiobook.getThumbnail();
		if(bm == null) cover_iv.setImageDrawable(noCover); 
		else cover_iv.setImageBitmap(bm);
		author_tv.setText(audiobook.getAuthor());
		album_tv.setText(audiobook.getAlbum());
	}
	private void displayNoBookmark(){
		cover_iv.setImageDrawable(noCover);
		author_tv.setText(R.string.g_author);
		album_tv.setText(R.string.g_album);
	}
	
	@Override
	public void onEvent(Event event) {
		Bookmark bookmark;
		switch(event.getType()){
		case AUDIOBOOKS_LOADED_EVENT: break;
		case AUDIOBOOKS_SELECTED_EVENT: break;
		case BOOKMARKS_LOADED_EVENT: break;
		case BOOKMARK_DELETED_EVENT: 
			bookmark = ((HasBookmarkEvent)event).getBookmark();
			if(bookmark != null && bookmark.equals(this.bookmark)){
				displayNoBookmark();
			}
			break;
		case BOOKMARK_SELECTED_EVENT:
			bookmark = ((HasBookmarkEvent)event).getBookmark();
			displayBookmark(bookmark);
			break;
		case BOOKMARK_UPDATED_EVENT: 
			bookmark = ((HasBookmarkEvent)event).getBookmark();
			displayBookmark(bookmark);
			break;
		case FILE_FOUND_EVENT: break;
		case NO_AUDIOBOOKS_FOUND_EVENT: break;
		case NO_BOOKMARKS_FOUND_EVENT: break;
		case PLAY_PAUSE_EVENT: break;
		case TIME_OUT_EVENT: break;
		default:
			break;
		
		}
		
	}

}
