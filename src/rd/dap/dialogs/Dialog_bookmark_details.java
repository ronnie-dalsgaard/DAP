package rd.dap.dialogs;

import rd.dap.R;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkEvent;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class Dialog_bookmark_details extends CustomDialog {
	private Bookmark bookmark;
	private Callback callback;
	
	public interface Callback {
		public void onDeleteBookmark();
		public void onItemSelected(BookmarkEvent event);
	}

	public Dialog_bookmark_details(Activity activity, ViewGroup parent, Bookmark bookmark, Callback callback) {
		super(activity, parent);
		this.bookmark = bookmark;
		this.callback = callback;
	}

	public void show(){
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dv = inflater.inflate(R.layout.dialog_text_2btn, parent, false);

		//Title
		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
		title_tv.setText("Bookmark details");

		//Message
		TextView msg_tv = (TextView) dv.findViewById(R.id.dialog_msg_tv);
		msg_tv.setText(bookmark.getAuthor() + "\n" + bookmark.getAlbum());

		//Exit button
		ImageButton exit_btn = (ImageButton) dv.findViewById(R.id.dialog_exit_btn);
		exit_btn.setOnClickListener(new ExitListener());

		//Left button
		Button left_btn = (Button) dv.findViewById(R.id.dialog_left_btn);
		left_btn.setText("Delete");
		left_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				callback.onDeleteBookmark();
			}
		});

		//Right button
		Button right_btn = (Button) dv.findViewById(R.id.dialog_right_btn);
		right_btn.setText("View");
		right_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				new Dialog_bookmark_history(activity, parent, bookmark, new Dialog_bookmark_history.Callback() {
					@Override
					public void onItemSelected(BookmarkEvent event) {
						callback.onItemSelected(event); //Just pass it on up the chain.
					}
				}).show();
			}
		});

		dialog.setContentView(dv);
		dialog.show();
	}
	
}
