package rd.dap.dialogs;

import rd.dap.R;
import rd.dap.events.Event;
import rd.dap.events.EventBus;
import rd.dap.events.Event.Type;
import rd.dap.events.HasBookmarkEvent;
import rd.dap.model.Bookmark;
import rd.dap.model.BookmarkManager;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class Dialog_delete_bookmark extends CustomDialog {
	private Bookmark bookmark;
	
	public Dialog_delete_bookmark(Activity activity, ViewGroup parent, Bookmark bookmark) {
		super(activity, parent);
		this.bookmark = bookmark;
	}

	public void show(){
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dv = inflater.inflate(R.layout.dialog_text_2btn, parent, false);

		//Title
		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
		title_tv.setText("Delete bookmark");

		//Message
		TextView msg_tv = (TextView) dv.findViewById(R.id.dialog_msg_tv);
		msg_tv.setText(bookmark.getAuthor() + "\n" + bookmark.getAlbum());

		//Exit button
		ImageButton exit_btn = (ImageButton) dv.findViewById(R.id.dialog_exit_btn);
		exit_btn.setOnClickListener(new ExitListener());

		//Left button
		Button left_btn = (Button) dv.findViewById(R.id.dialog_left_btn);
		left_btn.setText("Cancel");
		left_btn.setOnClickListener(new ExitListener());

		//Right button
		Button right_btn = (Button) dv.findViewById(R.id.dialog_right_btn);
		right_btn.setText("Confirm");
		right_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				dialog.dismiss();
				BookmarkManager bm = BookmarkManager.getInstance();
				bm.removeBookmark(activity, bookmark);
				
				Event event = new HasBookmarkEvent(getClass().getSimpleName(), Type.BOOKMARK_DELETED_EVENT, bookmark);
				EventBus.fireEvent(event);
			}
		});

		dialog.setContentView(dv);
		dialog.show();
	}

}
