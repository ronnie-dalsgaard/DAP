package rd.dap.fragments;

import rd.dap.R;
import rd.dap.events.Event;
import rd.dap.events.EventBus;
import rd.dap.events.Subscriber;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class LoadAudiobooksDialogFragment extends DialogFragment implements Subscriber {
	private Activity activity;
	private TextView tv;
	private Dialog dialog;
	
	 public static LoadAudiobooksDialogFragment newInstance() {
		 LoadAudiobooksDialogFragment frag = new LoadAudiobooksDialogFragment();
	        return frag;
	    }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		EventBus.addSubsciber(this);
		dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(activity);
		View layout = inflater.inflate(R.layout.loading, null, false);
		tv = (TextView) layout.findViewById(R.id.loading_content);
		dialog.setContentView(layout);
		return dialog;
	}

	@Override
	public void onEvent(Event event) {
		switch(event.getType()){
		case AUDIOBOOKS_DISCOVER_ELEMENT_EVENT:
			String _progress = event.getString();
			tv.setText(_progress);
			break;
		case AUDIOBOOKS_DISCOVERED_EVENT:
			tv.setText("");
			dialog.dismiss();
			break;
		default:
			break;
		}
	}
}
