package rd.dap.dialogs;

import rd.dap.R;
import rd.dap.activities.MainActivity;
import rd.dap.model.Bookmark;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class Dialog_delete_bookmark extends CustomDialog {
	private Bookmark bookmark;
	private Callback callback;
	
	public interface Callback {
		public void onDeleteBookmarkConfirmed();
	}
	
	public Dialog_delete_bookmark(MainActivity activity, ViewGroup parent, Bookmark bookmark, Callback callback) {
		super(activity, parent);
		this.bookmark = bookmark;
		this.callback = callback;
	}

	public void show(){
		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
			public void onClick(View arg0) {
				dialog.dismiss();
				callback.onDeleteBookmarkConfirmed();
			}
		});

		dialog.setContentView(dv);
		dialog.show();
	}

}
