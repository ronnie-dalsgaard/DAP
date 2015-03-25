package rd.dap.fragments;

import rd.dap.R;
import rd.dap.events.Event;
import rd.dap.events.Event.Type;
import rd.dap.events.EventBus;
import rd.dap.events.Subscriber;
import rd.dap.model.Audiobook;
import rd.dap.model.AudiobookManager;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import android.app.Activity;
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
	private Activity activity;
	private ImageView cover_iv;
	private TextView author_tv, album_tv;
	private ImageButton play_btn;
	private Bookmark bookmark;
	private boolean isPlay = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_player, container, false);
		
		if(noCover == null)	noCover = getResources().getDrawable(R.drawable.ic_action_picture);
		if(drw_play == null) drw_play = getResources().getDrawable(R.drawable.ic_action_play);
		if(drw_pause == null) drw_pause = getResources().getDrawable(R.drawable.ic_action_pause);
		if(drw_play_on_cover == null) drw_play_on_cover = getResources().getDrawable(R.drawable.ic_action_play_over_video);
		if(drw_pause_on_cover == null) drw_pause_on_cover = getResources().getDrawable(R.drawable.ic_action_pause_over_video);
		
		//Buttons
		play_btn = (ImageButton) layout.findViewById(R.id.fragment_player_btn_cover);
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

			isPlay = savedInstanceState.getBoolean("isPlay");
			if(isPlay) setPauseButton();
			else setPlayButton();
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
			outState.putBoolean("isPlay", isPlay);
		}
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.fragment_player_btn_cover:
			EventBus.fireEvent(new Event(getClass().getSimpleName(), Type.REQUEST_TOGGLE).setBoolean(true));
			break;
		}
	}
	@Override
	public void onEvent(final Event event) {
		Bookmark bookmark;
		switch(event.getType()){
		case BOOKMARK_DELETED_EVENT: 
			bookmark = event.getBookmark();
			if(bookmark != null && bookmark.equals(this.bookmark)){
				displayNoBookmark();
			}
			break;
		case BOOKMARK_SELECTED_EVENT:
			bookmark = event.getBookmark();
			displayBookmark(bookmark);
			break;
		case BOOKMARK_UPDATED_EVENT: 
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					displayBookmark(event.getBookmark());
				}
			});
			break;
		case ON_PLAY:
			setPauseButton();
			break;
		case ON_PAUSE:
			setPlayButton();
			break;
		default:
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
		if(isPlay) setPauseButton();
		else setPlayButton();
		author_tv.setText(audiobook.getAuthor());
		album_tv.setText(audiobook.getAlbum());
	}
	private void displayNoBookmark(){
		cover_iv.setImageDrawable(noCover);
		play_btn.setImageDrawable(null);
		author_tv.setText(R.string.g_author);
		album_tv.setText(R.string.g_album);
	}
	private void setPlayButton(){
		play_btn.setImageDrawable(drw_play_on_cover);
		isPlay = false;
	}
	private void setPauseButton(){
		play_btn.setImageDrawable(drw_pause_on_cover);
		isPlay = true;
	}
	
}
