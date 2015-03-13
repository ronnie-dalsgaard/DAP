package rd.dap.dialogs;

import rd.dap.R;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class Dialog_expired extends CustomDialog {

	public Dialog_expired(Activity activity, ViewGroup parent) {
		super(activity, parent);
	}

	public void show(){
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dv = inflater.inflate(R.layout.dialog_expired, parent, false);

		//Title
		TextView title_tv = (TextView) dv.findViewById(R.id.dialog_title_tv);
		title_tv.setText("DAP has expired");

		//Exit button
		ImageButton exit_btn = (ImageButton) dv.findViewById(R.id.dialog_exit_btn);
		exit_btn.setOnClickListener(new ExitListener());

		//Right button
		Button right_btn = (Button) dv.findViewById(R.id.dialog_right_btn);
		right_btn.setText("OK");
		right_btn.setOnClickListener(new ExitListener());

		dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				System.exit(0);
			}
		});
		
		dialog.setContentView(dv);
		dialog.show();
	}

}
