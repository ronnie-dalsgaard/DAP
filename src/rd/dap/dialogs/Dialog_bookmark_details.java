package rd.dap.dialogs;

import rd.dap.R;
import rd.dap.model.Bookmark;
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
	
	public Dialog_bookmark_details(Activity activity, ViewGroup parent, Bookmark bookmark) {
		super(activity, parent);
		this.bookmark = bookmark;
	}

	public void show(){
		LayoutInflater inflater = LayoutInflater.from(activity);
		final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.dialog_text_2btn, parent, false);

		//Title
		TextView title_tv = (TextView) layout.findViewById(R.id.dialog_title_tv);
		title_tv.setText("Bookmark details");

		//Message
		TextView msg_tv = (TextView) layout.findViewById(R.id.dialog_msg_tv);
		msg_tv.setText(bookmark.getAuthor() + "\n" + bookmark.getAlbum());

		//Exit button
		ImageButton exit_btn = (ImageButton) layout.findViewById(R.id.dialog_exit_btn);
		exit_btn.setOnClickListener(new ExitListener());

		//Left button
		Button left_btn = (Button) layout.findViewById(R.id.dialog_left_btn);
		left_btn.setText("Delete");
		left_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				new Dialog_delete_bookmark(activity, layout, bookmark).show();
			}
		});

		//Right button
		Button right_btn = (Button) layout.findViewById(R.id.dialog_right_btn);
		right_btn.setText("View");
		right_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				new Dialog_bookmark_history(activity, parent, bookmark).show();
			}
		});

		dialog.setContentView(layout);
		dialog.show();
	}
	
}
