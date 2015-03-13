package rd.dap.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;

public abstract class CustomDialog {
	protected Activity activity;
	protected ViewGroup parent;
	protected Dialog dialog;

	public CustomDialog(Activity activity, ViewGroup parent) {
		this.activity = activity;
		this.parent = parent;
		
		dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public abstract void show();
	
	protected class ExitListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			dialog.dismiss();			
		}
		
	}
}
